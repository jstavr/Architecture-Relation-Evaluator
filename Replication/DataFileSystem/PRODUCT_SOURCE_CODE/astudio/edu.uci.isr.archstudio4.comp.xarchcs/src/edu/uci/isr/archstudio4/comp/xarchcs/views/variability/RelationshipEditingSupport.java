package edu.uci.isr.archstudio4.comp.xarchcs.views.variability;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Item;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class RelationshipEditingSupport extends EditingSupport{

	CellEditor editor;
	XArchChangeSetInterface xarch;
	ObjRef relationshipRef;

	public RelationshipEditingSupport(ColumnViewer viewer, XArchChangeSetInterface xarch, ObjRef relationshipRef){
		super(viewer);
		this.xarch = xarch;
		this.editor = new CheckboxCellEditor();
		this.relationshipRef = relationshipRef;
	}

	@Override
	protected boolean canEdit(Object element){
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object element){
		return editor;
	}

	@Override
	protected Object getValue(Object element){
		return Boolean.FALSE;
	}

	@Override
	protected void setValue(Object element, Object value){
		if(element instanceof Item){
			element = ((Item)element).getData();
		}

		ObjRef changeSetRef = (ObjRef)element;
		if(changeSetRef != null){
			String changeSetID = XadlUtils.getID(xarch, changeSetRef);
			if(xarch.isInstanceOf(relationshipRef, "changesets#AndRelationship")){
				toggle(relationshipRef, new String[]{"andChangeSet", "andNotChangeSet", "impliesChangeSet", "impliesNotChangeSet"}, changeSetID);
			}
			else if(xarch.isInstanceOf(relationshipRef, "changesets#OrRelationship")){
				toggle(relationshipRef, new String[]{"orChangeSet", "orNotChangeSet", "impliesChangeSet", "impliesNotChangeSet"}, changeSetID);
			}
			else if(xarch.isInstanceOf(relationshipRef, "changesets#VariantRelationship")){
				toggle(relationshipRef, new String[]{"variantChangeSet"}, changeSetID);
			}
		}
	}

	protected boolean removeLinkId(ObjRef relationshipRef, String typeOfThing, String changeSetID){
		String href = "#" + changeSetID;
		for(ObjRef linkRef: xarch.getAll(relationshipRef, typeOfThing)){
			if(href.equals(XadlUtils.getHref(xarch, linkRef))){
				xarch.remove(relationshipRef, typeOfThing, linkRef);
				return true;
			}
		}
		return false;
	}

	protected void addLinkId(ObjRef relationshipRef, String typeOfThing, String changeSetID){
		String href = "#" + changeSetID;
		for(ObjRef linkRef: xarch.getAll(relationshipRef, typeOfThing)){
			if(href.equals(XadlUtils.getHref(xarch, linkRef))){
				return;
			}
		}

		ObjRef xarchRef = xarch.getXArch(relationshipRef);
		ObjRef typesContextRef = xarch.createContext(xarchRef, "types");
		ObjRef linkRef = xarch.create(typesContextRef, "XMLLink");
		xarch.set(linkRef, "type", "simple");
		xarch.set(linkRef, "href", href);
		xarch.add(relationshipRef, typeOfThing, linkRef);
	}

	protected void toggle(ObjRef relationshipRef, String[] typesOfThings, String changeSetID){
		for(int i = 0; i < typesOfThings.length; i++){
			if(removeLinkId(relationshipRef, typesOfThings[i], changeSetID)){
				if(++i < typesOfThings.length){
					addLinkId(relationshipRef, typesOfThings[i], changeSetID);
				}
				return;
			}
		}
		addLinkId(relationshipRef, typesOfThings[0], changeSetID);
	}
}
