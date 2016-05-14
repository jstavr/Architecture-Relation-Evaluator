package edu.uci.isr.archstudio4.comp.archipelago.util;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class BasicPropertyCoder
    implements IPropertyCoder{

	public boolean encode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef, Object propertyValue){
		if(propertyValue instanceof java.lang.String){
			AS.xarch.set(propertyValueRef, "type", "java.lang.String");
			AS.xarch.set(propertyValueRef, "data", (java.lang.String)propertyValue);
			return true;
		}
		else if(propertyValue instanceof java.lang.Boolean){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Boolean");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Boolean)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Byte){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Byte");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Byte)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Short){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Short");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Short)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Character){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Character");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Character)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Integer){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Integer");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Integer)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Long){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Long");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Long)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Float){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Float");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Float)propertyValue).toString());
			return true;
		}
		else if(propertyValue instanceof java.lang.Double){
			AS.xarch.set(propertyValueRef, "type", "java.lang.Double");
			AS.xarch.set(propertyValueRef, "data", ((java.lang.Double)propertyValue).toString());
			return true;
		}
		return false;
	}

	public Object decode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef) throws PropertyDecodeException{
		String propertyType = (String)AS.xarch.get(propertyValueRef, "type");
		if(propertyType == null){
			return null;
		}

		String data = (String)AS.xarch.get(propertyValueRef, "data");
		if(data == null){
			return null;
		}

		try{
			if(propertyType.equals("java.lang.String")){
				return data;
			}
			else if(propertyType.equals("java.lang.Boolean")){
				return Boolean.parseBoolean(data);
			}
			else if(propertyType.equals("java.lang.Byte")){
				return Byte.parseByte(data);
			}
			else if(propertyType.equals("java.lang.Short")){
				return Short.parseShort(data);
			}
			else if(propertyType.equals("java.lang.Character")){
				return data.charAt(0);
			}
			else if(propertyType.equals("java.lang.Integer")){
				return Integer.parseInt(data);
			}
			else if(propertyType.equals("java.lang.Long")){
				return Long.parseLong(data);
			}
			else if(propertyType.equals("java.lang.Float")){
				return Float.parseFloat(data);
			}
			else if(propertyType.equals("java.lang.Double")){
				return Double.parseDouble(data);
			}
		}
		catch(NumberFormatException nfe){
			throw new PropertyDecodeException("Number format error decoding basic property value.", nfe);
		}
		return null;
	}
}
