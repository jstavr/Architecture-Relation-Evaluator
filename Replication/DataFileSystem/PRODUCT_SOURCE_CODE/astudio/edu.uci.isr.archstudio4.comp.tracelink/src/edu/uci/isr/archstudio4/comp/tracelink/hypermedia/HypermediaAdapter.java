package edu.uci.isr.archstudio4.comp.tracelink.hypermedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.FileEditorInput;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

/**
 * @author dpurpura, Hazel
 */

public class HypermediaAdapter
	extends AbstractMyxSimpleBrick
	implements IHypermediaAdapter{

	public static final IMyxName INTERFACE_NAME_IN_INVOKEHADAPTER = MyxUtils.createName("invokehadapter");

	MyxRegistry myxr = MyxRegistry.getSharedInstance();

	//TracelinkController controller;

	
	public void begin(){

		myxr.register(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKEHADAPTER)){
			return this;
		}
		else{
			return null;
		}
	}

	public void invokeHAdapter(String filename){
		System.out.println("HypermediaAdapterView*******************");

		//Need to check if the filename passed in is within the Eclipse workspace
		//case 1: component/connector

		if(filename.substring(0, 1).compareTo("#") == 0){
			//TODO: call Archipelago's find component/connector function
		}

		//case 2: filename = URL
		
		else if(filename.substring(0, 7).compareTo("http://") == 0){
			URL url;
			try{
				
				url = new URL(filename);
			
				//4/27/09 - invoke a default browser if we can
				//if not, use Eclipse's browser
				
				//8/20/09 - this causes a dependency on Java 1.6
				// I've made it dynamic since it's such a small piece of code
				// We need to figure out how to handle this better in the future.
								
				//				Desktop desktop = null;
				//			    if (Desktop.isDesktopSupported()) {
				//			        desktop = Desktop.getDesktop();
				//			        if (desktop.isSupported(Desktop.Action.BROWSE)) {
				//			            URI uri = url.toURI();
				//			            desktop.browse(uri);
				//			        }
				//			    }

				try{
					Class<?> desktopClass = Class.forName("java.awt.Desktop");
					Object desktop = desktopClass.getMethod("getDesktop", (Class<?>[])null).invoke(null);
					desktop.getClass().getMethod("browse", java.net.URI.class).invoke(desktop, url.toURI());
					return;
				}catch(ClassNotFoundException e){
					// oh well, use the Eclipse browser
				}catch(Exception e){
					e.printStackTrace();
				}
				
				IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
				IWebBrowser browser = browserSupport.createBrowser("myBrowser");
				browser.openURL(url);
				
			}
			catch(MalformedURLException e1){
				e1.printStackTrace();
			}
			catch(PartInitException e2){
				e2.printStackTrace();
			} 
			catch (IOException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			}

		}

		//case 3: filename = textfile outside the Eclipse workspace
		//This is an important distinction since Eclipse treats resources (i.e. files) outside the workspace
		//differently from resources within the workspace
		//else if(filename.substring(0, 3).compareTo("C:\\") == 0){
		//TODO: currently can't handle displaying files with spaces in the paths
		else if(filename.contains("file:")){

			try{
				//URI uriPath = new URI(filename);
				String fixedPath = filename.substring(5);
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				//TODO: remove the hardcoded project name
				IProject project = ws.getRoot().getProject("ProjectArtifacts");
				if(!project.exists()){
					project.create(null);
				}
				if(!project.isOpen()){
					project.open(null);
				}
				IPath location = new Path(fixedPath);
				IFile ifile = project.getFile(location.lastSegment());
				if(!ifile.exists()){
					ifile.createLink(location, IResource.NONE, null);
				}
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
				IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(ifile.getName());
				FileEditorInput fileEditorInput = new FileEditorInput(ifile);
				if(page != null){
					page.openEditor(fileEditorInput, desc.getId());
				}

			/*
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			*/
			}
			
			catch(CoreException e1){
				e1.printStackTrace();
			}

		}

		//case 4: filename is within the Eclipse workspace
		//The filename should be a path with root = project name
		//This cannot be a hardcoded absolute path outside the Eclipse workspace since we will 
		//pass this into the IFile, which requires resources to be within the workspace.
		//Not all URI have an equivalent IFile
		else{
			// Open new file in editor
			IPath path = new Path(filename);

			//H: 5/23/08 
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			System.out.println(" " + ifile + " ifile exists: " + ifile.exists()); //ifile does not exist

			//H: TODO: Delete this if block?
			if(!ifile.exists()){
				try{
					ifile.refreshLocal(IResource.DEPTH_INFINITE, null);
					System.out.println(" " + ifile + " ifile exists: " + ifile.exists());
				}
				catch(CoreException e){
					e.printStackTrace();
				}
			}

			try{

				FileEditorInput fileEditorInput = new FileEditorInput(ifile);

				// now that the FileEditorInput is created opening the Editor is trivial

				IWorkbench workbench = PlatformUI.getWorkbench();
				//The following works with html, txt, code
				//check if desc is null to handle pdf, ppt, etc.
				IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(ifile.getName());
				if(desc == null){
					desc = workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
				}
				IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
				System.out.println("editor: " + desc.getLabel());
				page.openEditor(fileEditorInput, desc.getId());

			}
			catch(CoreException e1){
				e1.printStackTrace();
			}

		}
	}

}
