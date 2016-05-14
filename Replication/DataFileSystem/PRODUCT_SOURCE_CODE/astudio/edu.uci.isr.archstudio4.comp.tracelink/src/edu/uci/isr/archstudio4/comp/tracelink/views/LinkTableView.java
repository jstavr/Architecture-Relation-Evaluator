package edu.uci.isr.archstudio4.comp.tracelink.views;


import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.LinkColumnSorterAdapter;
import edu.uci.isr.archstudio4.comp.tracelink.hypermedia.IHypermediaAdapter;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement;

//H: 5/24/08
//public class LinkTableView implements IWidget{
public class LinkTableView implements IWidget, Listener{

	
	private IViewSite viewSite;
	private Composite parent;
	private TableViewer viewer;
	private ITracelinkController controller;
	private IHypermediaAdapter hAdapter;		//H: 5/31/08
	//private IEditorPart activeEditor;			//H: 9/30/08 - to accommodate subarch.  removed
	
	private ICellModifier cellModifier;
	private ITableLabelProvider labelProvider;
	private int tableItemSelected;				//contains the selected row number in the table
	private int tableItemSelectedPrev = -1;		//previous selection
	
	
	public LinkTableView(Composite parent, int style, 
			ITracelinkController controller, IViewSite viewSite, IHypermediaAdapter hAdapter) {
			//IEditorPart activeEditor) {
		//this.parent = new Composite(parent, style | SWT.RESIZE);
		//this.parent.setLayout(new GridLayout(1, true));
		this.parent = parent;
		this.hAdapter = hAdapter;
		//this.activeEditor = activeEditor;
		
		parent.addControlListener(new TableSizeControlAdapter());
		
		this.viewer = new TableViewer(this.parent, 
				SWT.BORDER | SWT.FULL_SELECTION);
		
		this.controller = controller;
		this.viewSite = viewSite;
		
		this.cellModifier  = controller.getCellModifier(viewer.getTable());
		this.labelProvider = controller.getTableLabelProvider(); 
		
		
		controller.registerView(this);
		
		
	}
	
	public Widget getWidget() {
		return parent;
	}
	
	
	/**
	 * Updates the content in the table
	 */
	public void update() {
		System.out.println("UPDATE: link table");
		System.out.println(controller.getFilteredElements());
		loadTableContent(viewSite);
		System.out.println(viewer.getTable().getItemCount());
		//viewer.update(controller.getFilteredElements(),
		//		controller.getAttributeNames());
		parent.layout();
		parent.getParent().layout();
		tableItemSelectedPrev = -1;
	}
	
	private void loadTableContent(IViewSite viewSite) {
		Table table = viewer.getTable();
		
		int layoutStyle = GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL;
		table.setLayoutData(new GridData(layoutStyle));
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		//remove old columns
		//note: all columns must be removed to dispose of old selection listeners
		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++)
			columns[i].dispose();
		
		// add new columns
		String[] columnNames = controller.getDisplayedAttributeNames();
		TableColumn column;
		for (int i=0; i<columnNames.length; i++) {
			column = new TableColumn(table, SWT.LEFT, i);
			column.setText(columnNames[i]);
			column.setWidth(100);
			column.addSelectionListener(
					new LinkColumnSorterAdapter(controller, columnNames[i]));
		}
		
		//H: 5/23/08
		table.addListener (SWT.MouseDoubleClick, this);
		table.addListener(SWT.MouseDown, this);
		
		viewer.setColumnProperties(columnNames);
		
		viewer.setCellEditors(getTableViewerCellEditors(viewer));
		viewer.setCellModifier(cellModifier);
		viewer.setContentProvider(controller.getContentProvider());
		viewer.setLabelProvider(labelProvider);
		//note: the controller now handles sorting
		
		viewer.setInput(cellModifier);
		createContextMenu();
	}
	
	//H: 5/24/08
	//index = row, i = column
	/**
	 * Method handles both left mouse click (select link) and
	 * right mouse click ("Delete Link" in the context menu)
	 */
	public void handleEvent(Event event) {
		System.out.println("EVENT: " + event.toString() + " ITEM " + event.item);
		//tableItemSelectedPrev = -1;
		//selection of link item
		if (event.type == SWT.MouseDown) {
			Table table = viewer.getTable();
			int columnCount = table.getColumnCount();
			Rectangle clientArea = table.getClientArea ();
			Point pt = new Point (event.x, event.y);
			int index = table.getTopIndex ();
			while (index < table.getItemCount ()) {
				boolean visible = false;
				TableItem item = table.getItem (index);
				for (int i=0; i < columnCount; i++) {
					Rectangle rect = item.getBounds (i);
					if (rect.contains (pt)) {
						System.out.println ("Item " + index + "-" + i);
						//if (event.type == SWT.MouseDoubleClick) {	
							//invokeEditor(index);
							tableItemSelected = index;
						//	System.out.println("mouse down");
						//}
						//else if (event.type == SWT.Selection) //"Delete Link" context menu
						//	System.out.println("Delete is selected");
						
						//reset the previously selected item in order to allow the resource to 
						//be opened after a mouse click
						if (event.type != SWT.MouseDoubleClick) {	
							tableItemSelectedPrev = -1;
						}
							
					}
					if (!visible && rect.intersects (clientArea)) {
						visible = true;
					}
				}
				if (!visible) return;
				index++;
			}
			
		}
		if (event.type == SWT.MouseDoubleClick) {
			//in order to avoid opening the same resource multiple times,
			//check if the previous selection is not the same as the previous selection
			System.out.println("table item selected previous: " + tableItemSelectedPrev + "& table Item selected: " + tableItemSelected);
			if (tableItemSelectedPrev != tableItemSelected) {
				invokeEditor(tableItemSelected);
				tableItemSelectedPrev = tableItemSelected;
			}
				
		}
		
		//else if (event.type == SWT.Selection) {
		//	System.out.println("Delete the item in " + tableItemSelected);
		//}
	}
	
	//this is to detect whether a new bna canvas is created
	public void setInput(IEditorPart part) {
		viewer.setInput(part);
	}
	
	
		
	private CellEditor[] getTableViewerCellEditors(TableViewer viewer) {
		CellEditor[] editors = new CellEditor[viewer.getColumnProperties().length];
		
		//TODO adapt cell editors to change between pictures for quality column
		
		for (int i=0; i< viewer.getColumnProperties().length; i++) {
			editors[i] = new TextCellEditor(viewer.getTable());
		}
		
		return editors;
	}
	
	
	/**
	 * Table example snippet: resize columns as table resizes
	 *
	 * For a list of all SWT example snippets see
	 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/dev.html#snippets
	 */
	private class TableSizeControlAdapter extends ControlAdapter {
		
		
		public void controlResized(ControlEvent e) {
			Rectangle area = parent.getClientArea();
			
			Table table = viewer.getTable();
			
			Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int width = area.width - 2*table.getBorderWidth();
			if (preferredSize.y > area.height + table.getHeaderHeight()) {
				// Subtract the scrollbar width from the total column width
				// if a vertical scrollbar will be required
				Point vBarSize = table.getVerticalBar().getSize();
				width -= vBarSize.x;
			}
			Point oldSize = table.getSize();
			
			int numCol = table.getColumnCount();
			TableColumn[] cols = table.getColumns();
			
			if (oldSize.x > area.width) {
				// table is getting smaller so make the columns 
				// smaller first and then resize the table to
				// match the client area width
				for (TableColumn col : cols) {
					col.setWidth(width/numCol);
				}
				table.setSize(area.width, area.height);
			} else {
				// table is getting bigger so make the table 
				// bigger first and then make the columns wider
				// to match the client area width
				table.setSize(area.width, area.height);
				for (TableColumn col : cols) {
					col.setWidth(width/numCol);
				}
			}
		}
	}
	
	/**
	 * Method launches the appropriate editor for the selected link
	 * @param elementNum the link offset location of the tracelink in the vector
	 */
	private void invokeEditor(int elementNum) {
		ITracelinkElement element = controller.getFilteredElements().elementAt(elementNum);
		String location = element.getAttribute("location").toString();
		System.out.println(location);
		hAdapter.invokeHAdapter(location);
		/*MyEditor myEditor = new MyEditor();
		try {
			myEditor.run(location);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		*/
		
		
	}

	/**
	 * Method deletes the selected element from the xADL file
	 * @param elementNum the link offset location of the tracelink in the vector
	 */
	private void deleteElement(int elementNum) {
		ITracelinkElement element = controller.getFilteredElements().elementAt(elementNum);
		String location = element.getAttribute("location").toString();
		System.out.println("deleting " + location);
		
		controller.removeElementFromXadl(element);
		//TODO: the following does not reflect the deleted element
		//    	update();
		//      find a way to refresh the link table
	}
	
	/**
	 * Create the context menu for the link table 
	 */
	private void createContextMenu() {
		Menu pop = new Menu(parent.getShell(), SWT.POP_UP);
		MenuItem item = new MenuItem(pop, SWT.PUSH);
		item.setText("Delete Link");
		//item.addListener(SWT.Selection, this);
		item.addSelectionListener(new DeleteSelectionAdapter());
		
		//item.addListener(SWT.MouseDown, this);
		//--
		//item.addListener(SWT.Selection, new Listener() {
		//	public void handleEvent(Event event) {
		//		System.out.println("Delete Item " + event.item);
		//    }
      	//});
		
		//item = new MenuItem(pop, SWT.PUSH);
		//item.setText("Traverse"); 
		//item.addListener(SWT.Selection, this);
		//----
		//item.addListener(SWT.MouseDown, this);
		//item.addListener(SWT.Selection, new Listener() {
		//	public void handleEvent(Event event) {
		//		System.out.println("Traverse Item " + event.item);
	    //    }
	    //});
			
		
		viewer.getTable().setMenu(pop);

	}
	
	//Handle context menu selections
	private class DeleteSelectionAdapter extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			System.out.println("Delete Button: " + tableItemSelected);
			deleteElement(tableItemSelected);
			

		}
	}
	
}
