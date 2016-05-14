package edu.uci.isr.archstudio4.comp.typewrangler;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.archstudio4.util.XadlTreeContentProvider;
import edu.uci.isr.archstudio4.util.XadlTreeLabelProvider;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class TypeWranglerOutlinePage extends AbstractArchstudioOutlinePage{

	public TypeWranglerOutlinePage(XArchFlatInterface xarch, ObjRef xArchRef, IResources resources){
		super(xarch, xArchRef, resources, false, false);
	}
	
	protected ITreeContentProvider createViewContentProvider(){
		return new XadlTreeContentProvider(xarch, xArchRef,
			XadlTreeUtils.STRUCTURE | XadlTreeUtils.COMPONENT | 
			XadlTreeUtils.CONNECTOR | XadlTreeUtils.COMPONENT_TYPE | 
			XadlTreeUtils.CONNECTOR_TYPE);
	}
	
	protected ILabelProvider createViewLabelProvider(){
		return new XadlTreeLabelProvider(xarch, resources);
	}
	
	public void createControl(Composite parent){
		super.createControl(parent);
		getTreeViewer().expandToLevel(2);
	}
	
	private ObjRef normalize(ObjRef ref){
		String pathString = xarch.getXArchPath(ref).toTagsOnlyString();
		if(!pathString.startsWith("xArch")){
			return xarch.getXArch(ref);
		}
		if(pathString.equals("xArch") ||
			pathString.equals("xArch/archStructure") ||
			pathString.equals("xArch/archStructure/component") ||
			pathString.equals("xArch/archStructure/connector") ||
			pathString.equals("xArch/archTypes") ||
			pathString.equals("xArch/archTypes/componentType") ||
			pathString.equals("xArch/archTypes/connectorType")){
			return ref;
		}
		ObjRef parentRef = xarch.getParent(ref);
		if(parentRef == null){
			return xarch.getXArch(ref);
		}
		return normalize(parentRef);
	}

	public void focusEditor(String editorName, ObjRef[] refs){
		if(refs.length > 0){
			ObjRef ref = refs[0];
			ref = normalize(ref);

			ObjRef[] ancestors = xarch.getAllAncestors(ref);
			for(int j = (ancestors.length - 1); j >= 1; j--){
				getTreeViewer().expandToLevel(ancestors[j], 1);
			}
			IStructuredSelection ss = new StructuredSelection(new ObjRef[]{ref});
			getTreeViewer().setSelection(ss, true);
		}
	}

}
