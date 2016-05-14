/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.analysis;

import edu.uci.isr.sysutils.ArrayUtils;

/**
 * @author dpurpura
 */
public class SimpleRulePart
	implements IRulePart{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8775978604128562099L;

	private String archObj;
	private String command;
	private String[] parameters;
	private IRulePart[] referencedRules;
	private boolean state;

	/**
	 * Default constructor required for XMLSerializer
	 */
	public SimpleRulePart(){

	}

	public SimpleRulePart(String archObj, String command, String[] parameters, IRulePart[] referencedRules){
		this.archObj = archObj;
		this.command = command;

		if(parameters == null){
			this.parameters = new String[0];
		}
		else{
			this.parameters = parameters;
		}

		if(referencedRules == null){
			this.referencedRules = new IRulePart[0];
		}
		else{
			this.referencedRules = referencedRules;
		}
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart#getArchObject()
	 */
	public String getArchObject(){
		return archObj;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart#getCommand()
	 */
	public String getCommand(){
		return command;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart#getParameters()
	 */
	public String[] getParameters(){
		return parameters;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart#setArchObject(java.lang.String)
	 */
	public void setArchObject(String archObj){
		this.archObj = archObj;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart#setCommand(java.lang.String)
	 */
	public void setCommand(String command){
		this.command = command;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart#setParameters(java.lang.String[])
	 */
	public void setParameters(String[] params){
		this.parameters = params;
	}

	public IRulePart[] getReferencedRules(){
		return referencedRules;
	}

	public void setReferencedRules(IRulePart[] rules){
		this.referencedRules = rules;
	}

	
	public String toString(){
		String result = "";

		result += archObj + "." + command;
		result += "(" + ArrayUtils.join(parameters, ", ") + ")";
		if(!isBase()){
			result += " := " + ArrayUtils.join(referencedRules, ", ");
		}

		return result;
	}

	public boolean isBase(){
		return referencedRules.length <= 0;
	}

	public boolean getState(){
		return state;
	}

	public void setState(boolean state){
		this.state = state;
	}
}
