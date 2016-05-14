package edu.uci.isr.archstudio4.comp.changesetsviewer;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IChangeSetADT.IChangeReference;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xarchflat.ObjRef;

public class ChangeSetsDisplayLabelProvider
extends LabelProvider
implements ITableLabelProvider, ITableColorProvider{

	private String T_CHANGE_SET_ID = "T_CHANGE_SET_ID";

	TreeViewer viewer;

	XArchChangeSetInterface xArch;

	IChangeSetADT csadt;

	IChangeSetSync cssync;

	public ChangeSetsDisplayLabelProvider(TreeViewer viewer, XArchChangeSetInterface xArch, IChangeSetADT csadt, IChangeSetSync cssync){
		this.viewer = viewer;
		this.xArch = xArch;
		this.csadt = csadt;
		this.cssync = cssync;
	}

	@Override
	public boolean isLabelProperty(Object element, String property){
		return super.isLabelProperty(element, property);
	}

	public Image getColumnImage(Object element, int columnIndex){
		return null;
	}

	public String getColumnText(Object element, int columnIndex){

		Object input = viewer.getInput();
		if(input instanceof ObjRef){
			ObjRef xArchRef = (ObjRef)input;
			if(element instanceof IChangeReference){
				IChangeReference cRef = (IChangeReference)element;
				if(columnIndex == 0){
					return csadt.getElementReference(cRef);
				}
				else{
					ObjRef changeSetRef = xArch.getByID((String)viewer.getTree().getColumn(columnIndex).getData(T_CHANGE_SET_ID));
					if(changeSetRef != null){
						ObjRef changeSegmentRef = csadt.getChangeSegmentRef(xArchRef, changeSetRef, cRef);
						if(changeSegmentRef != null){
							if(xArch.isInstanceOf(changeSegmentRef, "changesets#ElementManySegment")){
								return (String)xArch.get(changeSegmentRef, "Reference");
							}
							else if(xArch.isInstanceOf(changeSegmentRef, "changesets#ElementSegment")){
								return (String)xArch.get(changeSegmentRef, "Type");
							}
							else if(xArch.isInstanceOf(changeSegmentRef, "changesets#AttributeSegment")){
								return (String)xArch.get(changeSegmentRef, "Value");
							}
						}
					}
				}
			}
		}
		return "";
	}

	public Color getBackground(Object element, int columnIndex){

		Object input = viewer.getInput();
		if(input instanceof ObjRef){
			ObjRef xArchRef = (ObjRef)input;
			if(element instanceof IChangeReference){
				IChangeReference cRef = (IChangeReference)element;
				ObjRef changeSetRef = xArch.getByID((String)viewer.getTree().getColumn(columnIndex).getData(T_CHANGE_SET_ID));
				if(changeSetRef != null){
					ObjRef changeSegmentRef = csadt.getChangeSegmentRef(xArchRef, changeSetRef, cRef);
					if(changeSegmentRef != null){
						if(xArch.isInstanceOf(changeSegmentRef, "changesets#ElementManySegment")){
							return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
						}
						else if(xArch.isInstanceOf(changeSegmentRef, "changesets#ElementSegment")){
							ObjRef[] changeSetRefs = new ObjRef[]{changeSetRef};
							switch(cssync.getChangeStatus(xArchRef, changeSetRefs,cRef, csadt.getChangeSegmentRefs(xArchRef, changeSetRefs, cRef),false)[0]){
							case REMOVED:
								return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
							case ADDED:
								return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_CYAN);
							}
						}
					}
				}
			}
		}
		return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_WHITE);
	}

	public Color getForeground(Object element, int columnIndex){
		return null;
	}
}
