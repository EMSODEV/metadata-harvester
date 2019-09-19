package de.pangaea.harvesters;


import java.io.File;

import java.io.FileOutputStream;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import de.pangaea.dcwriter.MetadataExtractor;

public class EuroSites {
	private URL catalogURL;
	public ArrayList<DataContext> harvestedFileNames = new ArrayList<DataContext>();
	private String dataCenter;
	private String timeSeries;
	private String defaultLocation;
	
	public void Harvest(int count) throws SocketException, IOException {
		String ftpserver=catalogURL.getHost();		
		System.out.println("Connect to: "+ftpserver);
		FTPFile[] fileNames;
		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpserver);
		ftpClient.login("anonymous", "anonymous");
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		System.out.println(ftpClient.getReplyString());
		ftpClient.changeWorkingDirectory(catalogURL.getPath().toString());	
		fileNames = ftpClient.listFiles();		
		for(int i=0;i<fileNames.length;i++) {
			Calendar fileDate =fileNames[i].getTimestamp();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
			String timestamp= format.format(fileDate.getTime());
			if(fileNames[i].getName().contains(".nc")) {
				count++;
				System.out.println("NetCDF: "+fileNames[i].getName()+" "+timestamp);
				//cache
				String tempname ="cache/"+this.getDataCenter()+"_"+count+"_"+fileNames[i].getName();
				File cachedFile = new File(tempname);	
				//check if cache file exists and has same timestamp otherwise get a new version..
				if(cachedFile.exists() && (cachedFile.lastModified()==fileDate.getTimeInMillis())) {
					System.out.println("Using cached file..");
				}else {
					FileOutputStream outS = new FileOutputStream(tempname);
					ftpClient.retrieveFile(fileNames[i].getName(), outS);				
					outS.flush();
					outS.close();
					cachedFile.setLastModified(fileDate.getTimeInMillis());
				}
				String dataLink=catalogURL.getProtocol()+"://"+catalogURL.getHost()+ftpClient.printWorkingDirectory()+"/"+fileNames[i].getName();
				DataContext context =new DataContext(dataLink);
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
	
	
	public EuroSites(String urlstring) {
		try {
			URL url = new URL(urlstring);
			this.catalogURL=url;
		} catch (MalformedURLException e) {
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
		if(timeSeries.equals("STATIONM")) {
			this.timeSeries="STATION-M";
		}
		else {
			this.timeSeries = timeSeries;
		}
	}
	
	public static void main(String[] args) {
		
		String timeseries[]= {"ANTARES","PAP","NOG","DYFAMED","SOG","STATION-M","TENATSO","LION","FRAM"};
		String location[]= {"Ligurian Sea","Porcupine Abyssal Plain","North Atlantic","Ligurian Sea","South Atlantic","Nordic Sea","Cape Verde","Ligurian Sea","Arctic"};
		

		for (int i=0;i< timeseries.length;i++) {
			String ftp="ftp://ftp.ifremer.fr/ifremer/oceansites/DATA/"+timeseries[i];
			EuroSites eur = new EuroSites(ftp);
			eur.setDataCenter("EuroSITES");
			eur.setTimeSeries(timeseries[i]);
			eur.setDefaultLocation(location[i]);
			try {
				eur.Harvest(0);
				} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}





	public String getDefaultLocation() {
		return defaultLocation;
	}


	public void setDefaultLocation(String defaultLocation) {
		this.defaultLocation = defaultLocation;
	}


}
