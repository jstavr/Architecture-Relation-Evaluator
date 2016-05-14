package edu.uci.isr.archstudio4.comp.archipelago.things;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.util.XadlTreeContentProvider;
import edu.uci.isr.archstudio4.util.XadlTreeLabelProvider;
import edu.uci.isr.bna4.things.swt.AbstractSWTTreeThingPeer;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class SWTXadlSelectorThingPeer extends AbstractSWTTreeThingPeer<SWTXadlSelectorThing> {

	public SWTXadlSelectorThingPeer(SWTXadlSelectorThing t) {
		super(t, SWTXadlSelectorThing.class);
	}

	@Override
	protected Object getInput() {
		return t.getContentProviderRootRef();
	}

	@Override
	protected ITreeContentProvider getContentProvider() {
		XArchFlatInterface xarch = t.getRepository();
		if (xarch != null) {
			ObjRef rootRef = t.getContentProviderRootRef();
			if (rootRef != null) {
				int flags = t.getContentProviderFlags();
				XadlTreeContentProvider contentProvider = new XadlTreeContentProvider(xarch, rootRef, flags);
				return contentProvider;
			}
		}
		return null;
	}

	@Override
	protected ILabelProvider getLabelProvider() {
		XArchFlatInterface xarch = t.getRepository();
		if (xarch != null) {
			IResources resources = t.getResources();
			if (resources != null) {
				return new XadlTreeLabelProvider(xarch, resources);
			}
		}
		return null;
	}

}
