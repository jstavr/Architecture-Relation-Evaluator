package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.bna4.facets.IRelativeMovable;
import edu.uci.isr.bna4.things.swt.AbstractSWTTreeThing;

public class SWTBrickSelectorThing extends AbstractSWTTreeThing implements IRelativeMovable {

	final static String BRICK_LIST_PROPERTY_NAME = "brickList";
	final static String BRICK_LABEL_LIST_PROPERTY_NAME = "brickLabelList";
	

	public SWTBrickSelectorThing() {
		this(null);
	}

	public SWTBrickSelectorThing(String id) {
		super(id);
	}

	public void setBrickList(BrickList brickList) {
		setProperty(SWTBrickSelectorThing.BRICK_LIST_PROPERTY_NAME, brickList);
	}

	public BrickList getBrickList() {
		return getProperty(SWTBrickSelectorThing.BRICK_LIST_PROPERTY_NAME);
	}
	
	public void setBrickLabelList(BrickLabelList brickLabelList) {
		setProperty(SWTBrickSelectorThing.BRICK_LABEL_LIST_PROPERTY_NAME, brickLabelList);
	}

	public BrickLabelList getBrickLabelList() {
		return getProperty(SWTBrickSelectorThing.BRICK_LABEL_LIST_PROPERTY_NAME);
	}

	public void setResources(IResources resources) {
		setProperty("$resources", resources);
	}

	public IResources getResources() {
		return getProperty("$resources");
	}

}
