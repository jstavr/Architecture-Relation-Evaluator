package edu.uci.isr.archstudio4.rationale.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.MyxViewPart;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.rationale.RationaleViewManager;
import edu.uci.isr.archstudio4.rationale.actions.AddRationaleAction;
import edu.uci.isr.archstudio4.rationale.actions.DeleteRationaleAction;
import edu.uci.isr.archstudio4.utils.HasObjRefUtil;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.xarchflat.ObjRef;

public class RationaleViewPart extends MyxViewPart<RationaleViewMyxComponent> implements ISelectionListener, IPartListener, ExplicitADTListener {

	Composite parent;
	XArchChangeSetInterface xarch;
	IExplicitADT explicitadt;

	ObjRef xArchRef = null;

	TableViewer rationaleTableViewer;
	TableViewer associatedItemsTableViewer;
	RationaleViewManager rationaleViewManager;
	AddRationaleAction addRationaleAction;
	DeleteRationaleAction deleteRationaleAction;

	public RationaleViewPart() {
		super(RationaleViewMyxComponent.class);
	}

	static void addCellEditor(TableViewer tableViewer, CellEditor editor) {
		CellEditor[] editors = tableViewer.getCellEditors();
		if (editors == null)
			editors = new CellEditor[0];
		CellEditor[] newEditors = new CellEditor[editors.length + 1];
		System.arraycopy(editors, 0, newEditors, 0, editors.length);
		newEditors[newEditors.length - 1] = editor;
		tableViewer.setCellEditors(newEditors);
	}

	static void addColumnProperty(TableViewer tableViewer, String property) {
		String[] properties = (String[]) tableViewer.getColumnProperties();
		if (properties == null)
			properties = new String[0];
		String[] newProperties = new String[properties.length + 1];
		System.arraycopy(properties, 0, newProperties, 0, properties.length);
		newProperties[newProperties.length - 1] = property;
		tableViewer.setColumnProperties(newProperties);
	}

	@Override
	public void createMyxPartControl(Composite parent) {
		this.parent = parent;

		brick = MyxRegistry.getSharedInstance().waitForBrick(RationaleViewMyxComponent.class);
		xarch = brick.xarch;
		explicitadt = brick.explicitadt;
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		rationaleTableViewer = new TableViewer(sashForm, SWT.MULTI | SWT.FULL_SELECTION);
		associatedItemsTableViewer = new TableViewer(sashForm, SWT.MULTI | SWT.FULL_SELECTION);
		rationaleTableViewer.setUseHashlookup(true);
		associatedItemsTableViewer.setUseHashlookup(true);
		rationaleViewManager = new RationaleViewManager(xarch, rationaleTableViewer, associatedItemsTableViewer, explicitadt);
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		this.addRationaleAction = new AddRationaleAction(xarch, rationaleTableViewer, rationaleViewManager);
		this.deleteRationaleAction = new DeleteRationaleAction(xarch, rationaleTableViewer, rationaleViewManager);
		toolBarManager.add(addRationaleAction);
		toolBarManager.add(deleteRationaleAction);
		addRationaleAction.setEnabled(false);
		deleteRationaleAction.setEnabled(false);

		rationaleTableViewer.setContentProvider(new RationaleContentProvider(rationaleTableViewer, rationaleViewManager, xarch, xarch, explicitadt));
		rationaleTableViewer.setLabelProvider(new RationaleLabelProvider(xarch, rationaleTableViewer, rationaleViewManager, explicitadt));
		associatedItemsTableViewer.setContentProvider(new AssociatedItemsContentProvider(rationaleViewManager));
		associatedItemsTableViewer.setLabelProvider(new AssociatedItemsLabelProvider(xarch, xarch, explicitadt, rationaleViewManager,
		        associatedItemsTableViewer));

		Table rationaleTable = rationaleTableViewer.getTable();
		TableLayout rationaleTableLayout = new TableLayout();
		rationaleTable.setHeaderVisible(true);
		rationaleTable.setLinesVisible(true);
		rationaleTable.setLayout(rationaleTableLayout);
		rationaleTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Table associatedItemsTable = associatedItemsTableViewer.getTable();
		TableLayout associatedItemsTableLayout = new TableLayout();
		associatedItemsTable.setHeaderVisible(true);
		associatedItemsTable.setLinesVisible(true);
		associatedItemsTable.setLayout(associatedItemsTableLayout);
		associatedItemsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumn rationaleTableColumn = new TableColumn(rationaleTable, SWT.LEFT);
		rationaleTableColumn.setText("Sandbox");
		rationaleTableColumn.setImage(null);
		rationaleTableColumn.setResizable(false);
		rationaleTableLayout.addColumnData(new ColumnPixelData(0, rationaleTableColumn.getResizable()));
		CellEditor editor = null;
		addCellEditor(rationaleTableViewer, editor);
		addColumnProperty(rationaleTableViewer, rationaleTableColumn.getText());

		TableColumn associatedItemsTableColumn = new TableColumn(associatedItemsTable, SWT.LEFT);
		associatedItemsTableColumn.setText("Sandbox");
		associatedItemsTableColumn.setImage(null);
		associatedItemsTableColumn.setResizable(false);
		associatedItemsTableLayout.addColumnData(new ColumnPixelData(0, associatedItemsTableColumn.getResizable()));
		CellEditor sandBoxEditor = null;
		addCellEditor(associatedItemsTableViewer, sandBoxEditor);
		addColumnProperty(associatedItemsTableViewer, associatedItemsTableColumn.getText());

		rationaleTableColumn = new TableColumn(rationaleTable, SWT.LEFT);
		rationaleTableColumn.setText("");
		rationaleTableColumn.setResizable(false);
		rationaleTableColumn.setAlignment(SWT.CENTER);
		rationaleTableLayout.addColumnData(new ColumnPixelData(20, rationaleTableColumn.getResizable()));
		CellEditor annotationCellEditor = new CheckboxCellEditor(rationaleTable);
		addCellEditor(rationaleTableViewer, annotationCellEditor);
		addColumnProperty(rationaleTableViewer, "Annotation");

		associatedItemsTableColumn = new TableColumn(associatedItemsTable, SWT.LEFT);
		associatedItemsTableColumn.setText("");
		associatedItemsTableColumn.setResizable(false);
		associatedItemsTableColumn.setAlignment(SWT.CENTER);
		associatedItemsTableLayout.addColumnData(new ColumnPixelData(20, associatedItemsTableColumn.getResizable()));
		CellEditor newAnnotationCellEditor = new CheckboxCellEditor(associatedItemsTable);
		addCellEditor(associatedItemsTableViewer, newAnnotationCellEditor);
		addColumnProperty(associatedItemsTableViewer, "Annotation");

		rationaleTableColumn = new TableColumn(rationaleTable, SWT.LEFT);
		rationaleTableColumn.setText("Rationale");
		rationaleTableColumn.setResizable(true);
		rationaleTableLayout.addColumnData(new ColumnWeightData(100, rationaleTableColumn.getResizable()));
		CellEditor rationaleDescriptionEditor = new TextCellEditor(rationaleTable);
		addCellEditor(rationaleTableViewer, rationaleDescriptionEditor);
		addColumnProperty(rationaleTableViewer, rationaleTableColumn.getText());

		associatedItemsTableColumn = new TableColumn(associatedItemsTable, SWT.LEFT);
		associatedItemsTableColumn.setText("Associated Items");
		associatedItemsTableColumn.setResizable(true);
		associatedItemsTableLayout.addColumnData(new ColumnPixelData(100, associatedItemsTableColumn.getResizable()));
		CellEditor itemDetailsEditor = null;
		addCellEditor(associatedItemsTableViewer, itemDetailsEditor);
		addColumnProperty(associatedItemsTableViewer, associatedItemsTableColumn.getText());

		ObjRef xArchRef = null;

		IEditorPart editorPart = getSite().getPage().getActiveEditor();
		if (editorPart != null && editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
			xArchRef = xarch.getOpenXArch(fileEditorInput.getFile().getFullPath().makeAbsolute().toString());
		}
		updateXArchRef(xArchRef);
		getViewSite().getPage().addPartListener(this);
		getViewSite().getPage().addSelectionListener(this);
		rationaleTableViewer.setCellModifier(new RationaleCellModifier(rationaleTableViewer, rationaleViewManager, xarch, xarch, explicitadt));

		rationaleTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				rationaleViewManager.clearSelectedRationaleRefList();
				for (ObjRef objRef : HasObjRefUtil.getObjRefs(event.getSelection()))
					rationaleViewManager.addSelectedRationale(objRef);
				associatedItemsTableViewer.refresh();
			}
		});
		rationaleTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				for (ObjRef objRef : HasObjRefUtil.getObjRefs(event.getSelection())){
					if(event.getViewer() instanceof TableViewer){
						TableViewer v = (TableViewer) event.getViewer();
						if(v.getLabelProvider() instanceof ITableLabelProvider){
							ITableLabelProvider l = (ITableLabelProvider)v.getLabelProvider();
							String t = l.getColumnText(objRef, 2);
							MessageDialog.openInformation(v.getControl().getShell(), "Rationale", t);
							break;
						}
					}
				}
			}
		});

		associatedItemsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				rationaleViewManager.clearSelectedAssociatedItemRefList();
				for (ObjRef objRef : HasObjRefUtil.getObjRefs(event.getSelection()))
					rationaleViewManager.addSelectedAssociatedItemRefList(objRef);
				rationaleTableViewer.refresh();
			}
		});
		MyxRegistry.getSharedInstance().map(brick, this);
	}

	@Override
	public void setMyxFocus() {

	}

	public synchronized void selectionChanged(IWorkbenchPart part, ISelection selection) {
		List<ObjRef> currentSelectionList = new ArrayList<ObjRef>();
		try {
			boolean archChangeSetsRationaleCreated = false;
			for (ObjRef selectedObjRef : HasObjRefUtil.getObjRefs(selection)) {
				ObjRef[] ancestors = xarch.getAllAncestors(selectedObjRef);
				if (xarch.isInstanceOf(ancestors[ancestors.length - 2], "changesets#ArchChangeSets") && !archChangeSetsRationaleCreated) {
					ObjRef changeSetsContextRef = xarch.createContext(xArchRef, "changesets");
					ObjRef archChangeSetsElementRef = xarch.getElement(changeSetsContextRef, "ArchChangeSets", xArchRef);
					if (!xarch.isInstanceOf(archChangeSetsElementRef, "rationale#ArchChangeSetsRationale")) {
						ObjRef rationaleContextRef = xarch.createContext(xArchRef, "rationale");
						xarch.promoteTo(rationaleContextRef, "ArchChangeSetsRationale", archChangeSetsElementRef);
						ObjRef archRationaleRef = xarch.create(rationaleContextRef, "ArchRationale");
						xarch.set(archChangeSetsElementRef, "ArchRationale", archRationaleRef);
						archChangeSetsRationaleCreated = true;
					}
				}
				currentSelectionList.add((ObjRef) selectedObjRef);
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
		}
		rationaleViewManager.loadRationales(currentSelectionList);
		associatedItemsTableViewer.refresh();

		if (currentSelectionList.size() > 0 && xArchRef != null) {
			addRationaleAction.setEnabled(true);
			deleteRationaleAction.setEnabled(true);
		}
		else {
			addRationaleAction.setEnabled(false);
			deleteRationaleAction.setEnabled(false);
		}
	}

	public void partActivated(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) part;
			if (editorPart.getEditorInput() instanceof IURIEditorInput) {
				IURIEditorInput uriEditorInput = (IURIEditorInput) editorPart.getEditorInput();
				updateXArchRef(xarch.getOpenXArch(uriEditorInput.getURI().toString()));
			}
		}
	}

	public synchronized void updateXArchRef(ObjRef xArchRef) {
		this.xArchRef = xArchRef;
		if (this.xArchRef != null) {
			ObjRef rationaleContextRef = xarch.createContext(xArchRef, "rationale");
			ObjRef rationaleElementRef = xarch.getElement(rationaleContextRef, "archRationale", xArchRef);
			if (rationaleElementRef == null) {
				rationaleElementRef = xarch.createElement(rationaleContextRef, "archRationale");
				xarch.add(xArchRef, "object", rationaleElementRef);
			}
		}
		rationaleTableViewer.setInput(xArchRef);
		associatedItemsTableViewer.setInput(xArchRef);
		rationaleViewManager.setXArchRef(xArchRef);
		addRationaleAction.setXArchRef(xArchRef);
		deleteRationaleAction.setXArchRef(xArchRef);
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) part;
			if (editorPart == null) {
				updateXArchRef(null);
			}
			else if (editorPart.getEditorInput() instanceof IURIEditorInput) {
				IURIEditorInput uriEditorInput = (IURIEditorInput) editorPart.getEditorInput();
				ObjRef xArchRef = xarch.getOpenXArch(uriEditorInput.getURI().toString());
				if (xArchRef == null || xArchRef.equals(rationaleTableViewer.getInput())) {
					updateXArchRef(null);
				}
			}
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void dispose() {

		getSite().getPage().removePartListener(this);
		getSite().getPage().removeSelectionListener(this);

		super.dispose();
	}

	public void handleExplicitEvent(ExplicitADTEvent evt) {

		if (evt.getEventType().equals(ExplicitADTEvent.ExplicitEventType.UPDATED_EXPLICIT_OBJREF)) {
			if (!rationaleTableViewer.getControl().getDisplay().isDisposed()) {
				rationaleTableViewer.getControl().getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (rationaleTableViewer == null)
							return;
						if (rationaleTableViewer.getControl().getDisplay().isDisposed())
							return;

						rationaleTableViewer.refresh();
					}
				});
			}
			if (!associatedItemsTableViewer.getControl().getDisplay().isDisposed()) {
				associatedItemsTableViewer.getControl().getDisplay().asyncExec(new Runnable() {

					public void run() {
						if (associatedItemsTableViewer == null)
							return;
						if (associatedItemsTableViewer.getControl().getDisplay().isDisposed())
							return;

						associatedItemsTableViewer.refresh();
					}
				});
			}

		}

	}

}
