package edu.uci.isr.archstudio4.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.archstudio4.comp.resources.IResources;

public class ArchstudioResources{

	public static final String ICON_STRUCTURE = "archstudio:icon/structure";
	public static final String ICON_TYPES = "archstudio:icon/types";
	public static final String ICON_XML_DOCUMENT = "archstudio:icon/xml-document";
	public static final String ICON_OVERLAY_XML = "archstudio:icon-overlay/xml";

	public static final String ICON_COMPONENT = "archstudio:icon/component";
	public static final String ICON_COMPONENT2 = "archstudio:icon/component2";
	public static final String ICON_CONNECTOR = "archstudio:icon/connector";
	public static final String ICON_INTERFACE = "archstudio:icon/interface";
	public static final String ICON_LINK = "archstudio:icon/link";

	public static final String ICON_COMPONENT_TYPE = "archstudio:icon/component-type";
	public static final String ICON_CONNECTOR_TYPE = "archstudio:icon/connector-type";
	public static final String ICON_INTERFACE_TYPE = "archstudio:icon/interface-type";

	public static final String ICON_STATECHART = "archstudio:icon/statechart";
	public static final String ICON_STATE = "archstudio:icon/state";
	public static final String ICON_INITIAL_STATE = "archstudio:icon/initial-state";
	public static final String ICON_FINAL_STATE = "archstudio:icon/final-state";

	public static final String ICON_GRID = "archstudio:icon/grid";

	public static final String ICON_OVERLAY_ADDED = "archstudio:icon-overlay/added";

	public static final String ICON_OVERLAY_ADDED_COMPONENT = "archstudio:icon-overlay-added/component";

	public static final String ICON_ACTOR = "archstudio:icon/actor";
	public static final String ICON_ACTIVITY_DIAGRAMS_DECISION = "archstudio:icon/decision";
	public static final String ICON_ACTIVITY_DIAGRAMS_MERGE = "archstudio:icon/merge";
	public static final String ICON_ACTIVITY_DIAGRAMS_JOIN = "archstudio:icon/join";
	public static final String ICON_ACTIVITY_DIAGRAMS_FORK = "archstudio:icon/fork";
	public static final String ICON_ACTIVITY_DIAGRAMS_NOTE = "archstudio:icon/note";
	public static final String ICON_ACTIVITY_DIAGRAMS_COMPOSITE = "archstudio:icon/composite";
	public static final String ICON_ACTIVITY_DIAGRAMS = "archstudio:icon/diagrams";

	protected static Set<IResources> initedResources = new HashSet<IResources>();

	public static void init(IResources resources){
		if(initedResources.contains(resources)){
			return;
		}

		resources.createImage(ICON_STRUCTURE, Archstudio4Activator.class.getResourceAsStream("res/icon-structure.gif"));
		resources.createImage(ICON_TYPES, Archstudio4Activator.class.getResourceAsStream("res/icon-types.gif"));

		resources.createImage(ICON_OVERLAY_XML, Archstudio4Activator.class.getResourceAsStream("res/decorator-xml.gif"));
		Image xmlOverlay = resources.getImage(ICON_OVERLAY_XML);
		resources.createOverlayImage(ICON_XML_DOCUMENT, resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER), new Image[]{xmlOverlay}, new int[]{IResources.BOTTOM_RIGHT});

		resources.createImage(ICON_COMPONENT, Archstudio4Activator.class.getResourceAsStream("res/icon-component.gif"));
		resources.createImage(ICON_CONNECTOR, Archstudio4Activator.class.getResourceAsStream("res/icon-connector.gif"));
		resources.createImage(ICON_INTERFACE, Archstudio4Activator.class.getResourceAsStream("res/icon-interface.gif"));
		resources.createImage(ICON_LINK, Archstudio4Activator.class.getResourceAsStream("res/icon-link.gif"));

		resources.createImage(ICON_COMPONENT_TYPE, Archstudio4Activator.class.getResourceAsStream("res/icon-component-type.gif"));
		resources.createImage(ICON_CONNECTOR_TYPE, Archstudio4Activator.class.getResourceAsStream("res/icon-connector-type.gif"));
		resources.createImage(ICON_INTERFACE_TYPE, Archstudio4Activator.class.getResourceAsStream("res/icon-interface-type.gif"));

		resources.createImage(ICON_STATECHART, Archstudio4Activator.class.getResourceAsStream("res/icon-statechart.gif"));
		resources.createImage(ICON_STATE, Archstudio4Activator.class.getResourceAsStream("res/icon-state.gif"));
		resources.createImage(ICON_INITIAL_STATE, Archstudio4Activator.class.getResourceAsStream("res/icon-initialstate.gif"));
		resources.createImage(ICON_FINAL_STATE, Archstudio4Activator.class.getResourceAsStream("res/icon-finalstate.gif"));

		resources.createImage(ICON_GRID, Archstudio4Activator.class.getResourceAsStream("res/icon-grid.gif"));
		resources.createImage(ICON_OVERLAY_ADDED, Archstudio4Activator.class.getResourceAsStream("res/ovr_added.gif"));
		//resources.createImage(ICON_COMPONENT_TYPE, Archstudio4Activator.class.getResourceAsStream("res/ovr_added.gif"));

		//resources.createImage(ICON_COMPONENT_TYPE, Archstudio4Activator.class.getResourceAsStream("res/icon-component-type.gif"));
		/*Image addedOverlay = resources.getImage(ICON_OVERLAY_ADDED);
		resources.createOverlayImage(ICON_COMPONENT_TYPE, 
			resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER), 
			new Image[]{addedOverlay}, new int[]{IResources.TOP_LEFT});
			*/

		resources.createImage(ICON_ACTIVITY_DIAGRAMS_DECISION, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-decision-16x16.gif"));
		resources.createImage(ICON_ACTIVITY_DIAGRAMS_MERGE, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-merge-16x16.gif"));
		resources.createImage(ICON_ACTIVITY_DIAGRAMS_JOIN, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-join-16x16.gif"));
		resources.createImage(ICON_ACTIVITY_DIAGRAMS_FORK, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-fork-16x16.gif"));
		resources.createImage(ICON_ACTIVITY_DIAGRAMS_NOTE, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-note-16x16.gif"));
		resources.createImage(ICON_ACTIVITY_DIAGRAMS_COMPOSITE, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-adcomposite-16x16.gif"));
		resources.createImage(ICON_ACTIVITY_DIAGRAMS, Archstudio4Activator.class.getResourceAsStream("res/icon-activitydiagrams-adcomposite-16x16.gif"));

		initedResources.add(resources);
	}
}
