package edu.uci.isr.archstudio4.comp.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.launcher.ILaunchData;
import edu.uci.isr.archstudio4.launcher.ILaunchable;
import edu.uci.isr.myx.conn.IMultiwayResults;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

public class LauncherMyxComponent extends AbstractMyxSimpleBrick{

	public static final IMyxName INTERFACE_NAME_OUT_RESOURCES = MyxUtils.createName("resources");
	public static final IMyxName INTERFACE_NAME_OUT_LAUNCHER = MyxUtils.createName("launcher");
	public static final IMyxName INTERFACE_NAME_OUT_LAUNCHER_RESULTS = MyxUtils.createName("results");
	
	protected IMultiwayResults launchable_results = null;
	protected ILaunchable launchable = null;
	protected IResources resources = null;
	
	private MyxRegistry er = MyxRegistry.getSharedInstance();
	
	public LauncherMyxComponent(){
	}
	
	public void begin(){
		resources = (IResources)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_RESOURCES);
		launchable = (ILaunchable)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_LAUNCHER);
		launchable_results = (IMultiwayResults)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_LAUNCHER_RESULTS);
		er.register(this);
	}
	
	public void end(){
		er.unregister(this);
	}

	public Object getServiceObject(IMyxName interfaceName){
		return null;
	}
	
	public IResources getResources(){
		return resources;
	}
	
	public ILaunchData[] getLaunchData(){
		launchable.getLaunchData();
		Object[] res = launchable_results.getReturnValues();
		java.util.List l = new ArrayList();
		for(int i = 0; i < res.length; i++){
			if(res[i] != null){
				l.add(res[i]);
			}
		}
		Collections.sort(l, new Comparator(){
			public int compare(Object o1, Object o2){
				String n1 = ((ILaunchData)o1).getName();
				String n2 = ((ILaunchData)o2).getName();
				return n1.compareTo(n2);
			}
		});
		return (ILaunchData[])l.toArray(new ILaunchData[l.size()]);
	}

}
