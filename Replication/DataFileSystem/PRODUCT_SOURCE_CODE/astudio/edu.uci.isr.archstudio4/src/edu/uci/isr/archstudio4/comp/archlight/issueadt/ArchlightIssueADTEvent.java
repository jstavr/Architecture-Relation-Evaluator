package edu.uci.isr.archstudio4.comp.archlight.issueadt;

import edu.uci.isr.archstudio4.archlight.ArchlightIssue;

public class ArchlightIssueADTEvent implements java.io.Serializable{

	public static final int ISSUES_ADDED = 100;
	public static final int ISSUES_REMOVED = 200;
	
	protected int type;
	protected ArchlightIssue[] issues;
	
	public ArchlightIssueADTEvent(int type, ArchlightIssue[] issues){
		this.type = type;
		this.issues = issues;
	}
	
	public ArchlightIssue[] getIssues(){
		return issues;
	}

	public int getEventType(){
		return type;
	}

}
