package edu.uci.isr.archstudio4.comp.filetracker;


import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;

import edu.uci.isr.archstudio4.util.EclipseUtils;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;



public class FileTrackerView extends ViewPart implements XArchFileListener, XArchFlatListener{
	private FileTrackerMyxComponent comp = null;
	private MyxRegistry er = MyxRegistry.getSharedInstance();
	
	private TreeViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	protected XArchFlatInterface xarch = null;
	
	public FileTrackerView(){
		comp = (FileTrackerMyxComponent)er.waitForBrick(FileTrackerMyxComponent.class);
		er.map(comp, this);
		xarch = comp.xarch;
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		refreshView();
	}
	
	public void refreshView(){
		SWTWidgetUtils.async(viewer, new Runnable(){
			public void run(){
				viewer.refresh();
			}
		});
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent){
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		/*
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		*/
	}
	
	class ViewContentProvider implements ITreeContentProvider{
		private Object[] EMPTY_ARRAY = new Object[0];
		
		public Object[] getElements(Object inputElement){
			return getChildren(inputElement);
		}
		
		public Object[] getChildren(Object parentElement){
			if(parentElement instanceof IViewSite){
				IProject[] openArchStudioProjects = EclipseUtils.getOpenArchStudioProjects();
				return openArchStudioProjects;
			}
			return EMPTY_ARRAY;
		}
		
		public Object getParent(Object element){
			return null;
		}
		
		public boolean hasChildren(Object element){
			if(element instanceof IViewSite){
				return true;
			}
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			//refreshView();
		}
		
		public void dispose(){
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ILabelProvider{
		
		public Image getImage(Object element){
			return null;
		}
		
		public String getText(Object element){
			if(element instanceof IProject){
				return ((IProject)element).getName();
			}
			return super.getText(element);
		}
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	


}
