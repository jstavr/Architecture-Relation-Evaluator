package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logics.editing;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ActivityDiagramsEditorContextMenuAddDiagramLogic
	extends AbstractThingLogic
	implements IBNAMenuListener{

	final protected XArchFlatInterface xarch;

	final protected ObjRef xArchRef;

	public ActivityDiagramsEditorContextMenuAddDiagramLogic(XArchFlatInterface xarch, ObjRef xArchRef){
		this.xarch = xarch;
		this.xArchRef = xArchRef;
	}

	public boolean matches(IBNAView view, IThing t){
		String xArchID = getXArchID(view, t);
		if(xArchID != null){
			ObjRef objRef = xarch.getByID(xArchRef, xArchID);
			if(objRef != null){
				return xarch.isInstanceOf(objRef, "activitydiagrams#ActivityDiagramReference");
			}
		}
		return false;
	}

	public String getXArchID(IBNAView view, IThing t){
		String xArchID = null;
		if(t != null){
			if(xArchID == null){
				xArchID = t.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
			}
			if(xArchID == null){
				IAssembly assembly = AssemblyUtils.getAssemblyWithPart(t);
				if(assembly != null){
					xArchID = assembly.getRootThing().getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
				}
			}
		}
		return xArchID;
	}

	/**
	 * This adds the contextMenu entry to add a Diagram public void
	 * addDiagram(IBNAView view, IThing t, ObjRef ref){ String elementName =
	 * xarch.getElementName(ref); if(elementName != null){ ObjRef parentRef =
	 * xarch.getParent(ref); if(parentRef != null){ xarch.remove(parentRef,
	 * elementName, ref); } } }
	 */

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		IThing[] selectedThings = BNAUtils.getSelectedThings(view.getWorld().getBNAModel());
		if(selectedThings.length > 1){
			return;
		}

		if(matches(view, t)){
			for(IAction action: getActions(view, t, worldX, worldY)){
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;

		final String eltXArchID = getXArchID(view, t);

		/*
		 * if(eltXArchID == null){ //Nothing to set description on return new
		 * IAction[0]; }
		 */

		final ObjRef eltRef = xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			//Nothing to set description on
			return new IAction[0];
		}

		Action addDiagramAction = new Action("Add Activity Diagram"){

			@Override
			public void run(){
				//addDiagram(fview, ft, eltRef);
				chooseAndAssignDiagram(fview, ft, eltRef);
			}
		};

		return new IAction[]{addDiagramAction};
	}

	protected void chooseAndAssignDiagram(IBNAView view, IThing thingToEdit, ObjRef eltRef){

		//Composite c = BNAUtils.getParentComposite(view);
		// Get the diagrams ObjRef
		final ArrayList adlist = new ArrayList();
		final ArrayList adnames = new ArrayList();
		ObjRef openArchitectures[] = xarch.getOpenXArches();
		for(ObjRef arch: openArchitectures){
			ObjRef activitydiagramsContextRef = xarch.createContext(xArchRef, "activitydiagrams");
			ObjRef[] activitydiagramRefs = xarch.getAllElements(activitydiagramsContextRef, "activityDiagram", xArchRef);
			for(ObjRef ref: activitydiagramRefs){
				adlist.add(ref);

				String description = XadlUtils.getDescription(xarch, ref);
				if(description == null){
					description = "[No Description]";
				}
				//System.out.println("  description=" + description);
				adnames.add(description);

			}
		}
		;
		//System.out.println("lists:");
		//System.out.println(adlist);
		//System.out.println("names:");
		//System.out.println(adnames);

		IThing things[] = view.getWorld().getBNAModel().getAllThings();
		ObjRef diagrams[];
		MyDialog d = new MyDialog(BNAUtils.getParentComposite(view).getShell(), adlist, adnames);
		Object selected = d.open();
		//System.out.println("Post shell, selected(" + selected + ")=" + d.getSelectedString());

		if(selected != null){
			addDiagram2xadl(selected, eltRef);
		}

		/*
		 * ColorSelectorDialog csd = new
		 * ColorSelectorDialog(BNAUtils.getParentComposite(view).getShell());
		 * RGB rgb = csd.open(initialRGB); if(rgb != null){ assignColor(view,
		 * thingsToEdit, rgb); }
		 */
	}

	protected void addDiagram2xadl(Object selected, ObjRef eltRef){

		//System.out.println("addDiagram2xadl");
		//System.out.println("eltRef="+ eltRef);
		//String id = XadlUtils.getID(xarch, eltRef);
		//System.out.println("id="+ id);
		//String desc = XadlUtils.getDescription(xarch, eltRef, "none");
		//System.out.println("description="+ desc);

		//String id2 = XadlUtils.getID(xarch, (ObjRef) selected);
		//System.out.println("AD traget id="+ id2);
		//String descAD = XadlUtils.getDescription(xarch, (ObjRef) selected, "none");
		//System.out.println("description AD="+ descAD);
		ObjRef pointer = (ObjRef)xarch.get(eltRef, "diagramPointer");
		if(pointer != null){
			//System.out.println("have XLink");
		}
		else{
			//System.out.println("NULL XLink");
			//ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
			//XadlUtils.setXLink(xarch, ref, linkElementName, targetRef)
			//ObjRef activityRef = xarch.create(typesContextRef, "activityDiagramReference");

			XadlUtils.setXLink(xarch, eltRef, "diagramPointer", (ObjRef)selected);
			//xarch.createElement(contextObjectRef, typeOfThing)
			//ObjRef ptype =xarch.create(typesContextRef, "diagramPointer");
			//System.out.println("ptype created =" + ptype);
			//ObjRef linkRef = xarch.create(typesContextRef, "diagramPointer");
			//System.out.println("linkRef created =" + linkRef);
		}

		//XadlUtils.setXLink(xarch, eltRef, "adlink", (ObjRef) selected);

		//	ObjRef typesContextRef = xarch.createContext(xArchRef, "activitydiagrams");
		//ObjRef activityFinalRef = xarch.create(typesContextRef, "controlFlow");
		//xarch.set(activityFinalRef, "id", UIDGenerator.generateUID("controlFlow"));
		//XadlUtils.setDescription(xarch, activityFinalRef, "[New ControlFlow]");
		//xarch.add(activityDiagramRef, "controlFlow", activityFinalRef);
		//	XadlUtils.setXLink(xarch, eltRef, "adlink", (ObjRef) selected);
		//XadlUtils.setDescription(xarch, eltRef, newValue);

	}

	public class MyDialog
		extends Dialog{

		Object result;
		Display display;
		List list;
		Canvas canvas;
		//Button start, stop, check;
		Text text;
		Label label;
		//Error [] errors = new Error [0];
		//Object [] objects = new Object [0];
		String selectedValue;
		Object selectedObject;
		boolean canceled = false;
		boolean selected = false;
		Object adnames[];
		Object adobj[];

		public MyDialog(Shell parent, int style){
			super(parent, style);
		}

		public MyDialog(Shell parent, ArrayList ADobjRefList, ArrayList ADnamesList){
			//adnames = ADnamesList.toArray();
			this(parent, 0); // your default style bits go here (not the Shell's style bits)
			adnames = ADnamesList.toArray();
			adobj = ADobjRefList.toArray();
			//adnames2 =(String[])ADnamesList.toArray();
		}

		public Object open(){
			Shell parent = getParent();
			final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			//shell.setText(getText());
			shell.setText("Select Activity Diagram");
			shell.setLayout(new FillLayout());
			Composite mainComposite = new Composite(shell, SWT.NONE);
			mainComposite.setLayout(new GridLayout(1, false));

			Composite cFind = new Composite(mainComposite, SWT.NONE);
			cFind.setLayout(new GridLayout(3, false));

			Label lFind = new Label(cFind, SWT.NONE);
			lFind.setText("Activity Diagram:");
			//String initialValue = "AD1";
			//final Text tFind = new Text(cFind, SWT.BORDER);
			//final Text tFind = new Text(cFind, SWT.MULTI | SWT.BORDER);
			//if(initialValue != null){
			//	tFind.setText(initialValue);
			//}
			/*
			 * String buffer = ""; for (int i = 0; i < 100; i++) { buffer +=
			 * "This is line " + i + "\r\n"; } tFind.setText(buffer);
			 */
			String tempnames[] = new String[adnames.length];
			for(int j = 0; j < adnames.length; j++){
				tempnames[j] = (String)adnames[j];
			}

			final String items[] = tempnames;
			//List l = new List(cFind, SWT.MULTI );
			//l.setSelection(items);
			final Combo cbSelectScheme = new Combo(cFind, SWT.READ_ONLY);
			cbSelectScheme.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
			cbSelectScheme.setItems(items);
			cbSelectScheme.select(-1);

			cbSelectScheme.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent e){
					int index = cbSelectScheme.getSelectionIndex();

					//System.out.println("INDEX=" + index);
					// now set the Objref to the AD ref
					//System.out.println("Selected=" + items[index]);
					selectedValue = items[index];
					selectedObject = adobj[index];
					canceled = false;

				}

				public void widgetDefaultSelected(SelectionEvent e){
					widgetSelected(e);
				}
			});

			final Button bSelect = new Button(cFind, SWT.PUSH);
			bSelect.setText("Select");
			bSelect.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent e){

					selected = true;
					shell.dispose();
				}

				public void widgetDefaultSelected(SelectionEvent e){
					widgetSelected(e);
				}
			});

			Button bCancel = new Button(cFind, SWT.NONE);
			bCancel.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false));
			bCancel.setText("Cancel");
			bCancel.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent e){
					canceled = true;
					shell.dispose();
				}

				public void widgetDefaultSelected(SelectionEvent e){
					widgetSelected(e);
				}
			});

			shell.open();
			display = parent.getDisplay();
			while(!shell.isDisposed()){
				if(!display.readAndDispatch()){
					display.sleep();
				}
			}
			result = selectedObject;
			return result;
		}

		public String getSelectedString(){
			return selectedValue;
		}
	}
}
