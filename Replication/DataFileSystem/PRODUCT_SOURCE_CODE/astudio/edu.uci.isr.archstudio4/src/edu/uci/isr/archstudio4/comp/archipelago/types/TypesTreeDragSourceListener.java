package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesTreeDragSourceListener implements DragSourceListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public TypesTreeDragSourceListener(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public void dragStart(DragSourceEvent event){
		if((event.data != null) && (event.data instanceof ObjRef)){
			if(AS.xarch.isInstanceOf((ObjRef)event.data, "types#ArchStructure")){
				//For dropping structures on types; only allow a drag if we're editing a type
				BNAComposite bnaComposite = ArchipelagoUtils.getBNAComposite(AS.editor);
				if(bnaComposite != null){
					IBNAView view = bnaComposite.getView();
					if(view != null){
						IBNAModel m = view.getWorld().getBNAModel();
						if(m != null){
							EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(m);
							String editingXArchID = ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
							if(editingXArchID != null){
								ObjRef editingRef = AS.xarch.getByID(xArchRef, editingXArchID);
								if(editingRef != null){
									if(AS.xarch.isInstanceOf(editingRef, "types#ComponentType") || AS.xarch.isInstanceOf(editingRef, "types#ConnectorType")){
										event.doit = true;
										event.detail = DND.DROP_LINK;
									}
								}
							}
						}
					}
				}
			}
			else if(AS.xarch.isInstanceOf((ObjRef)event.data, "types#ComponentType")){
				event.doit = true;
				event.detail = DND.DROP_LINK;
			}
			else if(AS.xarch.isInstanceOf((ObjRef)event.data, "types#ConnectorType")){
				event.doit = true;
				event.detail = DND.DROP_LINK;
			}
			else if(AS.xarch.isInstanceOf((ObjRef)event.data, "types#InterfaceType")){
				event.doit = true;
				event.detail = DND.DROP_LINK;
			}
		}
	}
	
	public void dragSetData(DragSourceEvent event){
	}
	
	public void dragFinished(DragSourceEvent event){
	}
	
}
