package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.util.ArrayList;
import java.util.Collection;

import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;

/**
 * Brick Label container for selector logics 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 *
 */
class BrickLabelList {

	private Collection<BrickLabel> brickLabels;
	
	public BrickLabelList(BrickList brickList) {
		this.brickLabels = new ArrayList<BrickLabel>();
		for(IMyxBrickExtension brick : brickList.getBricks()) {
			add(brick);
		}
	}
	
	public boolean add(IMyxBrickExtension brick) {
		return this.brickLabels.add(new BrickLabel(brick.getId(), brick.getSymbolicName(), brick.getName()));
	}
	
	public Collection<BrickLabel> getBrickLabels(){
		return brickLabels;
	}
}
