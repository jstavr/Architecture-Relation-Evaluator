/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.analysis;

import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;

/**
 * @author Hazel, dpurpura
 */
public class SimpleRuleObject
	implements ITracelinkRule{

	private static final long serialVersionUID = -8584160886627677619L;

	private boolean isUnion;
	private IRulePart[] antecedents;
	private IRulePart consequent;

	/**
	 * Default contructor required for XMLSerializer
	 */
	public SimpleRuleObject(){

	}

	public SimpleRuleObject(boolean isUnion, IRulePart[] antecedents, IRulePart consequent){
		this.isUnion = isUnion;

		this.antecedents = antecedents;
		if(consequent != null){
			this.consequent = consequent;
		}
		else{
			this.consequent = new SimpleRulePart();
		}
	}

	public boolean isUnionOperator(){
		return isUnion;
	}

	public void setUnionOperator(boolean isUnion){
		this.isUnion = isUnion;
	}

	public IRulePart[] getAntecedents(){
		return antecedents;
	}

	public IRulePart getConsequent(){
		return consequent;
	}

	public void setAntecedents(IRulePart[] antecedents){
		this.antecedents = antecedents;
	}

	public void setConsequent(IRulePart consequent){
		this.consequent = consequent;
	}

	
	public String toString(){
		String result = "";

		result += "Antecedents: \n";
		for(IRulePart ant: antecedents){
			result += ant.toString() + "\n";
		}

		result += "Consequent: \n" + consequent.toString();

		return result;
	}

	public boolean isFollowingRule(ITraceEndpointModel endpoint){
		boolean isFollowingRule = false;

		//FIXME requires that base antecendents come first; probably should recusively eval antecedents

		for(IRulePart antecedent: getAntecedents()){
			if(antecedent.isBase()){
				if(isUnionOperator()){
					isFollowingRule &= evaluateBaseRulePart(endpoint, antecedent);
				}
				else{
					isFollowingRule |= evaluateBaseRulePart(endpoint, antecedent);
				}
			}
			else{
				// check the state of nested antecendents
				for(IRulePart referencedPart: antecedent.getReferencedRules()){

					if(isUnionOperator()){
						isFollowingRule &= referencedPart.getState();
					}
					else{
						isFollowingRule |= referencedPart.getState();
					}
				}
			}
		}

		return isFollowingRule;
	}

	/**
	 * @param endpoint
	 * @param antecedent
	 */
	private boolean evaluateBaseRulePart(ITraceEndpointModel endpoint, IRulePart antecedent){
		boolean isFollowingRule = false;

		//System.out.println("curRule: Base Rule ");
		String archObject = antecedent.getArchObject();
		String command = antecedent.getCommand();
		String parameter = antecedent.getParameters()[0].toLowerCase();

		//for now only accommodate TraceEndpoint and TraceLink objects
		if(archObject.equals("TraceEndpoint")){
			if(command.equals("getLocationHref")){
				antecedent.setState(false);

				String location = endpoint.getLocationHref().toLowerCase();
				if(location.contains(parameter)){
					//if (scanner.findInLine(parameter).compareTo("") != 0) {
					antecedent.setState(true);
					//System.out.println("curRule " + curRule + "true");
					isFollowingRule = true;
				}
			}
		}
		else if(archObject.equals("TraceLink")){

		}

		return isFollowingRule;
	}

}
