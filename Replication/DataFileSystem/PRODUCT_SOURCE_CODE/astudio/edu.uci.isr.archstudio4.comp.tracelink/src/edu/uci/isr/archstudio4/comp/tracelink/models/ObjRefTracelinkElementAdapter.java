/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

/**
 * @author David
 */
public abstract class ObjRefTracelinkElementAdapter
	implements ITracelinkElement{

	private XArchFlatInterface xArch;
	private ObjRef xArchRef;
	private ObjRef elementObjRef;

	/**
	 * Adapts an ObjRef to a model for use in ITracelinkController
	 * 
	 * @param xArch
	 *            reference to the XArchChangeSetInterface
	 * @param xArchRef
	 *            Reference to the xArch object from which to retrieve the
	 *            top-level element.
	 * @param elementObjRef
	 *            Reference to a element object (created by
	 *            <CODE>getElement(...)</CODE> contains a method called
	 *            <CODE>getArchStructure(IXArch)</CODE> then the
	 *            <CODE>typeOfThing</CODE> parameter will be
	 *            <CODE>"ArchStructure"</CODE>.
	 */
	public ObjRefTracelinkElementAdapter(XArchFlatInterface xArch, ObjRef xArchRef, ObjRef elementObjRef){
		this.xArch = xArch;
		this.xArchRef = xArchRef;
		this.elementObjRef = elementObjRef;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getAttribute(java.lang.String)
	 */
	public String getAttribute(String key){
		String attribute = (String)xArch.get(elementObjRef, key);
		if(attribute != null && !"".equals(attribute.trim())){
			return attribute;
		}
		else{
			return "";
		}

	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getID()
	 */
	public abstract String getID();

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#getKeys()
	 */
	public Set<String> getKeys(){
		Set<String> keys = new LinkedHashSet<String>();

		IXArchPropertyMetadata[] data = xArch.getTypeMetadata(elementObjRef).getProperties();
		for(IXArchPropertyMetadata property: data){
			keys.add(property.getName());
		}
		return keys;
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String key){
		return getAttribute(key) != "";
	}

	/**
	 * @see edu.uci.isr.archstudio4.comp.tracelink.models.ITracelinkElement#setAttribute(java.lang.String,
	 *      java.lang.String)
	 */
	public void setAttribute(String key, Object value){
		xArch.set(elementObjRef, key, value.toString());
	}
}
