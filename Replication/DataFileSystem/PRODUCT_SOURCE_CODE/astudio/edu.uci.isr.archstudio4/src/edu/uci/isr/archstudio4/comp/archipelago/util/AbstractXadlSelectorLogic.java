package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.things.SWTXadlSelectorThing;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModelListener;
import edu.uci.isr.bna4.IBNAMouseListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.constants.CompletionStatus;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.xarchflat.ObjRef;

public abstract class AbstractXadlSelectorLogic
	extends AbstractThingLogic
	implements IBNAMenuListener, IBNAModelListener, IBNAMouseListener{

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	protected List<SWTXadlSelectorThing> openControls = Collections.synchronizedList(new ArrayList<SWTXadlSelectorThing>());

	public AbstractXadlSelectorLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public abstract boolean matches(IBNAView view, IThing t);

	public abstract String getXArchID(IBNAView view, IThing t);

	public abstract String getMenuItemString();

	public abstract ImageDescriptor getMenuItemIcon(ObjRef eltRef);

	public abstract Object getInitialValue(ObjRef eltRef);

	public abstract ObjRef getRootRef(ObjRef eltRef);

	public abstract int getFlags(ObjRef eltRef);

	public abstract void setValue(ObjRef eltRef, Object newValue);

	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		IThing[] selectedThings = BNAUtils.getSelectedThings(view.getWorld().getBNAModel());
		if(selectedThings.length > 1){
			return;
		}

		if(matches(view, t)){
			for(IAction action: getActions(view, t, worldX, worldY)){
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		final ArchipelagoServices fAS = AS;
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;

		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			//Nothing to set description on
			return new IAction[0];
		}

		final ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			//Nothing to set description on
			return new IAction[0];
		}

		final Object finitialValue = getInitialValue(eltRef);

		Action action = new Action(getMenuItemString()){

			@Override
			public void run(){
				Point p = BNAUtils.getCentralPoint(ft);
				if(p == null){
					p = new Point(fworldX, fworldY);
				}

				SWTXadlSelectorThing st = new SWTXadlSelectorThing();
				st.setProperty("#targetXArchID", eltXArchID);
				st.setRepository(fAS.xarch);
				st.setResources(fAS.resources);
				st.setContentProviderRootRef(getRootRef(eltRef));
				st.setContentProviderFlags(getFlags(eltRef));

				if(finitialValue != null){
					st.setValue(finitialValue);
				}

				st.setAnchorPoint(p);
				MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, st);
				st.setEditing(true);
				openControls.add(st);
				fview.getWorld().getBNAModel().addThing(st, ft);
			}
		};
		ImageDescriptor icon = getMenuItemIcon(eltRef);
		if(icon != null){
			action.setImageDescriptor(icon);
		}
		return new IAction[]{action};
	}

	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.EventType.THING_CHANGED){
			if(evt.getTargetThing() instanceof SWTXadlSelectorThing){
				SWTXadlSelectorThing st = (SWTXadlSelectorThing)evt.getTargetThing();
				if(openControls.contains(st)){
					if(st.getCompletionStatus() == CompletionStatus.OK){
						String targetXArchID = st.getProperty("#targetXArchID");
						if(targetXArchID != null){
							ObjRef eltRef = AS.xarch.getByID(xArchRef, targetXArchID);
							if(eltRef != null){
								setValue(eltRef, st.getValue());
							}
						}
					}
					if(st.getCompletionStatus() != CompletionStatus.INCOMPLETE){
						evt.getSource().removeThing(st);
						openControls.remove(st);
					}
				}
			}
		}
	}

	public void mouseUp(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}

	public void mouseDown(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
		if(evt.button == 1){
			if(openControls.size() > 0){
				SWTXadlSelectorThing[] oc = openControls.toArray(new SWTXadlSelectorThing[openControls.size()]);
				for(SWTXadlSelectorThing st: oc){
					st.setCompletionStatus(CompletionStatus.CANCEL);
					st.setEditing(false);
				}
			}
		}
	}

	public void mouseClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}

	public void mouseDoubleClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}
}
