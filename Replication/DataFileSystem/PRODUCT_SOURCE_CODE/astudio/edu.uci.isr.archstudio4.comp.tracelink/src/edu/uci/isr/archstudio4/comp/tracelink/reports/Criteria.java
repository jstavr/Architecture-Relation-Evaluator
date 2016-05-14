/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.reports;

/**
 * Data structure for different parts of the criteria
 * @author Hazel
 *
 */
public class Criteria {
	
	//checked: negateOperator = true 
	boolean negateOperator;
	
	//corresponds to the field name
	String attribute;
	
	//value is either "equals" or "contains"
	String operator;
	
	//value of the attribute
	String property;

	public boolean isNegateOperator() {
		return negateOperator;
	}

	public void setNegateOperator(boolean negateOperator) {
		this.negateOperator = negateOperator;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	
	public String toString() {
		String s = "";
		if (negateOperator) 
			s += "Not ";
		s += attribute + " " + operator + " " + property;
		return s;
	}
	

}
