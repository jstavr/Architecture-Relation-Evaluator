/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.views;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author dpurpura
 *
 */
//TODO: cleanup this code - added widgets here related to update query 
//these widgets should be placed in a different class
public class GenericView extends Composite{
	
	protected Button notButton;
	protected ComboViewer attributeCombo;
	protected ComboViewer operatorCombo;
	protected Text text;
	protected ComboViewer andOrCombo;
	protected Button addButton;
	
	protected List rules;
	protected Button submitButton;
	
	protected Button updateButton;
	protected ComboViewer attributeToUpdateCombo;
	protected Text textUpdate;
	
	public GenericView(Composite parent, int style) {
		super(parent, style);
		
		GridLayout layout = new GridLayout(1, false);
		this.setLayout(layout);
		
		init();
	}
	
	protected void init() {
		int style = SWT.None;
		RowLayout layout;
		RowData data;
		//int gridStyle = GridData.HORIZONTAL_ALIGN_FILL 
		//				| GridData.GRAB_HORIZONTAL
		//				| GridData.HORIZONTAL_ALIGN_BEGINNING;
		
		int gridStyle = GridData.HORIZONTAL_ALIGN_BEGINNING
		| GridData.GRAB_HORIZONTAL;
		
		
		int gridStyle2 = GridData.VERTICAL_ALIGN_FILL
		| GridData.GRAB_VERTICAL
		| GridData.VERTICAL_ALIGN_CENTER;
		
		
		//-- Row 1 ----
		Composite row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);
		
		Label label = new Label(row, style);
		label.setText("If");
		
		notButton = new Button(row, style | SWT.CHECK);
		notButton.setText("not");
		
		attributeCombo = new ComboViewer(row, style);
		
		operatorCombo = new ComboViewer(row, style);
		operatorCombo.add("equals");
		operatorCombo.add("contains");
		
		text = new Text(row, style | SWT.SINGLE | SWT.BORDER);
		
		
		//-- Row 2 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		//layout.justify = true;
		layout.justify = false;
		layout.pack = false;
		row.setLayout(layout);
		
		andOrCombo = new ComboViewer(row, style);
		andOrCombo.add("and");
		andOrCombo.add("or");
		
		addButton = new Button(row, style);
		addButton.setText("Add");
		
		//submitButton = new Button(this, style);
		submitButton = new Button(row, style);
		submitButton.setText("Run Report");
		
		//-- Row 3 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//row.setSize(this.getSize().x, this.getSize().y/3);
		FillLayout fillLayout = new FillLayout();
		row.setLayout(fillLayout);
		
		//layout = new RowLayout();
		//layout.justify = true;
		//row.setLayout(layout);
		
		//rules = new Text(this, style | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		//rules.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		rules = new List(row, style | SWT.MULTI| SWT.BORDER);
 
		
		data = new RowData();
		data.width = this.getSize().x;
		
		//rules.setLayoutData(data);
		
		//-- Row 4 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		//layout.justify = true;
		layout.justify = false;
		row.setLayout(layout);
		
		//submitButton = new Button(this, style);
		//submitButton.setText("Run Report");

		Label labelUpdate = new Label(row, style);
		labelUpdate.setText("Update Criteria: ");
		
		attributeToUpdateCombo = new ComboViewer(row, style);
		attributeToUpdateCombo.add("status");
		textUpdate = new Text(row, style | SWT.SINGLE | SWT.BORDER);
		
		//updateButton = new Button(this, style);
		updateButton = new Button(row, style);
		updateButton.setText("Run Update");
		
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell (display);
		shell.setText ("Shell");
		shell.setSize (500, 300);
		shell.setLayout(new FillLayout());

		//new GenericView(shell, SWT.NONE);
		
		ExpandBar bar = new ExpandBar(shell, SWT.NONE);
		Composite view = new GenericView(bar, SWT.NONE);
		ExpandItem item = new ExpandItem(bar, SWT.NONE);
		
		item.setText("Generic View");
		item.setHeight(view.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(view);
		item.setExpanded(true);
		
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
		
		
	}
	
}
