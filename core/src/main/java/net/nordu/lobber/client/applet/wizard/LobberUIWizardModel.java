package net.nordu.lobber.client.applet.wizard;

import java.io.File;

import org.pietschy.wizard.models.DynamicModel;

public class LobberUIWizardModel extends DynamicModel {

	public String getTracker() {
		return tracker;
	}

	public void setTracker(String tracker) {
		this.tracker = tracker;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public String getApiurl() {
		return apiurl;
	}

	public void setApiurl(String apiurl) {
		this.apiurl = apiurl;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setPublicAccess(boolean publicAccess) {
		this.publicAccess = publicAccess;
	}
	
	public boolean isPublicAccess() {
		return publicAccess;
	}

	private File file;
	private String name;
	
	private String tracker;
	private String expires;
	private String sessionid;
	private String apiurl;
	private String description;
	private boolean publicAccess;
	
	public LobberUIWizardModel() {
		super();
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
