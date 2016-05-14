package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import edu.uci.isr.bna4.things.swt.AbstractSWTTreeThingPeer;

public class SWTBrickSelectorThingPeer extends AbstractSWTTreeThingPeer<SWTBrickSelectorThing> {

	public SWTBrickSelectorThingPeer(SWTBrickSelectorThing thing) {
		super(thing, SWTBrickSelectorThing.class);
	}

	@Override
	protected ITreeContentProvider getContentProvider() {
		return new BrickContentProvider(t.getBrickLabelList());
	}

	@Override
	protected Object getInput() {
		return t.getBrickLabelList();
	}

	@Override
	protected ILabelProvider getLabelProvider() {
		return new BrickLabelProvider(t.getResources());
	}

}
