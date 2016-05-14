/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.analysis;

import java.io.Serializable;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;

/**
 * @author dpurpura
 *
 */
public interface ITracelinkRule extends Serializable{
	
	public boolean isUnionOperator();
	public void setUnionOperator(boolean isUnion);
	
	/**
	 * @return the "if" part of the rule
	 */
	public IRulePart[] getAntecedents();
	public void setAntecedents(IRulePart[] antecedents);
	
	/**
	 * @return the "then" part of the rule
	 */
	public IRulePart getConsequent();
	public void setConsequent(IRulePart consequent);
	
	/**
	 * @param endpoint
	 * @return true if the Endpoint if the rule's antecedents evalate to true; 
	 * otherwise returns false;
	 */
	public boolean isFollowingRule(ITraceEndpointModel endpoint);

}
