package edu.uci.isr.archstudio4.comp.archipelago;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchSite;

import edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.ActivityDiagramsTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.options.OptionsTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.statecharts.StatechartsTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.stateline.StatelineTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesTreePlugin;
import edu.uci.isr.archstudio4.comp.archipelago.variants.VariantsTreePlugin;
import edu.uci.isr.archstudio4.comp.booleannotation.IBooleanNotation;
import edu.uci.isr.archstudio4.comp.fileman.IFileManager;
import edu.uci.isr.archstudio4.comp.fileman.IFileManagerListener;
import edu.uci.isr.archstudio4.comp.guardtracker.IGuardTracker;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.selector.ISelector;
import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent.ExplicitEventType;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.archstudio4.graphlayout.IGraphLayout;
import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.widgets.swt.LocalSelectionTransfer;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ArchipelagoOutlinePage extends AbstractArchstudioOutlinePage implements IFileManagerListener, ExplicitADTListener {

	protected List<IArchipelagoTreePlugin> treePluginList = new ArrayList<IArchipelagoTreePlugin>();
	protected IArchipelagoTreePlugin[] treePlugins = new IArchipelagoTreePlugin[0];

	protected ArchipelagoServices AS = null;

	public ArchipelagoOutlinePage(ArchipelagoEditor editor, XArchFlatInterface xarch, ObjRef xArchRef, IResources resources, IFileManager fileman,
	        IPreferenceStore prefs, IGraphLayout graphLayout, IBooleanNotation bni, IGuardTracker guardTracker, ISelector selector,
	        XArchChangeSetInterface xarchcs, IExplicitADT explicit, IWorkbenchSite workbenchSite) {
		super(xarch, xArchRef, resources, false, true);

		IArchipelagoEventBus eventBus = new DefaultArchipelagoEventBus();
		//This stuff lets us open multiple editors on the same document.
		ArchipelagoDataCache servicesCache = ArchipelagoDataCache.getInstance();
		TreeNodeDataCache data = servicesCache.getData(xArchRef);
		if (data == null) {
			data = new TreeNodeDataCache();
		}
		AS = new ArchipelagoServices(eventBus, new DefaultArchipelagoEditorPane(editor), data, xarch, resources, fileman, prefs, graphLayout, bni,
		        guardTracker, selector, xarchcs, explicit, workbenchSite, editor.copyPasteManager);
		servicesCache.addCacheEntry(this, xArchRef, data);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		getTreeViewer().setSorter(new ViewerSorter());

		addTreePlugin(new RootTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new FolderNodeTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new StructureTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new TypesTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new OptionsTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new VariantsTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new StatechartsTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new StatelineTreePlugin(getTreeViewer(), AS, xArchRef));
		addTreePlugin(new ActivityDiagramsTreePlugin(getTreeViewer(), AS, xArchRef));

		getTreeViewer().expandToLevel(4);
		getTreeViewer().reveal(xArchRef);
		getTreeViewer().addDoubleClickListener(new DoubleClickListener());
		initDragAndDrop();

		getTreeViewer().setColumnProperties(new String[] { "treeNode" });
		getTreeViewer().setCellEditors(new CellEditor[] { new TextCellEditor(getTreeViewer().getTree()) });
		getTreeViewer().setCellModifier(new DefaultArchipelagoCellModifier());
	}

	@Override
	public void dispose() {
		super.dispose();
		ArchipelagoDataCache.getInstance().removeCacheEntry(ArchipelagoOutlinePage.this);
	}

	protected void initDragAndDrop() {
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getInstance() };
		getTreeViewer().addDragSupport(DND.DROP_LINK, transfers, new ArchipelagoOutlinePageDragSourceListener());
	}

	@Override
	protected ITreeContentProvider createViewContentProvider() {
		return new ViewContentProvider();
	}

	@Override
	protected ILabelProvider createViewLabelProvider() {
		return new ViewLabelProvider();
	}

	@Override
	public void updateOutlinePage() {
		super.updateOutlinePage();
	}

	public Object[] getSelectedNodes() {
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object[] nodes = ss.toArray();
			return nodes;
		}
		else {
			return new Object[0];
		}
	}

	@Override
	protected void fillContextMenu(IMenuManager menuMgr) {
		Object[] selectedNodes = getSelectedNodes();

		if (selectedNodes.length == 0) {
			Action noAction = new Action("[No Selection]") {

				@Override
				public void run() {
				}
			};
			noAction.setEnabled(false);
			menuMgr.add(noAction);
		}
		else {
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoTreeContextMenuFiller[] ls = treePlugin.getContextMenuFillers();
				if (ls != null) {
					for (IArchipelagoTreeContextMenuFiller element : ls) {
						element.fillContextMenu(menuMgr, selectedNodes);
					}
				}
			}
		}
		if (menuMgr.getItems() != null && menuMgr.getItems().length == 0) {
			Action noAction = new Action("[No Actions Available]") {

				@Override
				public void run() {
				}
			};
			noAction.setEnabled(false);
			menuMgr.add(noAction);
		}
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt) {

		//See if we're removing the currently editing objref;
		//if so, then clear the editor and display the default editor
		if (evt.getEventType() == XArchFlatEvent.REMOVE_EVENT) {
			BNAComposite bnaComposite = ArchipelagoUtils.getBNAComposite(AS.editor);
			if (bnaComposite != null) {
				EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(bnaComposite.getView().getWorld().getBNAModel());
				if (ept != null) {
					String editingID = (String) ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
					if (editingID != null) {
						ObjRef editingObjRef = xarch.getByID(xArchRef, editingID);
						if (editingObjRef == null || !xarch.isAttached(editingObjRef)) {
							AS.editor.clearEditor();
							AS.editor.displayDefaultEditor();
							return;
						}
					}
				}
			}
		}

		for (IArchipelagoTreePlugin treePlugin : treePlugins) {
			XArchFlatListener l = treePlugin.getXArchFlatListener();
			if (l != null) {
				l.handleXArchFlatEvent(evt);
			}
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt) {
		for (IArchipelagoTreePlugin treePlugin : treePlugins) {
			XArchFileListener l = treePlugin.getXArchFileListener();
			if (l != null) {
				l.handleXArchFileEvent(evt);
			}
		}
	}

	public void fileSaving(final ObjRef xArchRef, IProgressMonitor monitor) {
		final IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Notifying Archipelago Elements", treePlugins.length);
				for (int i = 0; i < treePlugins.length; i++) {
					IArchipelagoTreePlugin treePlugin = treePlugins[i];
					IFileManagerListener l = treePlugin.getFileManagerListener();
					if (l != null) {
						l.fileSaving(xArchRef, monitor);
					}
					monitor.worked(i);
				}
				monitor.done();
			}
		};
		final Shell s = getSite().getShell();
		SWTWidgetUtils.async(s, new Runnable() {

			public void run() {
				try {
					ProgressMonitorDialog pmd = new ProgressMonitorDialog(getSite().getShell());
					pmd.run(true, false, op);
				}
				catch (InvocationTargetException e) {
				}
				catch (InterruptedException e) {
				}
			}
		});
	}

	public void fileDirtyStateChanged(ObjRef xArchRef, boolean dirty) {
		for (IArchipelagoTreePlugin treePlugin : treePlugins) {
			IFileManagerListener l = treePlugin.getFileManagerListener();
			if (l != null) {
				l.fileDirtyStateChanged(xArchRef, dirty);
			}
		}
	}

	@Override
	public void focusEditor(String editorName, ObjRef[] refs) {
		for (IArchipelagoTreePlugin treePlugin : treePlugins) {
			IArchipelagoEditorFocuser editorFocuser = treePlugin.getEditorFocuser();
			if (editorFocuser != null) {
				editorFocuser.focusEditor(editorName, refs);
			}
		}
	}

	public void addTreePlugin(IArchipelagoTreePlugin treePlugin) {
		treePluginList.add(treePlugin);
		treePlugins = treePluginList.toArray(new IArchipelagoTreePlugin[treePluginList.size()]);
	}

	public void removeTreePlugin(IArchipelagoTreePlugin treePlugin) {
		treePluginList.remove(treePlugin);
		treePlugins = treePluginList.toArray(new IArchipelagoTreePlugin[treePluginList.size()]);
	}

	class ViewContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			Object[] children = new Object[0];
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoTreeContentProvider contentProvider = treePlugin.getContentProvider();
				if (contentProvider != null) {
					children = contentProvider.getChildren(parentElement, children);
				}
			}

			return ChangeSetUtils.filterOutDetatched((XArchChangeSetInterface) xarch, AS.explicit, children);
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object getParent(Object element) {
			Object parent = null;
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoTreeContentProvider contentProvider = treePlugin.getContentProvider();
				if (contentProvider != null) {
					parent = contentProvider.getParent(element, parent);
				}
			}
			return parent;
		}

		public boolean hasChildren(Object element) {
			boolean hasChildren = false;
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoTreeContentProvider contentProvider = treePlugin.getContentProvider();
				if (contentProvider != null) {
					hasChildren = contentProvider.hasChildren(element, hasChildren);
				}
			}
			return hasChildren;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoTreeContentProvider contentProvider = treePlugin.getContentProvider();
				if (contentProvider != null) {
					contentProvider.inputChanged(viewer, oldInput, newInput);
				}
			}
		}

		public void dispose() {
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoTreeContentProvider contentProvider = treePlugin.getContentProvider();
				if (contentProvider != null) {
					contentProvider.dispose();
				}
			}
		}
	}

	class ViewLabelProvider extends LabelProvider implements ILabelProvider {

		@Override
		public String getText(Object element) {
			String text = null;
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoLabelProvider labelProvider = treePlugin.getLabelProvider();
				if (labelProvider != null) {
					text = labelProvider.getText(element, text);
				}
			}
			if (text == null) {
				return "[Error: No Label Provider for " + element + "]";
			}
			return text;
		}

		@Override
		public Image getImage(Object element) {
			Image img = null;
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				IArchipelagoLabelProvider labelProvider = treePlugin.getLabelProvider();
				if (labelProvider != null) {
					img = labelProvider.getImage(element, img);
					if (img != null) {
						img = ChangeSetUtils.getOverlayImageIcon((XArchChangeSetInterface) xarch, AS.explicit, img, element);
					}
				}
			}
			return img;
		}
	}

	class DoubleClickListener implements IDoubleClickListener {

		public void doubleClick(DoubleClickEvent event) {
			ISelection selection = getTreeViewer().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (structuredSelection.size() != 1) {
					return;
				}
				Object o = structuredSelection.getFirstElement();
				for (IArchipelagoTreePlugin treePlugin : treePlugins) {
					IArchipelagoTreeDoubleClickHandler doubleClickHandler = treePlugin.getDoubleClickHandler();
					if (doubleClickHandler != null) {
						doubleClickHandler.treeNodeDoubleClicked(o);
					}
				}
			}
		}
	}

	/*
	 * This is a somewhat dirty hack because Eclipse's (actually JFace's) cell
	 * editing behavior is not particularly flexible. Basically, if you set a
	 * cell editor and a cell modifier the normal way, then any selection of a
	 * node will enable editing of that node. This is too permissive, it means
	 * that editors are always being activated and it doesn't feel right. What
	 * we do instead is only enable cell editing after the user has hit a
	 * context menu option (for example), and only on a particular cell for one
	 * shot. If callers (e.g., fillContextMenu) want to allow cell editing on a
	 * particular cell, they need to call ArchipelagoUtils.beginCellEditing()
	 * which will set the appropriate data element in the viewer to allow one
	 * shot of cell editing.
	 */
	class DefaultArchipelagoCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			if (element == null) {
				return false;
			}
			if (getTreeViewer().getData("allowCellEditing") != element) {
				return false;
			}
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				ICellModifier[] cms = treePlugin.getCellModifiers();
				if (cms != null) {
					for (ICellModifier element2 : cms) {
						if (element2.canModify(element, property)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		public Object getValue(Object element, String property) {
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				ICellModifier[] cms = treePlugin.getCellModifiers();
				if (cms != null) {
					for (ICellModifier element2 : cms) {
						if (element2.canModify(element, property)) {
							return element2.getValue(element, property);
						}
					}
				}
			}
			return null;
		}

		public void modify(Object element, String property, Object value) {
			//SWT bug workaround
			if (element instanceof Item) {
				element = ((Item) element).getData();
			}
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				ICellModifier[] cms = treePlugin.getCellModifiers();
				if (cms != null) {
					for (ICellModifier element2 : cms) {
						if (element2.canModify(element, property)) {
							element2.modify(element, property, value);
						}
					}
				}
			}
		}
	}

	//This class purposefully only supports single-selection drag;
	//if multi-selection is needed it must be updated later.
	class ArchipelagoOutlinePageDragSourceListener implements DragSourceListener {

		private ISelection selectionOnDrag = null;

		public void dragStart(DragSourceEvent event) {
			event.doit = false;

			//We have to hang on to the current selection because on some
			//platforms (Mac) the tree selection is cleared when dragging
			//starts
			selectionOnDrag = getTreeViewer().getSelection();
			if (selectionOnDrag instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selectionOnDrag;
				Object[] selectedNodes = structuredSelection.toArray();
				if (selectedNodes.length == 1) {
					Object data = selectedNodes[0];
					if (data instanceof java.io.Serializable) {
						event.data = selectedNodes[0];
						for (IArchipelagoTreePlugin treePlugin : treePlugins) {
							DragSourceListener dsl = treePlugin.getDragSourceListener();
							if (dsl != null) {
								dsl.dragStart(event);
							}
						}
					}
				}
			}
		}

		public void dragSetData(DragSourceEvent event) {
			if (LocalSelectionTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = selectionOnDrag;
				//LocalSelectionTransfer.getInstance().setSelection(selection);
				for (IArchipelagoTreePlugin treePlugin : treePlugins) {
					DragSourceListener dsl = treePlugin.getDragSourceListener();
					if (dsl != null) {
						dsl.dragSetData(event);
					}
				}
			}
		}

		public void dragFinished(DragSourceEvent event) {
			for (IArchipelagoTreePlugin treePlugin : treePlugins) {
				DragSourceListener dsl = treePlugin.getDragSourceListener();
				if (dsl != null) {
					dsl.dragFinished(event);
				}
			}
		}
	}

	static class DefaultArchipelagoEditorPane implements IArchipelagoEditorPane {

		protected ArchipelagoEditor editor = null;
		protected Map<String, Object> propertyMap = new HashMap<String, Object>();

		public DefaultArchipelagoEditorPane(ArchipelagoEditor editor) {
			this.editor = editor;
		}

		public void clearEditor() {
			editor.clearEditor();
			propertyMap.clear();
		}

		public void displayDefaultEditor() {
			editor.updateEditor();
		}

		public Composite getParentComposite() {
			return editor.getParentComposite();
		}

		public IActionBars getActionBars() {
			return ((IEditorSite) editor.getSite()).getActionBars();
		}

		public void setProperty(String name, Object value) {
			propertyMap.put(name, value);
		}

		public Object getProperty(String name) {
			return propertyMap.get(name);
		}
	}

	public void handleExplicitEvent(ExplicitADTEvent evt) {
		if (evt.getEventType() == ExplicitEventType.UPDATED_EXPLICIT_CHANGE_SETS) {
			final TreeViewer treeViewer = getTreeViewer();
			if (treeViewer != null) {
				SWTWidgetUtils.async(treeViewer.getControl(), new Runnable() {

					public void run() {
						treeViewer.refresh();
					}
				});
			}
		}
	}

}
