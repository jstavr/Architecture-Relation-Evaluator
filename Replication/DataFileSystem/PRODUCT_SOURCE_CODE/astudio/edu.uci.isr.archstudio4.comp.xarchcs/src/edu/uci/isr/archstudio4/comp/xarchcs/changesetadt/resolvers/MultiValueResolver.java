package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class MultiValueResolver extends AbstractResolver{

	private static String merge(String[] namesPaths){
		StringBuffer sb = new StringBuffer();
		for(int j = 0; j < namesPaths.length; j++){
			if(j > 0)
				sb.append(',');
			sb.append(namesPaths[j].replace('/', '.'));
		}
		return sb.toString();
	}

	IXArchTypeMetadata[][] types;

	String[][] names;

	public MultiValueResolver(XArchFlatQueryInterface xarch, String type, String... namesPaths){
		super("Value[" + type + ":" + merge(namesPaths) + "]:");
		types = new IXArchTypeMetadata[namesPaths.length][];
		names = new String[namesPaths.length][];

		for(int j = 0; j < namesPaths.length; j++){
			this.names[j] = namesPaths[j].split("\\/");
			this.types[j] = new IXArchTypeMetadata[names[j].length];
			types[j][0] = xarch.getTypeMetadata(type);
			for(int i = 0; i < types[j].length; i++){
				IXArchPropertyMetadata prop = types[j][0].getProperty(names[j][0]);
				if(prop.getMaxOccurs() > 1)
					throw new IllegalArgumentException("Property must be singular");
				//			if(prop.getMetadataType() == IXArchPropertyMetadata.ELEMENT && prop.isOptional())
				//				throw new IllegalArgumentException("Property must be required");
				if((i + 1) < types[j].length)
					types[j][i + 1] = xarch.getTypeMetadata(prop.getType());
			}
		}
	}

	@Override
	public boolean canResolve(XArchFlatQueryInterface xarch, ObjRef objRef){
		IXArchTypeMetadata type = xarch.getTypeMetadata(objRef);
		return xarch.isAssignable(types[0][0].getType(), type.getType());
	}

	@Override
	protected String getReferenceData(XArchFlatQueryInterface xarch, ObjRef jObjRef){
		try{
			StringBuffer sb = new StringBuffer();
			for(int j = 0; j < names.length; j++){
				ObjRef objRef = jObjRef;
				for(int i = 0; i < names[j].length - 1; i++){
					if(objRef == null)
						return null;
					objRef = (ObjRef)xarch.get(objRef, names[j][i]);
				}
				if(objRef == null)
					return null;
				if(j > 0)
					sb.append(',');
				sb.append((String)xarch.get(objRef, names[j][names[j].length - 1]));
			}
			return sb.toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
