package edu.uci.isr.archstudio4.comp.tracelink.models;


import java.util.Set;

/**
 * This class is used as the model for Link objects.
 * 
 * In order to allow for dynamic content types and link qualities, 
 * ITracelinkElement stores link attributes in a {@link java.util.HashMap},  
 * with the value hashed to the value name.
 * 
 * @author dpurpura
 *
 */
public interface ITracelinkElement {
	
	public boolean hasAttribute(String key);
	
	public Object getAttribute(String key);
	
	public String getID();
	
	public void setAttribute(String key, Object value);
	
	public Set<String> getKeys();
	
	public String toString();
	
}
