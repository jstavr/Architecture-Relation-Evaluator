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
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchMetadataUtils;

public class AssociatedItemsLabelProvider implements ITableLabelProvider,IFontProvider,ITableColorProvider{

	XArchFlatInterface xArchExplicit;
	XArchChangeSetInterface xArchCS;
	IExplicitADT explicitADT;
	RationaleViewManager rationaleViewManager;
	TableViewer viewer;

	Font boldFont;
	Font normalFont;
	Font boldItalicFont;
	Font normalItalicFont;

	Image addedItem = Archstudio4Activator.getImageDescriptor("res/icons/added_item.gif").createImage(true);
	Image deletedDeletedItem = Archstudio4Activator.getImageDescriptor("res/icons/deleted_item.gif").createImage(true);
	Image modifiedItem = Archstudio4Activator.getImageDescriptor("res/icons/modified_item.gif").createImage(true);


	public AssociatedItemsLabelProvider(XArchChangeSetInterface xArchCS,XArchFlatInterface xArchExplicit,IExplicitADT explicitADT,RationaleViewManager rationaleViewManager,TableViewer viewer) {
		this.xArchExplicit = xArchExplicit;
		this.rationaleViewManager = rationaleViewManager;
		this.viewer = viewer;
		boldFont = new Font(viewer.getControl().getDisplay(),"Arial",8,SWT.BOLD);
		normalFont = new Font(viewer.getControl().getDisplay(),"Arial",8,SWT.NORMAL);
		boldItalicFont = new Font(viewer.getControl().getDisplay(),"Arial",8,SWT.BOLD|SWT.ITALIC);
		normalItalicFont = new Font(viewer.getControl().getDisplay(),"Arial",8,SWT.NORMAL|SWT.ITALIC);
		this.xArchCS = xArchCS;
		this.explicitADT = explicitADT;
	}

	public Image getColumnImage(Object element, int columnIndex) {

		if(columnIndex == 1 && element instanceof ObjRef) {
			ObjRef elementRef = (ObjRef)element;
			ObjRef[] ancestors = xArchExplicit.getAllAncestors(elementRef);
			if(!(ancestors.length > 1 && xArchExplicit.isInstanceOf(ancestors[ancestors.length-2], "changesets#ArchChangeSets"))) {

				ObjRef xArchRef = xArchExplicit.getXArch(elementRef);
				ObjRef[] explicitRefs = explicitADT.getExplicitChangeSetRefs(xArchRef);
				if(explicitRefs != null && explicitRefs.length > 0) {
					ChangeStatus changeStatus = xArchCS.getChangeStatus(elementRef, explicitRefs);
					switch(changeStatus) {
					case ADDED:
						return addedItem;
					case MODIFIED:
						return modifiedItem;
					case REMOVED:
						return deletedDeletedItem;
					}
				}
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {

		switch(columnIndex) {
		case 0:
			return null;
		case 1:
			return null;
		case 2:
			if(element instanceof ObjRef) {
				ObjRef objRef = (ObjRef)element;
				String descriptionValue = "";
				try {
					ObjRef descriptionRef = (ObjRef)xArchExplicit.get(objRef,"Description");
					descriptionValue = (String)xArchExplicit.get(descriptionRef,"value");
				}
				catch(Exception e) {}
				IXArchTypeMetadata typeMetadata = xArchExplicit.getTypeMetadata(objRef);
				String typeName = XArchMetadataUtils.getTypeName(typeMetadata.getType());
				return typeName + " " + descriptionValue;
			}
			break;
		}
		return null;
	}

	public void addListener(ILabelProviderListener listener) {


	}

	public void dispose() {
		// TODO Auto-generated method stub

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

		if(element instanceof ObjRef) {
			ObjRef elementRef = (ObjRef)element;
			List<ObjRef> rationaleRefsList = rationaleViewManager.getSelectedRationales();
			List<ObjRef> selectedRefs = rationaleViewManager.getCurrentSelectedRefs();
			boolean found = false;
			if(rationaleRefsList.size() > 0) {
				for(ObjRef rationaleRef : rationaleRefsList) {
					if(xArchExplicit.isInstanceOf(rationaleRef, "changesets#RelationshipRationale")) {
						ObjRef[] ancestors = xArchExplicit.getAllAncestors(rationaleRef);
						if(ancestors != null && ancestors.length > 1) {
							ObjRef ancestor = ancestors[1];
							if(elementRef.equals(ancestor)) {
								found = true;
								font = boldFont;
							}
						}
					}
					else {
						ObjRef[] itemRefArray = xArchExplicit.getAll(rationaleRef,"item");
						for(ObjRef itemRef : itemRefArray) {
							String href = (String)xArchExplicit.get(itemRef,"Href");
							if(href != null && !"".equals(href.trim())) {
								String xArchID = href.replaceFirst("#", "");
								ObjRef associatedItemRef = xArchExplicit.getByID(xArchID);
								if(associatedItemRef.equals(elementRef)) {
									found = true;
									if(selectedRefs.contains(associatedItemRef)) {
										font = boldFont; 
									}
									else {
										font = boldItalicFont;								
									}
									break;
								}
							}
						}
					}
					if(found) {
						break;
					}
				}
				if(!found) {
					if(!selectedRefs.contains(elementRef)) {
						font = normalItalicFont;
					}
				}
			}
			else {
				if(!selectedRefs.contains(elementRef)) {
					font = normalItalicFont;
				}
			}
		}
		return font;
	}

	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {

		if(columnIndex == 2 && element instanceof ObjRef) {
			ObjRef elementRef = (ObjRef)element;
			if(!rationaleViewManager.getCurrentSelectedRefs().contains(elementRef)) {
				return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
			}
		}
		return null;

	}
}
