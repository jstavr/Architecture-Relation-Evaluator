package edu.uci.isr.archstudio4.comp.archlight.issueadt;

import edu.uci.isr.archstudio4.archlight.ArchlightIssue;
import edu.uci.isr.xarchflat.ObjRef;

public interface IArchlightIssueADT{

	public ArchlightIssue[] getAllIssues();

	public ArchlightIssue[] getAllIssues(ObjRef documentRef);

	public ArchlightIssue[] getAllIssues(String toolID);

	public ArchlightIssue[] getAllIssues(ObjRef documentRef, String toolID);

	public ArchlightIssue[] getAllIssuesByTestUID(ObjRef documentRef,
		String testUID);

	public ArchlightIssue[] getAllIssues(ObjRef documentRef, String toolID,
		String testUID);

	public void addIssues(ArchlightIssue[] issues);

	public void removeIssues(ArchlightIssue[] issues);

}