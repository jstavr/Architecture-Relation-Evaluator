package edu.uci.isr.archstudio4.comp.copypaste;

import java.util.ArrayList;
import java.util.List;

import edu.uci.isr.archstudio4.comp.relatedelements.IRelatedElement;

public class RelatedElementNode implements ICopiedElementNode{
	
	IRelatedElement element;
	
	boolean selected = false;
	
	ICopiedElementNode parent;
	List<ICopiedElementNode> children;
	
	public RelatedElementNode(ICopiedElementNode parent,IRelatedElement element) {
		this.parent = parent;
		this.children = new ArrayList<ICopiedElementNode>();
		this.element = element;
	}
	public RelatedElementNode(ICopiedElementNode parent,List<RelatedElementNode> children,IRelatedElement element) {
		this.parent = parent;
		this.children = new ArrayList<ICopiedElementNode>();
		if(children != null && children.size() > 0) {
			this.children.addAll(children);
		}
		this.element = element;
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
		this.selected = selected;
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public String getDescription() {
		
		return element.getDescription();
	}
	
	public IRelatedElement getRelatedElement() {
		return this.element;
	}
}
