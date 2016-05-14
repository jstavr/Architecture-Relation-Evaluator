package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAModelListener;
import edu.uci.isr.bna4.IBNAMouseListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.myx2.eclipse.extension.IInterface;
import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;
import edu.uci.isr.myx2.eclipse.extension.MyxBrickExtensionUtils;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoader;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoaderUtil;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

/**
 * Logic for interfaces to add menu for opening the java source file
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class BrickInterfaceSelectorLogic extends AbstractThingLogic implements IBNAMenuListener, IBNAModelListener, IBNAMouseListener {

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	protected ObjRef archStructureRef = null;

	public BrickInterfaceSelectorLogic(ArchipelagoServices services, ObjRef xArchRef) {
		this.AS = services;
		this.xArchRef = xArchRef;
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
			//single thing is allowed
			return;
		}

		if (matches(view, t)) {

			//open editor menu
			final IBNAView fview = view;
			final IThing ft = t;
			m.add(new Action("Open java source") {
				@Override
				public void run() {
					openEditor(fview, ft);
				}
			});
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	/**
	 * Checks if the menu item can be displayed
	 */
	public boolean matches(IBNAView view, IThing t) {
		if (t instanceof EndpointGlassThing) {
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if (pt != null) {
				return StructureMapper.isInterfaceAssemblyRootThing(pt);
			}
		}
		return false;
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

	public String getXArchID(IBNAView view, IThing t) {
		if (t instanceof EndpointGlassThing) {
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
	}

	private void openEditor(IBNAView view, IThing t) {

		//the target project
		IProject project = getProject();

		final String intfRefId = getXArchID(view, t);
		if (intfRefId == null) {

			return;
		}

		ObjRef intfRef = AS.xarch.getByID(xArchRef, intfRefId);
		ObjRef intfTypeEltRef = (ObjRef) AS.xarch.get(intfRef, "type");
		String intfTypeUrl = intfTypeEltRef != null ? XadlUtils.getHref(AS.xarch, intfTypeEltRef) : null;
		String extBrickIntfId = MyxBrickExtensionUtils.getIdFromExtensionURI(MyxBrickExtensionUtils.toUrl(intfTypeUrl));
		if (extBrickIntfId == null) {
			//uses xadl to get the java class

			EditorUtil.openEditor(project, AS.xarch, intfRef);

		}
		else {

			//look for the extension brick interface in the extension point

			Map<String, IMyxBrickExtension> extUrlToExtBrickMap = new HashMap<String, IMyxBrickExtension>();

			//gets all the components and connectors
			Set<ObjRef> brickRefs = new HashSet<ObjRef>();
			brickRefs.addAll(Arrays.asList(AS.xarch.getAll(archStructureRef, "component")));
			brickRefs.addAll(Arrays.asList(AS.xarch.getAll(archStructureRef, "connector")));

			for (ObjRef brickRef : brickRefs) {
				for (ObjRef interfaceEltRef : AS.xarch.getAll(brickRef, "interface")) {
					String intrefaceId = XadlUtils.getID(AS.xarch, interfaceEltRef);
					if (intfRefId.equals(intrefaceId)) {
						//found the component or connector of the interface

						//gets the extension brick
						IMyxBrickExtension extBrick = getExtensionBrick(AS.xarch, brickRef, extUrlToExtBrickMap);
						for (IInterface intf : extBrick.getInterfaces()) {
							if (extBrickIntfId.equals(intf.getId())) {
								//found the extension brick interface

								//check the project where the extension brick interface is defined
								if (!project.getName().equals(intf.getSymbolicName())) {
									project = ExtensionLoaderUtil.findProject(intf.getSymbolicName());
								}
								EditorUtil.openEditor(JavaCore.create(project), intf.getFQJavaInterfaceName());
								return;
							}
						}
					}
				}
			}
		}

	}

	public void bnaModelChanged(BNAModelEvent evt) {
		if (evt.getEventType() == BNAModelEvent.EventType.THING_ADDED) {
			IBNAModel bnaModel = evt.getSource();
			EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
			final String archStructureXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
			if (archStructureXArchID != null) {
				archStructureRef = AS.xarch.getByID(xArchRef, archStructureXArchID);
			}
		}
	}

	private IMyxBrickExtension getExtensionBrick(XArchFlatInterface xarch, ObjRef brickRef, Map<String, IMyxBrickExtension> extUrlToExtBrickMap) {

		// gets the type href of brickRef
		ObjRef brickTypeEltRef = (ObjRef) xarch.get(brickRef, "type");
		String brickTypeUrl = null;
		if (brickTypeEltRef != null) {
			brickTypeUrl = XadlUtils.getHref(xarch, brickTypeEltRef);
		}
		if (brickTypeUrl == null) {
			return null;
		}

		//try to find a brick from the external project specified by the link
		IPluginModelBase pluginModel = ExtensionLoaderUtil.getPluginModelFromExtensionURI(brickTypeUrl);
		if (pluginModel != null) {

			ExtensionLoader extLoader = ExtensionLoaderUtil.getExtensionLoader(pluginModel);
			for (IMyxBrickExtension extBrick : extLoader.getExtensionBricks()) {
				extUrlToExtBrickMap.put(extBrick.getExtensionPointUrl().toString(), extBrick);
			}

		}

		IMyxBrickExtension brick = extUrlToExtBrickMap.get(brickTypeUrl);

		return brick;
	}

	public void mouseClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

	public void mouseDoubleClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
		openEditor(view, t);

	}

	public void mouseDown(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

	public void mouseUp(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

}
