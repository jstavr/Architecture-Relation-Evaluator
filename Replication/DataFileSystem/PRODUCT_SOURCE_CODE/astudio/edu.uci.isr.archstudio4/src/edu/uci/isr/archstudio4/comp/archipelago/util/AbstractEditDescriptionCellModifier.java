package edu.uci.isr.archstudio4.comp.archipelago.util;

import org.eclipse.jface.viewers.ICellModifier;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public abstract class AbstractEditDescriptionCellModifier implements ICellModifier {
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public AbstractEditDescriptionCellModifier(ArchipelagoServices services, ObjRef xArchRef) {
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public Object getValue(Object element, String property) {
		if (element instanceof ObjRef) {
			ObjRef targetRef = (ObjRef) element;
			String description = XadlUtils.getDescription(AS.xarch, targetRef);
			return description == null ? "" : description;
		}
		return null;
	}

	public void modify(Object element, String property, Object value) {
		if (element instanceof ObjRef) {
			ObjRef targetRef = (ObjRef) element;
			if (value != null) {
				String newDescription = value.toString();
				XadlUtils.setDescription(AS.xarch, targetRef, newDescription);
			}
		}
	}
}
