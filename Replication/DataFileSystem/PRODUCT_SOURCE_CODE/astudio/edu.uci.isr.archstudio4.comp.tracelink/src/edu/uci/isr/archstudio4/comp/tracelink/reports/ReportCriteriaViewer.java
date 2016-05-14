/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.reports;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.views.GenericView;

/**
 * @author Hazel
 *
 */
public class ReportCriteriaViewer extends GenericView {

	//private Vector<String> criteriaList = new Vector<String>();
	private Vector<Criteria> criteriaList = new Vector<Criteria>();
	//private ITracelinkController controller;
	private ReportsView reportsView;
	private String[] attributeNames;
	
	public ReportCriteriaViewer(Composite parent, ReportsView reportsView) {
		super(parent, SWT.NONE);
		this.reportsView = reportsView;
	}
	
	/**
	 * Method sets the ITracelinkController to dynamically obtain the list of attributes 
	 * for the attribute combo box
	 * @param controller
	 */
	/*
	public void setController(ITracelinkController controller) {
		this.controller = controller;
	}
	*/
	
	public void setAttributeNames(String[] attributeNames) {
		this.attributeNames = attributeNames;
	}
	
	/**
	 * Method dynamically fills in the values for the combo box and
	 * adds custom listeners to the widgets
	 */
	public void setup() {
		
		//String[] attributes = controller.getAttributeNames();
		for (String attribute : attributeNames) {
			attributeCombo.add(attribute);
		}
		
		notButton.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				//System.out.println(notButton.getSelection());
			}
			
		});
		
	    addButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		//String criteria = "";
	    		Criteria criteria = new Criteria();
	    		String temp;
	    		if ( (! attributeCombo.getSelection().isEmpty()) && 
	    			 (! operatorCombo.getSelection().isEmpty()) &&
	    			 (text.getText().length() > 0) ) {
	    			
	    			//if (notButton.getSelection())
	    				//criteria += "not";
	    			criteria.setNegateOperator(notButton.getSelection());
	    			
	    			//strip off the brackets before adding to the criteria
	    			temp = attributeCombo.getSelection().toString();
	    			temp = temp.substring(1, temp.length() - 1);
	    			//criteria += " " + temp;
	    			criteria.setAttribute(temp);
	    			
	    			temp = operatorCombo.getSelection().toString();
	    			temp = temp.substring(1, temp.length() - 1);	    	
	    			//criteria += " " + temp;
	    			criteria.setOperator(temp);
	    			
	    			//criteria += " " + text.getText();
	    			criteria.setProperty(text.getText());
	    			
	    		}
	    		//System.out.println("Adding criteria " + criteria);
	    		criteriaList.add(criteria);
	    		rules.add(criteria.toString());
	    		rules.redraw();
	    		
	    		//display all the criteria stored in the vector
	    		String criteriaToDisplay = "";
	    		for (Criteria c: criteriaList) {
	    			criteriaToDisplay += c.toString() + "\n";
	    		}
	    		
	    		System.out.println(criteriaToDisplay);
	    		//rules.setText(criteriaToDisplay);
	    		
	    		
	    	}
	    });
	    
	    submitButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		String temp = andOrCombo.getSelection().toString();
	    		boolean isAndOperator = temp.toLowerCase().contains("and");
	    		reportsView.runReport(criteriaList, isAndOperator);
	    	}
	    });
	    
	    updateButton.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {
	    		String temp = andOrCombo.getSelection().toString();
	    		boolean isAndOperator = temp.toLowerCase().contains("and");
	    		
	    		String key = attributeToUpdateCombo.getSelection().toString();
	    		int length = key.length();
	    		key = key.substring(1, length-1);
	    		String property = textUpdate.getText();
	    		reportsView.runUpdate(criteriaList, isAndOperator, key, property);
	    		System.out.println("update: " + key + textUpdate.getText());
	    	}
	    });
	}
}
