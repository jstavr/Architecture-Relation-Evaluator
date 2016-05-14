package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
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
import edu.uci.isr.bna4.things.swt.SWTComboThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public abstract class AbstractEditDirectionLogic
	extends AbstractThingLogic
	implements IBNAMenuListener, IBNAModelListener, IBNAMouseListener{

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	protected List<SWTComboThing> openControls = Collections.synchronizedList(new ArrayList<SWTComboThing>());

	public AbstractEditDirectionLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public abstract boolean matches(IBNAView view, IThing t);

	public abstract String getXArchID(IBNAView view, IThing t);

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
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;

		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			//Nothing to set description on
			return new IAction[0];
		}

		ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			//Nothing to set description on
			return new IAction[0];
		}

		String oldDirection = XadlUtils.getDirection(AS.xarch, eltRef);
		if(oldDirection == null){
			oldDirection = "none";
		}

		final String foldDirection = oldDirection;

		Action editDirectionAction = new Action("Edit Direction..."){

			@Override
			public void run(){
				Point p = BNAUtils.getCentralPoint(ft);
				if(p == null){
					p = new Point(fworldX, fworldY);
				}

				SWTComboThing lt = new SWTComboThing();
				lt.setProperty("#targetXArchID", eltXArchID);
				lt.setOptions(new String[]{"none", "in", "out", "inout"});
				lt.setAllowsArbitraryInput(false);
				lt.setText(foldDirection);
				lt.setAnchorPoint(p);
				MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, lt);
				lt.setEditing(true);
				openControls.add(lt);
				fview.getWorld().getBNAModel().addThing(lt, ft);
			}
		};

		return new IAction[]{editDirectionAction};
	}

	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.EventType.THING_CHANGED){
			if(evt.getTargetThing() instanceof SWTComboThing){
				SWTComboThing lt = (SWTComboThing)evt.getTargetThing();
				if(openControls.contains(lt)){
					if(lt.getCompletionStatus() == CompletionStatus.OK){
						String targetXArchID = lt.getProperty("#targetXArchID");
						if(targetXArchID != null){
							ObjRef eltRef = AS.xarch.getByID(xArchRef, targetXArchID);
							if(eltRef != null){
								String newValue = lt.getText();
								if(newValue == null){
									AS.xarch.clear(eltRef, "direction");
								}
								else{
									XadlUtils.setDirection(AS.xarch, eltRef, newValue);
								}
							}
						}
					}
					if(lt.getCompletionStatus() != CompletionStatus.INCOMPLETE){
						evt.getSource().removeThing(lt);
						openControls.remove(lt);
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
				SWTComboThing[] oc = openControls.toArray(new SWTComboThing[openControls.size()]);
				for(SWTComboThing tt: oc){
					tt.setCompletionStatus(CompletionStatus.CANCEL);
					tt.setEditing(false);
				}
			}
		}
	}

	public void mouseClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}

	public void mouseDoubleClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}
}
