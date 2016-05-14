package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetUtils;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ChangeSetSorter
    extends ViewerComparator
    implements XArchFlatListener{

	protected final XArchChangeSetInterface xarch;

	public ChangeSetSorter(XArchChangeSetInterface xarch){
		this.xarch = xarch;
	}

	Viewer viewer = null;
	ObjRef xArchRef = null;

	synchronized public void handleXArchFlatEvent(XArchFlatEvent evt){
		if(evt.getIsAttached() && !evt.getIsExtra() && evt.getXArchRef().equals(xArchRef)){
			if((evt.getTargetName().equals("changeSetOrder") || evt.getTargetName().equals("appliedChangeSets")) && evt.getSourceTargetPath().toTagsOnlyString().equals("xArch/archChangeSets")){
				refreshView();
			}
		}
	}

	boolean needsRefresh = false;

	void refreshView(){
		needsRefresh = true;
		final Viewer fViewer = viewer;
		SWTWidgetUtils.async(fViewer, new Runnable(){

			public void run(){
				if(needsRefresh){
					needsRefresh = false;
					fViewer.refresh();
				}
			}
		});
	}

	@Override
	public boolean isSorterProperty(Object element, String property){
		return false;
	}

	@Override
	public int category(Object element){
		throw new UnsupportedOperationException();
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		throw new UnsupportedOperationException();
	}

	@Override
	synchronized public void sort(Viewer viewer, Object[] elements){
		this.viewer = viewer;
		xArchRef = (ObjRef)viewer.getInput();
		if(xArchRef != null){
			List<ObjRef> orderList = XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef, "appliedChangeSets", false);
			if(orderList != null){
				final Map<ObjRef, Integer> orderMap = new HashMap<ObjRef, Integer>();
				for(int i = 0, len = orderList.size(); i < len; i++){
					orderMap.put(orderList.get(i), Integer.valueOf(i));
				}
				final Integer FIRST = Integer.MIN_VALUE;
				Arrays.sort((ObjRef[])elements, new Comparator<ObjRef>(){

					public int compare(ObjRef o1, ObjRef o2){
						Integer i1 = orderMap.get(o1);
						Integer i2 = orderMap.get(o2);
						return (i1 == null ? FIRST : i1).compareTo(i2 == null ? FIRST : i2);
					}
				});
				Collections.reverse(Arrays.asList(elements));
			}
		}
	}
}
