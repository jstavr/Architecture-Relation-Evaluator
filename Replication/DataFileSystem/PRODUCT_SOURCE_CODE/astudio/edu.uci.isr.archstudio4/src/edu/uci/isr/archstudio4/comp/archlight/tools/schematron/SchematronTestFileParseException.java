package edu.uci.isr.archstudio4.comp.archlight.tools.schematron;


public class SchematronTestFileParseException extends Exception implements java.io.Serializable{
	public SchematronTestFileParseException(String message){
		super(message);
	}

	public SchematronTestFileParseException(String message, Throwable t){
		super(message, t);
	}
}
