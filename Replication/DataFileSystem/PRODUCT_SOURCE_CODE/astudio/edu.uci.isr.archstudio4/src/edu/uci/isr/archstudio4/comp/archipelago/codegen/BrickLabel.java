package edu.uci.isr.archstudio4.comp.archipelago.codegen;
/**
 * Label info to be shown in the brick selector thing
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 *
 */
class BrickLabel {
	private String brickId;
	private String brickName;
	private String symbolicName;
	
	BrickLabel(String brickId, String symbolicName, String brickName) {
		this.brickId = brickId;
		this.symbolicName = symbolicName;
		this.brickName = brickName;
	}
	
	String getBrickId() {
		return this.brickId;
	}
	
	String getSymbolicName() {
		return this.symbolicName;
	}
	
	String getBrickName() {
		return this.brickName;
	}
	
	String toLabelString(){
		return getBrickId() + " - " + getBrickName() + " ["
        + getSymbolicName() + "]";
	}
}
