package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.lang.reflect.Array;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class ArrayPropertyCoder
    implements IPropertyCoder{

	public boolean encode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef, Object propertyValue){
		Class c = propertyValue.getClass();
		if(c.isArray()){
			AS.xarch.set(propertyValueRef, "type", ArchipelagoUtils.getClassName(c));

			ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
			Object[] arr = (Object[])propertyValue;
			for(Object element: arr){
				ObjRef childValueRef = AS.xarch.create(hintsContextRef, "propertyValue");
				if(masterCoder.encode(masterCoder, AS, xArchRef, childValueRef, element)){
					AS.xarch.add(propertyValueRef, "value", childValueRef);
				}
			}
			return true;
		}
		return false;
	}

	public Object decode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef) throws PropertyDecodeException{
		String propertyType = (String)AS.xarch.get(propertyValueRef, "type");
		if(propertyType == null){
			return null;
		}
		if(propertyType.endsWith("[]")){
			String innerPropertyType = propertyType.substring(0, propertyType.length() - 2);

			ObjRef[] refs = AS.xarch.getAll(propertyValueRef, "value");
			try{
				Class cc = HintSupport.getInstance().classForName(getRealClassName(innerPropertyType));
				Object[] arr = (Object[])Array.newInstance(cc, refs.length);
				for(int i = 0; i < refs.length; i++){
					arr[i] = masterCoder.decode(masterCoder, AS, xArchRef, refs[i]);
				}
				return arr;
			}
			catch(ClassNotFoundException cnfe){
				throw new PropertyDecodeException("Can't decode array type: " + propertyType, cnfe);
			}
		}
		return null;
	}

	private static String getRealClassName(String name){
		if(name.endsWith("[]")){
			String internalName = getRealClassName(name.substring(0, name.length() - 2));
			if(internalName.startsWith("[")){
				return "[" + internalName;
			}
			else{
				return "[L" + getRealClassName(name.substring(0, name.length() - 2)) + ";";
			}
		}
		else{
			return name;
		}
	}

}
