package edu.uci.isr.archstudio4.archlight;


public class ArchlightTestError extends ArchlightNotice{
	
	protected String testUID;
	
	public ArchlightTestError(String testUID, String toolID, String message, String additionalDetail, Throwable error){
		super(toolID, message, additionalDetail, error);
		this.testUID = testUID;
	}
	
	public String getTestUID(){
		return testUID;
	}

	public void setTestUID(String testUID){
		this.testUID = testUID;
	}
}
