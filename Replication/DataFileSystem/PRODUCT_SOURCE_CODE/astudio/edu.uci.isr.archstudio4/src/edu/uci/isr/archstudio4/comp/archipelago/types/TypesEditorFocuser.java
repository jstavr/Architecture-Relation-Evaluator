package edu.uci.isr.archstudio4.comp.archipelago.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.draw2d.geometry.Point;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.IArchipelagoEditorFocuser;
import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.FlyToUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchPath;

public class TypesEditorFocuser implements IArchipelagoEditorFocuser{
	protected TreeViewer viewer = null;
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	public TypesEditorFocuser(TreeViewer viewer, ArchipelagoServices services, ObjRef xArchRef){
		this.viewer = viewer;
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public void focusEditor(String editorName, ObjRef[] refs){
		//Highlight the tree nodes
		if(refs.length == 0) return;
		
		List<ObjRef> typeRefList = new ArrayList<ObjRef>();
		for(int i = 0; i < refs.length; i++){
			XArchPath path = AS.xarch.getXArchPath(refs[i]);
			if(path != null){
				String pathString = path.toTagsOnlyString();
				ObjRef[] ancestors = AS.xarch.getAllAncestors(refs[i]);
				if(pathString.startsWith("xArch/archTypes/componentType")){
					ObjRef typeRef = ancestors[ancestors.length - 3];
					if(i == 0){
						focusOnElement(typeRef, refs[i]);
					}
					typeRefList.add(typeRef);
				}
			}
		}
		if(typeRefList.size() > 0){
			IStructuredSelection ss = ArchipelagoUtils.addToSelection(viewer.getSelection(), typeRefList.toArray());
			viewer.setSelection(ss, true);
		}
	}
	
	protected void focusOnElement(ObjRef typeRef, ObjRef ref){
		TypesEditorSupport.setupEditor(AS, typeRef);
		if(BNAUtils.nulleq(typeRef, ref)) return;
		BNAComposite bnaComposite = (BNAComposite)ArchipelagoUtils.getBNAComposite(AS.editor);
		if(bnaComposite != null){
			IBNAView view = bnaComposite.getView();
			if(view != null){
				IBNAModel typeModel = view.getWorld().getBNAModel();
				String xArchID = XadlUtils.getID(AS.xarch, ref);
				if(xArchID != null){
					IThing t = ArchipelagoUtils.findThing(typeModel, xArchID);
					if(t != null){
						IThing glassThing = ArchipelagoUtils.getGlassThing(typeModel, t);
						if(glassThing != null){
							Point p = BNAUtils.getCentralPoint(glassThing);
							if(p != null){
								FlyToUtils.flyTo(view, p.x, p.y);
								ArchipelagoUtils.pulseNotify(typeModel, glassThing);
							}
						}
					}
				}
			}
		}
	}
}
