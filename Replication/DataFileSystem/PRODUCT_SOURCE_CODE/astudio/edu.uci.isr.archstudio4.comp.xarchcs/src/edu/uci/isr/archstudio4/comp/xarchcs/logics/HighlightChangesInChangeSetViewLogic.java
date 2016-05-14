package edu.uci.isr.archstudio4.comp.xarchcs.logics;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.PlatformUI;

import edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.ChangeSetViewPart;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.IBNAMouseListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.xarchflat.ObjRef;

public class HighlightChangesInChangeSetViewLogic extends AbstractThingLogic implements IBNAMouseListener {

	public static final String XARCH_ID_PROPERTY_NAME = "xArchID"; // TODO: use official source

	protected final XArchChangeSetInterface xarch;

	public HighlightChangesInChangeSetViewLogic(XArchChangeSetInterface xarch) {
		this.xarch = xarch;
	}

	public void mouseClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
		try {
			for (IThing t2 : view.getWorld().getBNAModel().getAllThings()) {
				if (view.getPeer(t2).isInThing(view, worldX, worldY)) {
					IAssembly a = AssemblyUtils.getAssemblyWithPart(t2);
					if (a != null) {
						String id = a.getRootThing().getProperty(XARCH_ID_PROPERTY_NAME);
						if (id != null) {
							ObjRef objRef = xarch.getByID(id);
							if (objRef != null) {
								ChangeSetViewPart csView = (ChangeSetViewPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
								        "edu.uci.isr.archstudio4.comp.xarchcs.view.variability.VariabilityViewPart");
								csView.selectionChanged(null, new StructuredSelection(new ObjRef[] {objRef}));
								return;
							}
						}
					}
				}
			}
		}
		catch (Throwable e) {
		}
	}

	public void mouseDoubleClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

	public void mouseDown(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}

	public void mouseUp(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY) {
	}
}
