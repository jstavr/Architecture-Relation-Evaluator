package edu.uci.isr.archstudio4.comp.xarchcs.views.variability;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.XArchCSActivator;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.NoSuchObjectException;
import edu.uci.isr.xarchflat.ObjRef;

public class RelationshipColumnLabelProvider
    extends ColumnLabelProvider{

	static final boolean equalz(Object o1, Object o2){
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private final ColumnViewer viewer;

	private final XArchChangeSetInterface xarch;

	private final ObjRef relationshipRef;

	private boolean isSatisfied = false;

	private boolean isGenerated = false;

	private final Map<Object, Image> imageMap = new HashMap<Object, Image>();

	private Color generatedColor = null;

	public RelationshipColumnLabelProvider(ColumnViewer viewer, XArchChangeSetInterface xarch, ObjRef relationshipRef){
		this.viewer = viewer;
		this.xarch = xarch;
		this.relationshipRef = relationshipRef;
		this.generatedColor = new Color(viewer.getControl().getDisplay(), 240, 240, 240);
		viewer.getControl().addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent e){
				if(generatedColor != null){
					generatedColor.dispose();
				}
				generatedColor = null;
			}
		});
	}

	public ColumnViewer getViewer(){
		return viewer;
	}

	@Override
	public Color getBackground(Object element){
		if(!isSatisfied){
			return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
		}
		if(isGenerated){
			return generatedColor;
		}
		return null;
	}

	@Override
	public Image getImage(Object element){
		return imageMap.get(element);
	}

	@Override
	public String getText(Object element){
		return null;
	}

	public void setSatisfied(boolean isSatisfied){
		if(this.isSatisfied != isSatisfied){
			this.isSatisfied = isSatisfied;
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	public void setGenerated(boolean isGenerated){
		if(this.isGenerated != isGenerated){
			this.isGenerated = isGenerated;
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	public void setImage(Object element, Image image){
		if(element != null){
			if(!equalz(imageMap.get(element), image)){
				imageMap.put(element, image);
				fireLabelProviderChanged(new LabelProviderChangedEvent(this, element));
			}
		}
	}

	Set<ObjRef> andRefs = Collections.emptySet();
	Set<ObjRef> andNotRefs = Collections.emptySet();
	Set<ObjRef> orRefs = Collections.emptySet();
	Set<ObjRef> orNotRefs = Collections.emptySet();
	Set<ObjRef> impliesRefs = Collections.emptySet();
	Set<ObjRef> impliesNotRefs = Collections.emptySet();
	Set<ObjRef> variantRefs = Collections.emptySet();

	public void updateRelationship(){
		try{
			ObjRef xArchRef = xarch.getXArch(relationshipRef);
			ObjRef relationshipRefR = ChangeSetUtils.resolveExternalRelationshipRef(xarch, relationshipRef);
			ImageRegistry ir = XArchCSActivator.getDefault().getImageRegistry();
			IXArchTypeMetadata type = xarch.getTypeMetadata(relationshipRef);
			Map<Object, Image> imageMap = new HashMap<Object, Image>();

			andRefs = scanRelationshipRefs(relationshipRefR, type, "andChangeSet");
			andNotRefs = scanRelationshipRefs(relationshipRefR, type, "andNotChangeSet");
			orRefs = scanRelationshipRefs(relationshipRefR, type, "orChangeSet");
			orNotRefs = scanRelationshipRefs(relationshipRefR, type, "orNotChangeSet");
			impliesRefs = scanRelationshipRefs(relationshipRefR, type, "impliesChangeSet");
			impliesNotRefs = scanRelationshipRefs(relationshipRefR, type, "impliesNotChangeSet");
			variantRefs = scanRelationshipRefs(relationshipRefR, type, "variantChangeSet");

			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, andRefs);
			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, andNotRefs);
			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, orRefs);
			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, orNotRefs);
			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, impliesRefs);
			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, impliesNotRefs);
			ChangeSetUtils.unresolveExternalChangeSetRefs(xarch, xArchRef, variantRefs);

			// update images
			setImages(andRefs, ir.get("res/icons/and_relationship_reference.gif"), imageMap);
			setImages(andNotRefs, ir.get("res/icons/and_not_relationship_reference.gif"), imageMap);
			setImages(orRefs, ir.get("res/icons/or_relationship_reference.gif"), imageMap);
			setImages(orNotRefs, ir.get("res/icons/or_not_relationship_reference.gif"), imageMap);
			setImages(impliesRefs, ir.get("res/icons/implies_relationship_reference.gif"), imageMap);
			setImages(impliesNotRefs, ir.get("res/icons/implies_not_relationship_reference.gif"), imageMap);
			setImages(variantRefs, ir.get("res/icons/variant_relationship_reference.gif"), imageMap);

			Set<Object> elements = new HashSet<Object>();
			elements.addAll(imageMap.keySet());
			elements.addAll(this.imageMap.keySet());
			for(Object element: elements){
				setImage(element, imageMap.get(element));
			}

			setGenerated("true".equals(xarch.get(relationshipRefR, "generated")));

			updateSatisfiedStatus(new HashSet<ObjRef>(Arrays.asList(xarch.getAppliedChangeSetRefs(xarch.getXArch(relationshipRef)))));
		}
		catch(NoSuchObjectException e){
		}
	}

	public void updateSatisfiedStatus(Set<ObjRef> appliedRefs){

		if(xarch.isInstanceOf(relationshipRef, "changesets#AndRelationship")){
			boolean p = true;
			p &= andRefs.isEmpty() ? true : hasAll(appliedRefs, andRefs);
			p &= andNotRefs.isEmpty() ? true : hasNone(appliedRefs, andNotRefs);
			if(impliesRefs.size() > 0 || impliesNotRefs.size() > 0){
				boolean q = hasAll(appliedRefs, impliesRefs) && hasNone(appliedRefs, impliesNotRefs);
				setSatisfied(q || !p);
			}
			else{
				setSatisfied(p);
			}
		}
		else if(xarch.isInstanceOf(relationshipRef, "changesets#OrRelationship")){
			boolean p = true;
			p &= orRefs.isEmpty() ? true : hasAny(appliedRefs, orRefs);
			p &= orNotRefs.isEmpty() ? true : !hasAll(appliedRefs, orNotRefs);
			if(impliesRefs.size() > 0 || impliesNotRefs.size() > 0){
				boolean q = hasAll(appliedRefs, impliesRefs) && hasNone(appliedRefs, impliesNotRefs);
				setSatisfied(q || !p);
			}
			else{
				setSatisfied(p);
			}
		}
		else if(xarch.isInstanceOf(relationshipRef, "changesets#VariantRelationship")){
			int atLeast = parseInt((String)xarch.get(relationshipRef, "atLeast"), 0);
			int atMost = parseInt((String)xarch.get(relationshipRef, "atMost"), 1);
			int count = hasCount(appliedRefs, variantRefs);
			setSatisfied(atLeast <= count && count <= atMost);
		}
	}

	private <T>boolean hasNone(Set<T> set, Collection<T> subset){
		for(T o: subset){
			if(set.contains(o)){
				return false;
			}
		}
		return true;
	}

	private <T>boolean hasAny(Set<T> set, Collection<T> subset){
		for(T o: subset){
			if(set.contains(o)){
				return true;
			}
		}
		return false;
	}

	private <T>boolean hasAll(Set<T> set, Collection<T> subset){
		return set.containsAll(subset);
	}

	private <T>int hasCount(Set<T> set, Collection<T> subset){
		int count = 0;
		for(T o: subset){
			if(set.contains(o)){
				count++;
			}
		}
		return count;
	}

	private int parseInt(String value, int defaultValue){
		try{
			return Integer.parseInt(value);
		}
		catch(Throwable t){
		}
		return defaultValue;
	}

	private Set<ObjRef> scanRelationshipRefs(ObjRef relationshipRef, IXArchTypeMetadata type, String name){
		Set<ObjRef> changeSetRefs = new HashSet<ObjRef>();
		if(type.getProperty(name) != null){
			for(ObjRef linkRef: xarch.getAll(relationshipRef, name)){
				changeSetRefs.add(XadlUtils.resolveXLink(xarch, linkRef));
			}
		}
		changeSetRefs.remove(null);
		return changeSetRefs;
	}

	private void setImages(Collection<ObjRef> objRefs, Image image, Map<Object, Image> imageMap){
		for(ObjRef objRef: objRefs){
			imageMap.put(objRef, image);
		}
	}
}
