package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.SaveAsDialog;

import edu.uci.isr.archstudio4.comp.xarchcs.ProgressToChangeSetMonitorWrapper;
import edu.uci.isr.archstudio4.comp.xarchcs.XArchCSActivator;
import edu.uci.isr.archstudio4.comp.xarchcs.actions.AddChangeSetAction;
import edu.uci.isr.archstudio4.comp.xarchcs.actions.IHasXArchRef;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.IChangeSetSyncMonitor;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.conversion.CSConverter;
import edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.conversion.ComparableBooleanGuardConverter;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetUtils;
import edu.uci.isr.archstudio4.utils.HasObjRefUtil;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxRegistryListener;
import edu.uci.isr.sysutils.DelayedExecuteOnceThread;
import edu.uci.isr.sysutils.ListenerList;
import edu.uci.isr.widgets.swt.ActionWithProgress;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class ChangeSetViewPart extends MyxViewPart<ChangeSetViewMyxComponent> implements ISelectionProvider, MyxRegistryListener, XArchFlatListener,
        XArchChangeSetListener, IPartListener, ISelectionChangedListener, INullSelectionListener, Listener {

	protected static final boolean equalz(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	protected static GridData excludeGridData() {
		GridData d = new GridData();
		d.exclude = true;
		return d;
	}

	ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<ISelectionChangedListener>(ISelectionChangedListener.class);
	List<ObjRef> selectedRefs = new ArrayList<ObjRef>();
	IAction overviewModeAction = null;

	protected static void addCellEditor(TreeViewer viewer, CellEditor editor) {
		CellEditor[] editors = viewer.getCellEditors();
		if (editors == null) {
			editors = new CellEditor[0];
		}
		CellEditor[] newEditors = new CellEditor[editors.length + 1];
		System.arraycopy(editors, 0, newEditors, 0, editors.length);
		newEditors[newEditors.length - 1] = editor;
		viewer.setCellEditors(newEditors);
	}

	protected static void addColumnProperty(TreeViewer viewer, String property) {
		String[] properties = (String[]) viewer.getColumnProperties();
		if (properties == null) {
			properties = new String[0];
		}
		String[] newProperties = new String[properties.length + 1];
		System.arraycopy(properties, 0, newProperties, 0, properties.length);
		newProperties[newProperties.length - 1] = property;
		viewer.setColumnProperties(newProperties);
	}

	protected static int indexOf(Item[] items, Object element) {
		for (int i = 0; i < items.length; i++) {
			if (ChangeSetViewPart.equalz(items[i].getData(), element)) {
				return i;
			}
		}
		return -1;
	}

	protected XArchChangeSetInterface xarch;
	protected IExplicitADT explicit;
	protected IChangeSetADT csadt;
	protected IChangeSetSync cssync;
	protected IChangeSetState changeSetState;
	protected TreeViewer changeSetViewer = null;
	protected ChangeSetSorter changeSetSorter = null;
	protected ChangeSetLabelProvider changeSetLabelProvider = null;
	protected Composite notificationComposite = null;
	protected Object ignoreEventsLock = new Object();
	protected int ignoreChangeSetSelectionEvents = 0;

	public ChangeSetViewPart() {
		super(ChangeSetViewMyxComponent.class);
	}

	protected Collection<Object> myxMapped = new ArrayList<Object>();

	protected void myxMap(Object o) {
		if (o != null) {
			myxMapped.add(o);
			MyxRegistry.getSharedInstance().map(brick, o);
		}
	}

	public void handleEvent(Event event) {
		if (hideSelection) {
			event.detail &= ~SWT.SELECTED;
			event.detail &= ~SWT.FOCUSED;
			event.detail &= ~SWT.HOT;
			event.detail |= SWT.BACKGROUND;
			event.detail |= SWT.FOREGROUND;

			if (event.item instanceof TreeItem) {
				Tree tree = (Tree) event.widget;
				TreeItem treeItem = (TreeItem) event.item;
				int column = event.index;
				TreeColumn treeColumn = tree.getColumn(column);
				CellLabelProvider labelProvider = changeSetViewer.getLabelProvider(column);
				Object data = treeColumn.getData();
				if (data == null)
					data = treeItem.getData();
				if (labelProvider instanceof IColorProvider) {
					IColorProvider colorProvider = (IColorProvider) labelProvider;
					Color fc = colorProvider.getForeground(data);
					if (fc != null)
						event.gc.setForeground(fc);
					Color bc = colorProvider.getBackground(data);
					if (bc != null) {
						event.gc.setBackground(bc);
						event.gc.fillRectangle(event.gc.getClipping());
					}
				}
			}
		}
	}

	protected void createMainMyxPartControl(Composite parent) {
		changeSetViewer = new TreeViewer(parent, SWT.MULTI | SWT.FULL_SELECTION) {

			boolean needsLabelUpdate = false;
			DelayedExecuteOnceThread labelUpdater = null;

			@Override
			protected synchronized void handleLabelProviderChanged(LabelProviderChangedEvent event) {
				/*
				 * We catch calls to this method and then perform a single call
				 * at a later time.
				 */
				if (labelUpdater == null) {
					labelUpdater = new DelayedExecuteOnceThread(250, new Runnable() {

						public void run() {
							SWTWidgetUtils.async(changeSetViewer, new Runnable() {

								public void run() {
									superHandleLabelProviderChanged(new LabelProviderChangedEvent(changeSetViewer.getLabelProvider()));
								}
							});
						}
					});
					labelUpdater.start();
				}
				needsLabelUpdate = true;
				labelUpdater.execute();
			}

			private void superHandleLabelProviderChanged(LabelProviderChangedEvent event) {
				if (needsLabelUpdate) {
					needsLabelUpdate = false;
					super.handleLabelProviderChanged(event);
				}
			}
		};

		changeSetViewer.getTree().addListener(SWT.EraseItem, this);
		//changeSetViewer.getTree().addListener(SWT.PaintItem, this);

		changeSetViewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		changeSetViewer.setUseHashlookup(true);
		changeSetViewer.setContentProvider(new ChangeSetContentProvider(xarch));
		changeSetViewer.setComparator(changeSetSorter = new ChangeSetSorter(xarch));
		changeSetViewer.setLabelProvider(changeSetLabelProvider = new ChangeSetLabelProvider(changeSetViewer, xarch, changeSetState));
		changeSetViewer.setCellModifier(new ChangeSetCellModifier(changeSetViewer, xarch, changeSetState));
		changeSetViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				synchronized (ignoreEventsLock) {
					if (ignoreChangeSetSelectionEvents > 0) {
						ignoreChangeSetSelectionEvents--;
						return;
					}
				}
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					ObjRef csRef = null;
					IStructuredSelection ss = (IStructuredSelection) selection;
					if (ss.size() == 1) {
						csRef = (ObjRef) ss.getFirstElement();
					}
					ObjRef xArchRef = (ObjRef) changeSetViewer.getInput();
					if (xArchRef != null) {
						xarch.setActiveChangeSetRef(xArchRef, csRef);
					}
				}
			}
		});
		changeSetViewer.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				for (Object element : ((IStructuredSelection) event.getSelection()).toArray()) {
					changeSetViewer.getCellModifier().modify(element, "Apply",
					        !Arrays.asList(xarch.getAppliedChangeSetRefs((ObjRef) changeSetViewer.getInput())).contains(element));
				}
			}
		});
		changeSetViewer.addSelectionChangedListener(this);
		changeSetViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { ObjRefTransfer.getInstance() }, new DragSourceAdapter() {

			ObjRef[] data = null;

			@SuppressWarnings("unchecked")
			@Override
			public void dragStart(DragSourceEvent event) {
				ISelection selection = changeSetViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					data = (ObjRef[]) ((IStructuredSelection) selection).toList().toArray(new ObjRef[0]);
				}
				event.doit &= data != null;

				// it seems like this should be done automatically!?
				if (changeSetViewer.isCellEditorActive()) {
					for (CellEditor e : changeSetViewer.getCellEditors()) {
						if (e != null) {
							e.deactivate();
						}
					}
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (ObjRefTransfer.getInstance().isSupportedType(event.dataType) && data != null) {
					event.data = data.clone();
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				if (!event.doit) {
					return;
				}
			}
		});
		changeSetViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { ObjRefTransfer.getInstance() }, new ViewerDropAdapter(changeSetViewer) {

			@Override
			protected int determineLocation(DropTargetEvent event) {
				if (!(event.item instanceof Item)) {
					return ViewerDropAdapter.LOCATION_NONE;
				}
				Item item = (Item) event.item;
				Point coordinates = new Point(event.x, event.y);
				coordinates = BNAUtils.toPoint(changeSetViewer.getControl().toControl(BNAUtils.toSwtPoint(coordinates)));
				if (item != null) {
					Rectangle bounds = BNAUtils.toRectangle(getBounds(item));
					if (bounds == null) {
						return ViewerDropAdapter.LOCATION_NONE;
					}
					if (coordinates.y - (bounds.y + bounds.height / 2) < 0) {
						return ViewerDropAdapter.LOCATION_BEFORE;
					}
					else {
						return ViewerDropAdapter.LOCATION_AFTER;
					}
				}
				return ViewerDropAdapter.LOCATION_ON;
			}

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return ObjRefTransfer.getInstance().isSupportedType(transferType);
			}

			@Override
			public boolean performDrop(Object data) {

				if (data instanceof ObjRef[] && ((ObjRef[]) data).length > 0) {
					if (xarch.isInstanceOf(((ObjRef[]) data)[0], "changesets#AbstractChangeSet")) {
						int newIndex = ChangeSetViewPart.indexOf(changeSetViewer.getTree().getItems(), getCurrentTarget());
						if (newIndex >= 0) {
							if (getCurrentLocation() == ViewerDropAdapter.LOCATION_AFTER) {
								newIndex++;
							}
							XArchChangeSetUtils.move(xarch, (ObjRef) changeSetViewer.getInput(), (ObjRef[]) data, -newIndex - 1, null);
							return true;
						}
					}
				}
				return false;
			}
		});

		// Tree tree = changeSetViewer.getTree();
		// tree.setHeaderVisible(true);
		// tree.setLinesVisible(true);
		// //tree.setLayout(new TableLayout());
		//
		// TreeViewerColumn vColumn;
		// TreeColumn tColumn;
		//
		// vColumn = new TreeViewerColumn(changeSetViewer, SWT.LEFT);
		// vColumn.setEditingSupport(new
		// ChangeSetColumnEditorSupport(changeSetViewer, xarch));
		// vColumn.setLabelProvider(new ChangeSetColumnLabelProvider(xarch));
		// tColumn = vColumn.getColumn();
		// tColumn.setText("Change Set");
		// tColumn.setAlignment(SWT.LEFT);
		// tColumn.setMoveable(false);
		// tColumn.setResizable(true);
		// tColumn.setWidth(20);

		Tree tree = changeSetViewer.getTree();
		TreeColumn column;
		CellEditor editor;
		TableLayout layout = new TableLayout();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		tree.setLayout(layout);

		/*
		 * Feature in Windows. The first column in a windows table reserves room
		 * for a check box. The fix is to have this column with a width of zero
		 * to hide it.
		 */
		column = new TreeColumn(tree, SWT.CENTER);
		column.setText("Windows first column fix");
		column.setImage(null);
		column.setResizable(false);
		column.setAlignment(SWT.CENTER);
		layout.addColumnData(new ColumnPixelData(1, column.getResizable()));
		editor = null;
		ChangeSetViewPart.addCellEditor(changeSetViewer, editor);
		ChangeSetViewPart.addColumnProperty(changeSetViewer, column.getText());

		column = new TreeColumn(tree, SWT.CENTER);
		column.setText("");
		column.setImage(XArchCSActivator.getDefault().getImageRegistry().get("res/icons/applied.gif"));
		column.setResizable(false);
		column.setAlignment(SWT.CENTER);
		layout.addColumnData(new ColumnPixelData(20, column.getResizable()));
		editor = new CheckboxCellEditor(tree);
		ChangeSetViewPart.addCellEditor(changeSetViewer, editor);
		ChangeSetViewPart.addColumnProperty(changeSetViewer, "Apply");
		column = new TreeColumn(tree, SWT.CENTER);
		column.setText("");
		column.setImage(XArchCSActivator.getDefault().getImageRegistry().get("res/icons/explicit.gif"));
		column.setResizable(false);
		column.setAlignment(SWT.CENTER);
		layout.addColumnData(new ColumnPixelData(20, column.getResizable()));
		editor = new CheckboxCellEditor(tree);
		ChangeSetViewPart.addCellEditor(changeSetViewer, editor);
		ChangeSetViewPart.addColumnProperty(changeSetViewer, "View");

		column = new TreeColumn(tree, SWT.LEFT);
		column.setText("Change Set");
		column.setResizable(true);
		column.setAlignment(SWT.LEFT);
		layout.addColumnData(new ColumnWeightData(1, column.getResizable()));
		editor = new TextCellEditor(tree);
		ChangeSetViewPart.addCellEditor(changeSetViewer, editor);
		ChangeSetViewPart.addColumnProperty(changeSetViewer, column.getText());

		ObjRef xArchRef = null;

		IEditorPart editorPart = getSite().getPage().getActiveEditor();
		if (editorPart != null && editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
			xArchRef = xarch.getOpenXArch(fileEditorInput.getFile().getRawLocationURI().toString());
		}

		setInput(xArchRef);

		getViewSite().getActionBars().getToolBarManager().add(new AddChangeSetAction(xarch));
		getViewSite().getActionBars().updateActionBars();

		/*
		 * This needs to be here since we always want to update the change sets
		 * viewer before the relationships viewer.
		 */
		myxMap(this);
		myxMap(changeSetViewer.getContentProvider());
		myxMap(changeSetViewer.getLabelProvider());
		myxMap(changeSetViewer.getSorter());
		getViewSite().setSelectionProvider(this);

		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		overviewModeAction = new Action("Overview Mode", IAction.AS_CHECK_BOX) {

			@Override
			public void run() {
				ObjRef xArchRef = (ObjRef) changeSetViewer.getInput();
				xarch.setOverviewMode(xArchRef, !xarch.getOverviewMode(xArchRef));
				overviewModeAction.setChecked(xarch.getOverviewMode(xArchRef));
			}
		};
		menuManager.add(overviewModeAction);

		IAction diffToExternalFile = new Action("(Experimental) Create Diff to External File") {

			@Override
			public void run() {
				FileDialog fileDialog = new FileDialog(changeSetViewer.getControl().getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				fileDialog.setFilterNames(new String[] { "XML Files (*.xml)" });
				fileDialog.setText("Select File");
				fileDialog.open();
				String fileName = fileDialog.getFileName();
				String filePath = fileDialog.getFilterPath();
				if (fileName != null && !"".equals(fileName.trim())) {
					try {
						ObjRef targetXArchRef = xarch.parseFromFile(filePath + java.io.File.separator + fileName);
						ObjRef sourceXArchRef = (ObjRef) changeSetViewer.getInput();
						xarch.diffToExternalFile(sourceXArchRef, targetXArchRef);
					}
					catch (Exception e) {
						MessageDialog.openError(changeSetViewer.getControl().getShell(), "Error", e.getMessage());
					}
				}
			}
		};
		menuManager.add(diffToExternalFile);

		// not yet enabled
		// IAction diffFromExternalFile = new
		// Action("Create Diff from External File"){
		//
		// @Override
		// public void run(){
		// FileDialog fileDialog = new
		// FileDialog(changeSetViewer.getControl().getShell(), SWT.OPEN);
		// fileDialog.setFilterExtensions(new String[]{"*.xml"});
		// fileDialog.setFilterNames(new String[]{"XML Files (*.xml)"});
		// fileDialog.open();
		// String fileName = fileDialog.getFileName();
		// String filePath = fileDialog.getFilterPath();
		// if(fileName != null && !"".equals(fileName.trim())){
		// try{
		// ObjRef targetXArchRef = xarch.parseFromFile(filePath +
		// java.io.File.separator + fileName);
		// ObjRef sourceXArchRef = (ObjRef)changeSetViewer.getInput();
		// xarch.diffFromExternalFile(sourceXArchRef, targetXArchRef);
		// }
		// catch(Exception e){
		// String[] labels = {"Ok"};
		// MessageDialog dialog = new
		// MessageDialog(changeSetViewer.getControl().getShell(), "Error", null,
		// e.getMessage(), MessageDialog.ERROR, labels, 0);
		// dialog.open();
		// }
		// }
		// }
		// };
		// menuManager.add(diffFromExternalFile);

		IAction convertPLAtoCS = new Action("(Experimental) Convert PLA Guards (and Menage versions) to Change Sets") {

			private String rename(String s) {
				if (s.endsWith(".xml")) {
					s = s.substring(0, s.length() - 4) + "-cs.xml";
				}
				return s;
			}

			@Override
			public void run() {

				final ObjRef oldXArchRef = (ObjRef) changeSetViewer.getInput();
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				final IPath originalPath = new Path(rename(xarch.getXArchURI(oldXArchRef)));
				final IFile originalFile = workspace.getRoot().getFile(originalPath);

				SaveAsDialog sad = new SaveAsDialog(changeSetViewer.getControl().getShell());
				sad.setOriginalFile(originalFile);
				sad.open();
				final IPath filePath = sad.getResult();

				if (filePath != null) {
					new Thread(new Runnable() {
						public void run() {
							IFile file = workspace.getRoot().getFile(filePath);
							try {
								String fileUrl = file.getLocationURI().toURL().toString();
								// pathURI = Platform.FileLocator.toFileURL(path)
								try {
									xarch.close(fileUrl.toString());
								}
								catch (Throwable t) {
								}
								final ObjRef newXArchRef = xarch.createXArch(fileUrl);
								try {
									CSConverter.convertPLAtoCS(xarch, oldXArchRef, newXArchRef);
									SWTWidgetUtils.sync(changeSetViewer, new Runnable() {

										public void run() {
											String contents = xarch.serialize(newXArchRef);
											InputStream is = new ByteArrayInputStream(contents.getBytes());
											ChangeSetViewPart.saveFile(changeSetViewer.getControl().getShell(), is, filePath);
										}
									});
								}
								finally {
									if (newXArchRef != null) {
										xarch.close(newXArchRef);
									}
									SWTWidgetUtils.async(new Runnable() {
										public void run() {
											MessageBox messageBox = new MessageBox(changeSetViewer.getControl().getShell(), SWT.OK);
											messageBox.setMessage("Finished");
											messageBox.open();
										}
									});
								}
							}
							catch (Throwable t) {
								t.printStackTrace();
							}
						}
					}).start();
				}
			}
		};
		menuManager.add(convertPLAtoCS);

		IAction plaStats = new Action("(Experimental) Dump PLA Stats") {

			@Override
			public void run() {
				dumpStats((ObjRef) changeSetViewer.getInput());
			}
		};
		menuManager.add(plaStats);

		IAction separateChangeSets = new ActionWithProgress("(Experimental) Enable Team Support") {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					final IWorkspace workspace = ResourcesPlugin.getWorkspace();
					final ObjRef oldXArchRef = (ObjRef) changeSetViewer.getInput();
					final IFile xArchFile = workspace.getRoot().findFilesForLocationURI(new URI(xarch.getXArchURI(oldXArchRef)))[0];
					final IFolder newFolder = xArchFile.getParent().getFolder(new Path(xArchFile.getName() + ".xadl-data"));

					if (!newFolder.exists()) {
						newFolder.create(IResource.FOLDER, true, null);
					}

					for (final ObjRef newXArchRef : xarch.seperateOutChangeSets(oldXArchRef, newFolder.getName(),
					        new ProgressToChangeSetMonitorWrapper(monitor))) {
						final IOException[] ie = new IOException[] { null };
						SWTWidgetUtils.sync(changeSetViewer, new Runnable() {

							public void run() {
								try {
									xarch.writeToURL(newXArchRef, xarch.getXArchURI(newXArchRef));
								}
								catch (IOException e) {
									ie[0] = e;
								}
							}
						});
						if (ie[0] != null) {
							throw ie[0];
						}
					}

					xarch.writeToURL(oldXArchRef, xarch.getXArchURI(oldXArchRef));
				}
				catch (Exception e) {
					MessageDialog.openError(changeSetViewer.getControl().getShell(), "Error", e.getMessage());
				}
			}
		};
		menuManager.add(separateChangeSets);
	}

	@Override
	public void createMyxPartControl(Composite parent) {
		xarch = brick.xarch;
		explicit = brick.explicit;
		csadt = brick.adt;
		cssync = brick.cssync;
		changeSetState = new ChangeSetState(xarch, explicit);
		myxMap(changeSetState);

		parent.setLayout(new GridLayout());

		notificationComposite = new Composite(parent, SWT.BORDER);

		notificationComposite.setLayoutData(ChangeSetViewPart.excludeGridData());
		notificationComposite.setVisible(false);

		createMainMyxPartControl(parent);

		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

		updateStatus();
	}

	protected boolean hideSelection = false;

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		hideSelection = false;
		if (ChangeSetViewPart.this != part) {
			ObjRef[] selectedRefs = HasObjRefUtil.getObjRefs(selection);
			if (selectedRefs.length <= 1) {
				ObjRef selectedRef = selectedRefs.length > 0 ? selectedRefs[0] : null;
				hideSelection = true;
				changeSetLabelProvider.setChangeColors(selectedRef);
				return;
			}
			changeSetLabelProvider.setChangeColors(null);
		}
	}

	@Override
	public void dispose() {
		for (Object o : myxMapped) {
			MyxRegistry.getSharedInstance().unmap(brick, o);
		}

		getSite().getPage().removePartListener(this);

		super.dispose();
	}

	@Override
	public void setMyxFocus() {
		changeSetViewer.getControl().setFocus();
	}

	public void handleXArchChangeSetEvent(final XArchChangeSetEvent evt) {
		if (evt.getEventType() == XArchChangeSetEvent.ChangeSetEventType.UPDATED_ACTIVE_CHANGE_SET) {
			SWTWidgetUtils.async(changeSetViewer, new Runnable() {

				public void run() {
					if (ChangeSetViewPart.equalz(evt.getXArchRef(), changeSetViewer.getInput())) {
						ObjRef activeChangeSetRef = evt.getActiveChangeSet();
						StructuredSelection selection = StructuredSelection.EMPTY;
						if (activeChangeSetRef != null) {
							selection = new StructuredSelection(activeChangeSetRef);
						}
						ignoreChangeSetSelectionEvents++;
						changeSetViewer.setSelection(selection);
					}
				}
			});
		}
		if (evt.getEventType() == XArchChangeSetEvent.ChangeSetEventType.UPDATED_ENABLED) {
			SWTWidgetUtils.async(changeSetViewer, new Runnable() {

				public void run() {
					if (ChangeSetViewPart.equalz(evt.getXArchRef(), changeSetViewer.getInput())) {
						updateStatus();
					}
				}
			});
		}
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt) {
		if (changeSetSorter != null) {
			changeSetSorter.handleXArchFlatEvent(evt);
		}
	}

	protected void setInput(ObjRef xArchRef) {
		if (!ChangeSetViewPart.equalz(changeSetViewer.getInput(), xArchRef)) {
			changeSetState.setXArchRef(xArchRef);
			changeSetViewer.setInput(xArchRef);
			updateStatus();
		}
	}

	public void partActivated(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) part;
			if (editorPart.getEditorInput() instanceof IURIEditorInput) {
				IURIEditorInput uriEditorInput = (IURIEditorInput) editorPart.getEditorInput();
				setInput(xarch.getOpenXArch(uriEditorInput.getURI().toString()));
			}
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partClosed(final IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorPart editorPart = (IEditorPart) part;
			if (editorPart == null) {
				setInput(null);
			}
			else if (editorPart.getEditorInput() instanceof IURIEditorInput) {
				IURIEditorInput uriEditorInput = (IURIEditorInput) editorPart.getEditorInput();
				ObjRef xArchRef = xarch.getOpenXArch(uriEditorInput.getURI().toString());
				if (xArchRef == null || xArchRef.equals(changeSetViewer.getInput())) {
					setInput(null);
				}
			}
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	protected void updateStatus() {
		boolean enabled = false;

		for (Control c : notificationComposite.getChildren()) {
			c.dispose();
		}
		ObjRef xArchRef = (ObjRef) changeSetViewer.getInput();
		if (xArchRef == null) {
			notificationComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			notificationComposite.setVisible(true);

			notificationComposite.setLayout(new GridLayout());
			notificationComposite.setBackground(notificationComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			Label message = new Label(notificationComposite, SWT.CENTER | SWT.WRAP);
			message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			message.setText("Open a xADL document to use change sets.");
			message.setBackground(notificationComposite.getBackground());
			message.setForeground(message.getDisplay().getSystemColor(SWT.COLOR_BLACK));

			// notificationComposite.setLayoutData(excludeGridData());
			// notificationComposite.setVisible(false);

		}
		else if (!xarch.getChangeSetsEnabled(xArchRef)) {
			notificationComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			notificationComposite.setVisible(true);

			notificationComposite.setLayout(new GridLayout());
			notificationComposite.setBackground(notificationComposite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

			Label message = new Label(notificationComposite, SWT.CENTER | SWT.WRAP);
			message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			message.setText("Change Sets are currently disabled for this document.");
			message.setBackground(notificationComposite.getBackground());
			message.setForeground(message.getDisplay().getSystemColor(SWT.COLOR_BLACK));

			Button enableButton = new Button(notificationComposite, SWT.PUSH);
			enableButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
			enableButton.setText("Enable");
			enableButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent event) {
					final ObjRef xArchRef = (ObjRef) changeSetViewer.getInput();
					if (xArchRef != null) {
						try {
							// PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new
							// IRunnableWithProgress(){
							// see:
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=156687
							ProgressMonitorDialog pd = new ProgressMonitorDialog(changeSetViewer.getControl().getShell());
							pd.run(true, true, new IRunnableWithProgress() {

								public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
									monitor.beginTask("Creating a baseline change set for the current document...", 1);
									xarch.enableChangeSets(xArchRef, new IChangeSetSyncMonitor() {

										IProgressMonitor m = monitor;

										public void beginTask(int totalWork) {
											m = new SubProgressMonitor(m, 1);
											m.beginTask("", totalWork);
										}

										public void worked(int work) {
											m.worked(work);
										}

										public void done() {
											m.done();
											if (m instanceof SubProgressMonitor) {
												m = ((SubProgressMonitor) m).getWrappedProgressMonitor();
											}
										}

										public boolean isCanceled() {
											return m.isCanceled();
										}

										public void setCanceled(boolean canceled) {
											m.setCanceled(canceled);
										}
									});
									monitor.done();
								}
							});
						}
						catch (InvocationTargetException e) {
							e.printStackTrace();
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		else {
			enabled = true;
			notificationComposite.setLayoutData(ChangeSetViewPart.excludeGridData());
			notificationComposite.setVisible(false);
			ObjRef activeChangeSetRef = xarch.getActiveChangeSetRef(xArchRef);
			++ignoreChangeSetSelectionEvents;
			changeSetViewer.setSelection(activeChangeSetRef != null ? new StructuredSelection(activeChangeSetRef) : StructuredSelection.EMPTY);
			if (overviewModeAction != null)
				overviewModeAction.setChecked(xarch.getOverviewMode(xArchRef));
		}

		changeSetViewer.getControl().setEnabled(enabled);
		for (IContributionItem item : getViewSite().getActionBars().getToolBarManager().getItems()) {
			if (item instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) item).getAction();
				if (action instanceof IHasXArchRef) {
					((IHasXArchRef) action).setXArchRef(enabled ? xArchRef : null);
				}
			}
		}

		notificationComposite.layout();
		notificationComposite.getParent().layout();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	public ISelection getSelection() {
		return new StructuredSelection(selectedRefs);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub

	}

	protected void fireSelectionChangedEvent(ISelection selection) {
		SelectionChangedEvent evt = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener l : selectionChangedListeners.getListeners()) {
			l.selectionChanged(evt);
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		Iterator iterator = ((IStructuredSelection) event.getSelection()).iterator();
		if (iterator != null) {
			selectedRefs.clear();
			while (iterator.hasNext()) {
				Object obj = iterator.next();
				if (obj != null && obj instanceof ObjRef) {
					ObjRef ref = (ObjRef) obj;
					selectedRefs.add(ref);
				}
			}
			fireSelectionChangedEvent(new StructuredSelection(selectedRefs));
		}
	}

	private static void saveFile(Shell shell, InputStream contents, IPath initialTargetPath) {
		final Shell fshell = shell;
		final InputStream fcontents = contents;
		final IPath targetPath = initialTargetPath;

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile targetFile = workspace.getRoot().getFile(targetPath);

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				IPath targetContainerPath = targetPath.removeLastSegments(1);
				boolean createContainer = true;
				if (workspace.getRoot().getContainerForLocation(targetContainerPath) != null) {
					createContainer = false;
				}
				ContainerGenerator gen = new ContainerGenerator(targetContainerPath);
				IContainer res = null;
				try {
					if (createContainer) {
						res = gen.generateContainer(monitor); // creates project
						// A and folder
						// B if required
					}
					if (targetFile.exists()) {
						targetFile.delete(false, monitor);
					}
					targetFile.create(fcontents, false, monitor);
					try {
						fcontents.close();
					}
					catch (IOException ioe) {
					}
				}
				catch (CoreException e) {
					MessageDialog.openError(fshell, "Error", "Could not save file: " + e.getMessage());
					return;
				}
				catch (OperationCanceledException e) {
					return;
				}
			}
		};
		try {
			operation.run(null);
		}
		catch (InterruptedException e) {
		}
		catch (InvocationTargetException ite) {
		}
	}

	private static class CSStats {

		final XArchFlatQueryInterface xarch;
		final ObjRef csRef;

		Set<String> comps = new HashSet<String>();
		Set<String> conns = new HashSet<String>();
		Set<String> ifaces = new HashSet<String>();
		int links = 0;

		public CSStats(XArchFlatQueryInterface xarch, ObjRef csRef) {
			this.xarch = xarch;
			this.csRef = csRef;
		}

		public void add(CSStats o) {
			comps.addAll(o.comps);
			conns.addAll(o.conns);
			ifaces.addAll(o.ifaces);
			links += o.links;
		}

		public String toString() {
			return "" + (csRef == null ? "Total:" : XadlUtils.getDescription(xarch, csRef)) + "\t" + comps.size() + "\t" + conns.size() + "\t" + ifaces.size()
			        + "\t" + links;
		}
	}

	private static class MStats {

		final XArchFlatQueryInterface xarch;
		final ObjRef structRef;

		int comps = 0;
		int conns = 0;
		int ifaces = 0;
		int links = 0;
		int variants = 0;
		int ocomps = 0;
		int oconns = 0;
		int oifaces = 0;
		int olinks = 0;
		int ovariants = 0;
		Set<String> guards = new HashSet<String>();

		public MStats(XArchFlatQueryInterface xarch, ObjRef structRef) {
			this.xarch = xarch;
			this.structRef = structRef;
		}

		public void add(MStats o) {
			comps += o.comps;
			conns += o.conns;
			ifaces += o.ifaces;
			links += o.links;
			variants += o.variants;
			ocomps += o.ocomps;
			oconns += o.oconns;
			oifaces += o.oifaces;
			olinks += o.olinks;
			ovariants += o.ovariants;
			guards.addAll(o.guards);
		}

		public String toString() {
			return "" + (structRef == null ? "Total:" : XadlUtils.getDescription(xarch, structRef)) //
			        + "\t" + ocomps + "\t" + oconns + "\t" + oifaces + "\t" + olinks + "\t" + ovariants //
			        + "\t" + comps + "\t" + conns + "\t" + ifaces + "\t" + links + "\t" + variants //
			        + "\t" + guards.size();
		}
	}

	public void dumpStats(ObjRef xArchRef) {
		if (!xarch.getOverviewMode(xArchRef) || xarch.getAppliedChangeSetRefs(xArchRef).length > 0)
			System.err.println("Architecture must be in overview mode with no applied change sets.");
		getTypeForSegmentCache.clear();

		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "ArchChangeSets", xArchRef);
		if (archChangeSetsRef != null) {
			Set<ObjRef> variantCSRefs = new HashSet<ObjRef>();
			for (ObjRef rRef : xarch.getAll(archChangeSetsRef, "relationship")) {
				if (xarch.isInstanceOf(rRef, "changesets#VariantRelationship")) {
					for (ObjRef vRef : xarch.getAll(rRef, "variantChangeSet")) {
						ObjRef csRef = XadlUtils.resolveXLink(xarch, vRef);
						variantCSRefs.add(csRef);
					}
				}
			}
			CSStats total = new CSStats(xarch, null);
			Map<String, CSStats> allStats = new HashMap<String, CSStats>();
			for (ObjRef csRef : xarch.getAll(archChangeSetsRef, "changeSet")) {
				CSStats stats = new CSStats(xarch, csRef);
				scanCS(xArchRef, stats, (ObjRef) xarch.get(csRef, "XArchElement"), variantCSRefs.contains(csRef));
				total.add(stats);
				allStats.put(XadlUtils.getDescription(xarch, csRef), stats);
			}
			System.err.println();
			System.err.println(total);
			System.err.println(allStats.remove("Baseline"));
			String[] names = allStats.keySet().toArray(new String[0]);
			Arrays.sort(names);
			for (String name : names)
				System.err.println(allStats.get(name));
		}
		else {
			// Menage stats
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			Map<ObjRef, MStats> allStats = new HashMap<ObjRef, MStats>();
			for (ObjRef structureRef : xarch.getAllElements(typesContextRef, "ArchStructure", xArchRef)) {
				scanMStruct(xArchRef, structureRef, allStats);
			}

			MStats total = new MStats(xarch, null);
			for (MStats stats : allStats.values()) {
				stats.guards.remove(null);
				total.add(stats);
			}

			System.err.println();
			System.err.println(total);
			Map<String, MStats> namedStats = new HashMap<String, MStats>();
			for (Map.Entry<ObjRef, MStats> e : allStats.entrySet()) {
				String name = XadlUtils.getDescription(xarch, e.getKey());
				if (namedStats.containsKey(name))
					System.err.println("Duplicate name: " + name);
				namedStats.put(name, e.getValue());
			}

			String[] names = namedStats.keySet().toArray(new String[0]);
			Arrays.sort(names);
			for (String name : names)
				System.err.println(namedStats.get(name));

			System.err.println();
			System.err.println("Guards:");
			String[] guards = total.guards.toArray(new String[0]);
			Arrays.sort(guards);
			for (String guard : guards)
				System.err.println(guard);

		}
	}

	private void scanCS(ObjRef xArchRef, CSStats stats, ObjRef cSegment, boolean isVariant) {
		if (cSegment == null)
			return;

		if (xarch.isInstanceOf(cSegment, "changesets#ElementSegment")) {
			boolean resolvable = csadt.isElementSegmentResolvable(xarch.getXArch(cSegment), cSegment, true);
			String type = (String) xarch.get(cSegment, "type");
			//if (resolvable && type != null) {
			if (xarch.isAssignable("types#ArchStructure", type)) {
				// no connectors currently have substructures for the systems looked at
				stats.comps.add(getTypeForSegment(xArchRef, cSegment));
			}
			else if (xarch.isAssignable("types#Component", type) || xarch.isAssignable("types#ComponentType", type)) {
				stats.comps.add(getTypeForSegment(xArchRef, cSegment));
			}
			else if (xarch.isAssignable("types#Connector", type) || xarch.isAssignable("types#ConnectorType", type)) {
				stats.conns.add(getTypeForSegment(xArchRef, cSegment));
			}
			else if (xarch.isAssignable("types#Interface", type) || xarch.isAssignable("types#Signature", type)) {
				stats.ifaces.add(getTypeForSegment(xArchRef, cSegment));
			}
			else if (xarch.isAssignable("types#Link", type) || xarch.isAssignable("types#SignatureInterfaceMapping", type)) {
				stats.links++;
			}
			//}
		}

		if (xarch.getTypeMetadata(cSegment).getProperty("ChangeSegment") != null) {
			for (ObjRef ccSegment : xarch.getAll(cSegment, "ChangeSegment"))
				scanCS(xArchRef, stats, ccSegment, isVariant);
		}
	}

	Map<String, String> getTypeForSegmentCache = new HashMap<String, String>();
	private String getTypeForSegment(ObjRef xArchRef, ObjRef cSegment) {
		String reference = (String) xarch.get(cSegment, "reference");
		if(getTypeForSegmentCache.containsKey(reference)){
			String value = getTypeForSegment_(xArchRef, reference);
			getTypeForSegmentCache.put(reference, value);
		}
		return getTypeForSegment_(xArchRef, reference);
	}
	
	private String getTypeForSegment_(ObjRef xArchRef, String reference) {
		ObjRef typedRef = null;
		if (reference.startsWith("ID:")) {
			String id = reference.substring(3);
			ObjRef refObjRef = xarch.getByID(id);
			if (xarch.isInstanceOf(refObjRef, "types#Component") || xarch.isInstanceOf(refObjRef, "types#Connector")) {
				typedRef = XadlUtils.resolveXLink(xarch, refObjRef, "type");
				if(typedRef == null)
					// must be a module or internal interface
					return id;
			}
			else if (xarch.isInstanceOf(refObjRef, "types#Interface")) {
				typedRef = XadlUtils.resolveXLink(xarch, refObjRef, "signature");
				if(typedRef == null)
					// must be an interface on a module or internal interface
					return id;
			}
			else if (xarch.isInstanceOf(refObjRef, "types#ComponentType") || xarch.isInstanceOf(refObjRef, "types#ConnectorType")) {
				typedRef = refObjRef;
			}
			else if (xarch.isInstanceOf(refObjRef, "types#Signature")) {
				typedRef = refObjRef;
			}
			else if (xarch.isInstanceOf(refObjRef, "types#ArchStructure")) {
				for(ObjRef linkRef : xarch.getReferences(xArchRef, id)){
					if("archStructure".equals(xarch.getElementName(linkRef))){
						typedRef = xarch.getAllAncestors(linkRef)[2];
						break;
					}
				}
			}
		}
		return (String)xarch.get(typedRef, "id");
	}

	private void scanMStruct(ObjRef xArchRef, ObjRef structureRef, Map<ObjRef, MStats> allStats) {
		if (structureRef == null)
			return;
		MStats stats = new MStats(xarch, structureRef);
		allStats.put(structureRef, stats);

		for (ObjRef brickRef : Arrays.asList(xarch.getAll(structureRef, "Component"))) {
			if (isOptional(xarch, brickRef)) {
				stats.guards.add(getGuard(xarch, brickRef));
				if (isVariant(xarch, brickRef))
					stats.ovariants++;
				else
					stats.ocomps++;
			}
			else {
				if (isVariant(xarch, brickRef))
					stats.variants++;
				else
					stats.comps++;
			}
			for (ObjRef objRef2 : xarch.getAll(brickRef, "interface")) {
				if (isOptional(xarch, objRef2)) {
					stats.guards.add(getGuard(xarch, brickRef));
					stats.oifaces++;
				}
				else
					stats.ifaces++;
			}
			scanMType(xArchRef, XadlUtils.resolveXLink(xarch, brickRef, "type"), allStats);
		}
		for (ObjRef brickRef : Arrays.asList(xarch.getAll(structureRef, "Connector"))) {
			if (isOptional(xarch, brickRef)) {
				stats.guards.add(getGuard(xarch, brickRef));
				if (isVariant(xarch, brickRef))
					stats.ovariants++;
				else
					stats.oconns++;
			}
			else {
				if (isVariant(xarch, brickRef))
					stats.variants++;
				else
					stats.conns++;
			}
			for (ObjRef objRef2 : xarch.getAll(brickRef, "interface")) {
				if (isOptional(xarch, objRef2)) {
					stats.guards.add(getGuard(xarch, brickRef));
					stats.oifaces++;
				}
				else
					stats.ifaces++;
			}
			scanMType(xArchRef, XadlUtils.resolveXLink(xarch, brickRef, "type"), allStats);
		}
		for (ObjRef objRef : xarch.getAll(structureRef, "link")) {
			if (isOptional(xarch, objRef)) {
				stats.guards.add(getGuard(xarch, objRef));
				stats.olinks++;
			}
			else
				stats.links++;
		}
	}

	private void scanMType(ObjRef xArchRef, ObjRef brickTypeRef, Map<ObjRef, MStats> allStats) {
		if (brickTypeRef == null || allStats.containsKey(brickTypeRef))
			return;
		MStats stats = new MStats(xarch, brickTypeRef);
		allStats.put(brickTypeRef, stats);

		for (ObjRef sigRef : xarch.getAll(brickTypeRef, "Signature")) {
			if (isOptional(xarch, sigRef)) {
				stats.guards.add(getGuard(xarch, sigRef));
				stats.oifaces++;
			}
			else
				stats.ifaces++;
		}
		ObjRef subArchitectureRef = (ObjRef) xarch.get(brickTypeRef, "subArchitecture");
		if (subArchitectureRef != null) {
			for (ObjRef objRef : xarch.getAll(subArchitectureRef, "SignatureInterfaceMapping")) {
				if (isOptional(xarch, objRef))
					stats.olinks++;
				else
					stats.links++;
			}
		}
		if (xarch.isInstanceOf(brickTypeRef, "variants#VariantComponentType") || xarch.isInstanceOf(brickTypeRef, "variants#VariantConnectorType")) {
			for (ObjRef variantRef : xarch.getAll(brickTypeRef, "Variant")) {
				stats.guards.add(getGuard(xarch, variantRef));
				scanMType(xArchRef, XadlUtils.resolveXLink(xarch, variantRef, "variantType"), allStats);
			}
		}
	}

	private boolean isOptional(XArchChangeSetInterface xarch, ObjRef srcRef) {
		return getGuard(xarch, srcRef) != null;
	}

	private String getGuard(XArchChangeSetInterface xarch, ObjRef srcRef) {
		ObjRef objRefWithGuard = null;
		if (objRefWithGuard == null) {
			IXArchTypeMetadata srcType = xarch.getTypeMetadata(srcRef);
			IXArchPropertyMetadata guardProperty = srcType.getProperty("guard");
			if (guardProperty != null && xarch.isAssignable("options#Guard", guardProperty.getType())) {
				objRefWithGuard = srcRef;
			}
		}
		if (objRefWithGuard == null) {
			IXArchTypeMetadata srcType = xarch.getTypeMetadata(srcRef);
			IXArchPropertyMetadata optionalProperty = srcType.getProperty("optional");
			if (optionalProperty != null && xarch.isAssignable("options#Optional", optionalProperty.getType())) {
				ObjRef optionalRef = (ObjRef) xarch.get(srcRef, "optional");
				if (optionalRef != null) {
					objRefWithGuard = optionalRef;
				}
			}
		}
		if (objRefWithGuard != null)
			return ComparableBooleanGuardConverter.booleanGuardToString(xarch, objRefWithGuard);
		return null;
	}

	private boolean isVariant(XArchChangeSetInterface xarch, ObjRef brickRef) {
		ObjRef brickTypeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");
		if (xarch.isInstanceOf(brickTypeRef, "variants#VariantComponentType") || xarch.isInstanceOf(brickTypeRef, "variants#VariantConnectorType")) {
			return xarch.getAll(brickTypeRef, "Variant").length > 0;
		}
		return false;
	}
}
