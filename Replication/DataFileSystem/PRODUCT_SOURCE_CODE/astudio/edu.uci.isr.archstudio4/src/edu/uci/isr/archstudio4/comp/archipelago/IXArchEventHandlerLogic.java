package edu.uci.isr.archstudio4.comp.archipelago;

import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThingLogic;
import edu.uci.isr.xarchflat.XArchFlatEvent;

public interface IXArchEventHandlerLogic extends IThingLogic{
	public void handleXArchFlatEvent(XArchFlatEvent evt, IBNAWorld world);
}
