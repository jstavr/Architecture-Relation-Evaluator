package edu.uci.isr.archstudio4.comp.archipelago.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoConstants;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoMyxComponent;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.archstructure.logics.mapping.MapXadlLinkLogic;
import edu.uci.isr.archstudio4.comp.archipelago.codegen.BrickInterfaceSelectorLogic;
import edu.uci.isr.archstudio4.comp.archipelago.codegen.BrickSelectorLogic;
import edu.uci.isr.archstudio4.comp.archipelago.codegen.ExternalBrickSelectorLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.coordinating.MaintainXadlLinksLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.AlignSignatureInterfaceMappingsLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.XadlCopyPasteLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.XadlRemoveElementLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.editing.XadlSelectionProviderLogic;
import edu.uci.isr.archstudio4.comp.archipelago.generic.logics.hints.XAdlHintRepository;
import edu.uci.isr.archstudio4.comp.archipelago.util.ArchipelagoFinder;
import edu.uci.isr.archstudio4.comp.archipelago.util.FileDirtyLogic;
import edu.uci.isr.archstudio4.comp.archipelago.util.HintSupport;
import edu.uci.isr.archstudio4.comp.xarchcs.logics.AnnotateExplicitChangeLogic;
import edu.uci.isr.archstudio4.comp.xarchcs.logics.ChangeSetsModifierLogic;
import edu.uci.isr.archstudio4.comp.xarchcs.logics.HighlightChangesInChangeSetViewLogic;
import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.BNARenderingSettings;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.DefaultBNAModel;
import edu.uci.isr.bna4.DefaultBNAView;
import edu.uci.isr.bna4.DefaultBNAWorld;
import edu.uci.isr.bna4.DefaultCoordinateMapper;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IMutableCoordinateMapper;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.IThingLogicManager;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.constants.ZoomOption;
import edu.uci.isr.bna4.logics.background.DirtyWorldThingsLogic;
import edu.uci.isr.bna4.logics.background.LifeSapperLogic;
import edu.uci.isr.bna4.logics.background.RotatingOffsetLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainAnchoredAssemblyOrientationLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainGridLayoutLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainInternalWorldMappingsLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainTagsLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorAnchorPointLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorBoundingBoxLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorEndpointLogic;
import edu.uci.isr.bna4.logics.coordinating.MirrorValueLogic;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.logics.coordinating.MaintainStickyPointLogic.StickyMode;
import edu.uci.isr.bna4.logics.editing.AlignAndDistributeLogic;
import edu.uci.isr.bna4.logics.editing.BoxReshapeHandleLogic;
import edu.uci.isr.bna4.logics.editing.ClickSelectionLogic;
import edu.uci.isr.bna4.logics.editing.DragMovableLogic;
import edu.uci.isr.bna4.logics.editing.IStickyModeSpecifier;
import edu.uci.isr.bna4.logics.editing.KeyNudgerLogic;
import edu.uci.isr.bna4.logics.editing.MarqueeSelectionLogic;
import edu.uci.isr.bna4.logics.editing.RectifyToGridLogic;
import edu.uci.isr.bna4.logics.editing.RotateTagsLogic;
import edu.uci.isr.bna4.logics.editing.RotaterLogic;
import edu.uci.isr.bna4.logics.editing.ShowHideTagsLogic;
import edu.uci.isr.bna4.logics.editing.SnapToGridLogic;
import edu.uci.isr.bna4.logics.editing.SplineBreakLogic;
import edu.uci.isr.bna4.logics.editing.SplineReshapeHandleLogic;
import edu.uci.isr.bna4.logics.editing.StandardCursorLogic;
import edu.uci.isr.bna4.logics.events.DragMoveEventsLogic;
import edu.uci.isr.bna4.logics.events.InternalWorldEventsLogic;
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
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.bna4.things.utility.NoThing;
import edu.uci.isr.myx.fw.IMyxBrick;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;

public class StructureEditorSupport {

	private static final boolean DEBUG = false;

	// For tree node cache
	public static final String BNA_WORLD_KEY = "bnaWorld";

	// For editor pane properties
	public static final String EDITING_BNA_COMPOSITE_KEY = "bnaComposite";

	public static void setupEditor(ArchipelagoServices AS, ObjRef archStructureRef) {
		ObjRef xArchRef = AS.xarch.getXArch(archStructureRef);

		IBNAWorld bnaWorld = setupWorld(AS, xArchRef, archStructureRef);
		if (bnaWorld == null) {
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

		bnaComposite.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
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
		BNAUtils.restoreCoordinateMapperData((IMutableCoordinateMapper) bnaComposite.getView().getCoordinateMapper(), ept);

		ArchipelagoUtils.setBNAComposite(AS.editor, bnaComposite);
		bnaComposite.setFocus();
	}

	public static IBNAWorld setupWorld(ArchipelagoServices AS, ObjRef xArchRef, ObjRef archStructureRef) {
		IBNAWorld bnaWorld = (IBNAWorld) AS.treeNodeDataCache.getData(xArchRef, archStructureRef, BNA_WORLD_KEY);
		IBNAModel bnaModel = null;

		if (bnaWorld != null) {
			bnaModel = bnaWorld.getBNAModel();
		}
		else {
			String archStructureID = XadlUtils.getID(AS.xarch, archStructureRef);
			if (archStructureID == null) {
				return null;
			}
			bnaModel = new DefaultBNAModel();

			EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
			ept.setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, archStructureID);

			bnaWorld = new DefaultBNAWorld("archStructure", bnaModel);
			AS.treeNodeDataCache.setData(xArchRef, archStructureRef, BNA_WORLD_KEY, bnaWorld);

			setupWorld(AS, AS.xarch.getXArch(archStructureRef), bnaWorld, archStructureRef);
			AS.eventBus.fireArchipelagoEvent(new StructureEvents.WorldCreatedEvent(archStructureRef, bnaWorld));
		}

		ArchipelagoUtils.applyGridPreferences(AS, bnaModel);

		return bnaWorld;
	}

	static void setupWorld(final ArchipelagoServices AS, final ObjRef xArchRef, final IBNAWorld bnaWorld, final ObjRef diagramInputRef) {
		IThingLogicManager logicManager = bnaWorld.getThingLogicManager();

		String format;
		long lTime, time;
		if (DEBUG) {
			System.err.println();
			format = "%-75s : %,14d\n";
			time = System.nanoTime();
		}

		// tracking logics
		TypedThingTrackingLogic tttl = new TypedThingTrackingLogic();
		logicManager.addThingLogic(tttl);
		ThingPropertyTrackingLogic tptl = new ThingPropertyTrackingLogic();
		logicManager.addThingLogic(tptl);
		ThingPropertyPrefixTrackingLogic tpptl = new ThingPropertyPrefixTrackingLogic(tptl);
		logicManager.addThingLogic(tpptl);
		ReferenceTrackingLogic rtl = new ReferenceTrackingLogic();
		logicManager.addThingLogic(rtl);
		AssemblyTrackingLogic atl = new AssemblyTrackingLogic();
		logicManager.addThingLogic(atl);

		// XArchEvent logics
		IThing splineThingsParent = new NoThing();
		bnaWorld.getBNAModel().addThing(splineThingsParent);

		final String linkKind = "link";
		final String interfaceKind = "interface";

		if (DEBUG) {
			lTime = System.nanoTime();
		}
		StructureMapper.updateStructure(AS, bnaWorld, diagramInputRef);
		if (DEBUG) {
			lTime = System.nanoTime() - lTime;
			System.err.printf(format, "Update structure", lTime);
		}
		logicManager.addThingLogic(new MapXadlLinkLogic(AS.xarchcs, diagramInputRef, "link", tptl, splineThingsParent, "link"));
		logicManager.addThingLogic(new StructureXArchEventHandlerLogic(AS, tttl));
		logicManager.addThingLogic(new FileDirtyLogic(AS, xArchRef));
		logicManager.addThingLogic(new MaintainXadlLinksLogic(tptl, tpptl));

		if (DEBUG) {
			lTime = System.nanoTime();
		}
		readHints(AS, xArchRef, bnaWorld.getBNAModel(), diagramInputRef);
		if (DEBUG) {
			lTime = System.nanoTime() - lTime;
			System.err.printf(format, "Old hints", lTime);
		}
		if (DEBUG) {
			lTime = System.nanoTime();
		}
		XAdlHintRepository hr = new XAdlHintRepository(AS.xarch, diagramInputRef, "edu.uci.isr.archstudio4.comp.archipelago", "4.1.0", tptl);
		if (DEBUG) {
			lTime = System.nanoTime() - lTime;
			System.err.printf(format, "Hint repository", lTime);
		}
		logicManager.addThingLogic(new SynchronizeHintsLogic(hr));

		// maintenance logics
		logicManager.addThingLogic(new MoveWithLogic(rtl));
		logicManager.addThingLogic(new MaintainStickyPointLogic(rtl, tpptl));
		logicManager.addThingLogic(new MaintainAnchoredAssemblyOrientationLogic(rtl));
		logicManager.addThingLogic(new MaintainGridLayoutLogic());
		logicManager.addThingLogic(new MaintainTagsLogic(rtl));

		InternalWorldEventsLogic iwel = new InternalWorldEventsLogic();
		logicManager.addThingLogic(new MaintainInternalWorldMappingsLogic(rtl, tptl, iwel));
		logicManager.addThingLogic(new MirrorAnchorPointLogic(rtl));
		logicManager.addThingLogic(new MirrorBoundingBoxLogic(rtl));
		logicManager.addThingLogic(new MirrorEndpointLogic(rtl));
		logicManager.addThingLogic(new MirrorValueLogic(rtl, tpptl));

		SelectionTrackingLogic stl = new SelectionTrackingLogic();
		logicManager.addThingLogic(stl);
		ModelBoundsTrackingLogic mbtl = new ModelBoundsTrackingLogic(tttl);
		logicManager.addThingLogic(mbtl);

		// event logics
		DragMoveEventsLogic dml = new DragMoveEventsLogic();
		logicManager.addThingLogic(dml);
		logicManager.addThingLogic(iwel);
		logicManager.addThingLogic(new WorldThingExternalEventsLogic(tttl));
		logicManager.addThingLogic(new XadlSelectionProviderLogic(AS.workbenchSite, tptl, AS.xarch));

		// background logics
		logicManager.addThingLogic(new LifeSapperLogic(tttl));
		logicManager.addThingLogic(new RotatingOffsetLogic(tttl));
		logicManager.addThingLogic(new ToolTipLogic());
		logicManager.addThingLogic(new DirtyWorldThingsLogic(iwel));

		// general editor logics
		logicManager.addThingLogic(new ClickSelectionLogic(tttl));
		//set zoom option
		ZoomOption zoomOption = ZoomOption.WHEEL;
		try {
			zoomOption = ZoomOption.valueOf(AS.prefs.getString(ArchipelagoConstants.PREF_ZOOM_OPTION));
		}
		catch (Exception e) {/* ignores */
		}
		logicManager.addThingLogic(new MouseWheelZoomingLogic(zoomOption));
		logicManager.addThingLogic(new MousePanningLogic());
		logicManager.addThingLogic(new SnapToGridLogic(dml));
		logicManager.addThingLogic(new KeyNudgerLogic());
		logicManager.addThingLogic(new MarqueeSelectionLogic(tttl));
		logicManager.addThingLogic(new DragMovableLogic(dml, tptl));
		logicManager.addThingLogic(new BoxReshapeHandleLogic(stl, dml));
		SplineReshapeHandleLogic srhl = new SplineReshapeHandleLogic(stl, dml);
		srhl.addStickyModeSpecifier(new IStickyModeSpecifier() {

			public StickyMode[] getAllowableStickyModes(IThing thing, String propertyName, IThing stickToThing) {
				IAssembly fa = AssemblyUtils.getAssemblyWithPart(thing);
				IAssembly ta = AssemblyUtils.getAssemblyWithPart(stickToThing);
				if (fa != null && ta != null && fa.isKind(linkKind) && ta.isKind(interfaceKind) && "glass".equals(AssemblyUtils.getPartName(stickToThing))) {
					return new StickyMode[] { StickyMode.EDGE_FROM_CENTER };
				}
				return null;
			}
		});
		logicManager.addThingLogic(srhl);
		logicManager.addThingLogic(new SplineBreakLogic());
		logicManager.addThingLogic(new StandardCursorLogic());
		logicManager.addThingLogic(new RotaterLogic());
		logicManager.addThingLogic(new HighlightChangesInChangeSetViewLogic(AS.xarchcs));

		// decorate logics
		logicManager.addThingLogic(new HighlightChangesInChangeSetViewLogic(AS.xarchcs));
		logicManager.addThingLogic(new AnnotateExplicitChangeLogic(tptl, tpptl, atl, AS.xarchcs, AS.explicit, xArchRef));

		// Menu logics
		logicManager.addThingLogic(new XadlCopyPasteLogic(AS.xarchcs, diagramInputRef, stl, AS.copyPasteManager));
		logicManager.addThingLogic(new ChangeSetsModifierLogic(AS.xarchcs, xArchRef, AS.explicit, stl));
		logicManager.addThingLogic(new FindDialogLogic(new ArchipelagoFinder(AS)));
		logicManager.addThingLogic(new AlignAndDistributeLogic());
		logicManager.addThingLogic(new AlignSignatureInterfaceMappingsLogic(rtl));
		logicManager.addThingLogic(new RectifyToGridLogic());
		logicManager.addThingLogic(new StructureEditDescriptionLogic(AS, xArchRef));
		logicManager.addThingLogic(new StructureEditDirectionLogic(AS, xArchRef));
		logicManager.addThingLogic(new StructureNewElementLogic(AS, xArchRef));

		logicManager.addThingLogic(new BrickSelectorLogic(AS, xArchRef));
		logicManager.addThingLogic(new ExternalBrickSelectorLogic(AS, xArchRef));
		logicManager.addThingLogic(new BrickInterfaceSelectorLogic(AS, xArchRef));

		logicManager.addThingLogic(new StructureNewInterfaceLogic(AS, xArchRef));
		logicManager.addThingLogic(new StructureInterfaceLocationsLogic(AS, xArchRef));
		logicManager.addThingLogic(new ShowHideTagsLogic());
		logicManager.addThingLogic(new RotateTagsLogic());
		logicManager.addThingLogic(new StructureAssignTypeLogic(AS, xArchRef));
		logicManager.addThingLogic(new StructureMapSignatureLogic(AS, xArchRef));
		logicManager.addThingLogic(new StructureEditColorLogic(AS.prefs));
		logicManager.addThingLogic(new XadlRemoveElementLogic(AS.xarchcs, xArchRef));

		logicManager.addThingLogic(new StructureGraphLayoutLogic(AS, xArchRef));
		logicManager.addThingLogic(new ExportBitmapLogic(mbtl));

		// Drop logics
		logicManager.addThingLogic(new StructureTypeDropLogic(AS, xArchRef));

		if (DEBUG) {
			System.err.printf(format, "TOTAL", System.nanoTime() - time);
		}

		// map and later remove the logics when the document is closed
		final IMyxBrick brick = MyxRegistry.getSharedInstance().waitForBrick(ArchipelagoMyxComponent.class);
		final MyxRegistry myxRegistry = MyxRegistry.getSharedInstance();
		final List<Object> myxObjs = new ArrayList<Object>(Arrays.asList(logicManager.getAllThingLogics()));
		myxObjs.addAll(Arrays.asList(new Object[] { hr, new XArchFileListener() {

			public void handleXArchFileEvent(XArchFileEvent evt) {
				if (xArchRef.equals(evt.getXArchRef()) && XArchFileEvent.XARCH_CLOSED_EVENT == evt.getEventType()) {
					for (Object o : myxObjs) {
						myxRegistry.unmap(brick, o);
					}
					bnaWorld.destroy();
				}
			}
		} }));
		for (Object o : myxObjs) {
			myxRegistry.map(brick, o);
		}
	}

	@Deprecated
	public static void readHints(ArchipelagoServices AS, ObjRef xArchRef, IBNAModel modelToPopulate, ObjRef archStructureRef) {
		ObjRef bundleRef = HintSupport.getArchipelagoHintsBundleRef(AS, xArchRef);
		if (bundleRef != null) {
			ObjRef structureBundleRef = HintSupport.findChildHintedElementRef(AS, xArchRef, bundleRef, archStructureRef);
			if (structureBundleRef != null) {
				StructureHintSupport.readHintsForStructure(AS, xArchRef, structureBundleRef, modelToPopulate, archStructureRef);
			}
		}
	}
}
