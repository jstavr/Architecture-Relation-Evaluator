package edu.uci.isr.archstudio4.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchSite;
//import org.eclipse.ui.IWorkbenchSite;

import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class XadlTreeContentProvider implements ITreeContentProvider{

	protected static final Object[] EMPTY_ARRAY = new Object[0];
	
	protected XArchFlatInterface xarch = null;
	protected ObjRef rootRef;
	protected int flags = 0;
	
	protected List<Filter> filters = Collections.synchronizedList(new ArrayList<Filter>());
	
	public XadlTreeContentProvider(XArchFlatInterface xarch, ObjRef rootRef, int flags){
		this.xarch = xarch;
		this.rootRef = rootRef;
		this.flags = flags;
	}
	
	public void addFilter(ObjRef[] showOnlyTheseElements, int type){
		Filter f = new Filter(showOnlyTheseElements, type);
		filters.add(f);
	}
	
	public void clearFilters(){
		filters.clear();
	}
	
	protected Filter getFilter(int type){
		for(Iterator it = filters.iterator(); it.hasNext(); ){
			Filter f = (Filter)it.next();
			if(f.type == type) return f;
		}
		return null;
	}
	
	public boolean shouldShow(int type){
		return (type & flags) != 0;
	}
	
	public Object[] getElements(Object inputElement){
		return getChildren(inputElement);
	}
	
	protected boolean isAllowedByFilter(ObjRef ref, int type){
		Filter f = getFilter(type);
		if(f == null) return true;
		for(int i = 0; i < f.elts.length; i++){
			if(ref.equals(f.elts[i])){
				return true;
			}
		}
		return false;
	}
	
	private void addChildren(List<ObjRef> l, ObjRef ref, String childName, int childType){
		if(shouldShow(childType)){
			ObjRef[] childRefs = xarch.getAll(ref, childName);
			for(int i = 0; i < childRefs.length; i++){
				if(isAllowedByFilter(childRefs[i], childType)){
					l.add(childRefs[i]);
				}
			}
		}
	}
	
	public Object[] getChildren(Object parentElement){
		if(parentElement instanceof XadlTreeInput){
			return new Object[]{rootRef};
		}
		else if(parentElement instanceof IWorkbenchSite){
			return new Object[]{rootRef};
		}
		else if(parentElement instanceof ObjRef){
			ObjRef ref = (ObjRef)parentElement;
			int type = XadlTreeUtils.getType(xarch, ref);
			List<ObjRef> childList = new ArrayList<ObjRef>();
			if(type == XadlTreeUtils.DOCUMENT){
				ObjRef typesContextRef = xarch.createContext(ref, "types");
				ObjRef statechartsContextRef = xarch.createContext(ref, "statecharts");

				if(shouldShow(XadlTreeUtils.STRUCTURE)){
					ObjRef[] structureRefs = xarch.getAllElements(typesContextRef, "archStructure", ref);
					if(structureRefs != null){
						for(int i = 0; i < structureRefs.length; i++){
							if(isAllowedByFilter(structureRefs[i], XadlTreeUtils.STRUCTURE)){
								childList.add(structureRefs[i]);
							}
						}
					}
				}
				if(shouldShow(XadlTreeUtils.STATECHART)){
					ObjRef[] statechartRefs = xarch.getAllElements(statechartsContextRef, "statechart", ref);
					if(statechartRefs != null){
						for(int i = 0; i < statechartRefs.length; i++){
							if(isAllowedByFilter(statechartRefs[i], XadlTreeUtils.STATECHART)){
								childList.add(statechartRefs[i]);
							}
						}
					}
				}
				if(shouldShow(XadlTreeUtils.ANY_TYPE)){
					ObjRef archTypesRef = xarch.getElement(typesContextRef, "archTypes", ref);
					if(archTypesRef != null){
						childList.add(archTypesRef);
					}
				}
			}
			else if(type == XadlTreeUtils.STRUCTURE){
				addChildren(childList, ref, "component", XadlTreeUtils.COMPONENT);
				addChildren(childList, ref, "connector", XadlTreeUtils.CONNECTOR);
			}
			else if(type == XadlTreeUtils.COMPONENT){
				addChildren(childList, ref, "interface", XadlTreeUtils.COMPONENT_INTERFACE);
			}
			else if(type == XadlTreeUtils.CONNECTOR){
				addChildren(childList, ref, "interface", XadlTreeUtils.CONNECTOR_INTERFACE);
			}
			else if(type == XadlTreeUtils.TYPES){
				addChildren(childList, ref, "componentType", XadlTreeUtils.COMPONENT_TYPE);
				addChildren(childList, ref, "connectorType", XadlTreeUtils.CONNECTOR_TYPE);
				addChildren(childList, ref, "interfaceType", XadlTreeUtils.INTERFACE_TYPE);
			}
			else if(type == XadlTreeUtils.COMPONENT_TYPE){
				addChildren(childList, ref, "signature", XadlTreeUtils.COMPONENT_TYPE_SIGNATURE);
			}
			else if(type == XadlTreeUtils.CONNECTOR_TYPE){
				addChildren(childList, ref, "signature", XadlTreeUtils.CONNECTOR_TYPE_SIGNATURE);
			}
			else if(type == XadlTreeUtils.STATECHART){
				addChildren(childList, ref, "state", XadlTreeUtils.STATE);
			}
			return childList.toArray();
		}
		return EMPTY_ARRAY;
	}
	
	public Object getParent(Object element){
		if(element instanceof ObjRef){
			return xarch.getParent((ObjRef)element);
		}
		return null;
	}
	
	public boolean hasChildren(Object element){
		return getChildren(element).length > 0;
	}
	
	public void dispose(){
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
	}
	
	
	protected static class Filter{
		public ObjRef[] elts;
		public int type;
		
		public Filter(ObjRef[] elts, int type){
			this.elts = elts;
			this.type = type;
		}

		public ObjRef[] getElts(){
			return elts;
		}

		public void setElts(ObjRef[] elts){
			this.elts = elts;
		}

		public int getType(){
			return type;
		}

		public void setType(int type){
			this.type = type;
		}
	}
	
	public static class XadlTreeInput{
	}
}
