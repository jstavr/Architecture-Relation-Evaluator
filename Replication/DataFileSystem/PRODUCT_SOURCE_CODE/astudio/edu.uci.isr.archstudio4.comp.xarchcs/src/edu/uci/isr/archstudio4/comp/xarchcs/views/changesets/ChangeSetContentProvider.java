package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.XArchPath;
import edu.uci.isr.xarchflat.utils.IXArchRelativePathTrackerListener;
import edu.uci.isr.xarchflat.utils.XArchRelativePathTracker;

public class ChangeSetContentProvider implements ITreeContentProvider, XArchFlatListener, IXArchRelativePathTrackerListener {

	protected XArchFlatInterface xarch;
	protected XArchRelativePathTracker changeSetTracker;
	protected Viewer viewer = null;
	protected ObjRef xArchRef = null;

	public ChangeSetContentProvider(XArchFlatInterface xarch) {
		this.xarch = xarch;
		this.changeSetTracker = new XArchRelativePathTracker(xarch);
		this.changeSetTracker.addTrackerListener(this);
	}

	public void dispose() {
		viewer = null;
		xArchRef = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		this.xArchRef = (ObjRef) newInput;
		this.changeSetTracker.setTrackInfo(xArchRef, "archChangeSets/changeSet");
		this.changeSetTracker.startScanning();
	}

	public Object[] getElements(Object inputElement) {
		return changeSetTracker.getAddedObjRefs();
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt) {
		changeSetTracker.handleXArchFlatEvent(evt);
	}

	public void processAdd(final ObjRef changeSetRef, ObjRef[] relativeAncestorRefs) {
		refresh();
	}

	public void processUpdate(ObjRef objRef, ObjRef[] relativeAncestorRefs, XArchFlatEvent evt, XArchPath relativeSourceTargetPath) {
	}

	public void processRemove(final ObjRef changeSetRef, ObjRef[] relativeAncestorRefs) {
		refresh();
	}

	boolean needsRefresh = false;

	private void refresh() {
		needsRefresh = true;
		SWTWidgetUtils.async(viewer, new Runnable() {

			public void run() {
				if (needsRefresh) {
					needsRefresh = false;
					viewer.refresh();
				}
			}
		});

	}
}
