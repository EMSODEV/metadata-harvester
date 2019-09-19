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

public class EuroGOOS {
	private URL catalogURL;
	public ArrayList<DataContext> harvestedFileNames = new ArrayList<DataContext>();
	private String dataCenter;
	
	public String[] timeSeries;
	//NetCDF filename has to be checked against this strings
	public String[] checkName;
	public String[] location;
	
	public void Harvest(int count) throws SocketException, IOException {
		String ftpserver=catalogURL.getHost();
		System.out.println("Connect to: "+ftpserver);
		FTPFile[] fileNames;
		FTPFile[] dirNames;
		FTPClient ftpClient = new org.apache.commons.net.ftp.FTPClient();
		ftpClient.connect(ftpserver);
		//REPLACE USERNAME AND PASSWORD
		ftpClient.login("username", "password");
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.enterLocalPassiveMode();
		String ftppath=catalogURL.getPath().toString();
		String ftphistorypath=ftppath.replace("monthly", "history");
		//Monthly directories
			ftpClient.changeWorkingDirectory(ftppath);	
			System.out.println(ftpClient.getReplyString());
			//System.out.println(ftppath);
			dirNames=ftpClient.listDirectories();
			System.out.println(ftpClient.getReplyString());
			for(int j=0;j<dirNames.length;j++) {			
				if(dirNames[j].getName().matches("[0-9]{6}")){
					System.out.println("Directory: "+dirNames[j].getName());
					ftpClient.changeWorkingDirectory(dirNames[j].getName());
					fileNames = ftpClient.listFiles();	
					for(int i=0;i<fileNames.length;i++) {
						String currentTimeSeries="";
						String currentLocation="";
						if(fileNames[i].getName().contains(".nc")) {
							boolean isNameOK=false;
							for(int k=0;k<this.checkName.length;k++) {
								if(fileNames[i].getName().contains(checkName[k])) {
									isNameOK=true;
									currentTimeSeries=timeSeries[k];
									currentLocation=location[k];									
								}
							}
							if(isNameOK) {
								count++;
								Calendar fileDate =fileNames[i].getTimestamp();
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
								String timestamp= format.format(fileDate.getTime());
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
								context.setTimeSeries(currentTimeSeries); 
								context.setDefaultLocation(currentLocation);
								System.out.println(currentLocation);
								context.setDataCenter(this.getDataCenter());
								context.setFileDate(timestamp);
								context.setTempFile(tempname);
								MetadataExtractor meta = new MetadataExtractor(context);
								meta.restricted=true;
								meta.setmetadata();
								meta.writeXML("xml/");
							}
							
						}
					}
					ftpClient.changeWorkingDirectory("../");
				}
			}
		//History directory
		//System.out.println(ftphistorypath);
		ftpClient.changeWorkingDirectory(ftphistorypath);	
		FTPFile[] hfileNames = ftpClient.listFiles();	
		for(int i=0;i<hfileNames.length;i++) {
			String hcurrentTimeSeries="";
			String hcurrentLocation="";
			if(hfileNames[i].getName().contains(".nc")) {
				boolean hfileNameOK=false;
				for(int k=0;k<this.checkName.length;k++) {
					if(hfileNames[i].getName().contains(checkName[k])) {
						hfileNameOK=true;
						hcurrentTimeSeries=timeSeries[k];
						hcurrentLocation=location[k];
					}
				}
				if(hfileNameOK) {
					Calendar hfileDate =hfileNames[i].getTimestamp();
					SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
					String htimestamp= format1.format(hfileDate.getTime());
					System.out.println("NetCDF: "+hfileNames[i].getName()+" Date: "+htimestamp);
					String htempname ="cache/"+hfileNames[i].getName();
					File hcachedFile = new File(htempname);	
					if(hcachedFile.exists() && (hcachedFile.lastModified()==hfileDate.getTimeInMillis())) {
						System.out.println("Using cached file..");
					}else {
						FileOutputStream houtS = new FileOutputStream(htempname);
						ftpClient.retrieveFile(hfileNames[i].getName(), houtS);				
						houtS.flush();
						houtS.close();
						hcachedFile.setLastModified(hfileDate.getTimeInMillis());						
					}
					String hdataLink=catalogURL.getProtocol()+"://"+catalogURL.getHost()+ftpClient.printWorkingDirectory()+"/"+hfileNames[i].getName();
					DataContext hcontext =new DataContext(hdataLink);
					hcontext.setTimeSeries(hcurrentTimeSeries);
					hcontext.setDefaultLocation(hcurrentLocation);
					hcontext.setDataCenter(this.getDataCenter());
					hcontext.setFileDate(htimestamp);
					hcontext.setTempFile(htempname);
					hcontext.setAccessRestricted(true);
					MetadataExtractor hmeta = new MetadataExtractor(hcontext);
					hmeta.setmetadata();
					hmeta.writeXML("xml/");
				}
			}
		}
		System.out.println(ftpClient.getReplyString());
		
	}
	
	public EuroGOOS(String urlstring) {
		try {
			URL url = new URL(urlstring);
			this.catalogURL=url;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setCatalogURL(String urlstring) {
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
}
