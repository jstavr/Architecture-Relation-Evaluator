package edu.uci.isr.archstudio4.rationale.views;

import java.util.List;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.Archstudio4Activator;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.rationale.RationaleViewManager;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class RationaleLabelProvider implements ITableLabelProvider, IFontProvider, ITableColorProvider {

	XArchChangeSetInterface xarch;
	RationaleViewManager rationaleViewManager;
	TableViewer viewer;
	Color generatedColor;

	Font boldFont;
	Font normalFont;
	Font boldItalicFont;
	Font normalItalicFont;
	Image addedRationaleImage = Archstudio4Activator.getImageDescriptor("res/icons/added_item.gif").createImage(true);
	Image deletedRationaleImage = Archstudio4Activator.getImageDescriptor("res/icons/deleted_item.gif").createImage(true);
	Image modifiedRationaleImage = Archstudio4Activator.getImageDescriptor("res/icons/modified_item.gif").createImage(true);
	IExplicitADT explicitADT;

	public RationaleLabelProvider(XArchChangeSetInterface xarch, TableViewer viewer,
	        RationaleViewManager rationaleViewManager, IExplicitADT explicitADT) {
		this.xarch = xarch;
		this.rationaleViewManager = rationaleViewManager;
		this.viewer = viewer;
		boldFont = new Font(viewer.getControl().getDisplay(), "Arial", 8, SWT.BOLD);
		normalFont = new Font(viewer.getControl().getDisplay(), "Arial", 8, SWT.NORMAL);
		generatedColor = new Color(viewer.getControl().getDisplay(), 240, 240, 240);
		this.explicitADT = explicitADT;
		this.xarch = xarch;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof ObjRef) {

			ObjRef ref = (ObjRef) element;

			ObjRef[] ancestors = xarch.getAllAncestors(ref);
			if (!(ancestors.length > 1 && xarch.isInstanceOf(ancestors[ancestors.length - 2], "changesets#ArchChangeSets"))) {

				switch (columnIndex) {
				case 0:
					return null;
				case 1:
					Object input = viewer.getInput();
					if (input != null && input instanceof ObjRef) {
						ObjRef[] explicitObjRefs = explicitADT.getExplicitChangeSetRefs((ObjRef) viewer.getInput());
						if (explicitObjRefs != null && explicitObjRefs.length > 0) {
							ChangeStatus changeStatus = xarch.getChangeStatus(ref, explicitObjRefs);
							switch (changeStatus) {
							case ADDED:
								return addedRationaleImage;
							case REMOVED:
								return deletedRationaleImage;
							case MODIFIED:
								return modifiedRationaleImage;
							}
						}
					}
					return null;
				case 2:
					return null;
				}
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {

		switch (columnIndex) {
		case 0:
			return null;
		case 1:
			return null;
		case 2:
			if (element instanceof ObjRef) {
				ObjRef objRef = (ObjRef) element;
				if (xarch.isInstanceOf(objRef, "changesets#RelationshipRationale")) {
					ObjRef[] sourceRefs = xarch.getAll(objRef, "Source");
					String descriptionValue = "";
					for (ObjRef sourceRef : sourceRefs) {
						String path = (String) xarch.get(sourceRef, "xArchPath");
						descriptionValue += path;
						ObjRef sourceChangeSetLinkRef = (ObjRef) xarch.get(sourceRef, "ChangeSet");
						if (sourceChangeSetLinkRef != null) {
							String href = (String) xarch.get(sourceChangeSetLinkRef, "Href");
							String xArchID = href.replaceFirst("#", "");
							ObjRef changeSetRef = xarch.getByID(xArchID);
							ObjRef descriptionRef = (ObjRef) xarch.get(changeSetRef, "Description");
							String value = (String) xarch.get(descriptionRef, "value");
							descriptionValue += " in " + value + "\n";
						}
					}
					if (descriptionValue.length() > 0) {
						descriptionValue = descriptionValue.substring(0, descriptionValue.length() - 1);
					}

					ObjRef[] requiresRefs = xarch.getAll(objRef, "Requires");
					if (requiresRefs != null && requiresRefs.length > 0) {
						descriptionValue += "\n references \n";
						for (ObjRef requiresRef : requiresRefs) {
							String path = (String) xarch.get(requiresRef, "xArchPath");
							descriptionValue += path;
							ObjRef requiresChangeSetLinkRef = (ObjRef) xarch.get(requiresRef, "ChangeSet");
							if (requiresChangeSetLinkRef != null) {
								String href = (String) xarch.get(requiresChangeSetLinkRef, "Href");
								String xArchID = href.replaceFirst("#", "");
								ObjRef changeSetRef = xarch.getByID(xArchID);
								ObjRef descriptionRef = (ObjRef) xarch.get(changeSetRef, "Description");
								String value = (String) xarch.get(descriptionRef, "value");
								descriptionValue += " in " + value + "\n";
							}

						}
						descriptionValue = descriptionValue.substring(0, descriptionValue.length() - 1);
					}

					return descriptionValue;
				}
				else {
					ObjRef descriptionRef = (ObjRef) xarch.get(objRef, "Description");
					String descriptionValue = (String) xarch.get(descriptionRef, "value");
					return descriptionValue;
				}
			}
			break;
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
	}

	public Font getFont(Object element) {
		Font font = null;
		if (element instanceof ObjRef) {
			List<ObjRef> selectedAssociatedItemRefs = rationaleViewManager.getSelectedAssociatedItemRefList();
			ObjRef rationaleRef = (ObjRef) element;
			if (xarch.isInstanceOf(rationaleRef, "changesets#RelationshipRationale")) {
				ObjRef[] ancestors = xarch.getAllAncestors(rationaleRef);
				if (ancestors != null && ancestors.length > 1) {
					ObjRef ancestor = ancestors[1];
					for (ObjRef selectedAssociatedItemRef : selectedAssociatedItemRefs) {
						if (selectedAssociatedItemRef.equals(ancestor)) {
							font = boldFont;
							break;
						}
					}
				}
			}
			else {

				ObjRef[] itemRefArray = xarch.getAll(rationaleRef, "item");
				for (ObjRef itemRef : itemRefArray) {
					String href = (String) xarch.get(itemRef, "Href");
					if (href != null && !"".equals(href.trim())) {
						String xArchID = href.replaceFirst("#", "");
						ObjRef associatedItemRef = xarch.getByID(xArchID);
						if (selectedAssociatedItemRefs.contains(associatedItemRef)) {
							font = boldFont;
						}
					}
				}
			}
		}
		return font;
	}

	public Color getBackground(Object element, int columnIndex) {
		if (element instanceof ObjRef) {
			switch (columnIndex) {
			case 0:
				return null;
			case 1:
				return null;
			case 2: {
				ObjRef elementRef = (ObjRef) element;
				if (xarch.isInstanceOf(elementRef, "changesets#RelationshipRationale")) {
					ObjRef[] ancestors = xarch.getAllAncestors(elementRef);
					if (ancestors != null && ancestors.length > 1) {
						ObjRef ancestor = ancestors[1];
						String generated = (String) xarch.get(ancestor, "generated");
						if ("true".equals(generated)) {
							return generatedColor;
						}
					}
				}
			}
			}
		}
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
}