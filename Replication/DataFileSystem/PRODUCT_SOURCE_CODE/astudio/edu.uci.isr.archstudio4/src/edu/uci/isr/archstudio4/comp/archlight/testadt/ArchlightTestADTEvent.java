package edu.uci.isr.archstudio4.comp.archlight.testadt;

import edu.uci.isr.archstudio4.archlight.ArchlightTest;

public class ArchlightTestADTEvent implements java.io.Serializable{

	public static final int TESTS_ADDED = 100;
	public static final int TESTS_REMOVED = 200;
	
	protected int type;
	protected ArchlightTest[] tests;
	
	public ArchlightTestADTEvent(int type, ArchlightTest[] tests){
		this.type = type;
		this.tests = tests;
	}
	
	public ArchlightTest[] getTests(){
		return tests;
	}

	public int getEventType(){
		return type;
	}

}
