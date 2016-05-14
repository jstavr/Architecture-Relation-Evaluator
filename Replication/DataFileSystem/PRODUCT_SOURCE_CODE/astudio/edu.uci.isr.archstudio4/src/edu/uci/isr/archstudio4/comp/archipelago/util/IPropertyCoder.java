package edu.uci.isr.archstudio4.comp.archipelago.util;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public interface IPropertyCoder{

	public boolean encode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef, Object propertyValue);

	public Object decode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef) throws PropertyDecodeException;

}
