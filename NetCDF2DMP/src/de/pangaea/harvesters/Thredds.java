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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.pangaea.dcwriter.MetadataExtractor;



public class Thredds {
	private URL catalogURL;
	private String fileServer="http://data.plocan.eu/thredds/fileServer/";
	public ArrayList<DataContext> harvestedFileNames = new ArrayList<DataContext>();
	
	private String dataCenter;
	private String timeSeries;
	private String defaultLocation;
	
	public void setError(String error) {
		System.out.println(error);
	}
	
	
	public URL getCatalogURL() {
		return this.catalogURL;
	}
	
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
	
	public void Harvest(URL url, int count) throws Exception {

		System.out.println("Harvesting: "+url.toString());
		int lastSlashPos=url.toString().lastIndexOf('/');
		String path=url.toString().substring(0,lastSlashPos)+"/";
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
				    NodeList catalogList=doc.getElementsByTagName("catalogRef");
				    NodeList datasetList=doc.getElementsByTagName("dataset");	    
				    for (int i = 0; i < catalogList.getLength(); ++i) {
				    	Node cNode =catalogList.item(i);
			        	Element e= (Element)cNode;
				    	String catalogLink=e.getAttribute("xlink:href");
						URL caturl=new URL(path+catalogLink);							
						this.Harvest(caturl,count);
				    }
				    for(int d=0;d<datasetList.getLength();d++) {				    	
				    	Node dNode =datasetList.item(d);				    	
			        	Element ds= (Element)dNode;
			        	
			        	String datasetLink=ds.getAttribute("urlPath");
			        	if(!datasetLink.isEmpty()) {
			        		//http://data.plocan.eu/thredds/dodsC/aggregate/public/ESTOCInSitu/ANIMATE/OS_ESTOC-2_200304_D_CTD.nc.html
			        		//http://data.plocan.eu/thredds/fileServer/aggregate/public/ESTOCInSitu/ANIMATE/OS_ESTOC-2_200304_D_CTD.nc
			        		int lastSlashPos2=datasetLink.lastIndexOf('/');
			        		String fileName=datasetLink.substring(lastSlashPos2+1,datasetLink.length());
				        	String timestamp =ds.getElementsByTagName("date").item(0).getTextContent();
				        	if(datasetLink.contains(".nc")) {
				        		count++;
				        		URL fileUrl=new URL(fileServer+datasetLink);
				        		System.out.println(fileServer+datasetLink);
				        		System.out.println("NetCDF: "+fileName+" "+timestamp);
					        	Calendar fileDate = Calendar.getInstance();
					        	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
					        	fileDate.setTime(format.parse(timestamp));
								//cache
								String tempname ="cache/"+this.getDataCenter()+"_"+count+"_"+fileName;
								
								File cachedFile = new File(tempname);
							
								//check if cache file exists and has same timestamp otherwise get a new version..
								if(cachedFile.exists() && (cachedFile.lastModified()==fileDate.getTimeInMillis())) {
									System.out.println("Using cached file..");
								}else {
									this.downloadFile(fileUrl, tempname);
									cachedFile.setLastModified(fileDate.getTimeInMillis());
								}
								
								DataContext context =new DataContext(fileUrl.toString());
								context.setTimeSeries(this.getTimeSeries()); 
								context.setDefaultLocation(this.getDefaultLocation());
								context.setDataCenter(this.getDataCenter());
								context.setFileDate(timestamp);
								context.setTempFile(tempname);
								MetadataExtractor meta = new MetadataExtractor(context);
								meta.setmetadata();
								meta.writeXML("xml/");
				        	}
			        	}
				    }				   

				}else this.setError("Invalid response: "+con.getContentType());
			} else this.setError("Invalid Http content type: "+con.getContentType());			
			con.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
	public Thredds(String urlstring) {
		try {
			URL url = new URL(urlstring);
			this.catalogURL=url;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		Thredds thredds= new Thredds("http://data.plocan.eu/thredds/catalog/aggregate/public/catalog.xml");
		thredds.setDataCenter("PLOCAN");
		thredds.setTimeSeries("ESTOC");
		thredds.setDefaultLocation("Canary Islands");
		try {
			thredds.Harvest(thredds.catalogURL,0);
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
}
