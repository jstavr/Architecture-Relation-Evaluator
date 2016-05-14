package edu.uci.isr.archstudio4.comp.copypaste;

import java.util.ArrayList;
import java.util.List;

import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ObjRefNode implements ICopiedElementNode{
	
	ObjRef objRef;
	
	boolean selected = true;
	
	ICopiedElementNode parent;
	List<ICopiedElementNode> children;
	
	XArchFlatInterface xArch;
	
	public ObjRefNode(XArchFlatInterface xArch,ICopiedElementNode parent,ObjRef objRef) {
		this.parent = parent;
		this.children = new ArrayList<ICopiedElementNode>();
		this.objRef = objRef;
		this.xArch = xArch;
	}
	public ObjRefNode(ICopiedElementNode parent,ObjRef objRef,List<ICopiedElementNode> children) {
		this.parent = parent;
		this.children = new ArrayList<ICopiedElementNode>();
		if(children != null && children.size() > 0) {
			this.children.addAll(children);
		}
		this.objRef = objRef;
	}
	
	public ICopiedElementNode getParent() {
		return this.parent;
	}
	
	public List<ICopiedElementNode> getChildren() {
		return this.children;
	}
	
	public void addChild(ICopiedElementNode child) {
		this.children.add(child);
	}
	
	public void setSelected(boolean selected) {
		//Does nothing.
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public String getDescription() {
		try {
			ObjRef descriptionRef = (ObjRef)xArch.get(objRef,"Description");
			String descriptionValue = (String)xArch.get(descriptionRef,"value");
			return descriptionValue;
		}
		catch(Exception e) {}
		
		//Temporary Fix.
		return objRef.toString();
	}
	
	public ObjRef getObjRef() {
		return this.objRef;
	}
}
