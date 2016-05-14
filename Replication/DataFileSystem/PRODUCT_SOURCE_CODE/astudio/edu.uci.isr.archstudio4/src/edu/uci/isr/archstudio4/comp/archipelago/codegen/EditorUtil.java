package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import edu.uci.isr.myx2.eclipse.codegen.codegen.MyxCodeGenerator;
import edu.uci.isr.myx2.eclipse.codegen.translator.CodeGenXadlFullTranslator;
import edu.uci.isr.myx2.eclipse.codegen.ui.console.ConsoleDisplayMgr;
import edu.uci.isr.myx2.eclipse.codegen.util.TextUtil;
import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;
import edu.uci.isr.myx2.eclipse.extension.MyxBrickExtensionUtils;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoader;
import edu.uci.isr.myx2.eclipse.extension.pde.ExtensionLoaderUtil;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

/**
 * A utility class to open a java editor
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class EditorUtil {

	static Collection<ObjRef> getSameBricks(XArchFlatInterface xarch, ObjRef archStructureRef, ObjRef brickRef, IMyxBrickExtension extBrick) {

		Collection<ObjRef> sameBricks = new ArrayList<ObjRef>();
		String brickRefId = XadlUtils.getID(xarch, brickRef);
		String extBrickUrl = MyxBrickExtensionUtils.getExtensionPointPluginUrl(extBrick).toString();
		ObjRef brickTypeEltRef = (ObjRef) xarch.get(brickRef, "type");
		String brickTypeUrl = brickTypeEltRef != null ? XadlUtils.getHref(xarch, brickTypeEltRef) : "";

		String implClassName = extBrick.getFQDefaultImplClassName();
		String javaClassName = implClassName != null ? implClassName : extBrick.getFQBaseClassName();

		//gets all the components and connectors
		Set<ObjRef> brickRefs = new HashSet<ObjRef>();
		brickRefs.addAll(Arrays.asList(xarch.getAll(archStructureRef, "component")));
		brickRefs.addAll(Arrays.asList(xarch.getAll(archStructureRef, "connector")));

		for (ObjRef objRef : brickRefs) {
			ObjRef objTypeEltRef = (ObjRef) xarch.get(objRef, "type");
			String objUrl = objTypeEltRef != null ? XadlUtils.getHref(xarch, objTypeEltRef) : null;
			String objId = XadlUtils.getID(xarch, objRef);
			if (brickRefId.equals(objId)) {
				continue;
			}
			if (extBrickUrl.equals(objUrl) || brickTypeUrl.equals(objUrl)) {
				sameBricks.add(objRef);
			}
			else {
				ObjRef objTypeRef = xarch.resolveHref(xarch.getXArch(objRef), objUrl);
				String objJavaClassName = CodeGenXadlFullTranslator.getJavaClassName(xarch, objTypeRef);
				if (javaClassName.equals(objJavaClassName)) {
					sameBricks.add(objRef);
				}

			}
		}
		return sameBricks;
	}

	/**
	 * Opens an editor for the given brick
	 * 
	 * @param project
	 * @param xarch
	 * @param brickRef
	 */
	static boolean openEditor(IProject project, XArchFlatInterface xarch, ObjRef brickRef) {

		//uses the brick's description to match the brick defined in the eclipse extension point
		String brickName = XadlUtils.getDescription(xarch, brickRef);

		// reads a brick from eclipse extension point of this project
		// if not found, reads xadl 
		String javaClassName = null;
		ExtensionLoader extLoader = ExtensionLoaderUtil.getExtensionLoader(project);
		ObjRef brickTypeEltRef = (ObjRef) xarch.get(brickRef, "type");
		String brickUrl = brickTypeEltRef != null ? XadlUtils.getHref(xarch, brickTypeEltRef) : null;
		String brickId = MyxBrickExtensionUtils.getIdFromExtensionURI(MyxBrickExtensionUtils.toUrl(brickUrl));
		IMyxBrickExtension brickExt = extLoader.getExtensionBrickById(brickId);

		//		IMyxBrickExtension brickExt = extLoader.getExtensionBrickByName(brickName);
		if (brickExt != null) {
			//gets javaClassName defined in this brick
			javaClassName = brickExt.getFQDefaultImplClassName();
			if (javaClassName == null) {
				javaClassName = brickExt.getFQBaseClassName();
			}
		}
		else {
			//looks for xadl implementation
			ObjRef brickTypeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");
			javaClassName = CodeGenXadlFullTranslator.getJavaClassName(xarch, brickTypeRef);

		}

		if (javaClassName != null) {
			//open the editor
			IJavaProject javaProject = JavaCore.create(project);
			EditorUtil.openEditor(javaProject, javaClassName);

			return true;

		}
		else {
			//looks up other projects' extension points
			for (ExtensionLoader el : ExtensionLoaderUtil.getAllExtensionLoaders()) {
				//IMyxBrickExtension outsideBrick = el.getExtensionBrickByName(brickName);
				IMyxBrickExtension outsideBrick = el.getExtensionBrickById(brickId);
				if (outsideBrick != null) {
					javaClassName = outsideBrick.getFQDefaultImplClassName();
					if (javaClassName != null) {
						IProject outsideProject = ExtensionLoaderUtil.findProject(outsideBrick.getSymbolicName());

						EditorUtil.openEditor(JavaCore.create(outsideProject), javaClassName);
						return true;
					}
				}
			}

			if (javaClassName == null) {
				//java class is not found
				ConsoleDisplayMgr.printlnError("Class files are not created yet for " + brickName + ".");
			}

		}

		return false;

	}

	/**
	 * Opens an editor for the given class name.
	 * 
	 * @param javaProject
	 * @param fqClassName
	 */
	static void openEditor(final IJavaProject javaProject, final String fqClassName) {
		ConsoleDisplayMgr.println(fqClassName);

		SearchEngine searchEngine = new SearchEngine();
		try {

			//uses searchEngine to find the file
			searchEngine.searchAllTypeNames(TextUtil.getPackagePart(fqClassName).toCharArray(), SearchPattern.R_EXACT_MATCH, TextUtil.getClassPart(fqClassName)
			        .toCharArray(), SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.CLASS_AND_INTERFACE,
			//					SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject},true),
			        SearchEngine.createWorkspaceScope(), new TypeNameMatchRequestor() {
				        @Override
				        public void acceptTypeNameMatch(TypeNameMatch match) {
					        //opens the editor
					        EditorUtil.openEditor(match, fqClassName);
				        }
			        }, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());

		}
		catch (JavaModelException e) {
			e.printStackTrace();
			ConsoleDisplayMgr.println("Unable to open " + fqClassName);
		}
	}

	private static void openEditor(TypeNameMatch match, String fqClassName) {
		IPackageFragmentRoot root = match.getPackageFragmentRoot();
		if (!match.getFullyQualifiedName().equals(fqClassName)) {
			//ignores "bin.edu.....Foo" stuff
			return;
		}
		try {

			IType type = match.getType();
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				String srcFolderName = MyxCodeGenerator.getSrcRelativePathString(root);

				IContainer sourceContainer = srcFolderName.length() > 0 ? root.getJavaProject().getProject().getFolder(new Path(srcFolderName)) : root
				        .getJavaProject().getProject();

				IFile targetFile = sourceContainer.getFile(new Path(fqClassName.replace('.', File.separatorChar) + ".java"));
				if (targetFile.exists()) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IDE.openEditor(page, targetFile, true);
				}

			}
			else if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
				//TODO: need to open class file viewer. Instead, here this opens the java editor.
				IPath targetPath;
				if (root.getSourceAttachmentPath() != null) {
					//attachment source file
					targetPath = new Path(root.getSourceAttachmentPath().toString() + File.separator + "src" + File.separator
					        + fqClassName.replace('.', File.separatorChar) + ".java");
				}
				else {
					//class file
					targetPath = type.getPath();
				}
				URI uri = URIUtil.toURI(targetPath);

				// open the editor
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

				IEditorDescriptor[] dscs = PlatformUI.getWorkbench().getEditorRegistry().getEditors(targetPath.lastSegment());
				if (dscs.length > 0) {
					IDE.openEditor(page, uri, dscs[0].getId(), true);
				}
			}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
			ConsoleDisplayMgr.println("Unable to open " + fqClassName);
		}
		catch (PartInitException e) {
			e.printStackTrace();
			ConsoleDisplayMgr.println("Unable to open " + fqClassName);
		}
	}

	//	/**
	//	 * Opens the java editor
	//	 * @param project
	//	 * @param fqClassName
	//	 * @param srcFolderName
	//	 */
	//	private void openEditor(final IJavaProject javaProject, String fqClassName) {
	//		assert javaProject != null;
	//		assert fqClassName != null;
	//		
	//		//source folders
	//		List<IPackageFragmentRoot> sourceRoots = new ArrayList<IPackageFragmentRoot>();
	//		
	//		//TODO:open class files
	//		//attachment source path
	////		List<IPath> binSourceAttachmentPaths = new ArrayList<IPath>();
	//		
	//       try {
	//			for (IPackageFragmentRoot fragmentRoot : javaProject.getPackageFragmentRoots()) {
	//			  if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
	//				  sourceRoots.add(fragmentRoot);
	//		
	//			  } else if (fragmentRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
	//				  //TODO: open class files
	//				  
	////				  IPath path = fragmentRoot.getSourceAttachmentPath();
	////				  if(path != null) {
	////					  binSourceAttachmentPaths.add(path);
	////					  IPath srcPath = fragmentRoot.getSourceAttachmentPath();
	////					  IPath srcRootPath = fragmentRoot.getSourceAttachmentRootPath();
	////					  IFolder srcFolder = javaProject.getProject().getFolder(srcPath);
	////					  if(srcFolder.exists()) {
	////						  
	////					  }
	////					  IPackageFragment fragment = fragmentRoot.getPackageFragment(TextUtil.getPackagePart(fqClassName));
	////					  IClassFile file = fragment.getClassFile(TextUtil.getClassPart(fqClassName) + ".class"); //$NON-NLS-1$
	////						if (file.exists()) {
	////							
	////						}
	////					  PackageFragmentRootSourceContainer pfrsc = new PackageFragmentRootSourceContainer(fragmentRoot);
	////					  
	////					  Object[] srcElements = pfrsc.findSourceElements(fqClassName);
	////					  for(Object obj : srcElements) {
	////						 if(obj instanceof IFile){
	////							 
	////						 }
	////						 System.out.println(obj);
	////					  }
	////				  }
	//			  }
	//			}
	//		} catch (JavaModelException e) {
	//			e.printStackTrace();
	//		} 
	//		
	//		//look up source folders 
	//		for(IPackageFragmentRoot sourceRoot : sourceRoots) {
	//			String srcFolderName = MyxCodeGenerator.getSrcRelativePathString(sourceRoot);
	//		
	//			IContainer sourceContainer = (srcFolderName.length() > 0) 
	//				? javaProject.getProject().getFolder(new Path(srcFolderName))
	//				: javaProject.getProject();
	//		
	//			if(openEditor(sourceContainer, fqClassName)) {
	//				//success
	//				return;
	//			}
	//		}
	//		
	//
	//		
	//		ConsoleDisplayMgr.printlnError("No file is found for " + fqClassName);
	//	}

	//	private boolean openEditor(IContainer sourceContainer, String fqClassName) {
	//		if(sourceContainer == null || fqClassName == null) {
	//			return false;
	//		}
	//		String packageName = TextUtil.getPackagePart(fqClassName);
	//		String className = TextUtil.getClassPart(fqClassName);
	//		
	//		// looks for the folder corresponding to the package
	//		StringTokenizer stringTokenizer = new StringTokenizer(packageName, ".");
	//		while (stringTokenizer.hasMoreElements()) {
	//			String folderName = stringTokenizer.nextToken();
	//			sourceContainer = sourceContainer.getFolder(new Path(folderName));
	//			if (!sourceContainer.exists()) {
	//				//no class to open
	//				return false;
	//			}
	//		}
	//
	//		// looks for the java file
	//		IFile targetFile = sourceContainer.getFile(new Path(className + ".java"));
	//		if (targetFile.exists()) {
	//			// open the editor
	//			IWorkbenchPage page = PlatformUI.getWorkbench()
	//					.getActiveWorkbenchWindow().getActivePage();
	//			try {
	//				IDE.openEditor(page, targetFile, true);
	//				return true;
	//			} catch (PartInitException e) {
	//				e.printStackTrace();
	//				ConsoleDisplayMgr.printlnError(e.getMessage());
	//			}
	//		} 
	//		
	//		return false;
	//	}
}
