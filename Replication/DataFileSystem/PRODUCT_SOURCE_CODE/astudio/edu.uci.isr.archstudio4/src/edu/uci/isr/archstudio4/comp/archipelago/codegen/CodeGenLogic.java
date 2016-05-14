package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.myx2.eclipse.codegen.brick.CodegenBrick;
import edu.uci.isr.myx2.eclipse.codegen.codegen.MyxCodeGenerator;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlFullTranslator;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlPartialTranslator;
import edu.uci.isr.myx2.eclipse.codegen.translator.XadlTranslationType;
import edu.uci.isr.myx2.eclipse.codegen.ui.console.ConsoleDisplayMgr;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * Myx java code generation logic of Archipelago. Reads the data of specified
 * brick from eclipse extension point, and generates java code
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class CodeGenLogic extends AbstractThingLogic implements IBNAMenuListener {

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public CodeGenLogic(ArchipelagoServices services, ObjRef xArchRef) {
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public boolean matches(IBNAView view, IThing thing) {

		// checks if the thing is a brick
		if (thing instanceof BoxGlassThing) {
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(thing);
			if (parentThing != null) {
				return StructureMapper.isBrickAssemblyRootThing(parentThing);
			}
		}
		return false;
	}

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY) {
		if (matches(view, t)) {
			for (IAction action : getActions(view, t, worldX, worldY)) {
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY) {
		//final IBNAView fview = view;
		final int fworldX = worldX;
		final int fworldY = worldY;

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(view.getWorld().getBNAModel());

		final String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		if (archStructureXArchID == null) {
			//Nothing to set description on
			return new IAction[0];
		}

		final ObjRef archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
		if (archStructureRef == null) {
			//Nothing to add elements to
			return new IAction[0];
		}

		ArchipelagoUtils.setNewThingSpot(view.getWorld().getBNAModel(), fworldX, fworldY);

		final String brickXArchID = getXArchID(view, t);
		if (brickXArchID == null) {
			//Nothing to set description on
			return new IAction[0];
		}

		final ObjRef brickRef = AS.xarch.getByID(xArchRef, brickXArchID);
		if (brickRef == null) {
			//Nothing to set description on
			return new IAction[0];
		}

		// full translation from extension point to xadl
		Action codeGenerationAction = new MyxCodeGenAction("MyxCodeGen (Translation to XADL)", view, worldX, worldY, archStructureRef, brickRef,
		        XadlTranslationType.FULL_CONVERSION);

		// minimum translation from brick into xadl
		// only interfaces of a brick are translated.
		Action codeGenerationForAIMAction = new MyxCodeGenAction("MyxCodeGen (Eclipse Extension)", view, worldX, worldY, archStructureRef, brickRef,
		        XadlTranslationType.INTERFACE_ONLY);

		return new IAction[] { codeGenerationForAIMAction, codeGenerationAction };
	}

	public String getXArchID(IBNAView view, IThing t) {
		if (t instanceof BoxGlassThing) {
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
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
	private class MyxCodeGenAction extends Action {
		private IBNAView view;
		private int worldX;
		private int worldY;
		private ObjRef archStructureRef;
		private ObjRef brickRef;
		private XadlTranslationType translationType;

		MyxCodeGenAction(String actionTitle, IBNAView view, int worldX, int worldY, ObjRef archStructureRef, ObjRef brickRef,
		        XadlTranslationType translationType) {
			super(actionTitle);
			this.view = view;
			this.worldX = worldX;
			this.worldY = worldY;
			this.archStructureRef = archStructureRef;
			this.brickRef = brickRef;
			this.translationType = translationType;

		}

		/**
		 * Reads brick information from eclipse extension point, and generates
		 * template java source code, and generates ComponentType, Signatures,
		 * Interfaces and InterfaceTypes into XADL.
		 */
		public void run() {

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			/////////////////////////////
			//gets the project
			URI xArchURI = URI.create(AS.xarch.getXArchURI(xArchRef));
			IProject xArchProject = null;
			for (IContainer container : ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(xArchURI)) {
				xArchProject = container.getProject();
				break;
			}
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.setTimeZone(TimeZone.getTimeZone("PST"));
			ConsoleDisplayMgr.println(getText() + ": started. " + sdf.format(Calendar.getInstance().getTime()));

			///////////////////////////////////////////////
			// reads a brick from eclipse extension point 
			// and generates java source code
			MyxCodeGenerator codeGen = new MyxCodeGenerator(xArchProject.getName());
			CodegenBrick brick = codeGen.generateCodeByBrickName(XadlUtils.getDescription(AS.xarch, brickRef));
			if (brick == null) {
				//returns an error
				ConsoleDisplayMgr.printlnError("Unable to find the extension data corresponding to the brick (" + XadlUtils.getDescription(AS.xarch, brickRef)
				        + ").");
				ConsoleDisplayMgr.printlnError(getText() + ": failed.");
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), getText() + ": Error",
				        "Unable to find the extension data corresponding to the brick (" + XadlUtils.getDescription(AS.xarch, brickRef) + ").");
				return;
			}
			for (IFile sourceFile : codeGen.getGeneratedFiles()) {
				try {
					IDE.openEditor(page, sourceFile, true);
				}
				catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			////////////////////////////////////////
			// translates a brick into xadl
			//IMyxBrickExtension brickExt = extLoader.getExtensionBrickByName(XadlUtils.getDescription(AS.xarch, brickRef));

			if (translationType == XadlTranslationType.FULL_CONVERSION) {
				//translates brick into xadl
				CodeGenXadlFullTranslator translator = new CodeGenXadlFullTranslator(AS.xarch, xArchRef);
				translator.translate(archStructureRef, brickRef, brick);
			}
			else {
				//translates only interface info into xadl
				CodeGenXadlPartialTranslator translator = new CodeGenXadlPartialTranslator(AS.xarch, xArchRef);
				translator.translate(brickRef, brick);
			}

			////////////////////////////
			//set focus to the Archipelago editor
			IFile xArchFile = null;
			for (IFile file : ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(xArchURI)) {
				xArchFile = file;
				break;
			}
			try {
				IDE.openEditor(page, xArchFile);
			}
			catch (PartInitException e) {
				e.printStackTrace();
			}
			ArchipelagoUtils.showUserNotification(view.getWorld().getBNAModel(), "Generation finished.", worldX, worldY);
			ConsoleDisplayMgr.println(getText() + ": finished.");

		}
	}

}
