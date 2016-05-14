package edu.uci.isr.archstudio4.launcher;

import org.eclipse.swt.graphics.Image;

public interface ILaunchData{
	
	public static final int VIEW = 100;
	public static final int EDITOR = 200;

	public String getDescription();
	public Image getIcon();
	public int getLaunchType();
	public String getName();
	public String getEclipseID();
}