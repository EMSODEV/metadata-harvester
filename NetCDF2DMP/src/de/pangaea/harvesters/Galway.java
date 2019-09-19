package de.pangaea.harvesters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class Galway {
	public URL catalogURL;
	private String dataCenter;
	private String timeSeries;
	private String defaultLocation;
	
	public void downloadFile(URL url, String file) throws IOException {
	    System.out.println("opening connection");
	    InputStream in = url.openStream();
	    FileOutputStream fos = new FileOutputStream(new File(file));
	    System.out.println("reading file...");
	    int length = -1;
	    byte[] buffer = new byte[1024];
	    while ((length = in.read(buffer)) > -1) {
	        fos.write(buffer, 0, length);
	    }
	    fos.close();
	    in.close();
	    System.out.println("file was downloaded");
	}
	
	private void transformFGDCXML(String fgdcURL, String pansimpleFile) throws IOException {
		URL url;
		url = new URL(fgdcURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");				
		try {
			if(con.getResponseCode()==HttpURLConnection.HTTP_OK){
				if(con.getContentType().contains("xml")) {
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer content = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						content.append(inputLine);
					}
					in.close();
					String xml=content.toString();	

					String stylesheetPathname = "xsl/gcmd2pansimple.xsl";
					Source stylesheetSource = new StreamSource(new File(stylesheetPathname).getAbsoluteFile());
					TransformerFactory factory = TransformerFactory.newInstance();
					try {
						Transformer transformer = factory.newTransformer(stylesheetSource);
						StringReader reader =  new StringReader( xml );
						String outputPathname = pansimpleFile;
						Result outputResult = new StreamResult(new File(outputPathname).getAbsoluteFile());
						transformer.transform(new javax.xml.transform.stream.StreamSource(reader), outputResult);
						
					} catch (TransformerConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println(xml);
				}else System.out.println("No XML File?: "+con.getContentType().toString());
			}else System.out.println("HTP Response "+con.getResponseCode());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void Harvest(URL url) throws ParserConfigurationException, SAXException {
		System.out.println("Harvesting: "+url.toString());
		String xml = null;			
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");				
			if(con.getResponseCode()==HttpURLConnection.HTTP_OK){
				if(con.getContentType().contains("xml")) {
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String inputLine;
					StringBuffer content = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						content.append(inputLine);
					}
					in.close();
					xml=content.toString();	
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				    factory.setNamespaceAware(true);
				    DocumentBuilder builder;
					builder = factory.newDocumentBuilder();
				    Document doc = builder.parse(new InputSource(new StringReader(xml)));	    
				    NodeList catalogList=doc.getElementsByTagName("entry");
				    System.out.println(catalogList.getLength()); 
				    for (int i = 0; i < catalogList.getLength(); ++i) {
				    	Node cNode =catalogList.item(i);
			        	Element e= (Element)cNode;
			        	String title = e.getElementsByTagName("title").item(0).getTextContent();
			        	System.out.println("Title: "+title);
			        	//if(title.contains(this.getTimeSeries())) {
			        		String UrlString=e.getElementsByTagName("link").item(0).getAttributes().item(0).getTextContent();

			        		//https://erddap.marine.ie/erddap/tabledap/spiddal_obs_ctd.fgdc
							int lastSlashPos=UrlString.lastIndexOf('/')+1;
					    	String fileName=UrlString.substring(lastSlashPos,UrlString.length());
					    	fileName=this.dataCenter.replaceAll(" ", "_")+"_"+fileName+".xml";
							System.out.println("Filename: "+fileName);
			        		System.out.println("URL: " + UrlString);
			        		String metadataUrlString=UrlString.replace("html", "fgdc");
			        		System.out.println("Metadata: "+metadataUrlString);
			        		transformFGDCXML(metadataUrlString, "xml/"+fileName );		        		
			        	//}
				    }
				    
				}else System.out.println("Invalid response: "+con.getContentType());
			} else System.out.println("Invalid Http content type: "+con.getContentType());			
			con.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public Galway(String urlstring) {
		try {
			URL url = new URL(urlstring);
			this.catalogURL=url;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getTimeSeries() {
		return timeSeries;
	}

	public void setTimeSeries(String timeSeries) {
		this.timeSeries = timeSeries;
	}

	public String getDefaultLocation() {
		return defaultLocation;
	}

	public void setDefaultLocation(String defaultLocation) {
		this.defaultLocation = defaultLocation;
	}
	
	public static void main(String[] args) {
		Galway gal= new Galway("https://erddap.marine.ie/erddap/opensearch1.1/search?page=1&itemsPerPage=1000&searchTerms=Galway&format=atom");
		gal.setDataCenter("Marine Institute Ireland");
		gal.setTimeSeries("SmartBay");
		gal.setDefaultLocation("Galway Bay");
		try {
			gal.Harvest(gal.catalogURL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}
}
