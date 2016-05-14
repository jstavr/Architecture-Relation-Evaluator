package edu.uci.isr.archstudio4.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchPath;

public class XadlTreeUtils{

	public static final int UNKNOWN = 0;
	public static final int STRUCTURE = (1 << 1);
	public static final int COMPONENT = (1 << 2);
	public static final int CONNECTOR = (1 << 3);
	public static final int COMPONENT_INTERFACE = (1 << 4);
	public static final int CONNECTOR_INTERFACE = (1 << 5);
	public static final int ANY_INTERFACE = COMPONENT_INTERFACE | CONNECTOR_INTERFACE;
	public static final int COMPONENT_TYPE = (1 << 6);
	public static final int CONNECTOR_TYPE = (1 << 7);
	public static final int COMPONENT_TYPE_SIGNATURE = (1 << 8);
	public static final int CONNECTOR_TYPE_SIGNATURE = (1 << 9);
	public static final int ANY_SIGNATURE = COMPONENT_TYPE_SIGNATURE | CONNECTOR_TYPE_SIGNATURE;
	public static final int INTERFACE_TYPE = (1 << 10);
	public static final int ANY_TYPE = COMPONENT_TYPE | CONNECTOR_TYPE | INTERFACE_TYPE;

	public static final int STATECHART = (1 << 11);
	public static final int STATE = (1 << 12);
	
	public static final int DOCUMENT = (1 << 0);
	protected static final int TYPES = (1 << 15);

	protected static Map<String,Integer> pathToTypeMap = new HashMap<String,Integer>();
	protected static Map<Integer,String> typeToStringMap = new LinkedHashMap<Integer,String>();
	
	static{
		pathToTypeMap.put("xArch", new Integer(DOCUMENT));
		pathToTypeMap.put("xArch/archStructure", new Integer(STRUCTURE));
		pathToTypeMap.put("xArch/archStructure/component", new Integer(COMPONENT));
		pathToTypeMap.put("xArch/archStructure/component/interface", new Integer(COMPONENT_INTERFACE));
		pathToTypeMap.put("xArch/archStructure/connector", new Integer(CONNECTOR));
		pathToTypeMap.put("xArch/archStructure/connector/interface", new Integer(CONNECTOR_INTERFACE));
		
		pathToTypeMap.put("xArch/archTypes", new Integer(TYPES));
		pathToTypeMap.put("xArch/archTypes/componentType", new Integer(COMPONENT_TYPE));
		pathToTypeMap.put("xArch/archTypes/componentType/signature", new Integer(COMPONENT_TYPE_SIGNATURE));
		pathToTypeMap.put("xArch/archTypes/connectorType", new Integer(CONNECTOR_TYPE));
		pathToTypeMap.put("xArch/archTypes/connectorType/signature", new Integer(CONNECTOR_TYPE_SIGNATURE));
		pathToTypeMap.put("xArch/archTypes/interfaceType", new Integer(INTERFACE_TYPE));

		pathToTypeMap.put("xArch/statechart", new Integer(STATECHART));
		pathToTypeMap.put("xArch/statechart/state", new Integer(STATE));
		
		typeToStringMap.put(new Integer(DOCUMENT), "document");
		typeToStringMap.put(new Integer(STRUCTURE), "structure");
		typeToStringMap.put(new Integer(COMPONENT), "component");
		typeToStringMap.put(new Integer(COMPONENT_INTERFACE), "component interface");
		typeToStringMap.put(new Integer(CONNECTOR), "connector");
		typeToStringMap.put(new Integer(CONNECTOR_INTERFACE), "connector interface");
		typeToStringMap.put(new Integer(TYPES), "type set");
		typeToStringMap.put(new Integer(COMPONENT_TYPE), "component type");
		typeToStringMap.put(new Integer(COMPONENT_TYPE_SIGNATURE), "component type signature");
		typeToStringMap.put(new Integer(CONNECTOR_TYPE), "connector type");
		typeToStringMap.put(new Integer(CONNECTOR_TYPE_SIGNATURE), "connector type signature");
		typeToStringMap.put(new Integer(INTERFACE_TYPE), "interface type");
		typeToStringMap.put(new Integer(STATECHART), "statechart");
		typeToStringMap.put(new Integer(STATE), "state");
	}
	
	public static int getType(XArchFlatInterface xarch, ObjRef ref){
		XArchPath path = xarch.getXArchPath(ref);
		String pathString = path.toTagsOnlyString();
		Integer typeInteger = (Integer)pathToTypeMap.get(pathString);
		if(typeInteger == null) return UNKNOWN;
		return typeInteger.intValue();
	}
	
	public static Image getIconForType(IResources resources, int type){
		ArchstudioResources.init(resources);
		switch(type){
		case XadlTreeUtils.UNKNOWN:
			return null;
		case XadlTreeUtils.DOCUMENT:
			return resources.getImage(ArchstudioResources.ICON_XML_DOCUMENT);
		case XadlTreeUtils.STRUCTURE:
			return resources.getImage(ArchstudioResources.ICON_STRUCTURE);
		case XadlTreeUtils.COMPONENT:
			return resources.getImage(ArchstudioResources.ICON_COMPONENT);
		case XadlTreeUtils.CONNECTOR:
			return resources.getImage(ArchstudioResources.ICON_CONNECTOR);
		case XadlTreeUtils.COMPONENT_INTERFACE:
		case XadlTreeUtils.CONNECTOR_INTERFACE:
			return resources.getImage(ArchstudioResources.ICON_INTERFACE);
		case XadlTreeUtils.TYPES:
			return resources.getImage(ArchstudioResources.ICON_TYPES);
		case XadlTreeUtils.COMPONENT_TYPE:
			return resources.getImage(ArchstudioResources.ICON_COMPONENT_TYPE);
		case XadlTreeUtils.CONNECTOR_TYPE:
			return resources.getImage(ArchstudioResources.ICON_CONNECTOR_TYPE);
		case XadlTreeUtils.COMPONENT_TYPE_SIGNATURE:
		case XadlTreeUtils.CONNECTOR_TYPE_SIGNATURE:
			return resources.getImage(ArchstudioResources.ICON_INTERFACE);
		case XadlTreeUtils.INTERFACE_TYPE:
			return resources.getImage(ArchstudioResources.ICON_INTERFACE_TYPE);
		case XadlTreeUtils.STATECHART:
			return resources.getImage(ArchstudioResources.ICON_STATECHART);
		case XadlTreeUtils.STATE:
			return resources.getImage(ArchstudioResources.ICON_STATE);
		default:
			return null;
		}
	}
	
	public static String[] typesToStrings(int types){
		List<String> matchingList = new ArrayList<String>();
		for(Iterator it = typeToStringMap.keySet().iterator(); it.hasNext(); ){
			Integer integer = (Integer)it.next();
			int i = (integer).intValue();
			if((types & i) != 0){
				matchingList.add(typeToStringMap.get(integer));
			}
		}
		return (String[])matchingList.toArray(new String[matchingList.size()]);
	}

	private XadlTreeUtils(){}
}
