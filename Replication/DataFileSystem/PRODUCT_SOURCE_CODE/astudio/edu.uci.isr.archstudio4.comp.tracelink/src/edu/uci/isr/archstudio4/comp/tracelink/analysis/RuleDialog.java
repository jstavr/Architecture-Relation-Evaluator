/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.analysis;

import java.io.FileNotFoundException;
import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.uci.isr.archstudio4.comp.tracelink.models.XMLSerializer;

/**
 * @author dpurpura
 */
public class RuleDialog
	extends Composite{

	private final String[] getAttributes = {
	//"TraceLink.getClassification",
	//"TraceLink.getRelationship",
	//"TraceEndpoint.getDescription",
	"TraceEndpoint.getLocationHref"
	//"TraceEndpoint.getStatus"
	};

	private final String[] setAttributes = {"TraceLink.setRelationship", "TraceLink.setDescription"
	//"TraceEndpoint.setStatus"
	};

	// -- GUI elements ------
	private ComboViewer antecedentAttribute;
	private ComboViewer operatorCombo;
	private Text antecedentParameters;
	private Button addButton;

	private List rulesList;

	private ComboViewer andOrCombo;

	private ComboViewer consequentAttribute;
	private Text consequentParameters;

	private Button addActionButton;

	private List actionsList;

	private Button submitButton;

	// -- class fields----------------
	private boolean isSaved;
	private ArrayList<IRulePart> antecedents;
	private IRulePart consequent;

	public RuleDialog(Composite parent, int style){
		super(parent, style);

		isSaved = false;
		antecedents = new ArrayList<IRulePart>();

		GridLayout layout = new GridLayout(1, false);
		this.setLayout(layout);

		init();
	}

	protected void init(){
		int style = SWT.None;
		RowLayout layout;
		RowData data;
		int gridStyle = GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER;

		//-- Row 1 ----
		Composite row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);

		Label label = new Label(row, style);
		label.setText("If");

		antecedentAttribute = new ComboViewer(row, style);
		antecedentAttribute.add(getAttributes);

		label = new Label(row, style);
		label.setText("=");

		//operatorCombo = new ComboViewer(row, style);
		//operatorCombo.add("equals");
		//operatorCombo.add("contains");

		antecedentParameters = new Text(row, style | SWT.SINGLE | SWT.BORDER);

		//-- Row 2 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		layout.pack = false;
		row.setLayout(layout);

		addButton = new Button(row, style);
		addButton.setText("Add Rule");
		addButton.addListener(SWT.Selection, new AddRuleSelection());

		//-- Row 3 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		FillLayout fillLayout = new FillLayout();
		row.setLayout(fillLayout);

		rulesList = new List(row, style | SWT.MULTI | SWT.BORDER);

		//rulesList.setLayoutData(data);

		//-- Row 4 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);

		label = new Label(row, style);
		label.setText("Operator:");

		andOrCombo = new ComboViewer(row, style);
		andOrCombo.add("and");
		andOrCombo.add("or");

		label = new Label(row, style);
		label.setText("Then");

		consequentAttribute = new ComboViewer(row, style);
		//consequentAttribute.add("archobject + command");
		consequentAttribute.add(setAttributes);

		label = new Label(row, style);
		label.setText("=");

		consequentParameters = new Text(row, style | SWT.SINGLE | SWT.BORDER);

		//-- Row 5 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);

		addActionButton = new Button(row, SWT.PUSH);
		addActionButton.setText("Add Action");
		addActionButton.addListener(SWT.Selection, new AddActionSelection());

		//-- Row 6 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		fillLayout = new FillLayout();
		row.setLayout(fillLayout);

		actionsList = new List(row, SWT.MULTI | SWT.BORDER);

		//-- Row 7 ----
		row = new Composite(this, style);
		row.setLayoutData(new GridData(gridStyle));
		layout = new RowLayout();
		layout.justify = true;
		row.setLayout(layout);

		submitButton = new Button(row, style);
		submitButton.setText("Save Rules");
		submitButton.addListener(SWT.Selection, new SaveRulesSelection());

	}

	private String getArchObj(ComboViewer viewer){
		String text = viewer.getCombo().getText();
		int index = text.indexOf(".");

		text = text.substring(0, index);

		System.out.println("TracelinkObj: " + text);
		return text;
	}

	private String getCommand(ComboViewer viewer){
		String text = viewer.getCombo().getText();
		int index = text.indexOf(".") + 1;

		text = text.substring(index);
		System.out.println("Command: " + text);
		return text;
	}

	private String[] getAntecedentParameters(){
		// note: gui only allows one parameter
		String[] param = new String[1];
		param[0] = antecedentParameters.getText().trim();
		return param;
	}

	private String[] getConsequentParameters(){
		// note: gui only allows one parameter
		String[] param = new String[1];
		param[0] = consequentParameters.getText().trim();
		return param;
	}

	private String getAntecedentArchObj(){
		return getArchObj(antecedentAttribute);
	}

	private String getAntecedentCommand(){
		return getCommand(antecedentAttribute);
	}

	private String getConsequentArchObj(){
		return getArchObj(consequentAttribute);
	}

	private String getConsequentCommand(){
		return getCommand(consequentAttribute);
	}

	private boolean isUnionOperator(){
		boolean isUnion = false;
		String text = andOrCombo.getCombo().getText();

		if(text.equalsIgnoreCase("and")){
			isUnion = true;
		}

		return isUnion;
	}

	/**
	 * @return the rule specified by the user if the Save Rules button is
	 *         selected; otherwise, returns null;
	 */
	public ITracelinkRule getRule(){
		if(isSaved){
			IRulePart[] ants = antecedents.toArray(new IRulePart[antecedents.size()]);
			return new SimpleRuleObject(isUnionOperator(), ants, consequent);
		}
		else{
			return null;
		}
	}

	/**
	 * @return returns true if the rule was saved; otherwise returns false
	 */
	public boolean hasRule(){
		return isSaved;
	}

	private class AddRuleSelection
		implements Listener{

		public void handleEvent(Event event){
			System.out.println("Add Rule button press");

			IRulePart part = new SimpleRulePart(getAntecedentArchObj(), getAntecedentCommand(), getAntecedentParameters(), null);

			antecedents.add(part);
			rulesList.add(part.toString());
			rulesList.redraw();
		}
	}

	private class AddActionSelection
		implements Listener{

		public void handleEvent(Event event){
			System.out.println("add action button press");

			IRulePart[] ants = antecedents.toArray(new IRulePart[antecedents.size()]);

			consequent = new SimpleRulePart(getConsequentArchObj(), getConsequentCommand(), getConsequentParameters(), ants);

			actionsList.removeAll();
			actionsList.add(consequent.toString());
			actionsList.redraw();

		}
	}

	private class SaveRulesSelection
		implements Listener{

		public void handleEvent(Event event){
			isSaved = true;

			FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
			fd.setText("Save");
			fd.setFilterPath(System.getProperty("user.home"));
			String[] filterExt = {"*.xml", "*.*"};
			fd.setFilterExtensions(filterExt);

			String filename = fd.open();

			if(filename != null){
				ArrayList<ITracelinkRule> rules = new ArrayList<ITracelinkRule>();
				rules.add(getRule());

				System.out.println("Serializing to: " + filename + "... \n" + getRule());

				try{
					XMLSerializer.serialize(filename, rules);
				}
				catch(FileNotFoundException e1){
					e1.printStackTrace();
				}
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
		shell.setSize(600, 500);
		shell.setLayout(new FillLayout());

		//new GenericView(shell, SWT.NONE);

		ExpandBar bar = new ExpandBar(shell, SWT.NONE);
		Composite view = new RuleDialog(bar, SWT.NONE);

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
