package edu.uci.isr.archstudio4.comp.changesetsviewer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditor;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class ChangeSetsViewerEditor extends AbstractArchstudioEditor
	implements XArchChangeSetListener, ISelectionListener{

	public static final String[] COLUMN_NAMES = new String[]{"Name", "Value"};

	TreeViewer changeSetsTreeViewer;

	protected ChangeSetsDisplayContentProvider changeSetsDisplayContentProvider;

	IChangeSetADT csadt;

	IChangeSetSync cssync;

	public ChangeSetsViewerEditor(){
		super(ChangeSetsViewerMyxComponent.class, ChangeSetsViewerMyxComponent.EDITOR_NAME);
		csadt = ((ChangeSetsViewerMyxComponent)comp).getChangeSetADT();
		cssync = ((ChangeSetsViewerMyxComponent)comp).getChangeSetSync();
		setBannerInfo(((ChangeSetsViewerMyxComponent)comp).getIcon(), "ChangeSets Display");
		setHasBanner(true);
		setUpdateEditorOnXArchFlatEvent(false);
		setUpdateOnSelectionChange(false);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
		super.init(site, input);
	}

	@Override
	protected AbstractArchstudioOutlinePage createOutlinePage(){
		return new ChangeSetsViewerOutlinePage(xarch, xArchRef, resources);
	}

	@Override
	public void createEditorContents(Composite parent){

		changeSetsTreeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		changeSetsTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		changeSetsTreeViewer.setUseHashlookup(true);
		changeSetsTreeViewer.setContentProvider(changeSetsDisplayContentProvider = new ChangeSetsDisplayContentProvider((XArchChangeSetInterface)xarch, xArchRef, csadt));
		changeSetsTreeViewer.setLabelProvider(new ChangeSetsDisplayLabelProvider(changeSetsTreeViewer, (XArchChangeSetInterface)xarch, csadt, cssync));

		Tree tree = changeSetsTreeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TreeColumn column;
		column = new TreeColumn(tree, SWT.LEFT);
		column.setResizable(true);
		column.setImage(null);
		column.setText("Tree Structure");

		for(int i = 0, n = tree.getColumnCount(); i < n; i++){
			tree.getColumn(i).pack();
		}
		tree.pack();

		updateColumns();
		parent.layout(true, true);

		//getSite().setSelectionProvider(changeSetsTreeViewer);
		//getEditorSite().getPage().addSelectionListener(this);

		changeSetsTreeViewer.setInput(xArchRef);
	}

	public synchronized void handleXArchChangeSetEvent(XArchChangeSetEvent evt){
		final XArchChangeSetEvent event = evt;
		SWTWidgetUtils.async(changeSetsTreeViewer, new Runnable(){

			public synchronized void run(){
				if(event.getEventType() == XArchChangeSetEvent.ChangeSetEventType.UPDATED_APPLIED_CHANGE_SETS){
					updateColumns();
				}
			}
		});
	}

	private void updateColumns(){
		Tree tree = changeSetsTreeViewer.getTree();
		Map<Object, TreeColumn> doomedColumns = new HashMap<Object, TreeColumn>();
		for(TreeColumn column: tree.getColumns()){
			doomedColumns.put(column.getData(ChangeSetsViewerConstants.T_CHANGE_SET_ID), column);
		}
		doomedColumns.remove(null);

		Map<Object, Integer> columnOrder = new HashMap<Object, Integer>();

		if(this.xArchRef != null){
			XArchChangeSetInterface xArchCS = (XArchChangeSetInterface)xarch;

			ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
			ObjRef archChangeSets = xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);

			if(archChangeSets != null){
				for(ObjRef changeSetRef: xArchCS.getAppliedChangeSetRefs(xArchRef)){
					String currentID = (String)xarch.get(changeSetRef, "Id");
					ObjRef desRef = (ObjRef)xarch.get(changeSetRef, "Description");
					String description = (String)xarch.get(desRef, "Value");
					TreeColumn column = doomedColumns.remove(currentID);
					if(column == null){
						column = new TreeColumn(tree, SWT.LEFT);
						column.setData(ChangeSetsViewerConstants.T_CHANGE_SET_ID, currentID);
						column.setResizable(true);
						column.setImage(null);
						column.setMoveable(false);
					}
					columnOrder.put(column, columnOrder.size());
					column.setText(description);
					column.pack();
				}
			}
		}

		for(Map.Entry<Object, TreeColumn> entry: doomedColumns.entrySet()){
			entry.getValue().dispose();
		}

		tree.layout();

		TreeColumn[] columns = tree.getColumns();
		int[] order = new int[columns.length];
		int skippedColumns = 1;
		for(int i = 0; i < order.length; i++){
			if(i < skippedColumns){
				order[i] = i;
			}
			else{
				order[columnOrder.get(columns[i]) + skippedColumns] = i;
			}
		}
		tree.setColumnOrder(order);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection){

		//		if(selection instanceof IStructuredSelection){
		//			Object element = ((IStructuredSelection)selection).getFirstElement();
		//
		//			ArrayList<TreeNode> selectedNodes = new ArrayList();
		//
		//			if(element instanceof ObjRef){
		//				Object[] selectionArray = ((IStructuredSelection)selection).toArray();
		//				for(Object element2: selectionArray){
		//					if(element2 instanceof ObjRef){
		//						ObjRef ref = (ObjRef)element2;
		//						TreeNode node = searchTree(ref);
		//						if(node != null){
		//							selectedNodes.add(node);
		//						}
		//					}
		//				}
		//				if(selectedNodes != null && selectedNodes.size() > 0){
		//					StructuredSelection structuredSelection = new StructuredSelection(selectedNodes);
		//					//changeSetsTreeViewer.setSelection(structuredSelection, true);
		//				}
		//			}
		//		}
	}
}
