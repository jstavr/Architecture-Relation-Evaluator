package edu.uci.isr.archstudio4.comp.archedit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent.ExplicitEventType;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.archstudio4.util.EclipseUtils;
import edu.uci.isr.archstudio4.utils.IHasObjRef;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchInstanceMetadata;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchMetadataUtils;

public class ArchEditOutlinePage
    extends AbstractArchstudioOutlinePage
    implements ISelectionListener{

	protected boolean showIDs = false;
	protected boolean showDescriptions = true;
	protected boolean showObjRefs = false;

	protected XArchChangeSetInterface xarch;
	protected IExplicitADT explicit;

	public ArchEditOutlinePage(XArchChangeSetInterface xarch, ObjRef xArchRef, IResources resources, IExplicitADT explicit){
		super(xarch, xArchRef, resources, true, true);
		this.xarch = xarch;
		this.explicit = explicit;
	}

	@Override
	public void createControl(Composite parent){
		super.createControl(parent);
		if(xArchRef == null){
			return;
		}
		setupDoubleClick();
		setupDragAndDrop();
	}

	@Override
	protected ITreeContentProvider createViewContentProvider(){
		return new ViewContentProvider();
	}

	@Override
	protected ILabelProvider createViewLabelProvider(){
		return new ViewLabelProvider();
	}

	protected void setupDoubleClick(){
		getTreeViewer().addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event){
				ObjRef[] selectedRefs = getSelectedRefs();
				if(selectedRefs.length == 1){
					ObjRef ref = selectedRefs[0];
					if(xarch.isInstanceOf(ref, "instance#XMLLink")){
						ObjRef targetRef = XadlUtils.resolveXLink(xarch, ref);
						if(targetRef != null){
							ObjRef targetXArchRef = xarch.getXArch(targetRef);
							if(targetXArchRef.equals(xarch.getXArch(ref))){
								focusEditor(ArchEditMyxComponent.EDITOR_NAME, new ObjRef[]{targetRef});
							}
							else{
								EclipseUtils.openEditor(ArchEditMyxComponent.ECLIPSE_EDITOR_ID, new Path(xarch.getXArchURI(targetXArchRef)));
							}
						}
					}
				}
			}
		});
	}

	protected void setupDragAndDrop(){
		DropTarget target = new DropTarget(getTreeViewer().getTree(), DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		target.setTransfer(new Transfer[]{textTransfer});

		target.addDropListener(new DropTargetListener(){

			public void dragEnter(DropTargetEvent event){
				if(event.detail == DND.DROP_DEFAULT){
					if((event.operations & DND.DROP_COPY) != 0){
						event.detail = DND.DROP_COPY;
					}
					else{
						event.detail = DND.DROP_NONE;
					}
				}
			}

			public void dragOver(DropTargetEvent event){
				if(textTransfer.isSupportedType(event.currentDataType)){
					Object o = textTransfer.nativeToJava(event.currentDataType);
					if(o != null){
						String t = (String)o;
						if(t.startsWith("$$OBJREF$$")){
							event.feedback = DND.FEEDBACK_SCROLL;
						}
					}
				}
				event.feedback = DND.FEEDBACK_NONE;
			}

			public void dragOperationChanged(DropTargetEvent event){
				if(event.detail == DND.DROP_DEFAULT){
					if((event.operations & DND.DROP_COPY) != 0){
						event.detail = DND.DROP_COPY;
					}
					else{
						event.detail = DND.DROP_NONE;
					}
				}
			}

			public void dragLeave(DropTargetEvent event){
			}

			public void dropAccept(DropTargetEvent event){
			}

			public void drop(DropTargetEvent event){
				if(textTransfer.isSupportedType(event.currentDataType)){
					String text = (String)event.data;
					if(text.startsWith("$$OBJREF$$")){
						String uid = text.substring("$$OBJREF$$".length());
						ObjRef ref = new ObjRef(uid);

						if(event.item != null && event.item instanceof TreeItem){
							Object data = ((TreeItem)event.item).getData();
							if(data != null && data instanceof ArchEditNode){
								ObjRef targetRef = ((ArchEditNode)data).getRef();
								String id = XadlUtils.getID(xarch, targetRef);
								try{
									xarch.set(ref, "href", "#" + id);
									xarch.set(ref, "type", "simple");
								}
								catch(Exception e){
								}
							}
						}
					}
				}
			}
		});
	}

	class ViewContentProvider
	    implements ITreeContentProvider{

		private final Object[] EMPTY_ARRAY = new Object[0];

		ArchEditNode rootNode = null;

		public Object[] getElements(Object inputElement){
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement){
			if(parentElement instanceof IWorkbenchSite){
				if(rootNode == null){
					rootNode = new ArchEditNode(xArchRef);
				}
				return new Object[]{rootNode};
			}
			else if(parentElement instanceof ArchEditNode){
				return ((ArchEditNode)parentElement).getChildren();
			}
			return EMPTY_ARRAY;
		}

		public Object getParent(Object element){
			return null;
		}

		public boolean hasChildren(Object element){
			if(element instanceof IWorkbenchSite){
				return true;
			}
			else if(element instanceof ArchEditNode){
				return ((ArchEditNode)element).hasChildren();
			}
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		}

		public void dispose(){
		}

		public ArchEditNode getRootNode(){
			return rootNode;
		}
	}

	class ViewLabelProvider
	    extends LabelProvider
	    implements ILabelProvider, IColorProvider{

		@Override
		public Image getImage(Object element){
			if(element instanceof ArchEditNode){
				return ChangeSetUtils.getOverlayImageIcon((XArchChangeSetInterface)xarch, explicit, ((ArchEditNode)element).getImage(), ((ArchEditNode)element).getRef());
			}
			return null;
		}

		@Override
		public String getText(Object element){
			return super.getText(element);
		}
		
		public Color getBackground(Object element) {
		    return null;
		}
		
		public Color getForeground(Object element) {
			if(element instanceof ArchEditNode){
				ObjRef objRef = ((ArchEditNode)element).ref;
				ObjRef xArchRef = xarch.getXArch(objRef);
				ObjRef[] changeSetRefs = explicit.getExplicitChangeSetRefs(xArchRef);
				ChangeStatus changeStatus = xarch.getChangeStatus(objRef, changeSetRefs);
				switch(changeStatus){
				case ADDED:
					return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
				case MODIFIED:
					return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_MAGENTA);
				case REMOVED:
					return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED);
				case UNADDED:
					return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);
				case DETACHED:
				case UNMODIFIED:
					break;
				}
			}
		    return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
		}
	}

	public ObjRef[] getSelectedRefs(){
		Object[] selectedObjects = getSelectedObjects();
		ObjRef[] refs = new ObjRef[selectedObjects.length];
		for(int i = 0; i < selectedObjects.length; i++){
			refs[i] = ((ArchEditNode)selectedObjects[i]).getRef();
		}
		return refs;
	}

	protected class ArchEditNode implements IHasObjRef{

		protected ObjRef ref;
		protected IXArchTypeMetadata typeMetadata = null;
		protected IXArchInstanceMetadata instanceMetadata = null;
		protected IXArchPropertyMetadata[] properties = null;

		public ArchEditNode(ObjRef ref){
			this.ref = ref;
			typeMetadata = xarch.getTypeMetadata(ref);
			instanceMetadata = xarch.getInstanceMetadata(ref);
			if(typeMetadata != null){
				properties = typeMetadata.getProperties();
			}
		}

		public ObjRef getRef(){
			return ref;
		}

		@Override
		public boolean equals(Object o){
			if(o == this){
				return true;
			}
			if(o instanceof ArchEditNode){
				if(((ArchEditNode)o).ref.equals(ref)){
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode(){
			return ref.hashCode();
		}

		@Override
		public String toString(){
			StringBuffer sb = new StringBuffer();
			sb.append(SystemUtils.capFirst(xarch.getElementName(ref)));
			if(showIDs && !showDescriptions){
				String id = XadlUtils.getID(xarch, ref);
				if(id != null){
					sb.append(" [");
					sb.append(id);
					sb.append("]");
				}
			}
			else if(showDescriptions && !showIDs){
				String desc = XadlUtils.getDescription(xarch, ref);
				if(desc != null){
					sb.append(" [");
					sb.append(desc);
					sb.append("]");
				}
			}
			else if(showDescriptions && showIDs){
				String id = XadlUtils.getID(xarch, ref);
				String desc = XadlUtils.getDescription(xarch, ref);
				if(id != null || desc != null){
					if(id == null){
						id = "(No ID)";
					}
					if(desc == null){
						desc = "(No Description)";
					}
					sb.append(" [");
					sb.append(id);
					sb.append("; ");
					sb.append(desc);
					sb.append("]");
				}
			}
			if(showObjRefs){
				sb.append(" <" + ref + ">");
			}
			return sb.toString();
		}

		public ArchEditNode[] getChildren(){
			List<ArchEditNode> l = new ArrayList<ArchEditNode>();
			for(IXArchPropertyMetadata element: properties){
				if(element.getMetadataType() == IXArchPropertyMetadata.ELEMENT){
					String eltName = element.getName();
					ObjRef childRef = (ObjRef)xarch.get(ref, eltName);
					if(childRef != null){
						l.add(new ArchEditNode(childRef));
					}
				}
				else if(element.getMetadataType() == IXArchPropertyMetadata.ELEMENT_MANY){
					String eltName = element.getName();
					ObjRef[] childRefs = xarch.getAll(ref, eltName);
					for(ObjRef element2: childRefs){
						l.add(new ArchEditNode(element2));
					}
				}
			}
			ObjRef[] changeSetRefs = explicit.getExplicitChangeSetRefs(xArchRef);
			for(Iterator<ArchEditNode> i = l.iterator(); i.hasNext();){
				ArchEditNode node = i.next();
				ChangeStatus changeStatus = xarch.getChangeStatus(node.ref, changeSetRefs);
				switch(changeStatus){
				case ADDED:
				case MODIFIED:
				case REMOVED:
				case UNADDED:
				case UNMODIFIED:
					break;
				case DETACHED:
					i.remove();
				}
			}
			return l.toArray(new ArchEditNode[l.size()]);
		}

		public boolean hasChildren(){
			for(IXArchPropertyMetadata element: properties){
				if(element.getMetadataType() == IXArchPropertyMetadata.ELEMENT){
					return true;
				}
				if(element.getMetadataType() == IXArchPropertyMetadata.ELEMENT_MANY){
					return true;
				}
			}
			return false;
		}

		public Image getImage(){
			if(hasChildren()){
				return resources.getPlatformImage(ISharedImages.IMG_OBJ_FOLDER);
			}
			else{
				return resources.getPlatformImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
		}
	}

	@Override
	protected IAction[] createPulldownMenuItems(){
		Action showIDAction = new Action("Show IDs", Action.AS_CHECK_BOX){

			@Override
			public void run(){
				Object[] expandedElements = getTreeViewer().getExpandedElements();
				showIDs = isChecked();
				getTreeViewer().refresh(true);
				getTreeViewer().setExpandedElements(expandedElements);
			}
		};
		showIDAction.setChecked(showIDs);
		showIDAction.setText("Show IDs");
		showIDAction.setToolTipText("Show IDs with elements");

		Action showDescAction = new Action("Show Descriptions", Action.AS_CHECK_BOX){

			@Override
			public void run(){
				Object[] expandedElements = getTreeViewer().getExpandedElements();
				showDescriptions = isChecked();
				getTreeViewer().refresh(true);
				getTreeViewer().setExpandedElements(expandedElements);
			}
		};
		showDescAction.setChecked(showDescriptions);
		showDescAction.setText("Show Descriptions");
		showDescAction.setToolTipText("Show Descriptions with elements");

		Action showObjRefAction = new Action("Show ObjRefs", Action.AS_CHECK_BOX){

			@Override
			public void run(){
				Object[] expandedElements = getTreeViewer().getExpandedElements();
				showObjRefs = isChecked();
				getTreeViewer().refresh(true);
				getTreeViewer().setExpandedElements(expandedElements);
			}
		};
		showObjRefAction.setChecked(showObjRefs);
		showObjRefAction.setText("Show ObjRefs");
		showObjRefAction.setToolTipText("Show ObjRefs with elements");

		return new Action[]{showIDAction, showDescAction, showObjRefAction};
	}

	@Override
	protected void fillContextMenu(IMenuManager menuMgr){
		ObjRef[] selectedRefs = getSelectedRefs();
		if(selectedRefs.length == 0){
			Action noAction = new Action("[No Selection]"){

				@Override
				public void run(){
				}
			};
			noAction.setEnabled(false);
			menuMgr.add(noAction);
		}
		else if(selectedRefs.length > 1){
			Action noAction = new Action("[Select One Node for Menu]"){

				@Override
				public void run(){
				}
			};
			noAction.setEnabled(false);
			menuMgr.add(noAction);
		}
		else{
			final ObjRef ref = selectedRefs[0];
			final ObjRef parent = xarch.getParent(ref);
			if(parent == null){
				//This is the root node
				IAction[] actions = createContextMenuRootNodeAddActions(ref);
				if(actions.length == 0){
					Action noAction = new Action("[No Actions]"){

						@Override
						public void run(){
						}
					};
					noAction.setEnabled(false);
					menuMgr.add(noAction);
				}
				for(IAction element: actions){
					menuMgr.add(element);
				}
			}
			else{
				String name = xarch.getElementName(ref);
				if(xarch.getXArch(parent).equals(parent)){
					name = "object";
				}
				final IXArchPropertyMetadata type = xarch.getTypeMetadata(parent).getProperty(name);
				if(type != null && type.getMaxOccurs() > 1){
					menuMgr.add(new Action("Duplicate"){

						@Override
						public void run(){
							xarch.add(parent, type.getName(), xarch.cloneElement(ref, 100)); // TOOD use static variable
						}
					});
					menuMgr.add(new Separator());
				}
				menuMgr.add(createContextMenuRemoveAction(ref));
				menuMgr.add(new Separator());

				IAction[] actions = createContextMenuAddActions(ref);
				if(actions.length == 0){
					Action noAction = new Action("[No Actions]"){

						@Override
						public void run(){
						}
					};
					noAction.setEnabled(false);
					menuMgr.add(noAction);
				}
				for(IAction element: actions){
					menuMgr.add(element);
				}

				menuMgr.add(new Separator());

				actions = createContextMenuPromoteActions(ref);
				if(actions.length == 0){
					Action noAction = new Action("[No Promotions]"){

						@Override
						public void run(){
						}
					};
					noAction.setEnabled(false);
					menuMgr.add(noAction);
				}
				for(IAction element: actions){
					menuMgr.add(element);
				}

			}
		}
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public IAction createContextMenuRemoveAction(ObjRef ref){
		final ObjRef fref = ref;
		Action removeAction = new Action("Remove"){

			@Override
			public void run(){
				ObjRef parentRef = xarch.getParent(fref);
				String eltName = xarch.getElementName(fref);
				if(parentRef.equals(xarch.getXArch(parentRef))){
					eltName = "Object";
				}
				switch(xarch.getTypeMetadata(parentRef).getProperty(eltName).getMetadataType()){
				case IXArchPropertyMetadata.ATTRIBUTE:
					break;
				case IXArchPropertyMetadata.ELEMENT:
					xarch.clear(parentRef, eltName);
					break;
				case IXArchPropertyMetadata.ELEMENT_MANY:
					xarch.remove(parentRef, eltName, fref);
					break;
				}

				Object[] expandedElements = getTreeViewer().getExpandedElements();
				getTreeViewer().refresh(true);
				getTreeViewer().setExpandedElements(expandedElements);
			}
		};
		return removeAction;
	}

	public IAction[] createContextMenuRootNodeAddActions(ObjRef ref){
		List<IAction> actions = new ArrayList<IAction>();
		final ObjRef fref = ref;

		for(String type: XArchMetadataUtils.getTopLevelElements(xarch)){
			final IXArchTypeMetadata typeMetadata = xarch.getTypeMetadata(type);
			final String typePackage = XArchMetadataUtils.getTypeContext(typeMetadata.getType());
			final String typeName = XArchMetadataUtils.getTypeName(typeMetadata.getType());

			Action addEltAction = new Action("Add " + SystemUtils.capFirst(typeName)){

				@Override
				public void run(){
					ObjRef xArchRef = xarch.getXArch(fref);
					ObjRef contextRef = xarch.createContext(xArchRef, typePackage);
					ObjRef newRef = xarch.createElement(contextRef, typeName);
					xarch.add(fref, "Object", newRef);

					Object[] expandedElements = getTreeViewer().getExpandedElements();
					getTreeViewer().refresh(true);
					getTreeViewer().setExpandedElements(expandedElements);
				}
			};
			actions.add(addEltAction);
		}
		return actions.toArray(new IAction[actions.size()]);
	}

	public IAction[] createContextMenuAddActions(ObjRef ref){
		List<IAction> actions = new ArrayList<IAction>();
		IXArchTypeMetadata typeMetadata = xarch.getTypeMetadata(ref);
		final ObjRef fref = ref;

		for(IXArchPropertyMetadata propMetadata: typeMetadata.getProperties()){

			final String typePackage = XArchMetadataUtils.getTypeContext(propMetadata.getType());
			final String typeName = XArchMetadataUtils.getTypeName(propMetadata.getType());
			final String propName = propMetadata.getName();
			final int propType = propMetadata.getMetadataType();

			if(propType == IXArchPropertyMetadata.ELEMENT){
				Action addEltAction = new Action("Add " + SystemUtils.capFirst(propName)){

					@Override
					public void run(){
						ObjRef xArchRef = xarch.getXArch(fref);
						ObjRef contextRef = xarch.createContext(xArchRef, typePackage);
						ObjRef newRef = xarch.create(contextRef, typeName);
						xarch.set(fref, propName, newRef);

						Object[] expandedElements = getTreeViewer().getExpandedElements();
						getTreeViewer().refresh(true);
						getTreeViewer().setExpandedElements(expandedElements);
					}
				};
				if(xarch.get(ref, propName) != null){
					addEltAction.setEnabled(false);
				}
				actions.add(addEltAction);
			}
			else if(propType == IXArchPropertyMetadata.ELEMENT_MANY){
				Action addEltAction = new Action("Add " + SystemUtils.capFirst(propName)){

					@Override
					public void run(){
						ObjRef xArchRef = xarch.getXArch(fref);
						ObjRef contextRef = xarch.createContext(xArchRef, typePackage);
						ObjRef newRef = xarch.create(contextRef, typeName);
						xarch.add(fref, propName, newRef);

						Object[] expandedElements = getTreeViewer().getExpandedElements();
						getTreeViewer().refresh(true);
						getTreeViewer().setExpandedElements(expandedElements);
					}
				};
				actions.add(addEltAction);
			}
		}

		return actions.toArray(new IAction[actions.size()]);
	}

	public IAction[] createContextMenuPromoteActions(ObjRef ref){
		List<IAction> actions = new ArrayList<IAction>();
		final IXArchTypeMetadata refTypeMetadata = xarch.getTypeMetadata(ref);
		final ObjRef fref = ref;

		for(String type: XArchMetadataUtils.getAvailablePromotions(xarch, refTypeMetadata.getType())){
			final IXArchTypeMetadata typeMetadata = xarch.getTypeMetadata(type);
			final String typePackage = XArchMetadataUtils.getTypeContext(typeMetadata.getType());
			final String typeName = XArchMetadataUtils.getTypeName(typeMetadata.getType());

			Action addEltAction = new Action("Promote to " + SystemUtils.capFirst(typeName)){

				@Override
				public void run(){
					ObjRef xArchRef = xarch.getXArch(fref);

					for(String type: XArchMetadataUtils.getPromotionPathTypes(xarch, refTypeMetadata.getType(), typeMetadata.getType())){
						IXArchTypeMetadata tm = xarch.getTypeMetadata(type);
						ObjRef contextRef = xarch.createContext(xArchRef, XArchMetadataUtils.getTypeContext(tm.getType()));
						xarch.promoteTo(contextRef, XArchMetadataUtils.getTypeName(tm.getType()), fref);
					}

					Object[] expandedElements = getTreeViewer().getExpandedElements();
					getTreeViewer().refresh(true);
					getTreeViewer().setExpandedElements(expandedElements);
				}
			};
			actions.add(addEltAction);
		}
		return actions.toArray(new IAction[actions.size()]);
	}

	@Override
	public void focusEditor(String editorName, ObjRef[] refs){
		if(refs.length > 0){
			ArchEditNode[] nodes = new ArchEditNode[refs.length];
			for(int i = 0; i < refs.length; i++){
				nodes[i] = new ArchEditNode(refs[i]);
			}
			//Expand the ancestors of the selected items
			for(int i = 0; i < nodes.length; i++){
				ObjRef[] ancestors = xarch.getAllAncestors(refs[i]);
				for(int j = ancestors.length - 1; j >= 1; j--){
					ArchEditNode ancestorNode = new ArchEditNode(ancestors[j]);
					getTreeViewer().expandToLevel(ancestorNode, 1);
				}
			}
			IStructuredSelection ss = new StructuredSelection(nodes);
			getTreeViewer().setSelection(ss, true);
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection){
		TreeViewer treeViewer = this.getTreeViewer();
		if(treeViewer != null){
			if(selection instanceof IStructuredSelection){
				if(((IStructuredSelection)selection).getFirstElement() instanceof ArchEditNode){
					return;
				}
				Object[] selectionArray = ((IStructuredSelection)selection).toArray();
				if(selectionArray != null && selectionArray.length > 0){
					List<ArchEditNode> selectedNodes = new ArrayList<ArchEditNode>();
					for(Object selectedObject: selectionArray){
						if(selectedObject instanceof ObjRef){
							if(!selection.equals(getSelection())){
								ViewContentProvider contentProvider = (ViewContentProvider)getTreeViewer().getContentProvider();
								if(contentProvider != null){
									ArchEditNode rootNode = contentProvider.getRootNode();
									if(rootNode != null){
										List<ArchEditNode> treePath = new ArrayList<ArchEditNode>();
										ArchEditNode nodeFound = searchTree(rootNode, (ObjRef)selectedObject, treePath);
										if(nodeFound != null){
											selectedNodes.add(nodeFound);
											for(ArchEditNode node: treePath){
												treeViewer.setExpandedState(node, true);
											}
										}
									}
								}
							}
						}
					}
					if(selectedNodes.size() > 0){
						treeViewer.setSelection(new StructuredSelection(selectedNodes), true);
					}
				}
			}
		}
	}

	public ArchEditNode searchTree(ArchEditNode node, ObjRef ref, List<ArchEditNode> treePath){
		treePath.add(node);
		if(node.getRef().equals(ref)){
			return node;
		}

		ArchEditNode[] children = node.getChildren();
		for(ArchEditNode child: children){
			ArchEditNode searchNode = searchTree(child, ref, treePath);
			if(searchNode != null){
				return searchNode;
			}
			else{
				treePath.remove(child);
			}
		}
		treePath.remove(node);
		return null;
	}

	public void handleExplicitEvent(ExplicitADTEvent evt){
		if(evt.getEventType() == ExplicitEventType.UPDATED_EXPLICIT_CHANGE_SETS){
			final TreeViewer treeViewer = getTreeViewer();
			if(treeViewer != null){
				SWTWidgetUtils.async(treeViewer.getControl(), new Runnable(){

					public void run(){
						treeViewer.refresh();
					}
				});
			}
		}
	}
}
