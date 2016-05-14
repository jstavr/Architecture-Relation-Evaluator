package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.IXArchEventHandlerLogic;
import edu.uci.isr.bna4.BNAComposite;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ActivityDiagramsXArchEventHandler
	implements XArchFlatListener{

	protected ArchipelagoServices AS = null;

	public ActivityDiagramsXArchEventHandler(ArchipelagoServices AS){
		this.AS = AS;
	}

	public synchronized void handleXArchFlatEvent(XArchFlatEvent evt){
		BNAComposite currentlyEditingComposite = ArchipelagoUtils.getBNAComposite(AS.editor);
		if(currentlyEditingComposite != null){
			IBNAView view = currentlyEditingComposite.getView();
			if(view != null){
				IBNAWorld world = view.getWorld();
				if(world != null){
					String worldID = world.getID();
					if(worldID.equals("activityDiagram")){
						for(IXArchEventHandlerLogic logic: world.getThingLogicManager().getThingLogics(IXArchEventHandlerLogic.class)){
							logic.handleXArchFlatEvent(evt, world);
						}
					}
				}
			}
		}
	}
}
