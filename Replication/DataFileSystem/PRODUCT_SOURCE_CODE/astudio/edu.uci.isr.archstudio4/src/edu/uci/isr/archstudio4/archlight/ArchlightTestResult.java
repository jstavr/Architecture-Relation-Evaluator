package edu.uci.isr.archstudio4.archlight;

import edu.uci.isr.xarchflat.ObjRef;

public class ArchlightTestResult{

	protected ObjRef documentRef;
	protected String testUID;
	protected ArchlightIssue[] issues;
	
	public ArchlightTestResult(ObjRef documentRef, String testUID, ArchlightIssue[] issues){
		super();
		this.documentRef = documentRef;
		this.testUID = testUID;
		this.issues = issues;
	}
	
	public ObjRef getDocumentRef(){
		return documentRef;
	}
	
	public void setDocumentRef(ObjRef documentRef){
		this.documentRef = documentRef;
	}
	
	public ArchlightIssue[] getIssues(){
		return issues;
	}
	
	public void setIssues(ArchlightIssue[] issues){
		this.issues = issues;
	}
	
	public String getTestUID(){
		return testUID;
	}
	
	public void setTestUID(String testUID){
		this.testUID = testUID;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer("ArchlightTestResult[");
		buf.append("testUID=").append(testUID).append(";");
		if(issues == null){
			buf.append("issues=null;");
		}
		else{
			for(int i = 0; i < issues.length; i++){
				buf.append("issues[").append(i).append("]=").append(issues[i]).append(";");
			}
		}
		buf.append("];");
		return buf.toString();
	}
}
