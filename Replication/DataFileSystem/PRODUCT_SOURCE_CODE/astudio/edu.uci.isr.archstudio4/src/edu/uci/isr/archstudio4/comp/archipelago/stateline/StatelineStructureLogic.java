package edu.uci.isr.archstudio4.comp.archipelago.stateline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.archstudio4.comp.booleannotation.ParseException;
import edu.uci.isr.archstudio4.comp.booleannotation.TokenMgrError;
import edu.uci.isr.archstudio4.util.XadlSelectorDialog;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.facets.IHasAssemblyData;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.glass.SplineGlassThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StatelineStructureLogic extends AbstractThingLogic
	implements IBNAMenuListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public StatelineStructureLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public void fillMenu(final IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(t == null)
			return;
		IBNAModel bnaModel = view.getWorld().getBNAModel();
		IThing[] selectedThings = BNAUtils.getSelectedThings(bnaModel);
		if((selectedThings == null) && (selectedThings.length == 0)){
			return;
		}

		final List<IAssembly> targetAssemblies = new ArrayList<IAssembly>();

		for(IThing selectedThing: selectedThings){
			IAssembly assembly = AssemblyUtils.getAssemblyWithPart(selectedThing);
			if(assembly != null && StructureMapper.isBrickAssemblyRootThing(assembly.getRootThing()))
				targetAssemblies.add(assembly);
			if(assembly != null && StructureMapper.isLinkAssemblyRootThing(assembly.getRootThing()))
				targetAssemblies.add(assembly);
		}

		if(targetAssemblies.size() > 0){
			IAction setAllowedStatesAction = new Action("Select Stateline States"){
				public void run(){
					selectStatelineStates(view, targetAssemblies.toArray(new IAssembly[targetAssemblies.size()]));
				}
			};
			m.add(setAllowedStatesAction);
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	protected void selectStatelineStates(IBNAView view, IAssembly[] assemblies){
		Composite c = BNAUtils.getParentComposite(view);
		if(c != null){

			ObjRef[] stateRefs = XadlSelectorDialog.showSelectorDialog(c.getShell(), "Select Applicable States", AS.xarch, AS.resources, xArchRef, XadlTreeUtils.STATECHART | XadlTreeUtils.STATE, XadlTreeUtils.STATE, true);

			if(stateRefs == null){
				return;
			}

			Set<String> stateIDSet = new HashSet<String>();
			for(int i = 0; i < stateRefs.length; i++){
				String id = XadlUtils.getID(AS.xarch, stateRefs[i]);
				stateIDSet.add(id);
			}
			String[] stateIDs = stateIDSet.toArray(new String[stateIDSet.size()]);

			if(stateIDs.length == 0){
				return;
			}

			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < stateIDs.length; i++){
				sb.append("(state == \"");
				sb.append(StatelineUtils.mungeID(stateIDs[i]));
				sb.append("\")");
				if(i != (stateIDs.length - 1)){
					sb.append(" || ");
				}
			}
			String guard = sb.toString();

			ObjRef optionsContextRef = AS.xarch.createContext(xArchRef, "options");

			for(int i = 0; i < assemblies.length; i++){
				if(assemblies[i] instanceof BoxAssembly){
					BoxAssembly brickAssembly = (BoxAssembly)assemblies[i];
					String xArchID = brickAssembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						ObjRef brickRef = (ObjRef)AS.xarch.getByID(xArchRef, xArchID);
						if((brickRef != null) && AS.xarch.isInstanceOf(brickRef, "types#Component")){
							if(!AS.xarch.isInstanceOf(brickRef, "options#OptionalComponent")){
								brickRef = AS.xarch.promoteTo(optionsContextRef, "optionalComponent", brickRef);
							}
						}
						else if((brickRef != null) && AS.xarch.isInstanceOf(brickRef, "types#Connector")){
							if(!AS.xarch.isInstanceOf(brickRef, "options#OptionalConnector")){
								brickRef = AS.xarch.promoteTo(optionsContextRef, "optionalConnector", brickRef);
							}
						}
						else{
							continue;
						}

						// BrickRef now points to an optional something
						ObjRef optionalRef = (ObjRef)AS.xarch.get(brickRef, "optional");
						if(optionalRef == null){
							optionalRef = AS.xarch.create(optionsContextRef, "optional");
							AS.xarch.set(brickRef, "optional", optionalRef);
						}
						try{
							ObjRef guardRef = AS.booleanNotation.parseBooleanGuard(guard, xArchRef);
							AS.xarch.set(optionalRef, "guard", guardRef);
						}
						catch(ParseException pe){
							pe.printStackTrace();
							throw new RuntimeException("Stateline generated invalid guard expression.", pe);
						}
						catch(TokenMgrError tme){
							tme.printStackTrace();
							throw new RuntimeException("Stateline generated invalid guard expression.", tme);
						}
					}
				}
				else if(assemblies[i] instanceof SplineAssembly){
					SplineAssembly linkAssembly = (SplineAssembly)assemblies[i];
					String xArchID = linkAssembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						ObjRef linkRef = (ObjRef)AS.xarch.getByID(xArchRef, xArchID);
						if((linkRef != null) && AS.xarch.isInstanceOf(linkRef, "types#Link")){
							if(!AS.xarch.isInstanceOf(linkRef, "options#OptionalLink")){
								linkRef = AS.xarch.promoteTo(optionsContextRef, "optionalLink", linkRef);
							}
						}
						else{
							continue;
						}

						// LinkRef now points to an optional something
						ObjRef optionalRef = (ObjRef)AS.xarch.get(linkRef, "optional");
						if(optionalRef == null){
							optionalRef = AS.xarch.create(optionsContextRef, "optional");
							AS.xarch.set(linkRef, "optional", optionalRef);
						}
						try{
							ObjRef guardRef = AS.booleanNotation.parseBooleanGuard(guard, xArchRef);
							AS.xarch.set(optionalRef, "guard", guardRef);
						}
						catch(ParseException pe){
							throw new RuntimeException("Stateline generated invalid guard expression.", pe);
						}
						catch(TokenMgrError tme){
							throw new RuntimeException("Stateline generated invalid guard expression.", tme);
						}
					}
				}
			}
		}
	}
}
