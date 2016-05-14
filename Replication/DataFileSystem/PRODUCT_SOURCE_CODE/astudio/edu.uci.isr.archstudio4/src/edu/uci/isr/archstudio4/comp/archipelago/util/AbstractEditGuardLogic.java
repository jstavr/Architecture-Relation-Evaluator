package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.booleannotation.ParseException;
import edu.uci.isr.archstudio4.comp.booleannotation.TokenMgrError;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAModelListener;
import edu.uci.isr.bna4.IBNAMouseListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.constants.CompletionStatus;
import edu.uci.isr.bna4.logics.coordinating.MoveWithLogic;
import edu.uci.isr.bna4.things.swt.SWTComboThing;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xarchflat.ObjRef;

public abstract class AbstractEditGuardLogic
	extends AbstractThingLogic
	implements IBNAMenuListener, IBNAModelListener, IBNAMouseListener{

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	protected List<SWTComboThing> openControls = Collections.synchronizedList(new ArrayList<SWTComboThing>());

	public AbstractEditGuardLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}

	public abstract boolean matches(IBNAView view, IThing t);

	public abstract String getXArchID(IBNAView view, IThing t);

	public abstract ObjRef getGuardParentRef(IBNAModel m, ObjRef eltRef);

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

		try{
			ObjRef guardParentRef = getGuardParentRef(view.getWorld().getBNAModel(), eltRef);
			if(guardParentRef == null){
				return new IAction[0];
			}

			String oldGuard = null;
			try{
				oldGuard = AS.booleanNotation.booleanGuardToString(guardParentRef);
			}
			catch(Exception e){
			}
			if(oldGuard == null){
				oldGuard = "";
			}
			final String foldGuard = oldGuard;

			String[] docGuards = AS.guardTracker.getAllGuards(xArchRef);
			if(docGuards == null){
				docGuards = new String[0];
			}

			final String[] fdocGuards = docGuards;

			Action editGuardAction = new Action("Edit Guard..."){

				@Override
				public void run(){
					Point p = BNAUtils.getCentralPoint(ft);
					if(p == null){
						p = new Point(fworldX, fworldY);
					}

					SWTComboThing lt = new SWTComboThing();
					lt.setProperty("#targetXArchID", eltXArchID);
					lt.setOptions(fdocGuards);
					lt.setAllowsArbitraryInput(true);
					lt.setText(foldGuard);
					lt.setAnchorPoint(p);
					MoveWithLogic.moveWith(ft, MoveWithLogic.TRACK_ANCHOR_POINT_FIRST, lt);
					lt.setEditing(true);
					openControls.add(lt);
					fview.getWorld().getBNAModel().addThing(lt, ft);
				}
			};

			return new IAction[]{editGuardAction};
		}
		catch(Exception ioe){
			return new IAction[0];
		}
	}

	public synchronized void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.EventType.THING_CHANGED){
			if(evt.getTargetThing() instanceof SWTComboThing){
				final SWTComboThing lt = (SWTComboThing)evt.getTargetThing();
				if(openControls.contains(lt)){
					if(lt.getCompletionStatus() == CompletionStatus.OK){
						String targetXArchID = lt.getProperty("#targetXArchID");
						if(targetXArchID != null){
							ObjRef eltRef = AS.xarch.getByID(xArchRef, targetXArchID);
							if(eltRef != null){
								ObjRef guardParentRef = getGuardParentRef(evt.getSource(), eltRef);
								if(guardParentRef != null){
									String newValue = lt.getText();
									if(newValue != null){
										newValue = newValue.trim();
									}
									if(newValue == null || newValue.length() == 0){
										AS.xarch.clear(guardParentRef, "guard");
									}
									else{
										try{
											ObjRef guardRef = AS.booleanNotation.parseBooleanGuard(newValue, xArchRef);
											AS.xarch.set(guardParentRef, "guard", guardRef);
											ArchipelagoUtils.showUserNotification(evt.getSource(), "Guard Assigned", lt.getAnchorPoint().x, lt.getAnchorPoint().y);
										}
										catch(ParseException pe){
											final ParseException fpe = pe;
											final String fnewValue = newValue;
											final Composite c = AS.editor.getParentComposite();
											SWTWidgetUtils.sync(c, new Runnable(){

												public void run(){
													MessageDialog.openError(c.getShell(), "Guard Error", fpe.getMessage());
													lt.setText(fnewValue);
													lt.setCompletionStatus(CompletionStatus.INCOMPLETE);
													lt.setEditing(true);
												}
											});
										}
										catch(TokenMgrError tme){
											final TokenMgrError ftme = tme;
											final String fnewValue = newValue;
											final Composite c = AS.editor.getParentComposite();
											SWTWidgetUtils.sync(c, new Runnable(){

												public void run(){
													MessageDialog.openError(c.getShell(), "Guard Error", ftme.getMessage());
													lt.setText(fnewValue);
													lt.setCompletionStatus(CompletionStatus.INCOMPLETE);
													lt.setEditing(true);
												}
											});
										}
									}
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
