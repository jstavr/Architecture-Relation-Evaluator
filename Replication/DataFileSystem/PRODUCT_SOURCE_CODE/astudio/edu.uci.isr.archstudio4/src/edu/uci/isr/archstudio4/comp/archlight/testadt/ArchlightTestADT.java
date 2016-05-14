package edu.uci.isr.archstudio4.comp.archlight.testadt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import edu.uci.isr.archstudio4.archlight.ArchlightTest;
	
public class ArchlightTestADT implements IArchlightTestADT{

	private static final ArchlightTest[] emptyTestArray = new ArchlightTest[0];
	
	protected List testList;
	
	public ArchlightTestADT(){
		testList = new ArrayList();
	}
	
	public synchronized ArchlightTest[] getAllTests(){
		return (ArchlightTest[])testList.toArray(emptyTestArray);
	}
	
	public synchronized ArchlightTest[] getAllTests(String toolID){
		List matchingList = new ArrayList();
		for(Iterator it = testList.iterator(); it.hasNext(); ){
			ArchlightTest test = (ArchlightTest)it.next();
			if((test.getToolID() != null) && (test.getToolID().equals(toolID))){
				matchingList.add(test);
			}
		}
		return (ArchlightTest[])matchingList.toArray(emptyTestArray);
	}
	
	public synchronized ArchlightTest getTest(String testUID){
		for(Iterator it = testList.iterator(); it.hasNext(); ){
			ArchlightTest test = (ArchlightTest)it.next();
			if((test.getUID() != null) && (test.getUID().equals(testUID))){
				return test;
			}
		}
		return null;
	}
	
	public synchronized void addTests(ArchlightTest[] tests){
		for(int i = 0; i < tests.length; i++){
			testList.add(tests[i]);
		}
		fireTestsAdded(tests);
	}
	
	public synchronized  void removeTests(ArchlightTest[] tests){
		for(int i = 0; i < tests.length; i++){
			testList.remove(tests[i]);
		}
		fireTestsRemoved(tests);
	}
	
	public ArchlightTest[] getAllTestsByToolID(String toolID){
		List tests = new ArrayList();
		for(Iterator it = testList.iterator(); it.hasNext(); ){
			ArchlightTest test = (ArchlightTest)it.next();
			if((test.getToolID() != null) && (test.getToolID().equals(toolID))){
				tests.add(test);
			}
		}
		return (ArchlightTest[])tests.toArray(new ArchlightTest[tests.size()]);
	}
	
	protected Vector listeners = new Vector();
	
	public void addArchlightTestADTListener(ArchlightTestADTListener l){
		listeners.add(l);
	}
	
	public void removeArchlightTestADTListener(ArchlightTestADTListener l){
		listeners.remove(l);
	}
	
	protected void fireTestsAdded(ArchlightTest[] tests){
		fireEvent(ArchlightTestADTEvent.TESTS_ADDED, tests);
	}
	
	protected void fireTestsRemoved(ArchlightTest[] tests){
		fireEvent(ArchlightTestADTEvent.TESTS_REMOVED, tests);
	}
	
	protected void fireEvent(int type, ArchlightTest[] tests){
		ArchlightTestADTListener[] ls = (ArchlightTestADTListener[])listeners.toArray(new ArchlightTestADTListener[listeners.size()]);
		ArchlightTestADTEvent evt = new ArchlightTestADTEvent(type, tests);
		for(int i = 0; i < ls.length; i++){
			((ArchlightTestADTListener)ls[i]).testADTChanged(evt);
		}
	}
	
}
