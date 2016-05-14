package edu.uci.isr.archstudio4.rationale.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.rationale.RationaleViewManager;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RationaleContentProvider implements IStructuredContentProvider{

	protected RationaleViewManager rationaleViewManager;
	
	protected IExplicitADT explicitADT;
	
	XArchFlatInterface xArchExplicit;
	
	XArchChangeSetInterface xArchCS;
	
	Viewer viewer;
	
	public RationaleContentProvider(Viewer viewer,RationaleViewManager rationaleViewManager,XArchChangeSetInterface xArchCS,XArchFlatInterface xArchExplicit,IExplicitADT explicitADT) {
		this.rationaleViewManager = rationaleViewManager;
		this.xArchExplicit = xArchExplicit;
		this.explicitADT = explicitADT;
		this.viewer = viewer;
		this.xArchCS = xArchCS;
	}
	
	public Object[] getElements(Object arg0) {
		Object input = viewer.getInput();
		if(input != null && input instanceof ObjRef) {

			ObjRef[] explicitChangeSetRefs = explicitADT.getExplicitChangeSetRefs((ObjRef)input);
			if(explicitChangeSetRefs != null && explicitChangeSetRefs.length > 0) {

				List<ObjRef> allRationales = rationaleViewManager.getRationales(true);
				List<ObjRef> rationalesForCurrentAppliedChangeSets = rationaleViewManager.getRationales(false);
				List<ObjRef> finalListOfObjRefs = new ArrayList<ObjRef>();
				//System.err.println("All Rationales Size: " + allRationales.size());
				//System.err.println("Required Rationales Size: " + rationalesForCurrentAppliedChangeSets.size());
				for(ObjRef rationaleRef : allRationales) {
					if(rationalesForCurrentAppliedChangeSets.contains(rationaleRef)) {
						finalListOfObjRefs.add(rationaleRef);
					}
					else {
						
						ChangeStatus changeStatus = xArchCS.getChangeStatus(rationaleRef, explicitChangeSetRefs);
						//System.err.println("Change Status Entered!!!!  " + changeStatus);
						switch(changeStatus) {
						case ADDED:
						case MODIFIED:
						case REMOVED:
							finalListOfObjRefs.add(rationaleRef);
							break;
						default:
								break;
						}
					}
				}
				if(finalListOfObjRefs.size() > 0) {
					return finalListOfObjRefs.toArray(new ObjRef[finalListOfObjRefs.size()]);
				}
				else {
					return new Object[0];
				}
			}
			else {
				List<ObjRef> rationalesList = rationaleViewManager.getRationales(false);
				if(rationalesList.size() > 0) {
					return rationalesList.toArray(new ObjRef[rationalesList.size()]);
				}
				else {
					return new Object[0];
				}
			}
		}
		else {
			return new Object[0];
		}
	}

	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		viewer.refresh();
	}
}
