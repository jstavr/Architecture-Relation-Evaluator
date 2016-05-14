/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.publishextract;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceLink;

/**
 * @author dpurpura
 */
public class ExtractViewer
	extends Composite{

	//private ITracelinkController controller;
	protected IXADLFacade xadlFacade;
	private Table table;
	private HashMap<Integer, String> checkedLinks;

	private ArrayList<ITracelinkModel> tracelinks;

	//public ExtractViewer(Composite parent, ITracelinkController controller, ITracelinkModel[] links) {
	public ExtractViewer(Composite parent, IXADLFacade xadlFacade, ITracelinkModel[] links){
		super(parent, SWT.None);

		//this.controller = controller;
		this.xadlFacade = xadlFacade;
		this.checkedLinks = new HashMap<Integer, String>();

		this.tracelinks = new ArrayList<ITracelinkModel>();
		for(ITracelinkModel link: links){
			tracelinks.add(link);
		}

		this.setLayout(new RowLayout(SWT.VERTICAL));
		displayDiff(this, tracelinks);
	}

	public void displayDiff(Composite parent, ArrayList<ITracelinkModel> tracelinks){
		table = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.addListener(SWT.Selection, new CheckListener());

		populateTable(tracelinks);

		Button addButton = new Button(parent, SWT.None);
		addButton.setText("Import");
		addButton.addListener(SWT.Selection, new AddSelectionListener());
	}

	/**
	 * @param tracelinks
	 */
	private void populateTable(ArrayList<ITracelinkModel> tracelinks){
		String elem;
		TableItem item;

		String location;
		for(ITracelinkModel tracelink: tracelinks){
			item = new TableItem(table, SWT.NONE);
			elem = "";

			for(ITraceEndpointModel endpoint: tracelink.getEndpointList()){
				location = endpoint.getLocationHref();

				if(location != null && !location.equals("")){
					elem += location + "; ";
				}
			}

			if(elem.equals("")){
				elem = "None";
			}

			item.setText(elem);
		}
	}

	private class CheckListener
		implements Listener{

		public void handleEvent(Event event){
			if(event.item instanceof TableItem){
				TableItem item = (TableItem)event.item;

				int index = table.indexOf(item);
				String attribute = item.getText();

				if(event.detail == SWT.CHECK){
					if(checkedLinks.containsKey(index)){
						checkedLinks.remove(index);
					}
					else{
						checkedLinks.put(index, attribute);
					}
				}
			}
		}
	}

	private class AddSelectionListener
		implements Listener{

		public void handleEvent(Event event){
			System.out.println("Importing the following tracelinks:");
			for(int i: checkedLinks.keySet()){
				System.out.println(checkedLinks.get(i));
				//controller.addTraceLinks(tracelinks.get(i));
				xadlFacade.addTraceLinks(tracelinks.get(i));
			}
			getShell().close();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Shell");
		shell.setSize(400, 500);
		shell.setLayout(new FillLayout());

		ITracelinkModel link = new TraceLink();

		ITraceEndpointModel te = new TraceEndpoint();
		te.setLocationHref("C:/temp");
		link.addEndpoint(te);

		te = new TraceEndpoint();
		te.setLocationHref("C:/temp1");
		link.addEndpoint(te);

		te = new TraceEndpoint();
		te.setLocationHref("C:/temp1");
		link.addEndpoint(te);

		ITracelinkModel link2 = new TraceLink();

		te = new TraceEndpoint();
		te.setLocationHref("C:/temp");
		link2.addEndpoint(te);

		te = new TraceEndpoint();
		te.setLocationHref("C:/temp1");
		link2.addEndpoint(te);

		te = new TraceEndpoint();
		te.setLocationHref("C:/temp1");
		link2.addEndpoint(te);

		ITracelinkModel[] links = {link, link2};

		ExpandBar bar = new ExpandBar(shell, SWT.NONE);
		Composite view = new ExtractViewer(bar, null, links);

		ExpandItem item = new ExpandItem(bar, SWT.NONE);

		item.setText("Generic View");
		item.setHeight(view.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(view);
		item.setExpanded(true);

		shell.open();
		while(!shell.isDisposed()){
			if(!display.readAndDispatch()){
				display.sleep();
			}
		}
		display.dispose();

	}

}
