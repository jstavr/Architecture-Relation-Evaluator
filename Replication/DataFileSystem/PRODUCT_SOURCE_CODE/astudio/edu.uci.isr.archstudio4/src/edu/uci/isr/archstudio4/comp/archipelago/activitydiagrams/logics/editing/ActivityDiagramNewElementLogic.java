package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.editing;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ActivityDiagramNewElementLogic
	extends AbstractThingLogic
	implements IBNAMenuListener{

	final protected XArchFlatInterface xarch;

	final protected ObjRef activityDiagramRef;

	final protected ObjRef xArchRef;

	final protected IResources resources;

	public ActivityDiagramNewElementLogic(XArchFlatInterface xarch, ObjRef activityDiagramRef, IResources resources){
		this.xarch = xarch;
		this.activityDiagramRef = activityDiagramRef;
		this.xArchRef = xarch.getXArch(activityDiagramRef);
		this.resources = resources;
	}

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(t == null){
			ArchipelagoUtils.setNewThingSpot(getBNAModel(), worldX, worldY);

			m.add(new Action("New Action"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "action");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("action"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "action", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_STATE);
				}
			});

			m.add(new Action("New Actor"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "actor");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("actor"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "actor", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_ACTOR);
				}
			});

			m.add(new Action("New Control Flow"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "controlFlow");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("controlFlow"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "controlFlow", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_LINK);
				}
			});

			m.add(new Action("New Note"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "note");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("note"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "note", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_NOTE);
				}
			});

			m.add(new Action("New Initial Node"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "initialNode");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("initialNode"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "initialNode", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_INITIAL_STATE);
				}
			});

			m.add(new Action("New Final Node"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "activityFinalNode");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("activityFinalNode"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "activityFinalNode", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_FINAL_STATE);
				}
			});

			//m.add(new Action("New Decision Node"){
			//
			//	@Override
			//	public void run(){
			//		ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
			//		ObjRef newObjRef = xarch.create(typesContextRef, "decisionNode");
			//		xarch.set(newObjRef, "id", UIDGenerator.generateUID("decisionNode"));
			//		XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
			//		xarch.add(activityDiagramRef, "decisionNode", newObjRef);
			//	}
			//
			//	@Override
			//	public ImageDescriptor getImageDescriptor(){
			//		return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_DECISION);
			//	}
			//});
			//
			//m.add(new Action("New Merge Node"){
			//
			//	@Override
			//	public void run(){
			//		ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
			//		ObjRef newObjRef = xarch.create(typesContextRef, "mergeNode");
			//		xarch.set(newObjRef, "id", UIDGenerator.generateUID("mergeNode"));
			//		XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
			//		xarch.add(activityDiagramRef, "mergeNode", newObjRef);
			//	}
			//
			//	@Override
			//	public ImageDescriptor getImageDescriptor(){
			//		return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_MERGE);
			//	}
			//});

			m.add(new Action("New Decision/Merge Node"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "decisionNode");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("decisionNode"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "decisionNode", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_DECISION);
				}
			});

			//m.add(new Action("New Fork Node"){
			//
			//	@Override
			//	public void run(){
			//		ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
			//		ObjRef newObjRef = xarch.create(typesContextRef, "forkNode");
			//		xarch.set(newObjRef, "id", UIDGenerator.generateUID("forkNode"));
			//		XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
			//		xarch.add(activityDiagramRef, "forkNode", newObjRef);
			//	}
			//
			//	@Override
			//	public ImageDescriptor getImageDescriptor(){
			//		return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_FORK);
			//	}
			//});
			//
			//m.add(new Action("New Join Node"){
			//
			//	@Override
			//	public void run(){
			//		ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
			//		ObjRef newObjRef = xarch.create(typesContextRef, "joinNode");
			//		xarch.set(newObjRef, "id", UIDGenerator.generateUID("joinNode"));
			//		XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
			//		xarch.add(activityDiagramRef, "joinNode", newObjRef);
			//	}
			//
			//	@Override
			//	public ImageDescriptor getImageDescriptor(){
			//		return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_JOIN);
			//	}
			//});

			m.add(new Action("New Fork/Join Node"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "forkNode");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("forkNode"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "forkNode", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_FORK);
				}
			});

			m.add(new Action("New Activity Diagram Reference"){

				@Override
				public void run(){
					ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
					ObjRef newObjRef = xarch.create(typesContextRef, "activityDiagramReference");
					xarch.set(newObjRef, "id", UIDGenerator.generateUID("activityDiagramReference"));
					XadlUtils.setDescription(xarch, newObjRef, "[" + this.getText() + "]");
					xarch.add(activityDiagramRef, "activityDiagramReference", newObjRef);
				}

				@Override
				public ImageDescriptor getImageDescriptor(){
					return resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_COMPOSITE);
				}
			});

			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
}
