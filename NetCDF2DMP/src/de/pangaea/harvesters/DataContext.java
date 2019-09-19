package de.pangaea.harvesters;


public class DataContext {
	private String url;
	private String timeSeries;
	private String dataCenter;
	private String fileDate;
	public String checkname; //used to identify e.g. files in EuroGOOS
	private String defaultLocation; 
	private String tempFile; 
	private boolean accessRestricted=false; 
	
	
	public DataContext(String url){
		this.setUrl(url);
	}
	public DataContext(){
	}
	public String getTimeSeries() {
		return timeSeries;
	}
	public void setTimeSeries(String timeSeries) {
		this.timeSeries = timeSeries;
	}
	public String getDataCenter() {
		return dataCenter;
	}
	public void setDataCenter(String dataCenter) {
		this.dataCenter = dataCenter;
	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	public String getDefaultLocation() {
		return defaultLocation;
	}
	public void setDefaultLocation(String defaultLocation) {
		this.defaultLocation = defaultLocation;
	}
	public String getTempFile() {
		return tempFile;
	}
	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}
	public boolean isAccessRestricted() {
		return accessRestricted;
	}
	public void setAccessRestricted(boolean accessRestricted) {
		this.accessRestricted = accessRestricted;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
