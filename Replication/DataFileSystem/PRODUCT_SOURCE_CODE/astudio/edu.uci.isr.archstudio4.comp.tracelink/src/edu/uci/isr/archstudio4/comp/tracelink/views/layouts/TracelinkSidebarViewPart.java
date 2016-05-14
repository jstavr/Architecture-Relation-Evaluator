/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.eclipse.jface.viewers.TableViewer;

import edu.uci.isr.archstudio4.comp.tracelink.TracelinkViewMyxComponent;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.SimpleTracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.TraceRecorder;
import edu.uci.isr.archstudio4.comp.tracelink.preferences.PreferencesView;
import edu.uci.isr.archstudio4.comp.tracelink.publishextract.PublishExtractLinkView;
import edu.uci.isr.archstudio4.comp.tracelink.reports.ReportsView;
import edu.uci.isr.archstudio4.comp.tracelink.views.IWidget;
import edu.uci.isr.archstudio4.comp.tracelink.views.LinkAttributeFilterBoxView;
import edu.uci.isr.archstudio4.comp.tracelink.views.LinkTableView;
import edu.uci.isr.archstudio4.comp.tracelink.views.TracelinkDescriptionView;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.MyxViewPart;
import edu.uci.isr.archstudio4.comp.tracelink.models.ISelectionModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.SelectionModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.XMLSerializer;

import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * This goal of this class is to describe where each view 
 * component on the TracelinkSidebar should be placed, this class 
 * should not contain any logic, nor describe any one view
 * @author dpurpura
 *
 */
public class TracelinkSidebarViewPart extends MyxViewPart<TracelinkViewMyxComponent>
implements ISelectionListener, IPartListener, IWidget {

	//protected SimpleTracelinkController controller;			
	protected ITracelinkController controller;
	protected IExplicitADT explicit;
	protected Composite notificationComposite;
	protected Object ignoreEventsLock;
	protected Collection<Object> myxMapped;
	protected LinkTableView viewer;
	
	//assume the archTracelink node does not exist
	private Boolean existArchTracelink = false;
	
	//keep track of the most recent selection in Archipelago 
	protected String archSelection;

	
	protected TableViewer tableviewer;
	protected PageBook pagebook;


	//H
	private Button recordButton;
	private Boolean recordOn = false;
	private Collection<ISelectionModel> selectionList;
	private String openXADLFile;
	

	public TracelinkSidebarViewPart() {
		super(TracelinkViewMyxComponent.class);

		ignoreEventsLock = new Object();
		myxMapped = new ArrayList<Object>();

	}


	protected void myxMap(Object o){
		if(o != null){
			myxMapped.add(o);
			//MyxRegistry.getSharedInstance().map(controller, o);
		}
	}

	
	public void createMyxPartControl(Composite parent){
		//controller = MyxRegistry.getSharedInstance().waitForBrick(SimpleTracelinkController.class);
		controller = brick.getController();
		
		parent.setLayout(new FillLayout());


		ExpandBar expand = new ExpandBar(parent, SWT.V_SCROLL);
		final int layoutStyle = GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL; 
		expand.setLayoutData(new GridData(layoutStyle));

		notificationComposite = expand;

		createMainMyxPartControl(expand);

		//temp comment this out
		//getSite().getPage().addPartListener(this);
		//getSite().getPage().addPostSelectionListener(this);
		
		//getSite().getPage().addSelectionListener(this);
		//getSite().getWorkbenchWindow().getActivePage();
		
		//getSite().getWorkbenchWindow().getActivePage().addPostSelectionListener(this);
		
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);

		//getSite().getWorkbenchWindow().getActivePage().getActiveEditor()
		
		
		
	}

	
	public void dispose(){
		for(Object o: myxMapped){
			//MyxRegistry.getSharedInstance().unmap(controller, o);
		}

		//getSite().getPage().removePartListener(this);

		super.dispose();

		//H added - remove the selection listener
		//getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);

	}

	
	public void setMyxFocus(){

	}

	private ExpandItem tableItem;
	private Composite tableComposite;
	
	protected void createMainMyxPartControl(Composite parent) {
		final int style = SWT.BORDER | SWT.RESIZE;
		final int layoutStyle = GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL; 

		GridLayout layout;
		Composite composite;
		
		
		// == EXPAND ITEM 0 =====================
		composite = new Composite(parent, SWT.None);
		
		displayDefineLinksPanel(composite, SWT.SHADOW_NONE, layoutStyle);

		ExpandItem item0 = new ExpandItem((ExpandBar)parent, SWT.NONE, 0);
		item0.setText("New Tracelink");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);

		// == EXPAND ITEM 1 =====================
		composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout ();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
		layout.verticalSpacing = 10;
		composite.setLayout(layout);

		composite.setLayoutData(new GridData(layoutStyle));
		composite.setLayout(new GridLayout(1, false));

		//new TracelinkDescriptionView(composite, style, controller);
		new LinkAttributeFilterBoxView(composite, style, controller);
		
		//pass in the editor part to handle changes in the BNA canvas 
		//so that selections in subarchitectures can be detected
		//removed - was causing errors
		
		//new LinkTableView(composite, style, controller, getViewSite(), traceviewComp.getHAdapter(), null);
		new LinkTableView(composite, style, controller, getViewSite(), brick.getHAdapter());
		//IEditorPart activePart = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
		//if (activePart != null)
		//	viewer = new LinkTableView(composite, style, controller, getViewSite(), traceviewComp.getHAdapter(), activePart);
		//else
		//	viewer = new LinkTableView(composite, style, controller, getViewSite(), traceviewComp.getHAdapter(), null);
		

		displayLinkOptionsPanel(composite, style, layoutStyle);

		ExpandItem item1 = new ExpandItem((ExpandBar)parent, SWT.NONE, 1);
		item1.setText("Tracelink Details");
		item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item1.setControl(composite);
		
		tableItem = item1;
		tableComposite = composite;

		// == EXPAND ITEM 2 =====================
		composite = new Composite(parent, SWT.None);
		RowLayout rowLayout = new RowLayout();
		rowLayout.justify = true;
		composite.setLayout(rowLayout);


		Button traceReportsButton = new Button(composite, SWT.None);
		traceReportsButton.setText("Trace Analysis");
		traceReportsButton
		.addSelectionListener(new TraceReportsButtonSelectionAdapter());

		Button tracePreferencesButton = new Button(composite, SWT.NONE);
		tracePreferencesButton.setText("Trace Preferences");
		tracePreferencesButton
		.addSelectionListener(new TracePreferencesSelectionAdapter());

		ExpandItem item2 = new ExpandItem((ExpandBar)parent, SWT.NONE, 2);
		item2.setText("Tracelink Options");
		item2.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item2.setControl(composite);


		//Expand expand items

		item0.setExpanded(true);
		item1.setExpanded(true);
		item2.setExpanded(true);

		//NOTE: we want to register this view last, so that the layout manager 
		//knows the correct sizes of the views on the sidebar
		controller.registerView(this);
	}

	/**
	 * @param parent
	 * @param style
	 */
	private void displayDefineLinksPanel(Composite parent, final int style, int layoutStyle) {
		RowLayout layout = new RowLayout();
		layout.justify = true;
		parent.setLayout(layout);

		//H - modified
		recordButton = new Button(parent, SWT.None);

		recordButton.setText("Start Recording");
		recordButton.addSelectionListener(new RecordButtonSelectionAdapter());

		Button recoverButton = new Button(parent, SWT.NONE);
		recoverButton.setText("Recover");
		recoverButton.addSelectionListener(new RecoverButtonSelectionAdapter());

		Button newLinkButton = new Button(parent, SWT.None);
		newLinkButton.setText("Manual");
		newLinkButton.addSelectionListener(new NewLinkButtonSelectionAdapter());
	}

	/**
	 * @param style
	 * @param parent
	 */
	private void displayLinkOptionsPanel(Composite parent, final int style, int layoutStyle) {
		Composite linkOptions = new Composite(parent, style);
		linkOptions.setLayoutData(new GridData(layoutStyle));

		RowLayout layout = new RowLayout();
		layout.justify = true;
		linkOptions.setLayout(layout);

		Button extractLinksButton = new Button(linkOptions, SWT.None);
		extractLinksButton.setText("Import Links");
		extractLinksButton
		.addSelectionListener(new ExtractLinksButtonSelectionAdapter());

		Button publishLinksButton = new Button(linkOptions, SWT.NONE);
		publishLinksButton.setText("Export Links");
		publishLinksButton
		.addSelectionListener(new PublishLinksButtonSelectionAdapter());

	}


	protected static GridData excludeGridData(){
		GridData d = new GridData();
		d.exclude = true;
		return d;
	}

	private class RecordButtonSelectionAdapter extends SelectionAdapter {
		private TraceRecorder recorder;
		

		public RecordButtonSelectionAdapter() {
			//recorder = controller.getTraceRecorder();
			
			
		}

		
		public void widgetSelected(SelectionEvent e) {
			/*
			if (recorder.isRecording()) { 
				recorder.setRecording(false);
				setContentDescription("Recording OFF");
				recordButton.setText("Start Recording");
				controller.updateViews();
			}
			else {
				recorder.setRecording(true);
				setContentDescription("Recording Trace Links");
				recordButton.setText("Stop Recording");
			}
			*/
			checkLinkContainer();
			if (recordOn) {
				recordOn = false;
				setContentDescription("Recording OFF");
				recordButton.setText("Start Recording");			
				//serialize the vector
				//String filename = "sessionLog.xml";
				//for debugging
				System.out.println("---------RECORD OFF SELECTION:----------------------- ");
				for (ISelectionModel selection: selectionList) {
					System.out.println(selection.getElement());
				}
				System.out.println("---------end RECORD OFF SELECTION:----------------------- ");				

				
				brick.invokeRecordView(notificationComposite.getShell(), selectionList, archSelection);
				

				
			}
			else {
				recordOn = true;
				setContentDescription("Recording Trace Links");
				recordButton.setText("Stop Recording");
				
				selectionList = new Vector<ISelectionModel>();

			}
			
		}
	}

	//H: 5/27/08
	private class RecoverButtonSelectionAdapter extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Button Pushed: Recover Button");
			checkLinkContainer();
			brick.invokeRecoverView(notificationComposite.getShell(), archSelection);
			System.out.println("Recover done"); 

		}
	}

	private class NewLinkButtonSelectionAdapter extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Button Pushed: Manual Link Button");
			checkLinkContainer();
			brick.invokeManualView(notificationComposite.getShell(), archSelection);
			System.out.println("Manual Link Done");
		}
	}

	private class PublishLinksButtonSelectionAdapter extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Button Pushed: Extract Links Button");

			FileDialog fd = new FileDialog(notificationComposite.getShell(), SWT.SAVE);
			fd.setText("Export XArch");
			fd.setFilterPath(System.getProperty("user.home"));
			String[] filterExt = { "*.xml", "*.*" };
			fd.setFilterExtensions(filterExt);

			String filename = fd.open();

			if (filename != null) {
				System.out.println("Exporting to: " + filename + "...");
				//controller.getXADLFacade().writeXArchToFile(filename);
				controller.exportLinks(filename);
			}
		}
	}

	private class ExtractLinksButtonSelectionAdapter extends SelectionAdapter {
		private MyxRegistry myxr = MyxRegistry.getSharedInstance();
		private PublishExtractLinkView publisher;

		public ExtractLinksButtonSelectionAdapter() {
			publisher = myxr.waitForBrick(PublishExtractLinkView.class);
		}
		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Button Pushed: Publish Links Button");

			publisher.invokePublishExtractView(notificationComposite.getShell());
		}
	}

	private class TraceReportsButtonSelectionAdapter extends SelectionAdapter {
		private MyxRegistry myxr = MyxRegistry.getSharedInstance();
		private ReportsView reports;

		public TraceReportsButtonSelectionAdapter() {
			reports = myxr.waitForBrick(ReportsView.class);

		}

		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Button Pushed: Trace Reports Button");
			String[] attributeNames = controller.getAttributeNames();
			reports.invokeReportView(notificationComposite.getShell(), attributeNames);
		}
	}

	private class TracePreferencesSelectionAdapter extends SelectionAdapter {
		private MyxRegistry myxr = MyxRegistry.getSharedInstance();
		private PreferencesView view;

		public TracePreferencesSelectionAdapter() {
			view = myxr.waitForBrick(PreferencesView.class);
			view.setITracelinkController(controller);
		}

		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Button Pushed: Trace Preferences Button");
			view.invokePrefView(notificationComposite.getShell());
			
		}
	}

	public void partActivated(IWorkbenchPart part) {
		
		if(part instanceof IEditorPart){
			IEditorPart editorPart = (IEditorPart)part;
			//to handle subarchitectures - commented out because we're getting null
			//viewer.setInput(editorPart);
			/*
			if(editorPart.getEditorInput() instanceof IFileEditorInput){
				IFileEditorInput fileEditorInput = (IFileEditorInput)editorPart.getEditorInput();
				String filename = fileEditorInput.getFile().getFullPath().makeAbsolute().toString();
				controller.setXArchRef(filename);
			*/
			if(editorPart.getEditorInput() instanceof IURIEditorInput) {
				IURIEditorInput uriEditorInput = (IURIEditorInput)editorPart.getEditorInput();
				controller.setXArchRef(uriEditorInput.getURI().toString());
				//force a check on the existence of ArchTracelink node
				existArchTracelink = false;
			}
		}
	}

	
	public void partBroughtToTop(IWorkbenchPart part) {

	}

	
	public void partClosed(IWorkbenchPart part) {
		//to handle subarchitectures
		//comment out - was causing errors
		//if(part instanceof IEditorPart){
		//	//IEditorPart editorPart = (IEditorPart)part;
		//	viewer.setInput(null);
		//}
	}

	
	public void partDeactivated(IWorkbenchPart part) {

	}

	
	public void partOpened(IWorkbenchPart part) {

	}


	/*
	 * The following code deals with updating the xArchRef in order to accommodate
	 * referencing subarchitectures.  This uses global variable openXADLFile
	 
	private void updateXArchRef() {
		controller.setXArchRef(controller.getXArchFlatInterface().getOpenXArch(openXADLFile));
		controller.updateViews();
	}
	*/

	/*
	 * H: The code below deals with listening and capturing selection events
	 */

	public synchronized void  selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
		// we ignore our own selections
		if (sourcepart != TracelinkSidebarViewPart.this) {
			showSelection(sourcepart, selection);
		}
	}


	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
		//System.out.println("@@@@Selectionchanged ");
		//if (controller.getTraceRecorder().isRecording()) {
		Date timestamp;
		if(recordOn) {
			String selectedFile = sourcepart.getTitle();
			//temporarily remove this
			//controller.getTraceRecorder().record(selectedFile, selection);

			SelectionModel selectionModel = new SelectionModel(sourcepart.toString(), selection.toString(), new Date());
			selectionModel.setSelectedItem(selection);
			//selectionModel.setSelectedItem(objId);
			selectionList.add(selectionModel);
			
			System.out.println("@@@@Recording " + sourcepart.toString());
			//System.out.println("@@@@Recording " + selection.toString());
			System.out.println("@@@@Recording " + selectionModel.getSelectedItem().toString());
		}
		else {
			//display the links as components are selected 
			if (selection instanceof IStructuredSelection) {
				//make sure that we are reading from the correct XArchRef
				//updateXArchRef();
				IStructuredSelection ss = (IStructuredSelection) selection;
				Object o;
				int count = 0;			//temp - remove later
				for (Iterator it = ss.iterator(); it.hasNext();) {

					o = it.next();
					System.out.println(count + "ss " + o.toString());
					
					//if (o instanceof ObjRef) {
					String selectedItem = o.toString();
					if (selectedItem.contains("ObjRef")) {
						//note: we're assuming that the objref is surrounded by square brackets
						//TODO: need to handle cases where multiple objRefs are selected
						//		this currently only handles one selection at a time
						/*
						int beginIndex = selectedItem.lastIndexOf("[") + 1;
						int lastIndex = selectedItem.indexOf("]");
						if (beginIndex != -1 && lastIndex != -1) {
							selectedItem = o.toString().substring(beginIndex, lastIndex); 
							
							//transform the string to an objref
							ObjRef selectionRef = new ObjRef(selectedItem);
							
							//TODO: remove this pointer to XADLFacade
							//String objId = controller.getXADLFacade().getID((ObjRef)o);
							String objId = controller.getXADLFacade().getID(selectionRef);
							*/
							String objId = controller.getSelectionID(selectedItem);
							System.out.println("selected " + objId);
							archSelection = objId;
							if (!objId.contains("archStructure")) {
								controller.setEndpointHref(objId);
								controller.updateViews();
							}
						
							
							//handle selection in a subarchitecture
							//no longer need this
							/*
							else if (objId.contains("archStructure")) {
								if(sourcepart instanceof IEditorPart){
									IEditorPart editorPart = (IEditorPart)sourcepart;
									//to handle subarchitectures
									viewer.setInput(editorPart); 
									//TODO? add controllere.updateViews();
								}
							}
							*/
							
							//System.out.println("clicked on a component" + ObjId);
						
					}
					count++;
				}
			}
			else {
				System.out.println("####Selection " + sourcepart.toString());
				System.out.println("####Selection " + selection.toString());
			}
				
		}	
	}

	
	
	public Widget getWidget() {
		return notificationComposite;
	}

	
	public void update() {
		tableItem.setHeight(tableComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			
		notificationComposite.layout();
	}

	/*
	 * This method checks whether the main tracelink container node (i.e. ArchTraceLinks) exists.
	 * This method is called before each time a link is added
	 */	
	private void checkLinkContainer() {
		if (existArchTracelink == false) {
			if (controller.hasLinkContainer())
				existArchTracelink = true;
			else
				controller.addLinkContainer();
		}
			
			
		
		
	}

}
