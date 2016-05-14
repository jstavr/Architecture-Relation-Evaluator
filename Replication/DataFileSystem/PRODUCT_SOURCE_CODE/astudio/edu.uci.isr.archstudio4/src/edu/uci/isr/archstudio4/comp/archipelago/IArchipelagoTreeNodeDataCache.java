package edu.uci.isr.archstudio4.comp.archipelago;

import edu.uci.isr.xarchflat.ObjRef;

public interface IArchipelagoTreeNodeDataCache{

	public void setData(ObjRef xArchRef, Object treeNode, String key, Object data);

	public Object getData(ObjRef xArchRef, Object treeNode, String key);

	public void clear();

}