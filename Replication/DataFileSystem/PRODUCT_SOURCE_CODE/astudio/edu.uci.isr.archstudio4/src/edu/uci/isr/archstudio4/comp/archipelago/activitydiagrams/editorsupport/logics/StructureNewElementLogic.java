package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.editorsupport.logics;

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

public class StructureNewElementLogic
	extends AbstractThingLogic
	implements IBNAMenuListener{

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
			for(IAction action: getActions(view, t, worldX, worldY)){
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

		String activityDiagramXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		
		if(activityDiagramXArchID == null){
			//Nothing to set description on
			return new IAction[0];
		}

		final ObjRef activityDiagramRef = AS.xarch.getByID(xArchRef, activityDiagramXArchID);
		if(activityDiagramRef == null){
			//Nothing to add elements to
			return new IAction[0];
		}

		ArchipelagoUtils.setNewThingSpot(view.getWorld().getBNAModel(), fworldX, fworldY);

		

		/*
		<xsd:complexType name="ActivityDiagram">
		<xsd:sequence>
		<xsd:element name="description" type="instance:Description"/>
		<xsd:element name="note" type="Note" minOccurs="0" maxOccurs="unbounded"/>
		
		<xsd:element name="action" type="Action" minOccurs="0" maxOccurs="unbounded"/>
		<xsd:element name="activityFinalNode" type="ActivityFinalNode" minOccurs="0" maxOccurs="unbounded"/>
		<xsd:element name="controlFlow" type="ControlFlow" minOccurs="0" maxOccurs="unbounded"/>
		<xsd:element name="decisionNode" type="DecisionNode" minOccurs="0" maxOccurs="unbounded"/>
		<xsd:element name="forkNode" type="ForkNode" minOccurs="0" maxOccurs="unbounded"/>
		<xsd:element name="initialNode" type="InitialNode" minOccurs="0" maxOccurs="unbounded"/>			
		<xsd:element name="joinNode" type="JoinNode" minOccurs="0" maxOccurs="unbounded"/>
		<xsd:element name="mergeNode" type="MergeNode" minOccurs="0" maxOccurs="unbounded"/>
		</xsd:sequence>
		<xsd:attribute name="id" type="instance:Identifier"/>
		</xsd:complexType>
		*/
		Action newNoteAction = new Action("New Note"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef noteRef = AS.xarch.create(typesContextRef, "note");
				AS.xarch.set(noteRef, "id", UIDGenerator.generateUID("note"));
				XadlUtils.setDescription(AS.xarch, noteRef, "[New Note]");
				AS.xarch.add(activityDiagramRef, "note", noteRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_NOTE);
			}
		};

		Action newActionAction = new Action("New Action"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef actionRef = AS.xarch.create(typesContextRef, "action");
				AS.xarch.set(actionRef, "id", UIDGenerator.generateUID("action"));
				XadlUtils.setDescription(AS.xarch, actionRef, "[New Action]");
				AS.xarch.add(activityDiagramRef, "action", actionRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_STATE);
			}
		};

		Action newActivityFinalAction = new Action("New Activity Final Node"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef activityFinalRef = AS.xarch.create(typesContextRef, "activityFinalNode");
				AS.xarch.set(activityFinalRef, "id", UIDGenerator.generateUID("activityFinalNode"));
				XadlUtils.setDescription(AS.xarch, activityFinalRef, "[New activityFinal]");
				AS.xarch.add(activityDiagramRef, "activityFinalNode", activityFinalRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_FINAL_STATE);
			}
		};

		Action newControlFlowAction = new Action("New Control Flow"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef activityFinalRef = AS.xarch.create(typesContextRef, "controlFlow");
				AS.xarch.set(activityFinalRef, "id", UIDGenerator.generateUID("controlFlow"));
				XadlUtils.setDescription(AS.xarch, activityFinalRef, "[New ControlFlow]");
				AS.xarch.add(activityDiagramRef, "controlFlow", activityFinalRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_LINK);
			}
		};

		Action newDecisionAction = new Action("New Decision Node"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef decisionRef = AS.xarch.create(typesContextRef, "decisionNode");
				AS.xarch.set(decisionRef, "id", UIDGenerator.generateUID("decisionNode"));
				XadlUtils.setDescription(AS.xarch, decisionRef, "[New Decision]");
				AS.xarch.add(activityDiagramRef, "decisionNode", decisionRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_DECISION);
			}
		};

		Action newForkAction = new Action("New Fork Node"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef forkRef = AS.xarch.create(typesContextRef, "forkNode");
				AS.xarch.set(forkRef, "id", UIDGenerator.generateUID("forkNode"));
				XadlUtils.setDescription(AS.xarch, forkRef, "[New Fork]");
				AS.xarch.add(activityDiagramRef, "forkNode", forkRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_FORK);
			}
		};

		Action newInitialNodeAction = new Action("New Initial Node"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef initialNodeRef = AS.xarch.create(typesContextRef, "initialNode");
				AS.xarch.set(initialNodeRef, "id", UIDGenerator.generateUID("initialNode"));
				XadlUtils.setDescription(AS.xarch, initialNodeRef, "[New InitialNode]");
				AS.xarch.add(activityDiagramRef, "initialNode", initialNodeRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_INITIAL_STATE);
			}
		};

		Action newJoinNodeAction = new Action("New Join Node"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef joinNodeRef = AS.xarch.create(typesContextRef, "joinNode");
				AS.xarch.set(joinNodeRef, "id", UIDGenerator.generateUID("joinNode"));
				XadlUtils.setDescription(AS.xarch, joinNodeRef, "[New Join Node]");
				AS.xarch.add(activityDiagramRef, "joinNode", joinNodeRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_JOIN);
			}
		};

		Action newMergeNodeAction = new Action("New Merge Node"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef mergeNodeRef = AS.xarch.create(typesContextRef, "mergeNode");
				AS.xarch.set(mergeNodeRef, "id", UIDGenerator.generateUID("mergeNode"));
				XadlUtils.setDescription(AS.xarch, mergeNodeRef, "[New mergeNode]");
				AS.xarch.add(activityDiagramRef, "mergeNode", mergeNodeRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_MERGE);
			}
		};

		Action newActivityDiagramCompositeAction = new Action("New Actvity Diagram Reference"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef adrefNodeRef = AS.xarch.create(typesContextRef, "activityDiagramReference");
				AS.xarch.set(adrefNodeRef, "id", UIDGenerator.generateUID("activityDiagramReference"));
				XadlUtils.setDescription(AS.xarch, adrefNodeRef, "[New activityDiagramReference]");
				AS.xarch.add(activityDiagramRef, "activityDiagramReference", adrefNodeRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTIVITY_DIAGRAMS_COMPOSITE);
			}
		};
		
		Action newActorAction = new Action("New Actor"){

			@Override
			public void run(){
				ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "activitydiagrams");
				ObjRef actorRef = AS.xarch.create(typesContextRef, "actor");
				AS.xarch.set(actorRef, "id", UIDGenerator.generateUID("actor"));
				XadlUtils.setDescription(AS.xarch, actorRef, "[New actor]");
				AS.xarch.add(activityDiagramRef, "actor", actorRef);
			}

			@Override
			public ImageDescriptor getImageDescriptor(){
				return null;
				//return AS.resources.getImageDescriptor(ArchstudioResources.ICON_ACTOR);
			}
		};
		
		return new IAction[]{newActionAction, newNoteAction, newDecisionAction, newForkAction, newInitialNodeAction, newActivityFinalAction, newControlFlowAction, newActivityDiagramCompositeAction, newActorAction}; //, newJoinNodeAction, newMergeNodeAction};

	}

}
