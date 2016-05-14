package edu.uci.isr.archstudio4.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;

import biz.volantec.utils.widgets.WSFileDialog;
import edu.uci.isr.archstudio4.editors.FocusEditorListener;
import edu.uci.isr.archstudio4.natures.ArchStudio4Nature;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class EclipseUtils{

	private EclipseUtils(){
	}

	public static String getArchStudio4NatureID(){
		return ArchStudio4Nature.ID;
	}

	public static IProject[] getOpenArchStudioProjects(){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		List<IProject> l = new ArrayList<IProject>();
		IProject[] projects = root.getProjects();
		for(IProject element: projects){
			if(element.isOpen()){
				try{
					if(element.getNature(getArchStudio4NatureID()) != null){
						l.add(element);
					}
				}
				catch(CoreException ce){
					System.out.println(ce);
					ce.printStackTrace();
				}
			}
		}
		return l.toArray(new IProject[l.size()]);
	}

	public static void focusEditor(XArchFlatInterface xarch, ObjRef ref, String editorID, String editorName){
		final XArchFlatInterface fxarch = xarch;
		final ObjRef fref = ref;
		final String feditorID = editorID;
		final String feditorName = editorName;
		SWTWidgetUtils.async(new Runnable(){

			public void run(){
				_focusEditor(fxarch, fref, feditorID, feditorName);
			}
		});
	}

	private static void _focusEditor(XArchFlatInterface xarch, ObjRef ref, String editorID, String editorName){
		if(ref == null){
			return;
		}
		if(!xarch.isValidObjRef(ref)){
			return;
		}
		ObjRef xArchRef = xarch.getXArch(ref);
		if(!xarch.isValidObjRef(xArchRef)){
			return;
		}

		String uri = xarch.getXArchURI(xArchRef);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		Path path = new Path(uri);
		IFile file = root.getFile(path);
		if(file == null){
			return;
		}

		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb != null){
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			if(win != null){
				IWorkbenchPage page = win.getActivePage();
				if(page != null){
					try{
						IFileEditorInput fileEditorInput = new FileEditorInput(file);
						IEditorPart editorPart = page.openEditor(fileEditorInput, editorID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
						if(editorPart instanceof FocusEditorListener){
							((FocusEditorListener)editorPart).focusEditor(editorName, new ObjRef[]{ref});
						}
					}
					catch(PartInitException pie){
						//pie.printStackTrace();
					}
				}
			}
		}
	}

	public static IFileEditorInput getFileEditorInput(IPath path){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(path);
		if(file == null){
			return null;
		}
		IFileEditorInput fileEditorInput = new FileEditorInput(file);
		return fileEditorInput;
	}

	public static IEditorPart openEditor(String editorID, IPath path){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IFile file = root.getFile(path);
		if(file == null){
			return null;
		}

		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb != null){
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			if(win != null){
				IWorkbenchPage page = win.getActivePage();
				if(page != null){
					try{
						IFileEditorInput fileEditorInput = new FileEditorInput(file);
						IEditorPart editorPart = page.openEditor(fileEditorInput, editorID, true, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
						return editorPart;
					}
					catch(PartInitException pie){
						//pie.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public static void selectAndSaveFile(Shell shell, String targetExtension, InputStream contents){
		IPath path = selectFile(shell, targetExtension);
		if(path != null){
			saveFile(shell, contents, path);
		}
	}

	public static IPath selectFile(Shell shell, String targetExtension){
		final Shell fshell = shell;

		SaveAsDialog sad = new SaveAsDialog(shell);
		sad.open();
		IPath initialTargetPath = sad.getResult();
		if(initialTargetPath == null){
			return null;
		}

		if(targetExtension != null){
			String extension = initialTargetPath.getFileExtension();
			if(extension == null){
				initialTargetPath = initialTargetPath.addFileExtension(targetExtension);
			}
		}
		return initialTargetPath;
	}

	public static void saveFile(Shell shell, InputStream contents, IPath initialTargetPath){
		final Shell fshell = shell;
		final InputStream fcontents = contents;
		final IPath targetPath = initialTargetPath;

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile targetFile = workspace.getRoot().getFile(targetPath);

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation(){

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException{
				IPath targetContainerPath = targetPath.removeLastSegments(1);
				boolean createContainer = true;
				if(workspace.getRoot().getContainerForLocation(targetContainerPath) != null){
					createContainer = false;
				}
				ContainerGenerator gen = new ContainerGenerator(targetContainerPath);
				IContainer res = null;
				try{
					if(createContainer){
						res = gen.generateContainer(monitor); // creates project A and folder B if required
					}
					if(targetFile.exists()){
						targetFile.delete(false, monitor);
					}
					targetFile.create(fcontents, false, monitor);
					try{
						fcontents.close();
					}
					catch(IOException ioe){
					}
				}
				catch(CoreException e){
					MessageDialog.openError(fshell, "Error", "Could not save file: " + e.getMessage());
					return;
				}
				catch(OperationCanceledException e){
					return;
				}
			}
		};
		try{
			operation.run(null);
		}
		catch(InterruptedException e){
		}
		catch(InvocationTargetException ite){
		}
	}

	public static IFile[] selectFilesToOpen(Shell shell){
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ResourceSelectionDialog dialog = new ResourceSelectionDialog(shell, workspace.getRoot(), "Select a file to open");
		int result = dialog.open();
		if(result == ResourceSelectionDialog.OK){
			Object[] selection = dialog.getResult();
			IFile[] files = new IFile[selection.length];
			System.arraycopy(selection, 0, files, 0, selection.length);
			return files;
		}
		return null;
	}

	public static IResource[] selectResourcesToOpen(Shell shell, int selectionMode, String title, String[] extensions){
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		WSFileDialog dialog = new WSFileDialog(shell, selectionMode, title, workspace.getRoot(), true, extensions, null);

		int result = dialog.open();
		if(result == WSFileDialog.OK){
			if(selectionMode == SWT.SINGLE){
				IResource resource = dialog.getSingleResult();
				return new IResource[]{workspace.getRoot().getFile(resource.getFullPath())};
			}
			else if(selectionMode == SWT.MULTI){
				IResource[] resources = dialog.getMultiResult();
				return resources;
			}
		}
		return null;
	}

	public static void openExternalBrowser(String externalURL){
		try{
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new java.net.URL(externalURL));
		}
		catch(java.net.MalformedURLException mue){
		}
		catch(PartInitException pie){
		}
	}

	public static String[][] getFieldEditorPreferenceData(Class<? extends Enum> enumClass){
		Enum[] enumConstants = enumClass.getEnumConstants();
		String[][] values = new String[enumConstants.length][];
		for(int i = 0; i < enumConstants.length; i++){
			values[i] = new String[]{enumConstants[i].toString(), enumConstants[i].name()};
		}
		return values;
	}
}
