package edu.uci.isr.archstudio4.comp.archlight.issueadt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import edu.uci.isr.archstudio4.archlight.ArchlightIssue;
import edu.uci.isr.xarchflat.ObjRef;
	
public class ArchlightIssueADT implements IArchlightIssueADT{

	private static final ArchlightIssue[] emptyIssueArray = new ArchlightIssue[0];
	
	protected List issueList;
	
	public ArchlightIssueADT(){
		issueList = new ArrayList();
	}
	
	public synchronized ArchlightIssue[] getAllIssues(){
		return (ArchlightIssue[])issueList.toArray(emptyIssueArray);
	}
	
	public synchronized ArchlightIssue[] getAllIssues(ObjRef documentRef){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			ArchlightIssue issue = (ArchlightIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				matchingList.add(issue);
			}
		}
		return (ArchlightIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized ArchlightIssue[] getAllIssues(String toolID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			ArchlightIssue issue = (ArchlightIssue)it.next();
			if((issue.getToolID() != null) && (issue.getToolID().equals(toolID))){
				matchingList.add(issue);
			}
		}
		return (ArchlightIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized ArchlightIssue[] getAllIssues(ObjRef documentRef, String toolID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			ArchlightIssue issue = (ArchlightIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				if((issue.getToolID() != null) && (issue.getToolID().equals(toolID))){
					matchingList.add(issue);
				}
			}
		}
		return (ArchlightIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized ArchlightIssue[] getAllIssuesByTestUID(ObjRef documentRef, String testUID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			ArchlightIssue issue = (ArchlightIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				if((issue.getTestUID() != null) && (issue.getToolID().equals(testUID))){
					matchingList.add(issue);
				}
			}
		}
		return (ArchlightIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized ArchlightIssue[] getAllIssues(ObjRef documentRef, String toolID, String testUID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			ArchlightIssue issue = (ArchlightIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				if((issue.getToolID() != null) && (issue.getToolID().equals(toolID))){
					if((issue.getTestUID() != null) && (issue.getTestUID().equals(testUID))){
						matchingList.add(issue);
					}
				}
			}
		}
		return (ArchlightIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public void addIssues(ArchlightIssue[] issues){
		for(int i = 0; i < issues.length; i++){
			issueList.add(issues[i]);
		}
		fireIssuesAdded(issues);
	}
	
	public void removeIssues(ArchlightIssue[] issues){
		for(int i = 0; i < issues.length; i++){
			issueList.remove(issues[i]);
		}
		fireIssuesRemoved(issues);
	}
	
	protected Vector listeners = new Vector();
	
	public void addArchlightIssueADTListener(ArchlightIssueADTListener l){
		listeners.add(l);
	}
	
	public void removeArchlightIssueADTListener(ArchlightIssueADTListener l){
		listeners.remove(l);
	}
	
	protected void fireIssuesAdded(ArchlightIssue[] issues){
		fireEvent(ArchlightIssueADTEvent.ISSUES_ADDED, issues);
	}
	
	protected void fireIssuesRemoved(ArchlightIssue[] issues){
		fireEvent(ArchlightIssueADTEvent.ISSUES_REMOVED, issues);
	}
	
	protected void fireEvent(int type, ArchlightIssue[] issues){
		ArchlightIssueADTListener[] ls = (ArchlightIssueADTListener[])listeners.toArray(new ArchlightIssueADTListener[listeners.size()]);
		ArchlightIssueADTEvent evt = new ArchlightIssueADTEvent(type, issues);
		for(int i = 0; i < ls.length; i++){
			((ArchlightIssueADTListener)ls[i]).issueADTChanged(evt);
		}
	}
	

	
}
