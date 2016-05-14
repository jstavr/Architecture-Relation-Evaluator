package edu.uci.isr.archstudio4.launcher;

import org.eclipse.swt.graphics.Image;

public class LaunchData implements ILaunchData{
	protected Image icon = null;
	protected String name = null;
	protected String description = null;
	protected String eclipseID = null;
	protected int launchType = VIEW;

	public LaunchData(String eclipseID, String name, String description, Image icon, int launchType){
		super();
		this.eclipseID = eclipseID;
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.launchType = launchType;
	}
	
	public String getEclipseID(){
		return eclipseID;
	}

	public String getDescription(){
		return description;
	}

	public Image getIcon(){
		return icon;
	}

	public int getLaunchType(){
		return launchType;
	}

	public String getName(){
		return name;
	}
	
}
