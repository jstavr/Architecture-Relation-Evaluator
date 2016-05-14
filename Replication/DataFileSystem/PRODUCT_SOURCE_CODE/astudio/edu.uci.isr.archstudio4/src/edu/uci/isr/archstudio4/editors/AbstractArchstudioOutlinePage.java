package edu.uci.isr.archstudio4.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.widgets.swt.IMenuFiller;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public abstract class AbstractArchstudioOutlinePage extends ContentOutlinePage
	implements FocusEditorListener{

	protected XArchFlatInterface xarch;
	protected IResources resources = null;

	protected ObjRef xArchRef = null;

	protected boolean hasPulldownMenu = false;
	protected boolean hasContextMenu = false;

	protected TreeViewer viewer = null;

	public AbstractArchstudioOutlinePage(XArchFlatInterface xarch, ObjRef xArchRef, IResources resources, boolean hasPulldownMenu, boolean hasContextMenu){
		this.xarch = xarch;
		this.xArchRef = xArchRef;
		this.resources = resources;
		this.hasPulldownMenu = hasPulldownMenu;
		this.hasContextMenu = hasContextMenu;
	}

	@Override
	public void init(IPageSite pageSite){
		super.init(pageSite);
	}

	@Override
	public void createControl(Composite parent){
		super.createControl(parent);
		if(xArchRef == null){
			return;
		}
		viewer = getTreeViewer();
		viewer.setContentProvider(createViewContentProvider());
		viewer.setLabelProvider(createViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		// viewer.addSelectionChangedListener(this);
		viewer.setInput(getSite());

		// Do Pulldown Menu
		if(hasPulldownMenu){
			IMenuManager menu = getSite().getActionBars().getMenuManager();
			IAction[] actions = createPulldownMenuItems();
			for(IAction element: actions){
				menu.add(element);
			}
		}

		// Do Context Menu
		if(hasContextMenu){
			SWTWidgetUtils.setupContextMenu("#PopupMenu", getTreeViewer().getControl(), getSite(), new IMenuFiller(){

				public void fillMenu(IMenuManager m){
					fillContextMenu(m);
				}
			});
		}
	}

	protected abstract ITreeContentProvider createViewContentProvider();

	protected abstract ILabelProvider createViewLabelProvider();

	protected IAction[] createPulldownMenuItems(){
		return new IAction[0];
	}

	protected void fillContextMenu(IMenuManager m){
	}

	public void updateOutlinePage(){
		if(getTreeViewer() == null){
			return;
		}
		if(getTreeViewer().getTree() == null){
			return;
		}
		if(getTreeViewer().getTree().isDisposed()){
			return;
		}
		Object[] expandedElements = getTreeViewer().getExpandedElements();
		getTreeViewer().refresh(true);
		getTreeViewer().setExpandedElements(expandedElements);
	}

	public Object[] getSelectedObjects(){
		ISelection selection = getSelection();
		if(selection instanceof IStructuredSelection){
			IStructuredSelection ss = (IStructuredSelection)selection;
			Object[] nodes = ss.toArray();
			return nodes;
		}
		else{
			return new Object[0];
		}
	}

	public abstract void focusEditor(String editorName, ObjRef[] refs);

}
