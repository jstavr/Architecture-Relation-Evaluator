package edu.uci.isr.archstudio4.comp.archlight.tools.schematron;


public class SchematronTestException extends Exception{
	
	protected String testUID;
	
	public SchematronTestException(){
		super();
	}
	
	public SchematronTestException(String message){
		super(message);
	}
	
	public SchematronTestException(String message, Throwable cause){
		super(message, cause);
	}
	
	public SchematronTestException(Throwable cause){
		super(cause);
	}
	
	public void setTestUID(String testUID){
		this.testUID = testUID;
	}
	
	public String getTestUID(){
		return testUID;
	}
}
