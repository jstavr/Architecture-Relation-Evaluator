package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StructureNewElementLogic extends AbstractThingLogic implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public StructureNewElementLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public boolean matches(IBNAView view, IThing t){
		return t == null;
	}
	
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(matches(view, t)){
			for(IAction action : getActions(view, t, worldX, worldY)){
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;
		
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(view.getWorld().getBNAModel());

		String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		if(archStructureXArchID == null){
			//Nothing to set description on
			return new IAction[0];
		}
		
		final ObjRef archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
		if(archStructureRef == null){
			//Nothing to add elements to
			return new IAction[0];
		}
		
		ArchipelagoUtils.setNewThingSpot(view.getWorld().getBNAModel(), fworldX, fworldY);
		
		Action newComponentAction = new Action("New Component"){
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
				ObjRef componentRef = AS.xarch.create(typesContextRef, "component");
				AS.xarch.set(componentRef, "id", UIDGenerator.generateUID("component"));
				XadlUtils.setDescription(AS.xarch, componentRef, "[New Component]");
				AS.xarch.add(archStructureRef, "component", componentRef);
			}
			
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_COMPONENT);
			}
		};
		
		
		Action newConnectorAction = new Action("New Connector"){
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
				ObjRef connectorRef = AS.xarch.create(typesContextRef, "connector");
				AS.xarch.set(connectorRef, "id", UIDGenerator.generateUID("connector"));
				XadlUtils.setDescription(AS.xarch, connectorRef, "[New Connector]");
				AS.xarch.add(archStructureRef, "connector", connectorRef);
			}

			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_CONNECTOR);
			}
		};
		
		Action newLinkAction = new Action("New Link"){
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
				ObjRef linkRef = AS.xarch.create(typesContextRef, "link");
				AS.xarch.set(linkRef, "id", UIDGenerator.generateUID("link"));
				XadlUtils.setDescription(AS.xarch, linkRef, "[New Link]");
				AS.xarch.add(archStructureRef, "link", linkRef);
			}
			
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_LINK);
			}
		};
		
		return new IAction[]{newComponentAction, newConnectorAction, newLinkAction};
	}
	
}
