package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;

/**
 * IMyxBrickExtension container for selector logics
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 *
 */
class BrickList {
	private Map<String, IMyxBrickExtension> idBrickMap;
	
	public BrickList(Collection<IMyxBrickExtension> brickList) {
		this.idBrickMap = new HashMap<String, IMyxBrickExtension>();
		for(IMyxBrickExtension brick : brickList){
			this.idBrickMap.put(brick.getId(), brick);
		}
	}
	
	public Collection<IMyxBrickExtension> getBricks(){
		return this.idBrickMap.values();
	}
	
	public IMyxBrickExtension getBrick(String id) {
		return this.idBrickMap.get(id);
	}

}
