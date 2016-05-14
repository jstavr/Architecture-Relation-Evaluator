package edu.uci.isr.archstudio4.comp.fileman;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.xml.sax.SAXException;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.NoSuchObjectException;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class FileManagerMyxComponent
    extends AbstractMyxSimpleBrick
    implements XArchFileListener, XArchFlatListener, IFileManager{

	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("xarch");
	public static final IMyxName INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS = MyxUtils.createName("xarchfileevents");

	public static final IMyxName INTERFACE_NAME_IN_FILEMANAGER = MyxUtils.createName("filemanager");

	public static final IMyxName INTERFACE_NAME_OUT_FILEMANAGEREVENTS = MyxUtils.createName("filemanagerevents");

	private final MyxRegistry er = MyxRegistry.getSharedInstance();

	protected XArchFlatInterface xarch = null;
	protected IFileManagerListener fileManagerListener = null;

	protected Set<ObjRef> dirtySet = Collections.synchronizedSet(new HashSet<ObjRef>());

	//Keeps track of which tools have which documents open. When no tools have
	//a document open, it is closed in xArchADT.
	protected Map<ObjRef, List<String>> openerMap = Collections.synchronizedMap(new HashMap<ObjRef, List<String>>());

	public FileManagerMyxComponent(){
	}

	@Override
	public void begin(){
		xarch = (XArchFlatInterface)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_XARCH);
		fileManagerListener = (IFileManagerListener)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_FILEMANAGEREVENTS);
		er.register(this);
	}

	@Override
	public void end(){
		er.unregister(this);
	}

	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_FILEEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FLATEVENTS)){
			return this;
		}
		else if(interfaceName.equals(INTERFACE_NAME_IN_FILEMANAGER)){
			return this;
		}
		return null;
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof XArchFileListener){
				((XArchFileListener)element).handleXArchFileEvent(evt);
			}
		}
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		Object[] os = er.getObjects(this);
		for(Object element: os){
			if(element instanceof XArchFlatListener){
				((XArchFlatListener)element).handleXArchFlatEvent(evt);
			}
		}
		try{
			ObjRef xArchRef = xarch.getXArch(evt.getSource());
			makeDirty(xArchRef);
		}catch(NoSuchObjectException e){
		}
	}

	private static String getURI(IFile f){
		// see: http://lmap.blogspot.com/2008/03/platform-scheme-uri.html
		// return "platform:/resource" + f.getFullPath().toString();

		// this mirrors the URI returned by org.eclipse.ui.part.FileEditorInput
		return f.getLocationURI().toString();
	}

	private static String getURI(java.io.File f){
		return f.toURI().toString();
	}

	public boolean isOpen(IFile f){
		String uri = getURI(f);
		if(xarch.getOpenXArch(uri) != null){
			return true;
		}
		return false;
	}

	public ObjRef getXArch(IFile f){
		String uri = getURI(f);
		return xarch.getOpenXArch(uri);
	}

	public ObjRef openXArch(String toolID, IFile f) throws CantOpenFileException{
		String uri = getURI(f);
		ObjRef xArchRef;
		try{
			xArchRef = xarch.parseFromURL(uri);

			List<String> toolList = openerMap.get(xArchRef);
			if(toolList == null){
				openerMap.put(xArchRef, toolList = new ArrayList<String>());
			}
			toolList.add(toolID);

		}
		catch(MalformedURLException e){
			throw new CantOpenFileException("Can't open file: " + uri, e);
		}
		catch(IOException e){
			throw new CantOpenFileException("Can't open file: " + uri, e);
		}
		catch(SAXException e){
			throw new CantOpenFileException("Can't open file: " + uri, e);
		}

		return xArchRef;

		//		InputStream is = null;
		//		OutputStream os = null;
		//		String uri = null;
		//		try{
		//			uri = getURI(f);
		//			ObjRef xArchRef = xarch.getOpenXArch(uri);
		//			if(xArchRef == null){
		//				is = f.getContents();
		//				os = new ByteArrayOutputStream();
		//				SystemUtils.blt(is, os);
		//				String contents = new String(((ByteArrayOutputStream)os).toByteArray());
		//				xArchRef = xarch.parseFromString(uri, contents);
		//				contents = null;
		//			}
		//
		//			List<String> toolList = openerMap.get(xArchRef);
		//			if(toolList == null){
		//				toolList = new ArrayList<String>();
		//			}
		//			toolList.add(toolID);
		//			openerMap.put(xArchRef, toolList);
		//
		//			return xArchRef;
		//		}
		//		catch(Exception e){
		//			throw new CantOpenFileException("Can't open file: " + uri, e);
		//		}
		//		finally{
		//			try{
		//				if(is != null){
		//					is.close();
		//				}
		//			}
		//			catch(IOException e){
		//			}
		//			try{
		//				if(os != null){
		//					os.close();
		//				}
		//			}
		//			catch(IOException e2){
		//			}
		//		}
	}

	public ObjRef openXArch(String toolID, java.io.File f) throws CantOpenFileException{

		// XXX: This modification opens the files on the system running xArchADT, we'll need to figure out how to do this better at some point.

		String uri = getURI(f);
		ObjRef xArchRef;
		try{
			xArchRef = xarch.parseFromURL(uri);

			List<String> toolList = openerMap.get(xArchRef);
			if(toolList == null){
				openerMap.put(xArchRef, toolList = new ArrayList<String>());
			}
			toolList.add(toolID);

		}
		catch(MalformedURLException e){
			throw new CantOpenFileException("Can't open file: " + uri, e);
		}
		catch(IOException e){
			throw new CantOpenFileException("Can't open file: " + uri, e);
		}
		catch(SAXException e){
			throw new CantOpenFileException("Can't open file: " + uri, e);
		}

		return xArchRef;

		//		InputStream is = null;
		//		OutputStream os = null;
		//		String uri = null;
		//		try{
		//			uri = getURI(f);
		//			is = new FileInputStream(f);
		//			os = new ByteArrayOutputStream();
		//			SystemUtils.blt(is, os);
		//			String contents = new String(((ByteArrayOutputStream)os).toByteArray());
		//			ObjRef xArchRef = xarch.parseFromString(uri, contents);
		//			contents = null;
		//
		//			List<String> toolList = openerMap.get(xArchRef);
		//			if(toolList == null){
		//				toolList = new ArrayList<String>();
		//			}
		//			toolList.add(toolID);
		//			openerMap.put(xArchRef, toolList);
		//
		//			return xArchRef;
		//		}
		//		catch(Exception e){
		//			throw new CantOpenFileException("Can't open file: " + uri, e);
		//		}
		//		finally{
		//			try{
		//				if(is != null){
		//					is.close();
		//				}
		//			}
		//			catch(IOException e){
		//			}
		//			try{
		//				if(os != null){
		//					os.close();
		//				}
		//			}
		//			catch(IOException e2){
		//			}
		//		}
	}

	public void closeXArch(String toolID, ObjRef xArchRef){
		List<String> toolList = openerMap.get(xArchRef);
		if(toolList == null){
			xarch.close(xArchRef);
			return;
		}

		toolList.remove(toolID);
		if(toolList.size() == 0){
			xarch.close(xArchRef);
			openerMap.remove(xArchRef);
		}
	}

	public void makeDirty(ObjRef xArchRef){
		if(dirtySet.contains(xArchRef)){
			return;
		}
		dirtySet.add(xArchRef);
		if(fileManagerListener != null){
			fileManagerListener.fileDirtyStateChanged(xArchRef, true);
		}
	}

	public void makeClean(ObjRef xArchRef){
		if(!dirtySet.contains(xArchRef)){
			return;
		}
		dirtySet.remove(xArchRef);
		if(fileManagerListener != null){
			fileManagerListener.fileDirtyStateChanged(xArchRef, false);
		}
	}

	public boolean isDirty(ObjRef xArchRef){
		return dirtySet.contains(xArchRef);
	}

	public void save(ObjRef xArchRef, IProgressMonitor monitor){
		String uri = xarch.getXArchURI(xArchRef);
		try{
			if(monitor != null){
				monitor.beginTask("Saving File", 100);
				monitor.worked(1);
			}
			if(fileManagerListener != null){
				monitor.subTask("Notifying Editors");
				monitor.worked(2);
				try{
					fileManagerListener.fileSaving(xArchRef, monitor);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			if(monitor != null){
				monitor.worked(80);
			}

			xarch.writeToURL(xArchRef, uri);

			makeClean(xArchRef);
		}
		catch(Throwable t){
			t.printStackTrace();
			MessageDialog.openError(null, "Cannot Save File", "Failed to save file: " + uri + " " + t.getMessage());
		}
		finally{
			if(monitor != null){
				monitor.done();
			}
		}

		//		if(monitor != null){
		//			monitor.beginTask("Saving File", 100);
		//			monitor.worked(1);
		//		}
		//		if(fileManagerListener != null){
		//			monitor.subTask("Notifying Editors");
		//			monitor.worked(2);
		//			try{
		//				fileManagerListener.fileSaving(xArchRef, monitor);
		//			}
		//			catch(Exception e){
		//				e.printStackTrace();
		//			}
		//		}
		//		if(monitor != null){
		//			monitor.worked(80);
		//		}
		//
		//		String serializedXML = xarch.serialize(xArchRef);
		//		String uri = xarch.getXArchURI(xArchRef);
		//		Path filePath = new Path(uri);
		//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		//		IFile file = root.getFile(filePath);
		//		try{
		//			xarch.writeToFile(xArchRef, file.toString());
		//		}
		//		catch(IOException e){
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		InputStream source = new ByteArrayInputStream(serializedXML.getBytes());
		//		try{
		//			file.setContents(source, IFile.NONE, monitor);
		//			makeClean(xArchRef);
		//		}
		//		catch(CoreException ce){
		//			//TODO:Handle
		//		}
		//		if(monitor != null){
		//			monitor.worked(100);
		//		}
	}

	public void saveAs(ObjRef xArchRef, IFile f){
	}

}
