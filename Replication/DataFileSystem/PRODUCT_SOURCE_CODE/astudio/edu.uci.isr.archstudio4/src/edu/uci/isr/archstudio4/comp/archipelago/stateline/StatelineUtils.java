package edu.uci.isr.archstudio4.comp.archipelago.stateline;

public class StatelineUtils{
	private StatelineUtils(){}
	
	public static String mungeID(String id){
		id = id.replace('-', 'X');
		id = id.replace(' ', 'X');
		id = id.replace('_', 'X');
		return id;
	}
}
