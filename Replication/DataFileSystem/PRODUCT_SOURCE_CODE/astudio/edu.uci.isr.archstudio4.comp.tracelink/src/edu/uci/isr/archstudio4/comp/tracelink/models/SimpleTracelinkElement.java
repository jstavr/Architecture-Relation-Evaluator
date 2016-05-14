/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.HashMap;
import java.util.Set;

/**
 * @author dpurpura
 */
public class SimpleTracelinkElement
	implements ITracelinkElement{

	protected HashMap<String, Object> attributes;

	public SimpleTracelinkElement(){
		this.attributes = new HashMap<String, Object>();

	}

	public boolean hasAttribute(String key){
		return attributes.get(key) != null;
	}

	public Object getAttribute(String key){
		return attributes.get(key);
	}

	public void setAttribute(String key, Object value){
		attributes.put(key, value);
	}

	public Set<String> getKeys(){
		return attributes.keySet();
	}

	
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("Link: {");
		for(String key: getKeys()){
			result.append(key + ": " + getAttribute(key) + ", ");
		}
		result.append("}");
		return result.toString();

	}

	public String getID(){
		return getAttribute("id").toString();
	}

}
