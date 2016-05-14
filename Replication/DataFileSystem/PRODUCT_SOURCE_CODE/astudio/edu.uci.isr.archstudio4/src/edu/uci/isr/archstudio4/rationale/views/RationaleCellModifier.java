package edu.uci.isr.archstudio4.rationale.views;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Item;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.rationale.RationaleViewManager;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RationaleCellModifier implements ICellModifier{

	RationaleViewManager rationaleViewManager;

	TableViewer rationaleTableViewer;

	XArchFlatInterface xArchExplicit;
	
	XArchChangeSetInterface xArchCS;
	
	IExplicitADT explicitADT;
	
	
	private static boolean equalz(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}	

	public RationaleCellModifier(TableViewer rationaleTableViewer,RationaleViewManager rationaleViewManager,XArchFlatInterface xArchExplicit,XArchChangeSetInterface xArchCS,IExplicitADT explicitADT) {
		this.rationaleViewManager = rationaleViewManager;
		this.xArchExplicit = xArchExplicit;
		this.rationaleTableViewer = rationaleTableViewer;
		this.explicitADT = explicitADT; 
		this.xArchCS = xArchCS;
	}


	public boolean canModify(Object element, String property) {
		if(element instanceof ObjRef) {
			if("Rationale".equals(property)) {
				if(xArchExplicit.isInstanceOf((ObjRef)element, "changesets#RelationshipRationale")) {
					return false;
				}				
				else {
					Object input = rationaleTableViewer.getInput();
					if(input instanceof ObjRef) {
						ObjRef[] explicitRefs = explicitADT.getExplicitChangeSetRefs((ObjRef)input);
						if(explicitRefs.length > 0) {
							ChangeStatus changeStatus = xArchCS.getChangeStatus((ObjRef)element, explicitRefs);
							switch(changeStatus) {
							case REMOVED:
								return false;
							}
						}						
					}
					return true;
				}
			}
			else if("Annotation".equals(property)){
				return false;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	public Object getValue(Object element, String property) {

		if(element instanceof ObjRef) {
			int columnIndex = indexOf(rationaleTableViewer.getColumnProperties(), property);

			switch(columnIndex) {
			case 0:
				return null;
			case 1:
				return true;
			case 2:
				if(element instanceof ObjRef) {
					ObjRef objRef = (ObjRef)element;
					ObjRef descriptionRef = (ObjRef)xArchExplicit.get(objRef,"Description");
					return (String)xArchExplicit.get(descriptionRef,"value");
				}
				break;
			}
		}
		return null;

	}

	public void modify(Object element, String property, Object value) {
		int columnIndex = indexOf(rationaleTableViewer.getColumnProperties(), property);

		if (element instanceof Item)
			element = ((Item) element).getData();
		switch (columnIndex) {
		case 0:
			return;
		case 1:
			return;
		case 2:
			if (element instanceof ObjRef) {
				ObjRef rationaleRef = (ObjRef) element;
				ObjRef descriptionRef = (ObjRef)xArchExplicit.get(rationaleRef,"Description");
				if(value == null) {
					value = new String("");
				}
				xArchExplicit.set(descriptionRef, "Value",(String)value);
				rationaleTableViewer.refresh(rationaleRef);
			}
			break;
		}
	}

	public int indexOf(Object[] values, Object value) {
		for (int i = 0; i < values.length; i++) {
			if (equalz(values[i], value))
				return i;
		}
		return -1;
	}
}
