package edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XadlRemoveElementLogic
	extends AbstractThingLogic
	implements IBNAMenuListener{

	final protected XArchFlatInterface xarch;

	final protected ObjRef xArchRef;

	public XadlRemoveElementLogic(XArchFlatInterface xarch, ObjRef xArchRef){
		this.xarch = xarch;
		this.xArchRef = xArchRef;
	}

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){

		if(t != null){
			final Set<String> selectedThingIDs = new HashSet<String>();
			for(IThing selectedThing: BNAUtils.getSelectedThings(view.getWorld().getBNAModel())){
				IAssembly assembly = AssemblyUtils.getAssemblyWithPart(selectedThing);
				if(assembly != null){
					String xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						selectedThingIDs.add(xArchID);
					}
				}
			}
			if(t != null){
				IAssembly assembly = AssemblyUtils.getAssemblyWithPart(t);
				if(assembly != null){
					String xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if(xArchID != null){
						selectedThingIDs.add(xArchID);
					}
				}
			}
			if(selectedThingIDs.size() > 0){
				m.add(new Action(selectedThingIDs.size() == 1 ? "Remove" : "Remove " + selectedThingIDs.size() + " Elements"){

					@Override
					public void run(){
						for(String xArchID: selectedThingIDs){
							ObjRef objRef = xarch.getByID(xArchRef, xArchID);
							if(objRef != null){
								XadlUtils.remove(xarch, objRef);
							}
						}
					}
				});
			}
		}
	}
}
