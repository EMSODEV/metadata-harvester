package de.pangaea.dcwriter;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;


import java.util.HashSet;

import java.util.List;

import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.pangaea.harvesters.DataContext;
import de.pangaea.harvesters.EuroGOOS;
import de.pangaea.harvesters.EuroSites;
import de.pangaea.harvesters.Galway;
import de.pangaea.harvesters.OGS;
import de.pangaea.harvesters.Thredds;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class MetadataExtractor {
	public String filename;
	public String filedate;
	public boolean restricted=false;
	public String datalink;
	public String maxlat;
	public String maxlon;
	public String minlat;
	public String minlon;
	public String maxdate;
	public String mindate;
	public String author;
	public String title;
	public String year;
	public String source;
	public ArrayList<String> area = new ArrayList<String>();;
	public String pi;
	public String id;
	public String publisher;
	public String dataCenter;
	public String summary;
	public List<String> platform  = new ArrayList<String>();

	public Set<String> feature = new HashSet<String>();
	public String rights;
	public List<String> parameter = new ArrayList<String>();	
	
	//private static String localdirname="/home/abe/Daten/marum/FixO3/nc_input_for_metadata_creation/June_2017/testtest/";
	private static String localdirname="xml/";

	
	public void writeXML(String path){

		Namespace pan = Namespace.getNamespace("urn:pangaea.de:dataportals");
	    Namespace dc = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
	    Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	    Document doc = new Document( new Element("dataset", pan));

	    Element root=doc.getRootElement();
	    root.setAttribute("schemaLocation", "urn:pangaea.de:dataportals http://ws.pangaea.de/schemas/pansimple/pansimple.xsd", xsi);
	    root.addNamespaceDeclaration(pan);
	    root.addNamespaceDeclaration(dc);
	    root.addNamespaceDeclaration(xsi);
	    String emailPattern ="(?i)(\\(\\s*)?[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}(\\s*\\))?";
	    
	    int lastSlashPos=this.id.lastIndexOf('/');
    	String fileName =this.id.substring(lastSlashPos+1,this.id.length());
	    //this.id="urn:file:"+this.dataCenter+":"+fileName;

    	
	    Element title= new Element("title",dc); 
	    if(this.title==null) {
	    	this.title="A "+this.dataCenter+" dataset ("+fileName+")";
	    }else
	    if(this.title.trim().equals("")) {
    		this.title="A "+this.dataCenter+" dataset ("+fileName+")";
    	}
	    else {
	    	this.title=this.title+" ("+fileName+")";
	    }
	    title.addContent(this.title);
	    root.addContent(title);
	    
	    //we always prefer the file date..
	    Element year= new Element("date",dc);
	    year.addContent(this.filedate);	
	    root.addContent(year);
	    
	    if(this.summary!=null) {
	    	Element summary= new Element("description",dc);
	    	summary.addContent(this.summary);
	    	root.addContent(summary);
	    }
	    Element identifier= new Element("identifier",dc);
	    identifier.addContent(this.id);
	    root.addContent(identifier);
	    
	    //System.out.println("-"+this.author.trim()+"-");
	    Element creator= new Element("creator",dc);
	    if(this.author ==null) {
	    	creator.addContent("Anonymous");
	    }else
	    if(this.author.trim().equals("")) {
	    	creator.addContent("Anonymous");
	    }else{
	    	this.author=this.author.replaceAll(emailPattern, "").trim();
	    	creator.addContent(this.author);
	    }
	   

	    root.addContent(creator);
	    if(this.publisher!=null) {
		    Element publisher= new Element("publisher",dc);
		    publisher.addContent(this.publisher);
		    root.addContent(publisher);
	    }
	    if (this.dataCenter!=null) {
	    	Element datacenter= new Element("dataCenter",pan);
	    	datacenter.addContent(this.dataCenter);
	    	root.addContent(datacenter);
	    }
	    
	    Element source= new Element("source",dc);
	    source.addContent(this.source);
	    root.addContent(source);
	    
	    Element pi= new Element("principalInvestigator",pan);
	    if(this.pi== null) {
	    	pi.addContent("Anonymous");
	    }else
	    if(this.pi.trim().equals("")) {
	    	pi.addContent("Anonymous");
	    }
	    else {	
			this.pi=this.pi.replaceAll(emailPattern, "").trim();
	    	pi.addContent(this.pi);
	    }
	    root.addContent(pi);
	    
	    Element linkage= new Element("linkage",pan);
	    linkage.addContent(this.datalink);
	    linkage.setAttribute("type", "LinkType", xsi);
	    linkage.setAttribute("type", "data");	
	    if(this.restricted==true)
	    	linkage.setAttribute("accessRestricted", "true");
	    root.addContent(linkage);
	    
	    Element type= new Element("type",dc);
	    type.addContent("Dataset");
	    root.addContent(type);
	    //coverage
	    Element coverage= new Element("coverage",dc);
	    coverage.setAttribute("type", "CoverageType", xsi);
	    root.addContent(coverage);
	    
	    Element maxlat= new Element("northBoundLatitude",pan);
	    maxlat.addContent(this.maxlat);
	    Element minlon= new Element("westBoundLongitude",pan);	    
	    minlon.addContent(this.minlon);
	    Element minlat= new Element("southBoundLatitude",pan);
	    minlat.addContent(this.minlat);	    
	    Element maxlon= new Element("eastBoundLongitude",pan);
	    maxlon.addContent(this.maxlon);
	    
	    Element mindate= new Element("startDate",pan);
	    mindate.addContent(this.mindate);
	    Element maxdate= new Element("endDate",pan);
	    maxdate.addContent(this.maxdate);	    

	    
	    coverage.addContent(maxlat);
	    coverage.addContent(minlon);
	    coverage.addContent(minlat);
	    coverage.addContent(maxlon);
	    coverage.addContent(mindate);
	    coverage.addContent(maxdate);
	    for(int l=0; l<this.area.size();l++) {	
	    	Element loc= new Element("location",pan);    	
	    	loc.addContent(this.area.get(l));
	    	coverage.addContent(loc);
	    }
	    
	    
	    Element format= new Element("format",dc);
	    format.addContent("NetCDF");
	    root.addContent(format);

	    //subjects / Parameters etc..
	    for(int i=0; i<this.platform.size();i++){
	    	System.out.println("PLATFORM?");
	    	Element platform = new Element("subject",dc);
	    	platform.setAttribute("type", "SubjectType", xsi);
	    	platform.setAttribute("type", "platform");
	    	System.out.println(this.platform.get(i));
	    	platform.addContent(this.platform.get(i));
	    	root.addContent(platform);
	    }
	    
	    for (String feat : this.feature) {
	    	Element feature = new Element("subject",dc);
	    	feature.setAttribute("type", "SubjectType", xsi);
	    	feature.setAttribute("type", "feature");
	    	feature.addContent(feat);
	   		root.addContent(feature);
	    }
	    
	    
	    for(int i=0; i<this.parameter.size();i++){	    	
	    	Element parameter = new Element("subject",dc);
	    	parameter.setAttribute("type", "SubjectType", xsi);
	    	parameter.setAttribute("type", "parameter");
	    	//System.out.println(this.parameter.get(i));
	    	parameter.addContent(this.parameter.get(i));
	    	root.addContent(parameter);
	    }
	    
	    XMLOutputter outputter = new XMLOutputter();
	    try {
		System.out.println(this.filename);
		int i = this.filename.lastIndexOf("/");
		String xmlFileName=this.filename.substring(i,this.filename.length()-3);
	    java.io.FileWriter xmlwriter = new java.io.FileWriter(localdirname+ xmlFileName+".xml");
	     outputter.output(doc, xmlwriter);       
	      //System.out.println(this.id+".xml WRITTEN" );
	    }
	    catch (IOException e) {
	      System.err.println(e);
	    }
	}
	
	public boolean setmetadata() throws IOException{
		NetcdfDataset ncd = null;
		try {
			ncd = NetcdfDataset.openDataset(this.filename);
			List<Attribute> attr = new ArrayList<Attribute>();
			List<Variable> vars = new ArrayList<Variable>();
			attr = ncd.getGlobalAttributes();
			vars = ncd.getVariables();					
			System.out.println(attr);			
			// Dataset Parameters
			for (int k = 0; k < vars.size(); k++) {
				//System.out.println(vars.get(k).getName());
				List<Attribute> varatts = new ArrayList<Attribute>();
				varatts = vars.get(k).getAttributes();
				for (int a = 0; a < varatts.size(); a++) {
					//now standard name instead long name => cf compatibilit in metadata
					if (varatts.get(a).getName().equals("standard_name")) {
						String paramname=varatts.get(a).getStringValue();	
						if(!this.parameter.contains(paramname))
							this.parameter.add(paramname);	
					}
				}
			}
			for (int i = 0; i < attr.size(); i++) {
					String attname = attr.get(i).getName();
					// c.itation
					if (attname.equals("author"))
						this.author = attr.get(i).getStringValue();
					else if (attname.equals("pi_name"))
						this.pi = attr.get(i).getStringValue();
					else if (attname.equals("date_update") || (attname.equals("date_created"))){
						//System.out.println(attr.get(i).getValue(0).toString());
						if(attr.get(i).getValue(0).toString().length() >= 19){
							this.year = attr.get(i).getValue(0).toString().substring(0, 19);
						}else{
							this.year = attr.get(i).getValue(0).toString();
						}
						
					}						
					else if (attname.equals("title"))
						this.title = attr.get(i).getStringValue();
					else if (attname.equals("id")) {
						if(this.id==null) {
							System.out.println("ID:"+this.id);
							this.id = attr.get(i).getStringValue();
						}
					}
					else if (attname.equals("data_assembly_center")) {
						if(attr.get(i).getStringValue().trim().length()>0)
							this.publisher = attr.get(i).getStringValue();
					}
					// identification		
					
					else if (attname.equals("site_code")) {
						if(attr.get(i).getStringValue().trim().length()>0)
							this.feature.add(attr.get(i).getStringValue());
					}
					else if (attname.equals("source")) {
						if(attr.get(i).getStringValue().trim().length()>0)
							this.source = attr.get(i).getStringValue();
					}
					// location
					else if (attname.equals("area")) {
						if(attr.get(i).getStringValue().trim().length()>0)
							this.area.add(attr.get(i).getStringValue());
					}
					else if (attname.equals("geospatial_lat_min")){
						if(attr.get(i).getValue(0).toString().length() == 0){
							System.out.println("ERROR: no lat_min");
						}else{
							this.minlat= attr.get(i).getValue(0).toString();
						}
					}
					else if (attname.equals("geospatial_lat_max"))
						if(attr.get(i).getValue(0).toString().length() == 0){
							System.out.println("ERROR: no lat_max");
						}else{
							this.maxlat= attr.get(i).getValue(0).toString();
						}
					else if (attname.equals("geospatial_lon_min"))
						if(attr.get(i).getValue(0).toString().length() == 0){
							System.out.println("ERROR: no lon_min");
						}else{
							this.minlon= attr.get(i).getValue(0).toString();
						}
					else if (attname.equals("geospatial_lon_max")){
						if(attr.get(i).getValue(0).toString().length() == 0){
							System.out.println("ERROR: no lon_max");
						}else{
							this.maxlon= attr.get(i).getValue(0).toString();
						}
					}
					/**
					 * TODO: check if start/enddates are valid, format if required
					 */
					else if (attname.equals("time_coverage_start"))
						if(attr.get(i).getValue(0).toString().length() >= 19){
							this.mindate = attr.get(i).getValue(0).toString().substring(0, 19);
						}else{
							this.mindate = attr.get(i).getValue(0).toString();
						}
						
					else if (attname.equals("time_coverage_end"))
						if(attr.get(i).getValue(0).toString().length() >= 19){
							this.maxdate = attr.get(i).getValue(0).toString().substring(0, 19);
						}else{
							this.maxdate = attr.get(i).getValue(0).toString();
						}
						
					// rights
					else if (attname.equals("distribution_statement")) {
						if(attr.get(i).getStringValue().trim().length()>0)
							this.rights= attr.get(i).getStringValue();
					}
				}			
		
				ncd.close();
			
		} catch (IOException error) {
			System.out.println("Error: SetMetadata "+error.getMessage());
		} 
		return true;
	}


	public MetadataExtractor(DataContext context) throws IOException {
		this.filename = context.getTempFile();
		this.filedate=context.getFileDate();
		this.id=context.getUrl();
		this.datalink=context.getUrl();
		this.feature.add(context.getTimeSeries());
		System.out.println(context.getDefaultLocation());
		this.area.add(context.getDefaultLocation());
		this.dataCenter=context.getDataCenter();
		this.restricted=context.isAccessRestricted();
	}
	
	static boolean checkFileExists(String urlName) throws IOException {
		try {
	        new URL(urlName).openStream().close();
	        return true;
	    } catch (IOException e) {
	    	System.out.println(e.getMessage());
	        return false;
	    }
	}
	
	public static void CleanIndex() {
		System.out.println("Cleaning...");
		File[] files = new File("xml").listFiles();
		for (File file : files) {
			System.out.println("File: " + file.getName());			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = null;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				org.w3c.dom.Document doc = dBuilder.parse(file);
				String linkage = doc.getElementsByTagName("linkage").item(0).getTextContent();
				if(checkFileExists(linkage)) {
					System.out.println(linkage);
				}
				else {
					System.out.println("############################# FILE DOES NOT EXIST ######################################");
				}
				
				
			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		int counter=0;
		
		
		CleanIndex();
		System.exit(0);
		
		
		System.out.println("##################################### Galway ########################################");	
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
		
		
		
		System.out.println("##################################### OGS ########################################");		
		OGS ogs= new OGS("http://nodc.ogs.trieste.it/doi/archive/doilist.xml");
		ogs.setDataCenter("OGS Trieste");
		ogs.setTimeSeries("E2M3A");
		ogs.setDefaultLocation("Adriatic Sea");
		try {
			ogs.Harvest(ogs.catalogURL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("##################################### FixO3 ########################################");
		String ftimeseries[]= {"SOG","STATIONM"};
		String flocation[]= {"South Atlantic","Nordic Sea"};		

		for (int i=0;i< ftimeseries.length;i++) {
			String ftp="ftp://fixo3.eu/data/ftpdownload/"+ftimeseries[i];
			EuroSites fix = new EuroSites(ftp);
			fix.setDataCenter("FixO3");
			fix.setTimeSeries(ftimeseries[i]);
			fix.setDefaultLocation(flocation[i]);
			try {
				fix.Harvest(counter);
				} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		//EuroGOOS
		System.out.println("#################################### EuroGOOS #########################################");
		String[] euroGOOSftpurls = {"ftp://medinsitu.hcmr.gr/Core/INSITU_MED_NRT_OBSERVATIONS_013_035/monthly/vessel",
				"ftp://medinsitu.hcmr.gr/Core/INSITU_MED_NRT_OBSERVATIONS_013_035/monthly/mooring",
				"ftp://arcas.puertos.es/Core/INSITU_IBI_NRT_OBSERVATIONS_013_033/monthly/mooring",
				"ftp://vftpmo.io-bas.bg/Core/INSITU_BS_NRT_OBSERVATIONS_013_034/monthly/mooring"
				};
			
		for(int i=0; i< euroGOOSftpurls.length;i++) {
			EuroGOOS eur = new EuroGOOS(euroGOOSftpurls[i]);
			eur.timeSeries=  new String[]{"E1M3A","PYLOS","OBSEA","W1M3A","MOMAR","Biscay AGL","CIS","E2M3A","EUXRo01","EUXRo02","EUXRo03"};
			//NetCDF filename has to be checked against this strings
			eur.checkName=  new String[]{"E1M3A","68422","OBSEA","W1M3A","EXIF0002","6201030","CIS","E2M3A","EUXRo01","EUXRo02","EUXRo03"};
			eur.location=   new String[]{"Hellenic Arc","Hellenic Arc","Balearic Sea","Ligurian Sea","Azores Islands","Bay of Biscay","Central Irminger Sea","Adriatic Sea","Black Sea","Black Sea","Black Sea"};
			
			eur.setDataCenter("EuroGOOS");
			try {
				eur.Harvest(counter);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//EuroSites
		System.out.println("##################################### EuroSITES ########################################");
		String estimeseries[]= {"ANTARES","PAP","NOG","DYFAMED","SOG","STATION-M","TENATSO","LION","FRAM"};
		String eslocation[]= {"Ligurian Sea","Porcupine Abyssal Plain","North Atlantic","Ligurian Sea","South Atlantic","Nordic Sea","Cape Verde","Ligurian Sea","Arctic"};		

		for (int i=0;i< estimeseries.length;i++) {
			String ftp="ftp://ftp.ifremer.fr/ifremer/oceansites/DATA/"+estimeseries[i];
			EuroSites eur = new EuroSites(ftp);
			eur.setDataCenter("EuroSITES");
			eur.setTimeSeries(estimeseries[i]);
			eur.setDefaultLocation(eslocation[i]);
			try {
				eur.Harvest(counter);
				} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		//Plocan
		System.out.println("################################### PLOCAN  ##########################################");
		Thredds thredds= new Thredds("http://data.plocan.eu/thredds/catalog/aggregate/public/catalog.xml");
		thredds.setDataCenter("PLOCAN");
		thredds.setTimeSeries("ESTOC");
		thredds.setDefaultLocation("Canary Islands");
		try {
			thredds.Harvest(thredds.getCatalogURL(),counter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
		///////////////////////////////////////////
		// view-source:http://nodc.ogs.trieste.it/nodc/metadata/doidetails?doi=10.6092/f8e6d18e-f877-4aa5-a983-a03b06ccb987
		// http://fixo3.eu/data/ftpdownload/STATIONM
		// http://fixo3.eu/data/ftpdownload/SOG
		
		System.out.println("DONE...");
	}

}
