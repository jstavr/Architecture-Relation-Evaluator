/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.LinkedHashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uci.isr.xarch.DOMBased;

/**
 * @author David
 */
public abstract class AbstractDOMBasedTracelinkElementAdapter
	implements ITracelinkElement{

	protected Node node;

	public AbstractDOMBasedTracelinkElementAdapter(DOMBased element){
		this.node = element.getDOMNode();
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key){
		return node.getAttributes().getNamedItem(key).getNodeValue();
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getID()
	 */
	public abstract String getID();

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getKeys()
	 */
	public Set<String> getKeys(){
		NodeList children = node.getChildNodes();
		Node n;
		LinkedHashSet<String> keys = new LinkedHashSet<String>();
		for(int i = 0; i != children.getLength(); i++){
			n = children.item(i);
			n.getNodeName();
		}
		return keys;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String key){
		Node attribute = node.getAttributes().getNamedItem(key);
		return attribute != null;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#setAttribute(java.lang.String,
	 *      java.lang.String)
	 */
	public void setAttribute(String key, Object value){
		Node attribute = node.getAttributes().getNamedItem(key);
		attribute.setNodeValue(value.toString());
	}
}
