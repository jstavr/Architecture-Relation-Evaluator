package edu.uci.isr.archstudio4.comp.xarchcs.logics;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.ICoordinateMapper;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.IThingPeer;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.logics.tracking.SelectionTrackingLogic;
import edu.uci.isr.xarchflat.ObjRef;

public class ChangeSetsModifierLogic
    extends AbstractThingLogic
    implements IBNAMenuListener{

	protected XArchChangeSetInterface xArchCS;
	protected ObjRef xArchRef;
	protected IExplicitADT explicitAdt;

	protected SelectionTrackingLogic stl;

	public ChangeSetsModifierLogic(XArchChangeSetInterface xArch, ObjRef xArchRef, IExplicitADT explicitAdt, SelectionTrackingLogic stl){
		this.xArchCS = xArch;
		this.xArchRef = xArchRef;
		this.explicitAdt = explicitAdt;
		this.stl = stl;
	}

	protected Collection<ObjRef> getObjRefs(IThing... things){
		Collection<ObjRef> objRefs = new ArrayList<ObjRef>();
		for(IThing thing: things){
			ObjRef objRef = getObjRef(thing);
			if(objRef != null){
				objRefs.add(objRef);
			}
		}
		return objRefs;
	}

	protected ObjRef getObjRef(IThing thing){
		String id = thing.getProperty("xArchID");
		if(id == null){
			IAssembly assembly = AssemblyUtils.getAssemblyWithPart(thing);
			if(assembly != null){
				id = assembly.getRootThing().getProperty("xArchID");
			}
		}
		if(id != null){
			return xArchCS.getByID(xArchRef, id);
		}
		return null;
	}

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){

		ObjRef changeSetsContextRef = xArchCS.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsElementRef = xArchCS.getElement(changeSetsContextRef, "ArchChangeSets", xArchRef);
		boolean menuSet = false;
		if(archChangeSetsElementRef != null){
			final ObjRef[] changeSetRefs = ChangeSetUtils.resolveExternalChangeSetRefs(xArchCS, xArchCS.getAll(archChangeSetsElementRef, "ChangeSet"));
			if(changeSetRefs != null && changeSetRefs.length > 0){
				ObjRef[] selectedRefsArray = null;
				final Collection<ObjRef> selectedObjRefs = getObjRefs(stl.getSelectedThings());
				if(selectedObjRefs == null || selectedObjRefs.size() == 0){

					if(t != null){
						selectedRefsArray = new ObjRef[1];
						selectedRefsArray[0] = getObjRef(t);
					}
					else{
						ICoordinateMapper cm = view.getCoordinateMapper();
						int wx = cm.localXtoWorldX(localX);
						int wy = cm.localYtoWorldY(localY);
						IBNAModel model = view.getWorld().getBNAModel();
						IThing[] allThings = model.getAllThings();

						for(int i = allThings.length - 1; i >= 0; i--){
							IThing th = allThings[i];
							IThingPeer peer = view.getPeer(th);
							if(peer.isInThing(view, wx, wy)){
								selectedRefsArray = new ObjRef[1];
								selectedRefsArray[0] = getObjRef(th);
								break;
							}
						}
					}
				}
				else{
					selectedRefsArray = selectedObjRefs.toArray(new ObjRef[selectedObjRefs.size()]);
				}

				if(selectedRefsArray != null){
					IMenuManager moveMenu = new MenuManager("Move To");
					for(ObjRef changeSetRef: changeSetRefs){
						String id = (String)xArchCS.get(changeSetRef, "id");
						ObjRef descriptionRef = (ObjRef)xArchCS.get(changeSetRef, "Description");
						String description = (String)xArchCS.get(descriptionRef, "value");
						final ObjRef[] selectedObjRefsArray = selectedRefsArray;
						Action moveAction = new Action(description){

							@Override
							public void run(){
								ObjRef targetChangeSetRef = xArchCS.getByID(getId());
								ObjRef[] sourceChangeSetRefs = explicitAdt.getExplicitChangeSetRefs(xArchRef);
								if(sourceChangeSetRefs == null || sourceChangeSetRefs.length == 0){
									sourceChangeSetRefs = xArchCS.getAppliedChangeSetRefs(xArchRef);
								}
								for(ObjRef selectedObjRefRef: selectedObjRefsArray){
									xArchCS.moveChange(selectedObjRefRef, sourceChangeSetRefs, targetChangeSetRef);
								}
							}
						};
						moveAction.setId(id);
						moveMenu.add(moveAction);
						menuSet = true;
					}
					m.add(moveMenu);
				}
			}
		}
		if(!menuSet){
			IAction dummyAction = new Action("Move To"){

				@Override
				public void run(){

				}
			};
			dummyAction.setEnabled(false);
			m.add(dummyAction);

		}
	}
}
