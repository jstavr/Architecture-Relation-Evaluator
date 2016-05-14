package edu.uci.isr.archstudio4.comp.selectordriver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import edu.uci.isr.archstudio4.comp.booleaneval.NoSuchVariableException;
import edu.uci.isr.archstudio4.comp.booleaneval.SymbolTable;
import edu.uci.isr.archstudio4.comp.pruner.IPruner;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.selector.ISelector;
import edu.uci.isr.archstudio4.comp.versionpruner.IVersionPruner;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditor;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.archstudio4.util.EclipseUtils;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.widgets.swt.AutoResizeTableLayout;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class SelectorDriverEditor
	extends AbstractArchstudioEditor{

	private static final int COLUMN_INDEX_NAME = 0;
	private static final int COLUMN_INDEX_TYPE = 1;
	private static final int COLUMN_INDEX_VALUE = 2;

	private static final String[] COLUMN_NAMES = new String[]{"Name", "Type", "Value"};
	private static final String[] COLUMN_PROPERTY_NAMES = new String[]{"Name", "Type", "Value"};

	protected ISelector selector = null;
	protected IPruner pruner = null;
	protected IVersionPruner versionPruner = null;

	protected SymbolTable symbolTable = new SymbolTable();
	protected boolean selectEnabled = true;
	protected boolean pruneEnabled = false;
	protected boolean versionPruneEnabled = false;

	protected TableViewer symbolTableViewer = null;

	protected int count = 1;

	public SelectorDriverEditor(){
		super(SelectorDriverMyxComponent.class, SelectorDriverMyxComponent.EDITOR_NAME);

		setBannerInfo(((SelectorDriverMyxComponent)comp).getIcon(), "Reduce Product-Line to a Subset");
		setHasBanner(true);

		selector = ((SelectorDriverMyxComponent)comp).getSelector();
		pruner = ((SelectorDriverMyxComponent)comp).getPruner();
		versionPruner = ((SelectorDriverMyxComponent)comp).getVersionPruner();
	}

	@Override
	public void createEditorContents(Composite parent){
		ObjRef[] selectedRefs = ((SelectorDriverOutlinePage)outlinePage).getSelectedRefs();

		if(selectedRefs.length != 1){
			Label lInvalidSelection = new Label(parent, SWT.LEFT);
			lInvalidSelection.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lInvalidSelection.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			lInvalidSelection.setText("Select a structure or a type in the left as a starting point for selection.");
		}
		else{
			Group cBindings = new Group(parent, SWT.NONE);
			cBindings.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			cBindings.setText("Variable-to-Value Bindings");
			cBindings.setFont(resources.getBoldPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			cBindings.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
			cBindings.setLayout(new GridLayout(1, false));

			final TableViewer tvBindings = createTableViewer(cBindings);
			symbolTableViewer = tvBindings;
			tvBindings.getTable().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

			Composite cBindingsButtons = new Composite(cBindings, SWT.NONE);
			cBindingsButtons.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			cBindingsButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
			cBindingsButtons.setLayout(new GridLayout(6, false));

			Button bNewString = new Button(cBindingsButtons, SWT.PUSH);
			bNewString.setText("New String");
			bNewString.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					symbolTable.putString("[New String " + count++ + "]", "[none]");
					doUpdateNow();
				}
			});

			Button bNewNumeric = new Button(cBindingsButtons, SWT.PUSH);
			bNewNumeric.setText("New Numeric");
			bNewNumeric.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					symbolTable.putDouble("[New Numeric " + count++ + "]", 0.0d);
					doUpdateNow();
				}
			});

			Button bNewDate = new Button(cBindingsButtons, SWT.PUSH);
			bNewDate.setText("New Date");
			bNewDate.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					symbolTable.putDate("[New Date " + count++ + "]", new java.util.Date());
					doUpdateNow();
				}
			});

			final Button bRemove = new Button(cBindingsButtons, SWT.PUSH);
			bRemove.setText("Remove");
			bRemove.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					Object o = ((IStructuredSelection)tvBindings.getSelection()).getFirstElement();
					if(o != null && o instanceof String){
						symbolTable.remove((String)o);
						updateEditor();
					}
				}
			});
			bRemove.setEnabled(false);

			final Button bImport = new Button(cBindingsButtons, SWT.PUSH);
			bImport.setText("Import Symbols...");
			bImport.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					IResource[] resources = EclipseUtils.selectResourcesToOpen(getSite().getShell(), SWT.SINGLE, "Select Symbols File", new String[]{"sym"});
					if(resources != null){
						IResource res = resources[0];
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IFile file = root.getFile(res.getFullPath());
						if(!file.exists()){
							MessageDialog.openError(getSite().getShell(), "Error", "Invalid input: file does not exist.");
							return;
						}
						InputStream is = null;
						try{
							is = file.getContents();
							symbolTable.clearTable();
							SymbolTable.parse(is, symbolTable);
						}
						catch(Exception e){
							MessageDialog.openError(getSite().getShell(), "Error", e.getMessage());
						}
						finally{
							try{
								if(is != null){
									is.close();
								}
							}
							catch(IOException ioe){
							}
						}
						updateEditor();
					}
				}
			});
			bImport.setEnabled(true);

			final Button bExport = new Button(cBindingsButtons, SWT.PUSH);
			bExport.setText("Export Symbols...");
			bExport.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					StringBuffer fileContents = new StringBuffer();
					String[] lines = symbolTable.listTable();
					for(String element: lines){
						fileContents.append(element);
						fileContents.append(System.getProperty("line.separator"));
					}
					InputStream is = new ByteArrayInputStream(fileContents.toString().getBytes());
					EclipseUtils.selectAndSaveFile(getSite().getShell(), "sym", is);
					try{
						is.close();
					}
					catch(java.io.IOException e){
					}
				}
			});
			bExport.setEnabled(!symbolTable.isEmpty());

			tvBindings.addSelectionChangedListener(new ISelectionChangedListener(){

				public void selectionChanged(SelectionChangedEvent event){
					bRemove.setEnabled(!tvBindings.getSelection().isEmpty());
				}
			});

			//Tasks
			Group cTasks = new Group(parent, SWT.NONE);
			cTasks.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			cTasks.setText("Tasks to Perform");
			cTasks.setFont(resources.getBoldPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			cTasks.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
			cTasks.setLayout(new GridLayout(1, false));

			Composite cTaskButtons = new Composite(cTasks, SWT.NONE);
			cTaskButtons.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			cTaskButtons.setLayout(new GridLayout(3, false));

			final Button cbSelect = new Button(cTaskButtons, SWT.CHECK);
			cbSelect.setText("Select");
			cbSelect.setSelection(selectEnabled);
			cbSelect.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			final Button cbPrune = new Button(cTaskButtons, SWT.CHECK);
			cbPrune.setText("Prune");
			cbPrune.setSelection(pruneEnabled);
			cbPrune.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			final Button cbVersionPrune = new Button(cTaskButtons, SWT.CHECK);
			cbVersionPrune.setText("Version Prune");
			cbVersionPrune.setSelection(versionPruneEnabled);
			cbVersionPrune.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			final Button bRunTasks = new Button(cTasks, SWT.PUSH);
			bRunTasks.setText("Run selected tasks...");
			bRunTasks.setEnabled(selectEnabled || pruneEnabled || versionPruneEnabled);
			bRunTasks.addListener(SWT.Selection, new Listener(){

				public void handleEvent(Event event){
					doTasks(cbSelect.getSelection(), cbPrune.getSelection(), cbVersionPrune.getSelection());
				}
			});

			Listener runTasksEnabler = new Listener(){

				public void handleEvent(Event event){
					selectEnabled = cbSelect.getSelection();
					pruneEnabled = cbPrune.getSelection();
					versionPruneEnabled = cbVersionPrune.getSelection();
					bRunTasks.setEnabled(selectEnabled || pruneEnabled || versionPruneEnabled);
				}
			};
			cbSelect.addListener(SWT.Selection, runTasksEnabler);
			cbPrune.addListener(SWT.Selection, runTasksEnabler);
			cbVersionPrune.addListener(SWT.Selection, runTasksEnabler);
		}
	}

	private TableViewer createTableViewer(Composite parent){
		TableViewer tv = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tv.setContentProvider(new PropertyTableContentProvider());
		tv.setLabelProvider(new PropertyTableLabelProvider());

		tv.setInput(symbolTable);

		Table table = tv.getTable();

		for(String element: COLUMN_NAMES){
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setText(element);
		}

		TableLayout tableLayout = new AutoResizeTableLayout(table);
		tableLayout.addColumnData(new ColumnWeightData(30, true));
		tableLayout.addColumnData(new ColumnWeightData(20, false));
		tableLayout.addColumnData(new ColumnWeightData(50, true));
		table.setLayout(tableLayout);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		//table.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		tv.setColumnProperties(COLUMN_PROPERTY_NAMES);

		CellEditor nameEditor = new TextCellEditor(table);
		CellEditor valueEditor = new TextCellEditor(table);
		tv.setCellEditors(new CellEditor[]{nameEditor, null, valueEditor});
		tv.setCellModifier(new PropertiesTableCellModifier());

		tv.refresh(true);
		return tv;
	}

	@Override
	protected AbstractArchstudioOutlinePage createOutlinePage(){
		return new SelectorDriverOutlinePage(xarch, xArchRef, resources);
	}

	class PropertyTableContentProvider
		implements IStructuredContentProvider{

		private Object[] EMPTY_ARRAY = new Object[0];

		public Object[] getElements(Object inputElement){
			if(inputElement instanceof SymbolTable){
				SymbolTable st = (SymbolTable)inputElement;
				return st.getVariables();
			}
			return null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		}

		public void dispose(){
		}
	}

	class PropertyTableLabelProvider
		extends LabelProvider
		implements ITableLabelProvider{

		public String getColumnText(Object element, int columnIndex){
			if(element instanceof String){
				if(columnIndex == COLUMN_INDEX_NAME){
					return (String)element;
				}
				else if(columnIndex == COLUMN_INDEX_TYPE){
					try{
						int type = symbolTable.getType((String)element);
						return SymbolTable.typeToString(type);
					}
					catch(NoSuchVariableException nsve){
						return null;
					}
				}
				Object value = symbolTable.get((String)element);
				if(value != null){
					return value.toString();
				}
			}
			else if(columnIndex == COLUMN_INDEX_VALUE){
				Object value = symbolTable.get((String)element);
				if(value != null){
					return value.toString();
				}
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex){
			return null;
		}
	}

	class PropertiesTableCellModifier
		implements ICellModifier{

		public boolean canModify(Object element, String property){
			if(element instanceof String){
				return property.equals(COLUMN_PROPERTY_NAMES[COLUMN_INDEX_NAME]) || property.equals(COLUMN_PROPERTY_NAMES[COLUMN_INDEX_VALUE]);
			}
			return false;
		}

		public Object getValue(Object element, String property){
			if(element instanceof String){
				if(property.equals(COLUMN_PROPERTY_NAMES[COLUMN_INDEX_NAME])){
					return element;
				}
				else if(property.equals(COLUMN_PROPERTY_NAMES[COLUMN_INDEX_VALUE])){
					Object value = symbolTable.get((String)element);
					if(value != null){
						if(value instanceof java.util.Date){
							return DateFormat.getDateTimeInstance().format((java.util.Date)value);
						}
						return value.toString();
					}
				}
			}
			return null;
		}

		public void modify(Object element, String property, Object value){
			//SWT bug workaround
			if(element instanceof Item){
				element = ((Item)element).getData();
			}
			if(element instanceof String){
				if(property.equals(COLUMN_PROPERTY_NAMES[COLUMN_INDEX_NAME])){
					if(value != null){
						if(value.equals(element)){
							return;
						}
					}
					symbolTable.renameVariable((String)element, value.toString());
					updateTableViewer();
				}
				else if(property.equals(COLUMN_PROPERTY_NAMES[COLUMN_INDEX_VALUE])){
					if(value != null){
						Object oldValue = symbolTable.get((String)element);
						if(value.equals(oldValue)){
							return;
						}
					}
					try{
						int type = symbolTable.getType((String)element);
						if(type == SymbolTable.STRING){
							symbolTable.putString((String)element, value.toString());
							updateTableViewer();
						}
						else if(type == SymbolTable.DOUBLE){
							try{
								Double d = new Double(value.toString());
								symbolTable.putDouble((String)element, d.doubleValue());
								updateTableViewer();
							}
							catch(NumberFormatException nfe){
								showMessage("Input must be a numeric value.");
							}
						}
						else if(type == SymbolTable.DATE){
							java.util.Date date = SystemUtils.parseDate(value.toString());
							if(date != null){
								symbolTable.putDate((String)element, date);
								updateTableViewer();
							}
							else{
								showMessage("Input must be a valid date value.");
							}
						}
					}
					catch(NoSuchVariableException nsve){
					}
				}
			}
		}
	}

	protected void doTasks(boolean select, boolean prune, boolean versionPrune){
		String baseURI = xarch.getXArchURI(xArchRef);
		ObjRef[] selectedRefs = ((SelectorDriverOutlinePage)outlinePage).getSelectedRefs();
		if(selectedRefs.length != 1){
			showMessage("Invalid selection; can't run tasks.");
			return;
		}

		ObjRef selectedRef = selectedRefs[0];
		boolean isStructural = xarch.isInstanceOf(selectedRef, "types#ArchStructure");

		String startingID = XadlUtils.getID(xarch, selectedRef);
		if(startingID == null){
			showMessage("Selected element has no ID; can't run tasks.");
			return;
		}

		java.util.List urisToClose = new java.util.ArrayList();
		try{
			if(select){
				String newURI = UIDGenerator.generateUID("urn:");
				try{
					selector.select(baseURI, newURI, symbolTable, startingID, isStructural);
					urisToClose.add(newURI);
					baseURI = newURI;
				}
				catch(Exception e){
					IStatus status = new Status(IStatus.ERROR, SelectorDriverMyxComponent.ECLIPSE_EDITOR_ID, IStatus.ERROR, "Selection failed: " + e.getMessage(), e);
					showError(status);
					return;
				}
			}
			if(prune){
				String newURI = UIDGenerator.generateUID("urn:");
				try{
					pruner.prune(baseURI, newURI, startingID, isStructural);
					urisToClose.add(newURI);
					baseURI = newURI;
				}
				catch(Exception e){
					IStatus status = new Status(IStatus.ERROR, SelectorDriverMyxComponent.ECLIPSE_EDITOR_ID, IStatus.ERROR, "Pruning failed: " + e.getMessage(), e);
					showError(status);
					return;
				}
			}
			if(versionPrune){
				String newURI = UIDGenerator.generateUID("urn:");
				try{
					versionPruner.pruneVersions(baseURI, newURI);
					urisToClose.add(newURI);
					baseURI = newURI;
				}
				catch(Exception e){
					IStatus status = new Status(IStatus.ERROR, SelectorDriverMyxComponent.ECLIPSE_EDITOR_ID, IStatus.ERROR, "Pruning failed: " + e.getMessage(), e);
					showError(status);
					return;
				}
			}

			ObjRef processedRef = xarch.getOpenXArch(baseURI);
			if(processedRef != null){
				String contents = xarch.serialize(processedRef);
				InputStream is = new ByteArrayInputStream(contents.getBytes());
				EclipseUtils.selectAndSaveFile(getEditorSite().getShell(), "xml", is);
			}
			else{
				showMessage("Error: after processing, couldn't read output. This shouldn't happen.");
				return;
			}
		}
		finally{
			String[] uris = (String[])urisToClose.toArray(new String[urisToClose.size()]);
			for(String element: uris){
				xarch.close(element);
			}
		}
	}

	private void updateTableViewer(){
		SWTWidgetUtils.async(symbolTableViewer, new Runnable(){

			public void run(){
				symbolTableViewer.refresh();
				symbolTableViewer.getTable().getParent().layout(true);
			}
		});
	}

}
