package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAModelListener;
import edu.uci.isr.bna4.IBNAMouseListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.constants.CompletionStatus;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlFullTranslator;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlPartialTranslator;
import edu.uci.isr.myx2.eclipse.codegen.translator.XadlTranslationType;
import edu.uci.isr.myx2.eclipse.codegen.ui.console.ConsoleDisplayMgr;
import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;
import edu.uci.isr.myx2.eclipse.extension.MyxBrickExtensionUtils;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoader;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoaderUtil;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * TODO: still experimenting version A brick selector logic that reads the
 * eclipse extension points other than the current project and assign it to the
 * brick. This doesn't generate code, but only assign names and signatures to
 * the selected brick.
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class ExternalBrickSelectorLogic extends AbstractThingLogic implements IBNAMenuListener, IBNAModelListener, IBNAMouseListener {

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	protected List<SWTBrickSelectorThing> openControls = Collections.synchronizedList(new ArrayList<SWTBrickSelectorThing>());

	/**
	 * The property name for xadl element id
	 */
	private final static String PRP_NAME_TARGET_XARCH_ID = "#targetXArchID";

	/**
	 * the property name for translation type
	 */
	private final static String PRP_NAME_TRANSLATION_TYPE = "translationType";

	/**
	 * date format to output the current time to the console
	 */
	private final SimpleDateFormat sdf = new SimpleDateFormat();
	{
		sdf.setTimeZone(TimeZone.getTimeZone("PST"));
	}

	public ExternalBrickSelectorLogic(ArchipelagoServices services, ObjRef xArchRef) {
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	private boolean matches(IBNAView view, IThing thing) {

		// checks if the thing is a brick
		if (thing instanceof BoxGlassThing) {
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(thing);
			if (parentThing != null) {
				return StructureMapper.isBrickAssemblyRootThing(parentThing);
			}
		}
		return false;
	}

	private ImageDescriptor getMenuItemIcon(ObjRef eltRef) {
		if (AS.xarch.isInstanceOf(eltRef, "types#Component")) {
			return AS.resources.getImageDescriptor(ArchstudioResources.ICON_COMPONENT);
		}
		else if (AS.xarch.isInstanceOf(eltRef, "types#Connector")) {
			return AS.resources.getImageDescriptor(ArchstudioResources.ICON_CONNECTOR);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.bna4.IBNAMenuListener#fillMenu(edu.uci.isr.bna4.IBNAView,
	 * org.eclipse.jface.action.IMenuManager, int, int, edu.uci.isr.bna4.IThing,
	 * int, int)
	 */
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY) {
		IThing[] selectedThings = BNAUtils.getSelectedThings(view.getWorld().getBNAModel());
		if (selectedThings.length > 1) {
			return;
		}

		if (matches(view, t)) {
			for (IAction action : getActions(view, t, worldX, worldY)) {
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	private String getXArchID(IBNAView view, IThing t) {
		if (t instanceof BoxGlassThing) {
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
	}

	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY) {
		final ArchipelagoServices fAS = AS;
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;

		final String eltXArchID = getXArchID(view, t);
		if (eltXArchID == null) {
			//Nothing to set description on
			return new IAction[0];
		}

		final ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if (eltRef == null) {
			//Nothing to set description on
			return new IAction[0];
		}

		ImageDescriptor icon = getMenuItemIcon(eltRef);

		// reads extension point and shows a list of bricks.
		// when the user selects a brick, interfaces are imported into the component.
		// AIM reads the necessary information from the extension when it instantiates the component.
		Action partialConvAction = new Action("Assign External Brick ...") {

			@Override
			public void run() {
				Point p = BNAUtils.getCentralPoint(ft);
				if (p == null) {
					p = new Point(fworldX, fworldY);
				}

				SWTBrickSelectorThing st = new SWTBrickSelectorThing();

				st.setResources(fAS.resources);

				//remembers the xadl id of the component
				st.setProperty(ExternalBrickSelectorLogic.PRP_NAME_TARGET_XARCH_ID, eltXArchID);

				//remembers the brick Ids form the extension for displaying
				BrickList brickList = new BrickList(loadBricksFromOtherExtensions());
				st.setBrickList(brickList);
				st.setBrickLabelList(new BrickLabelList(brickList));

				//sets the translation type to "interface only.
				st.setProperty(ExternalBrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE, XadlTranslationType.INTERFACE_ONLY);

				//remembers the location of this action
				st.setAnchorPoint(p);

				MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, st);
				st.setEditing(true);
				openControls.add(st);
				fview.getWorld().getBNAModel().addThing(st, ft);
			}
		};
		if (icon != null) {
			partialConvAction.setImageDescriptor(icon);
		}

		Action fullConvAction = new Action("Convert External Brick to xADL ...") {

			@Override
			public void run() {
				Point p = BNAUtils.getCentralPoint(ft);
				if (p == null) {
					p = new Point(fworldX, fworldY);
				}

				SWTBrickSelectorThing st = new SWTBrickSelectorThing();

				st.setResources(fAS.resources);

				//remembers the xadl id of the component
				st.setProperty(ExternalBrickSelectorLogic.PRP_NAME_TARGET_XARCH_ID, eltXArchID);

				//remembers the brick Ids form the extension for displaying
				BrickList brickList = new BrickList(loadBricksFromOtherExtensions());
				st.setBrickList(brickList);
				st.setBrickLabelList(new BrickLabelList(brickList));

				//sets the translation type to "interface only.
				st.setProperty(ExternalBrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE, XadlTranslationType.FULL_CONVERSION);

				//remembers the location of this action
				st.setAnchorPoint(p);

				MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, st);
				st.setEditing(true);
				openControls.add(st);
				fview.getWorld().getBNAModel().addThing(st, ft);
			}
		};
		if (icon != null) {
			fullConvAction.setImageDescriptor(icon);
		}

		return new IAction[] { partialConvAction, fullConvAction };
	}

	/**
	 * Read bricks info defined in projects other than the current project.
	 * 
	 * @return
	 */
	private Collection<IMyxBrickExtension> loadBricksFromOtherExtensions() {
		//TODO: experiment
		Set<IMyxBrickExtension> brickSet = new HashSet<IMyxBrickExtension>();

		//gets the current project name
		IProject crntProject = getProject();
		//String crntProjectName = crntProject.getName();

		//gets the referenced projects
		Set<IProject> refProjects = new HashSet<IProject>();
		try {
			//looks at plugin.xml
			for (IProject refProject : crntProject.getDescription().getReferencedProjects()) {
				refProjects.add(refProject);
			}
			//looks at MANIFEST.MF
			for (IProject refProject : crntProject.getDescription().getDynamicReferences()) {
				refProjects.add(refProject);
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}

		for (IPluginModelBase pluginModel : PluginRegistry.getActiveModels()) {
			if (!pluginModel.isEnabled() || !pluginModel.isValid()) {
				continue;
			}

			IResource resource = pluginModel.getUnderlyingResource();
			if (resource == null) {
				continue;
			}
			IProject project = resource.getProject();
			if (project == null || !refProjects.contains(project)) {
				//ignores the project that is not referenced by this project
				continue;
			}

			ExtensionLoader extLoader = ExtensionLoaderUtil.getExtensionLoader(project);
			brickSet.addAll(extLoader.getExtensionBricks());
		}

		return brickSet;
	}

	/*
	 * (non-Javadoc)
	 * @seeedu.uci.isr.bna4.IBNAModelListener#bnaModelChanged(edu.uci.isr.bna4.
	 * BNAModelEvent evt)
	 */
	/**
	 * When the user selects a brick from a selector, this is called.
	 */
	public void bnaModelChanged(BNAModelEvent evt) {
		if (evt.getEventType() == BNAModelEvent.EventType.THING_CHANGED) {
			if (evt.getTargetThing() instanceof SWTBrickSelectorThing) {
				SWTBrickSelectorThing st = (SWTBrickSelectorThing) evt.getTargetThing();
				if (openControls.contains(st)) {
					if (st.getCompletionStatus() == CompletionStatus.OK) {
						String targetXArchID = st.getProperty(ExternalBrickSelectorLogic.PRP_NAME_TARGET_XARCH_ID);
						if (targetXArchID != null) {
							ObjRef brickRef = AS.xarch.getByID(xArchRef, targetXArchID);
							if (brickRef != null) {

								//gets the brick list
								BrickList brickList = st.getBrickList();
								
								//gets the brick from the id the user selected
								BrickLabel brickLabel = (BrickLabel)st.getValue();
								IMyxBrickExtension brick = brickList.getBrick(brickLabel.getBrickId());

								//copies the brick's name to a component
								XadlUtils.setDescription(AS.xarch, brickRef, brick.getName());

								//TODO: set the location of extension point to the brick
								String extensionPointUri = MyxBrickExtensionUtils.getExtensionPointPluginUrl(brick).toString();
								XadlUtils.setXLinkByHref(AS.xarch, brickRef, "type", extensionPointUri);
								//System.out.println(PDEUtils.resolve(extensionPointUrl)); 
								//ObjRef linkType = (ObjRef)AS.xarch.get(brickRef, "type");
								//String hrefString = (String)AS.xarch.get(linkType, "href");

								//assign the extension brick to the component/connector
								IMyxBrickExtension extBrick = assignBrick(brickRef, evt, brick.getId());

								//updates other components/connectors that have the same extension brick
								IBNAModel bnaModel = evt.getSource();
								EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
								final String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
								final ObjRef archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
								XadlTranslationType translationType = (XadlTranslationType) st
								        .getProperty(ExternalBrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE);
								for (ObjRef bRef : EditorUtil.getSameBricks(AS.xarch, archStructureRef, brickRef, extBrick)) {
									translate(translationType, archStructureRef, bRef, extBrick);
								}

								//shows a tiny notification 
								Point p = st.getAnchorPoint();
								ArchipelagoUtils.showUserNotification(evt.getSource(), "Generation finished.", p.x, p.y);

								//TODO: need to find a way to open an editor from a non-UI thread
								//IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							}
						}
					}
					if (st.getCompletionStatus() != CompletionStatus.INCOMPLETE) {
						evt.getSource().removeThing(st);
						openControls.remove(st);
					}
				}
			}
		}
	}

	/**
	 * Translates a extension brick into xADL.
	 * 
	 * @param translationType
	 * @param archStructureRef
	 * @param brickRef
	 * @param brickExt
	 */
	private void translate(XadlTranslationType translationType, ObjRef archStructureRef, ObjRef brickRef, IMyxBrickExtension brickExt) {

		////////////////////////////////////////
		// translates a brick into xadl
		if (translationType == XadlTranslationType.FULL_CONVERSION) {
			//translates brick into xadl
			CodeGenXadlFullTranslator translator = new CodeGenXadlFullTranslator(AS.xarch, xArchRef);
			translator.translate(archStructureRef, brickRef, brickExt);
		}
		else {
			//translates only interface info into xadl
			CodeGenXadlPartialTranslator translator = new CodeGenXadlPartialTranslator(AS.xarch, xArchRef);
			translator.translate(brickRef, brickExt);
		}
	}

	/**
	 * Gets the project of this architecture
	 * 
	 * @return
	 */
	private IProject getProject() {
		URI xArchURI = URI.create(AS.xarch.getXArchURI(xArchRef));
		IProject xArchProject = null;
		for (IContainer container : ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(xArchURI)) {
			xArchProject = container.getProject();
			break;
		}
		return xArchProject;
	}

	/**
	 * Reads brick information from eclipse extension point, and assigns
	 * Interfaces into XADL.
	 * 
	 * @author Nobu Takeo
	 */
	private IMyxBrickExtension assignBrick(ObjRef brickRef, BNAModelEvent evt, String brickId) {

		///////////////////////////////////////////////
		// reads a brick from eclipse extension point 
		// and generates java source code
		IMyxBrickExtension brickExt = null;
		for (IMyxBrickExtension b : loadBricksFromOtherExtensions()) {
			if (b.getId().equals(brickId)) {
				brickExt = b;
				break;
			}
		}
		if (brickExt == null) {
			//returns an error
			ConsoleDisplayMgr.println("Unable to find the extension data corresponding to the brick (" + XadlUtils.getDescription(AS.xarch, brickRef) + ").");
			ConsoleDisplayMgr.println(": failed." + sdf.format(Calendar.getInstance().getTime()));
			//			MessageDialog.openError(
			//					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
			//					"SourceCodeGeneration: Error", 
			//					"Unable to find the extension data corresponding to the brick (" 
			//					+ XadlUtils.getDescription(AS.xarch, brickRef) + ").");
			return null;
		}

		IBNAModel bnaModel = evt.getSource();
		SWTBrickSelectorThing st = (SWTBrickSelectorThing) evt.getTargetThing();
		XadlTranslationType translationType = (XadlTranslationType) st.getProperty(ExternalBrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE);

		//translates brick into xadl
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
		final String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		if (archStructureXArchID == null) {
			ConsoleDisplayMgr.printlnError("Unable to find the architecture data");
		}
		final ObjRef archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
		if (archStructureRef == null) {
			ConsoleDisplayMgr.printlnError("Unable to find the architecture data of " + archStructureXArchID);
		}
		translate(translationType, archStructureRef, brickRef, brickExt);

		return brickExt;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.bna4.IBNAMouseListener#mouseUp(edu.uci.isr.bna4.IBNAView,
	 * org.eclipse.swt.events.MouseEvent, edu.uci.isr.bna4.IThing, int, int)
	 */
	public void mouseUp(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.bna4.IBNAMouseListener#mouseDown(edu.uci.isr.bna4.IBNAView,
	 * org.eclipse.swt.events.MouseEvent, edu.uci.isr.bna4.IThing, int, int)
	 */
	public void mouseDown(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
		if (evt.button == 1) {
			if (openControls.size() > 0) {
				SWTBrickSelectorThing[] oc = openControls.toArray(new SWTBrickSelectorThing[openControls.size()]);
				for (SWTBrickSelectorThing st : oc) {
					st.setCompletionStatus(CompletionStatus.CANCEL);
					st.setEditing(false);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.bna4.IBNAMouseListener#mouseClick(edu.uci.isr.bna4.IBNAView,
	 * org.eclipse.swt.events.MouseEvent, edu.uci.isr.bna4.IThing, int, int)
	 */
	public void mouseClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.bna4.IBNAMouseListener#mouseDoubleClick(edu.uci.isr.bna4.
	 * IBNAView, org.eclipse.swt.events.MouseEvent, edu.uci.isr.bna4.IThing,
	 * int, int)
	 */
	/**
	 * Opens java editor if the selected bricks have java files.
	 */
	public void mouseDoubleClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {

	}

}
