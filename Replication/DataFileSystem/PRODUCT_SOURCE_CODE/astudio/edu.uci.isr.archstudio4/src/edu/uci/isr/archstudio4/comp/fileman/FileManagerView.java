package edu.uci.isr.archstudio4.comp.fileman;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class FileManagerView
	extends ViewPart
	implements XArchFileListener, XArchFlatListener{

	private FileManagerMyxComponent comp = null;
	private MyxRegistry er = MyxRegistry.getSharedInstance();

	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	protected XArchFlatInterface xarch = null;

	public FileManagerView(){
		comp = (FileManagerMyxComponent)er.waitForBrick(FileManagerMyxComponent.class);
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
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent){
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu(){
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener(){

			public void menuAboutToShow(IMenuManager manager){
				FileManagerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars(){
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager){
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager){
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager){
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions(){
		action1 = new Action(){

			@Override
			public void run(){
				xarch.createXArch("urn:arch" + System.currentTimeMillis());
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action(){

			@Override
			public void run(){
				FileDialog fd = new FileDialog(getSite().getShell(), SWT.OPEN);
				fd.setFilterExtensions(new String[]{"*.xml"});
				fd.setFilterNames(new String[]{"XML Files (*.xml)"});
				fd.open();
				String filePath = fd.getFilterPath();
				String fileName = fd.getFileName();

				if(fileName != null){
					try{
						xarch.parseFromFile(filePath + java.io.File.separator + fileName);
					}
					catch(Exception e){
						showMessage(e.getMessage());
					}
				}
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action(){

			@Override
			public void run(){
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction(){
		viewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event){
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message){
		MessageDialog.openInformation(viewer.getControl().getShell(), "Sample View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus(){
		viewer.getControl().setFocus();
	}

	class ViewContentProvider
		implements IStructuredContentProvider{

		public void inputChanged(Viewer v, Object oldInput, Object newInput){
		}

		public void dispose(){
		}

		public Object[] getElements(Object parent){
			//return new String[] { "One", "Two", "Three" };
			return xarch.getOpenXArchURIs();
		}
	}

	class ViewLabelProvider
		extends LabelProvider
		implements ITableLabelProvider{

		public String getColumnText(Object obj, int index){
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index){
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj){
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter
		extends ViewerSorter{
	}

}