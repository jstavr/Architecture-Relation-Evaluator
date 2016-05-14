package edu.uci.isr.archstudio4.comp.copypaste;

import java.util.List;

public interface ICopiedElementNode {
	
	String getDescription();
	ICopiedElementNode getParent();
	List<ICopiedElementNode> getChildren();
	void addChild(ICopiedElementNode child);
	void setSelected(boolean selected);
	boolean isSelected();
}
