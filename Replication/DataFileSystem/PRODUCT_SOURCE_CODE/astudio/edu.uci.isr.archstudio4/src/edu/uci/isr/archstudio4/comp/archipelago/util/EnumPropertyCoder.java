package edu.uci.isr.archstudio4.comp.archipelago.util;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class EnumPropertyCoder
    implements IPropertyCoder{

	public boolean encode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef, Object propertyValue){
		Class c = propertyValue.getClass();
		if(c.isEnum()){
			AS.xarch.set(propertyValueRef, "type", ArchipelagoUtils.getClassName(c) + "[E]");
			AS.xarch.set(propertyValueRef, "data", propertyValue.toString());
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Object decode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef) throws PropertyDecodeException{
		String propertyType = (String)AS.xarch.get(propertyValueRef, "type");
		if(propertyType == null){
			return null;
		}
		if(propertyType.endsWith("[E]")){
			String innerPropertyType = propertyType.substring(0, propertyType.length() - 3);

			String data = (String)AS.xarch.get(propertyValueRef, "data");
			if(data != null){
				try{
					Class cc = HintSupport.getInstance().classForName(innerPropertyType);
					return Enum.valueOf(cc, data);
				}
				catch(ClassNotFoundException cnfe){
					throw new PropertyDecodeException("Can't decode enum type: " + propertyType, cnfe);
				}
			}
		}
		return null;
	}
}
