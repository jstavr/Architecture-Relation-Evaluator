package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoConstants;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoMyxComponent;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logic.util.ExportPPTLogic2;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.editing.ActivityDiagramNewElementLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.editing.ActivityDiagramsEditorContextMenuAddDiagramLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlActionLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlActorLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlControlFlowLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlDecisionMergeLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlForkJoinLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlNoteLogic;
import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.mapping.MapXadlReferenceLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.coordinating.MaintainXadlLinksLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.XadlCopyPasteLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.XadlRemoveElementLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.XadlSelectionProviderLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.hints.XAdlHintRepository;
import edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.mapping.MapXadlFinalStateLogic;
import edu.uci.isr.archstudio4.comp.archipelago.statecharts.logics.mapping.MapXadlInitialStateLogic;
import edu.uci.isr.archstudio4.comp.archipelago.util.ArchipelagoFinder;
import edu.uci.isr.archstudio4.comp.archipelago.util.FileDirtyLogic;
import edu.uci.isr.archstudio4.comp.xarchcs.logics.AnnotateExplicitChangeLogic;
import edu.uci.isr.archstudio4.comp.xarchcs.logics.HighlightChangesInChangeSetViewLogic;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.BNARenderingSettings;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.DefaultBNAModel;
import edu.uci.isr.bna4.DefaultBNAView;
import edu.uci.isr.bna4.DefaultBNAWorld;
import edu.uci.isr.bna4.DefaultCoordinateMapper;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IMutableCoordinateMapper;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.IThingLogicManager;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.TableAssembly;
import edu.uci.isr.bna4.logics.background.LifeSapperLogic;
import edu.uci.isr.bna4.logics.background.RotatingOffsetLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainAnchoredAssemblyOrientationLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainGridLayoutLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorAnchorPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorEndpointLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorValueLogic;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainGridLayoutLogic.SizeHint;
import edu.uci.isr.bna4.logics.editing.AlignAndDistributeLogic;
import edu.uci.isr.bna4.logics.editing.BoxReshapeHandleLogic;
import edu.uci.isr.bna4.logics.editing.ClickSelectionLogic;
import edu.uci.isr.bna4.logics.editing.DragMovableLogic;
import edu.uci.isr.bna4.logics.editing.EditTextLogic;
import edu.uci.isr.bna4.logics.editing.KeyNudgerLogic;
import edu.uci.isr.bna4.logics.editing.MarqueeSelectionLogic;
import edu.uci.isr.bna4.logics.editing.RectifyToGridLogic;
import edu.uci.isr.bna4.logics.editing.RotaterLogic;
import edu.uci.isr.bna4.logics.editing.SnapToGridLogic;
import edu.uci.isr.bna4.logics.editing.SplineBreakLogic;
import edu.uci.isr.bna4.logics.editing.SplineReshapeHandleLogic;
import edu.uci.isr.bna4.logics.editing.StandardCursorLogic;
import edu.uci.isr.bna4.logics.events.DragMoveEventsLogic;
import edu.uci.isr.bna4.logics.events.WorldThingExternalEventsLogic;
import edu.uci.isr.bna4.logics.hints.SynchronizeHintsLogic;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.logics.navigating.FindDialogLogic;
import edu.uci.isr.bna4.logics.navigating.MousePanningLogic;
import edu.uci.isr.bna4.logics.navigating.MouseWheelZoomingLogic;
import edu.uci.isr.bna4.logics.tracking.AssemblyTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ModelBoundsTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ReferenceTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.SelectionTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyPrefixTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.bna4.logics.tracking.TypedThingTrackingLogic;
import edu.uci.isr.bna4.logics.util.ExportBitmapLogic;
import edu.uci.isr.bna4.things.labels.BoxedLabelThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.bna4.things.utility.NoThing;
import edu.uci.isr.myx.fw.IMyxBrick;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchPath;

public class ActivityDiagramsEditorSupport{

	// For tree node cache
	public static final String BNA_WORLD_KEY = "bnaWorld";

	// For editor pane properties
	public static final String EDITING_BNA_COMPOSITE_KEY = "bnaComposite";

	public static void setupEditor(ArchipelagoServices AS, ObjRef diagramInputRef){
		ObjRef xArchRef = AS.xarch.getXArch(diagramInputRef);

		IBNAWorld bnaWorld = setupWorld(AS, xArchRef, diagramInputRef);
		if(bnaWorld == null){
			return;
		}

		final IBNAView bnaView = new DefaultBNAView(null, bnaWorld, new DefaultCoordinateMapper());

		AS.editor.clearEditor();
		Composite parentComposite = AS.editor.getParentComposite();
		FillLayout fl = new FillLayout();
		fl.type = SWT.HORIZONTAL;
		parentComposite.setLayout(fl);

		final BNAComposite bnaComposite = new BNAComposite(parentComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.DOUBLE_BUFFERED, bnaView);
		bnaComposite.setBackground(parentComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		bnaComposite.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent e){
				EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaComposite.getView().getWorld().getBNAModel());
				BNAUtils.saveCoordinateMapperData(bnaComposite.getView().getCoordinateMapper(), ept);
				bnaComposite.removeDisposeListener(this);
			}
		});

		BNARenderingSettings.setAntialiasGraphics(bnaComposite, AS.prefs.getBoolean(ArchipelagoConstants.PREF_ANTIALIAS_GRAPHICS));
		BNARenderingSettings.setAntialiasText(bnaComposite, AS.prefs.getBoolean(ArchipelagoConstants.PREF_ANTIALIAS_TEXT));
		BNARenderingSettings.setDecorativeGraphics(bnaComposite, AS.prefs.getBoolean(ArchipelagoConstants.PREF_DECORATIVE_GRAPHICS));

		ArchipelagoUtils.addZoomWidget(bnaComposite, bnaView);

		bnaComposite.pack();
		parentComposite.layout(true);

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaComposite.getView().getWorld().getBNAModel());
		BNAUtils.restoreCoordinateMapperData((IMutableCoordinateMapper)bnaComposite.getView().getCoordinateMapper(), ept);

		ArchipelagoUtils.setBNAComposite(AS.editor, bnaComposite);
		bnaComposite.setFocus();
	}

	public static IBNAWorld setupWorld(ArchipelagoServices AS, ObjRef xArchRef, ObjRef diagramInputRef){
		IBNAWorld bnaWorld = (IBNAWorld)AS.treeNodeDataCache.getData(xArchRef, diagramInputRef, BNA_WORLD_KEY);
		IBNAModel bnaModel = null;

		if(bnaWorld != null){
			bnaModel = bnaWorld.getBNAModel();
		}
		else{
			String diagramInputID = XadlUtils.getID(AS.xarch, diagramInputRef);
			if(diagramInputID == null){
				return null;
			}
			bnaModel = new DefaultBNAModel();

			EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
			ept.setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, diagramInputID);

			bnaWorld = new DefaultBNAWorld("activityDiagram", bnaModel);
			AS.treeNodeDataCache.setData(xArchRef, diagramInputRef, BNA_WORLD_KEY, bnaWorld);

			setupWorld(AS, AS.xarch.getXArch(diagramInputRef), bnaWorld, diagramInputRef);
		}

		ArchipelagoUtils.applyGridPreferences(AS, bnaModel);

		return bnaWorld;
	}

	static void setupWorld(ArchipelagoServices AS, final ObjRef xArchRef, final IBNAWorld bnaWorld, ObjRef diagramInputRef){
		IThingLogicManager logicManager = bnaWorld.getThingLogicManager();

		// tracking logics
		TypedThingTrackingLogic tttl = new TypedThingTrackingLogic();
		logicManager.addThingLogic(tttl);
		ThingPropertyTrackingLogic tptl = new ThingPropertyTrackingLogic();
		logicManager.addThingLogic(tptl);
		ThingPropertyPrefixTrackingLogic tpptl = new ThingPropertyPrefixTrackingLogic(tptl);
		logicManager.addThingLogic(tpptl);
		ReferenceTrackingLogic rtl = new ReferenceTrackingLogic();
		logicManager.addThingLogic(rtl);
		SelectionTrackingLogic stl = new SelectionTrackingLogic();
		logicManager.addThingLogic(stl);
		ModelBoundsTrackingLogic mbtl = new ModelBoundsTrackingLogic(tttl);
		logicManager.addThingLogic(mbtl);
		AssemblyTrackingLogic atl = new AssemblyTrackingLogic();
		logicManager.addThingLogic(atl);

		// event logics
		DragMoveEventsLogic dml = new DragMoveEventsLogic();
		logicManager.addThingLogic(dml);
		logicManager.addThingLogic(new WorldThingExternalEventsLogic(tttl));

		// background logics
		logicManager.addThingLogic(new LifeSapperLogic(tttl));
		logicManager.addThingLogic(new RotatingOffsetLogic(tttl));
		logicManager.addThingLogic(new ToolTipLogic());
		logicManager.addThingLogic(new XadlSelectionProviderLogic(AS.workbenchSite, tptl, AS.xarch));

		// maintenance logics
		logicManager.addThingLogic(new MirrorAnchorPointLogic(rtl));
		logicManager.addThingLogic(new MirrorBoundingBoxLogic(rtl));
		logicManager.addThingLogic(new MirrorEndpointLogic(rtl));
		logicManager.addThingLogic(new MirrorValueLogic(rtl, tpptl));
		logicManager.addThingLogic(new MaintainAnchoredAssemblyOrientationLogic(rtl));
		logicManager.addThingLogic(new MaintainGridLayoutLogic());
		logicManager.addThingLogic(new MaintainStickyPointLogic(rtl, tpptl));
		logicManager.addThingLogic(new MaintainXadlLinksLogic(tptl, tpptl));
		logicManager.addThingLogic(new MoveWithLogic(rtl));

		// general editor logics
		logicManager.addThingLogic(new ClickSelectionLogic(tttl));
		logicManager.addThingLogic(new MouseWheelZoomingLogic());
		logicManager.addThingLogic(new MousePanningLogic());
		logicManager.addThingLogic(new SnapToGridLogic(dml));
		logicManager.addThingLogic(new KeyNudgerLogic());
		logicManager.addThingLogic(new MarqueeSelectionLogic(tttl));
		logicManager.addThingLogic(new DragMovableLogic(dml, tptl));
		logicManager.addThingLogic(new BoxReshapeHandleLogic(stl, dml));
		logicManager.addThingLogic(new SplineReshapeHandleLogic(stl, dml));
		logicManager.addThingLogic(new SplineBreakLogic());
		logicManager.addThingLogic(new StandardCursorLogic());
		logicManager.addThingLogic(new RotaterLogic());
		logicManager.addThingLogic(new HighlightChangesInChangeSetViewLogic(AS.xarchcs));

		// decorate logics
		logicManager.addThingLogic(new HighlightChangesInChangeSetViewLogic(AS.xarchcs));
		logicManager.addThingLogic(new AnnotateExplicitChangeLogic(tptl, tpptl, atl, AS.xarchcs, AS.explicit, xArchRef));

		// editor specific: general
		int dpi = 72;
		IBNAModel model = bnaWorld.getBNAModel();
		TableAssembly tableAssembly = new TableAssembly(model, null, null, new SizeHint[]{new SizeHint((int)(1.5 * dpi), 0), new SizeHint(0, 1), new SizeHint((int)(1.5 * dpi), 0)}, new SizeHint[]{new SizeHint(1 * dpi, 0), new SizeHint(0, 1)});
		tableAssembly.getBoxGlassThing().setBoundingBox(10000, 10000, (int)(11.0 * dpi), (int)(8.5 * dpi));
		tableAssembly.addBorders(model);
		tableAssembly.setPropertyOnAllParts(model, IBNAView.BACKGROUND_THING_PROPERTY_NAME, true, true);

		// editor specific: mapping
		IThing otherThingsParent = new NoThing();
		bnaWorld.getBNAModel().addThing(otherThingsParent);
		IThing splineThingsParent = new NoThing();
		bnaWorld.getBNAModel().addThing(splineThingsParent);

		logicManager.addThingLogic(new MapXadlActionLogic(AS.xarchcs, diagramInputRef, "action", tptl, otherThingsParent, "action"));
		logicManager.addThingLogic(new MapXadlActorLogic(AS.xarchcs, diagramInputRef, "actor", tptl, otherThingsParent, "actor"));
		logicManager.addThingLogic(new MapXadlControlFlowLogic(AS.xarchcs, diagramInputRef, "controlFlow", tptl, splineThingsParent, "controlFlow"));
		logicManager.addThingLogic(new MapXadlNoteLogic(AS.xarchcs, diagramInputRef, "note", tptl, otherThingsParent, "note"));
		logicManager.addThingLogic(new MapXadlInitialStateLogic(AS.xarchcs, diagramInputRef, "initialNode", tptl, otherThingsParent, "initialNode"));
		logicManager.addThingLogic(new MapXadlFinalStateLogic(AS.xarchcs, diagramInputRef, "activityFinalNode", tptl, otherThingsParent, "activityFinalNode"));
		logicManager.addThingLogic(new MapXadlDecisionMergeLogic(AS.xarchcs, diagramInputRef, "decisionNode", tptl, otherThingsParent, "decisionNode"));
		//logicManager.addThingLogic(new MapXadlDecisionMergeLogic(AS.xarchcs, diagramInputRef, "mergeNode", tptl, nodeThing, "mergeNode"));
		logicManager.addThingLogic(new MapXadlForkJoinLogic(AS.xarchcs, diagramInputRef, "forkNode", tptl, otherThingsParent, "forkNode"));
		//logicManager.addThingLogic(new MapXadlForkJoinLogic(AS.xarchcs, diagramInputRef, "joinNode", tptl, nodeThing, "joinNode"));
		logicManager.addThingLogic(new MapXadlReferenceLogic(AS.xarchcs, diagramInputRef, "activityDiagramReference", tptl, otherThingsParent, "diagramInputReference"));

		final BoxedLabelThing labelThing = new BoxedLabelThing();
		model.addThing(labelThing, tableAssembly.getBoxGlassThing());
		MirrorBoundingBoxLogic.mirrorBoundingBox(tableAssembly.getCellBacking(model, 1, 0), null, labelThing);
		labelThing.setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, XadlUtils.getID(AS.xarchcs, diagramInputRef));

		// editor specific: editing logics
		logicManager.addThingLogic(new ActivityDiagramsEditorContextMenuAddDiagramLogic(AS.xarchcs, xArchRef));
		// added  for traversal of ADRef -- reason, the Editor Launcher is needed and that is
		// in this outer class.
		class ActivityDiagramTraverseToRefMenuLogic2
			extends AbstractThingLogic
			implements IBNAMenuListener{

			final protected XArchFlatInterface xarch;

			final protected ObjRef xArchRef;

			protected ArchipelagoServices AS = null;

			//public ActivityDiagramTraverseToRefMenuLogic(XArchFlatInterface xarch, ObjRef diagramInputRef, IResources resources){
			public ActivityDiagramTraverseToRefMenuLogic2(ArchipelagoServices archipelagoServices, XArchFlatInterface xarch, ObjRef xArchRef){
				this.xarch = xarch;
				this.xArchRef = xArchRef;
				this.AS = archipelagoServices;

			}

			public boolean matches(IBNAView view, IThing t){
				String xArchID = getXArchID(view, t);

				if(xArchID != null){
					ObjRef objRef = xarch.getByID(xArchRef, xArchID);
					if(objRef != null){
						return xarch.isInstanceOf(objRef, "activitydiagrams#ActivityDiagramReference");
					}
				}
				return false;
			}

			public String getXArchID(IBNAView view, IThing t){
				String xArchID = null;
				if(t != null){
					if(xArchID == null){
						xArchID = t.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					}
					if(xArchID == null){
						IAssembly assembly = AssemblyUtils.getAssemblyWithPart(t);
						if(assembly != null){
							xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
						}
					}
				}
				return xArchID;
			}

			protected boolean hasTargetAD(Object ref){
				if(ref instanceof ObjRef){
					XArchPath refPath = AS.xarch.getXArchPath((ObjRef)ref);
					if(refPath.toTagsOnlyString().equals("xArch/activityDiagram")){
						return true;
					}
				}
				return false;
			}

			public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
				if(matches(view, t)){ // means the target object in display is AD Ref

					// separator line in menu list
					m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

					// only add Action traverse when you have a reference
					String xArchID = getXArchID(view, t);
					ObjRef ADRef = xarch.getByID(xArchRef, xArchID);

					try{ // if the pointer or target is not set, then we'll get null Pointer 
						// so an easy way to not add the action to the menu

						final ObjRef targetPointer = (ObjRef)xarch.get(ADRef, "diagramPointer");

						//System.out.println(target.toString());

						//extracting the id it references
						String csId = (String)xarch.get(targetPointer, "href");
						if(csId != null && csId.charAt(1) == '#'){
							csId = csId.substring(2);
						}

						//System.out.println(csId);
						String csId2 = csId.substring(1); // trim the leading #
						//System.out.println(csId2);

						final ObjRef ad = xarch.getByID(csId2);

						//System.out.println(ad);

						//System.out.println(xarch.get(ad, "description"));  

						if(ad != null){
							m.add(new Action("Traverse to Activity Diagram"){

								@Override
								public void run(){

									ActivityDiagramsEditorSupport.setupEditor(AS, ad);
								}

							});
						}
					}
					catch(Exception e){

					}

					m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				}
			}
		}

		logicManager.addThingLogic(new ActivityDiagramTraverseToRefMenuLogic2(AS, AS.xarch, xArchRef));

		// XArchEvent logics
		// Yuzo removed   -- logicManager.addThingLogic(new StructureXArchEventHandlerLogic(AS, ttstlView));
		logicManager.addThingLogic(new FileDirtyLogic(AS, xArchRef));

		// Menu logics
		logicManager.addThingLogic(new XadlCopyPasteLogic(AS.xarchcs, diagramInputRef, stl, AS.copyPasteManager));
		logicManager.addThingLogic(new FindDialogLogic(new ArchipelagoFinder(AS)));
		logicManager.addThingLogic(new AlignAndDistributeLogic());
		logicManager.addThingLogic(new RectifyToGridLogic());
		logicManager.addThingLogic(new EditTextLogic());
		logicManager.addThingLogic(new ActivityDiagramNewElementLogic(AS.xarchcs, diagramInputRef, AS.resources));
		logicManager.addThingLogic(new XadlRemoveElementLogic(AS.xarch, xArchRef));
		logicManager.addThingLogic(new ExportBitmapLogic(mbtl));
		logicManager.addThingLogic(new ExportPPTLogic2(mbtl, AS, diagramInputRef));

		// Note: Hints need to be applied last, after all things are synchronized
		// hint synchronization logics
		XAdlHintRepository hr = new XAdlHintRepository(AS.xarch, diagramInputRef, "edu.uci.isr.archstudio4.comp.archipelago", "4.1.0", tptl);
		logicManager.addThingLogic(new SynchronizeHintsLogic(hr));

		// map and later remove the logics when the document is closed
		final IMyxBrick brick = MyxRegistry.getSharedInstance().waitForBrick(ArchipelagoMyxComponent.class);
		final MyxRegistry myxRegistry = MyxRegistry.getSharedInstance();
		final List<Object> myxObjs = new ArrayList<Object>(Arrays.asList(logicManager.getAllThingLogics()));
		myxObjs.addAll(Arrays.asList(new Object[]{hr, new XArchFileListener(){

			public void handleXArchFileEvent(XArchFileEvent evt){
				if(xArchRef.equals(evt.getXArchRef()) && XArchFileEvent.XARCH_CLOSED_EVENT == evt.getEventType()){
					for(Object o: myxObjs){
						myxRegistry.unmap(brick, o);
					}
					bnaWorld.destroy();
				}
			}
		}}));
		for(Object o: myxObjs){
			myxRegistry.map(brick, o);
		}
	}
}
