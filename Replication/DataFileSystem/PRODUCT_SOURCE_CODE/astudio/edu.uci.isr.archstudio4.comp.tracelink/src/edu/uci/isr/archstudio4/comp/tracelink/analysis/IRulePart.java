/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.analysis;

import java.io.Serializable;

/**
 * @author dpurpura
 *
 */
public interface IRulePart extends Serializable {
	
	/**
	 * @return true if the rule part does not reference any other rule parts; 
	 * otherwise returns false.
	 */
	public boolean isBase();
	
	public String getArchObject();
	public String getCommand();
	public String[] getParameters();
	public IRulePart[] getReferencedRules();
	public boolean getState();
	
	public void setArchObject(String archObj);
	public void setCommand(String command);
	public void setParameters(String[] params);
	public void setReferencedRules(IRulePart[] rules);
	public void setState(boolean state);
}
