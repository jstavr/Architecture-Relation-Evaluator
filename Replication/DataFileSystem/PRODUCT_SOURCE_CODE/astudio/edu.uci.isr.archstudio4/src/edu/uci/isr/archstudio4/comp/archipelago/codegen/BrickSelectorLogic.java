package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
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
import edu.uci.isr.myx2.eclipse.codegen.brick.CodegenBrick;
import edu.uci.isr.myx2.eclipse.codegen.codegen.MyxCodeGenerator;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlFullTranslator;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlPartialTranslator;
import edu.uci.isr.myx2.eclipse.codegen.translator.XadlTranslationType;
import edu.uci.isr.myx2.eclipse.codegen.ui.console.ConsoleDisplayMgr;
import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoaderUtil;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * Myx java code generation logic of Archipelago. Reads the data of specified
 * brick from eclipse extension point, and generates java code
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class BrickSelectorLogic extends AbstractThingLogic implements IBNAMenuListener, IBNAModelListener, IBNAMouseListener {

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

	public BrickSelectorLogic(ArchipelagoServices services, ObjRef xArchRef) {
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

			//open editor menu
			final IBNAView fview = view;
			m.add(new Action("Open java source") {
				@Override
				public void run() {
					openEditor(fview);
				}
			});
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

			//assign brick menu
			for (IAction action : getActions(view, t, worldX, worldY)) {
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		}
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
		Action codeGenIFOnlyAction = new Action("Assign Brick ...") {

			@Override
			public void run() {
				Point p = BNAUtils.getCentralPoint(ft);
				if (p == null) {
					p = new Point(fworldX, fworldY);
				}

				SWTBrickSelectorThing st = new SWTBrickSelectorThing();

				st.setResources(fAS.resources);

				//remembers the xadl id of the component
				st.setProperty(BrickSelectorLogic.PRP_NAME_TARGET_XARCH_ID, eltXArchID);

				//remembers the brick names form the extension for displaying
				BrickList brickList = new BrickList(loadBricksFromExtension());
				st.setBrickList(brickList);
				st.setBrickLabelList(new BrickLabelList(brickList));

				//sets the translation type to "interface only.
				st.setProperty(BrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE, XadlTranslationType.INTERFACE_ONLY);

				//remembers the location of this action
				st.setAnchorPoint(p);

				MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, st);
				st.setEditing(true);
				openControls.add(st);
				fview.getWorld().getBNAModel().addThing(st, ft);
			}
		};
		if (icon != null) {
			codeGenIFOnlyAction.setImageDescriptor(icon);
		}

		// reads extension point and shows a list of bricks.
		// when the user selects a brick, all the information is converted into xadl.
		Action codeGenFullAction = new Action("Convert Brick to xADL...") {

			@Override
			public void run() {
				Point p = BNAUtils.getCentralPoint(ft);
				if (p == null) {
					p = new Point(fworldX, fworldY);
				}

				SWTBrickSelectorThing st = new SWTBrickSelectorThing();

				st.setResources(fAS.resources);

				//remembers the xadl id of the component
				st.setProperty(BrickSelectorLogic.PRP_NAME_TARGET_XARCH_ID, eltXArchID);

				//remembers the brick names form the extension for displaying
				BrickList brickList = new BrickList(loadBricksFromExtension());
				st.setBrickList(brickList);
				st.setBrickLabelList(new BrickLabelList(brickList));

				//sets the translation type to "interface only.
				st.setProperty(BrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE, XadlTranslationType.FULL_CONVERSION);

				//remembers the location of this action
				st.setAnchorPoint(p);
				MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, st);
				st.setEditing(true);
				openControls.add(st);
				fview.getWorld().getBNAModel().addThing(st, ft);
			}
		};
		if (icon != null) {
			codeGenFullAction.setImageDescriptor(icon);
		}

		return new IAction[] { codeGenIFOnlyAction, codeGenFullAction };
	}

	/**
	 * Read the information of brick from the eclipse's extension point
	 * 
	 * @return
	 */
	private Collection<IMyxBrickExtension> loadBricksFromExtension() {

		//gets the project
		IProject xArchProject = getProject();

		return ExtensionLoaderUtil.getExtensionLoader(xArchProject).getExtensionBricks();
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
						String targetXArchID = st.getProperty(BrickSelectorLogic.PRP_NAME_TARGET_XARCH_ID);
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

								//generates source code
								IMyxBrickExtension extBrick = generateCode(brickRef, evt, brick.getId());

								//updates other components/connectors that have the same extension brick
								IBNAModel bnaModel = evt.getSource();
								EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
								final String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
								final ObjRef archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
								XadlTranslationType translationType = (XadlTranslationType) st.getProperty(BrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE);
								for (ObjRef bRef : EditorUtil.getSameBricks(AS.xarch, archStructureRef, brickRef, extBrick)) {
									translate(translationType, archStructureRef, bRef, extBrick);
								}

								//shows a tiny notification 
								Point p = st.getAnchorPoint();
								ArchipelagoUtils.showUserNotification(evt.getSource(), "Generation finished.", p.x, p.y);

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
	 * Reads brick information from eclipse extension point, and generates
	 * template java source code. if XadlTranslationType.FULL_CONVERSION is
	 * selected, it generates ComponentType, Signatures, Interfaces and
	 * InterfaceTypes into XADL such that the conventional AIM can instantiate.
	 * if XadlTranslationType.INTERFACE_ONLY is selected, it generates only
	 * Interfaces into XADL since the new AIM can read the necessary data from
	 * eclipse extension point.
	 * 
	 * @author Nobu Takeo
	 */
	private CodegenBrick generateCode(ObjRef brickRef, BNAModelEvent evt, String brickId) {

		IBNAModel bnaModel = evt.getSource();
		SWTBrickSelectorThing st = (SWTBrickSelectorThing) evt.getTargetThing();
		XadlTranslationType translationType = (XadlTranslationType) st.getProperty(BrickSelectorLogic.PRP_NAME_TRANSLATION_TYPE);

		/////////////////////////////
		//gets the project
		IProject xArchProject = getProject();
		ConsoleDisplayMgr.println("SourceCodeGeneration: started. " + sdf.format(Calendar.getInstance().getTime()));

		///////////////////////////////////////////////
		// reads a brick from eclipse extension point 
		// and generates java source code
		MyxCodeGenerator codeGen = new MyxCodeGenerator(xArchProject.getName());
		//CodegenBrick brick = codeGen.generateCodeByBrickName(XadlUtils.getDescription(AS.xarch, brickRef));
		CodegenBrick brick = codeGen.generateCodeByBrickId(brickId);
		if (brick == null) {
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

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);

		final String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		if (archStructureXArchID == null) {
			ConsoleDisplayMgr.printlnError("Unable to find the architecture data");
		}

		final ObjRef archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
		if (archStructureRef == null) {
			ConsoleDisplayMgr.printlnError("Unable to find the architecture data of " + archStructureXArchID);
		}

		translate(translationType, archStructureRef, brickRef, brick);

		//TODO: unable to open an editor from a non-UI thread
		//		////////////////////////////
		//		//set focus to the Archipelago editor
		//		IFile xArchFile = null;
		//		for(IFile file : ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(xArchURI)) {
		//			xArchFile = file;
		//			break;
		//		}
		//		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		//		try {
		//            IDE.openEditor(page, xArchFile);
		//        }
		//        catch (PartInitException e) {
		//            e.printStackTrace();
		//        }
		for (IFile file : codeGen.getGeneratedFiles()) {
			ConsoleDisplayMgr.println("Generated: " + file.getName() + " " + sdf.format(Calendar.getInstance().getTime()));
		}
		ConsoleDisplayMgr.println("SourceCodeGeneration: finished." + sdf.format(Calendar.getInstance().getTime()));

		return brick;
	}

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
//		//the target project
//		IProject project = getProject();
//
//		//the selected things
//		IThing[] selectedThings = BNAUtils.getSelectedThings(view.getWorld().getBNAModel());
//		for (IThing thing : selectedThings) {
//
//			//if the selected thing is a brick,
//			//checks its class name and opens the editor.
//			if (matches(view, thing)) {
//
//				final String brickXArchID = getXArchID(view, thing);
//				if (brickXArchID == null) {
//					continue;
//				}
//
//				final ObjRef brickRef = AS.xarch.getByID(xArchRef, brickXArchID);
//				if (brickRef == null) {
//					continue;
//				}
//
//				if (EditorUtil.openEditor(project, AS.xarch, brickRef)) {
//					break;
//				}
//
//			}
//
//		}
//
	}

	private void openEditor(IBNAView view) {

		//the target project
		IProject project = getProject();

		//the selected things
		IThing[] selectedThings = BNAUtils.getSelectedThings(view.getWorld().getBNAModel());
		for (IThing thing : selectedThings) {

			//if the selected thing is a brick,
			//checks its class name and opens the editor.
			if (matches(view, thing)) {

				final String brickXArchID = getXArchID(view, thing);
				if (brickXArchID == null) {
					continue;
				}

				final ObjRef brickRef = AS.xarch.getByID(xArchRef, brickXArchID);
				if (brickRef == null) {
					continue;
				}

				if (EditorUtil.openEditor(project, AS.xarch, brickRef)) {
					break;
				}

			}

		}

	}

}
