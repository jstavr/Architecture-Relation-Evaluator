package edu.uci.isr.archstudio4.comp.archipelago.stateline;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.booleaneval.SymbolTable;
import edu.uci.isr.archstudio4.util.EclipseUtils;
import edu.uci.isr.archstudio4.util.XadlSelectorDialog;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.things.glass.BoxGlassThing;
import edu.uci.isr.bna4.things.glass.EllipseGlassThing;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class StatelineStatechartsLogic
	extends AbstractThingLogic
	implements IBNAMenuListener{

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public StatelineStatechartsLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, final IThing t, int worldX, int worldY){
		if(t == null){
			IBNAModel bnaModel = view.getWorld().getBNAModel();
			EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaModel);
			String xArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
			if(xArchID != null){
				final ObjRef rootRef = AS.xarch.getByID(xArchRef, xArchID);
				if(rootRef != null && AS.xarch.isInstanceOf(rootRef, "statecharts#Statechart")){
					IAction runStatelineSelectionAction = new Action("Run Stateline Selection for Statechart..."){

						@Override
						public void run(){
							runStatelineSelectionForStatechart(rootRef);
						}
					};
					m.add(runStatelineSelectionAction);
				}
			}
		}
		if(t != null && (t instanceof BoxGlassThing || t instanceof EllipseGlassThing)){
			IBNAModel bnaModel = view.getWorld().getBNAModel();
			IThing[] selectedThings = BNAUtils.getSelectedThings(bnaModel);
			if(selectedThings.length <= 1){
				final IAssembly assembly = AssemblyUtils.getAssemblyWithPart(t);
				if(assembly != null && assembly.isKind("state")){
					IAction runStatelineSelectionAction = new Action("Run Stateline Selection..."){

						@Override
						public void run(){
							runStatelineSelection(t, assembly);
						}
					};
					m.add(runStatelineSelectionAction);
					m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				}
			}
		}
	}

	protected void runStatelineSelectionForStatechart(ObjRef statechartRef){
		Shell shell = AS.editor.getParentComposite().getShell();
		ObjRef startingPointRef = XadlSelectorDialog.showSelectorDialog(shell, "Select Selection Start Point", AS.xarch, AS.resources, xArchRef, XadlTreeUtils.STRUCTURE | XadlTreeUtils.DOCUMENT | XadlTreeUtils.COMPONENT_TYPE | XadlTreeUtils.CONNECTOR_TYPE, XadlTreeUtils.STRUCTURE | XadlTreeUtils.COMPONENT_TYPE | XadlTreeUtils.CONNECTOR_TYPE);

		if(startingPointRef == null){
			return;
		}

		String startingID = XadlUtils.getID(AS.xarch, startingPointRef);
		if(startingID == null){
			return;
		}
		boolean isStructural = AS.xarch.isInstanceOf(startingPointRef, "types#ArchStructure");

		String baseURI = AS.xarch.getXArchURI(xArchRef);
		if(baseURI != null){
			ObjRef[] stateRefs = AS.xarch.getAll(statechartRef, "state");
			if(stateRefs.length > 0){
				IPath rootPath = EclipseUtils.selectFile(shell, "xml");
				String rootName = rootPath.lastSegment();
				rootName = rootName.substring(0, rootName.lastIndexOf('.'));
				List<ObjRef> processedRefList = new ArrayList<ObjRef>();
				for(int i = 0; i < stateRefs.length; i++){
					String stateID = XadlUtils.getID(AS.xarch, stateRefs[i]);
					if(stateID != null){
						SymbolTable symbolTable = new SymbolTable();
						symbolTable.putString("state", StatelineUtils.mungeID(stateID));

						String newURI = UIDGenerator.generateUID("urn:");
						try{
							AS.selector.select(baseURI, newURI, symbolTable, startingID, isStructural);
						}
						catch(Exception e){
							MessageDialog.openError(AS.editor.getParentComposite().getShell(), "Error Performing Selection", e.getClass().getName() + e.getMessage());
							e.printStackTrace();
							continue;
						}

						ObjRef processedRef = AS.xarch.getOpenXArch(newURI);
						if(processedRef != null){
							processedRefList.add(processedRef);
							String contents = AS.xarch.serialize(processedRef);
							InputStream is = new ByteArrayInputStream(contents.getBytes());

							String stateDescription = XadlUtils.getDescription(AS.xarch, stateRefs[i]);
							if(stateDescription == null){
								stateDescription = "state" + i;
							}
							stateDescription = stateDescription.replace(' ', '_');
							stateDescription = stateDescription.replace('?', '.');
							stateDescription = stateDescription.replace('*', '.');

							IPath newPath = rootPath.removeLastSegments(1);
							newPath = newPath.append(rootName + "_state_" + stateDescription);
							newPath = newPath.addFileExtension("xml");
							//System.err.println(newPath.toOSString());
							EclipseUtils.saveFile(shell, is, newPath);
						}
						else{
							throw new RuntimeException("Error: after processing, couldn't read output. This shouldn't happen.");
						}
					}
				}
				for(ObjRef processedRef: processedRefList){
					AS.xarch.close(processedRef);
				}
			}
		}
	}

	protected void runStatelineSelection(IThing t, IAssembly assembly){
		Shell shell = AS.editor.getParentComposite().getShell();
		ObjRef startingPointRef = XadlSelectorDialog.showSelectorDialog(shell, "Select Selection Start Point", AS.xarch, AS.resources, xArchRef, XadlTreeUtils.STRUCTURE | XadlTreeUtils.DOCUMENT | XadlTreeUtils.COMPONENT_TYPE | XadlTreeUtils.CONNECTOR_TYPE, XadlTreeUtils.STRUCTURE | XadlTreeUtils.COMPONENT_TYPE | XadlTreeUtils.CONNECTOR_TYPE);

		if(startingPointRef == null){
			return;
		}
		String startingID = XadlUtils.getID(AS.xarch, startingPointRef);
		if(startingID == null){
			return;
		}
		boolean isStructural = AS.xarch.isInstanceOf(startingPointRef, "types#ArchStructure");

		String baseURI = AS.xarch.getXArchURI(xArchRef);
		if(baseURI != null){
			String xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
			if(xArchID != null){
				SymbolTable symbolTable = new SymbolTable();
				symbolTable.putString("state", StatelineUtils.mungeID(xArchID));

				String newURI = UIDGenerator.generateUID("urn:");
				try{
					AS.selector.select(baseURI, newURI, symbolTable, startingID, isStructural);
				}
				catch(Exception e){
					MessageDialog.openError(AS.editor.getParentComposite().getShell(), "Error Performing Selection", e.getClass().getName() + e.getMessage());
					e.printStackTrace();
					return;
				}

				ObjRef processedRef = AS.xarch.getOpenXArch(newURI);
				if(processedRef != null){
					String contents = AS.xarch.serialize(processedRef);
					InputStream is = new ByteArrayInputStream(contents.getBytes());
					EclipseUtils.selectAndSaveFile(shell, "xml", is);
				}
				else{
					throw new RuntimeException("Error: after processing, couldn't read output. This shouldn't happen.");
				}
				AS.xarch.close(processedRef);
			}
		}
	}
}
