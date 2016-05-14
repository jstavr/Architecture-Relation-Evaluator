package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class ValueResolver extends AbstractResolver {

	IXArchTypeMetadata[] types;

	String[] names;

	public ValueResolver(XArchFlatQueryInterface xarch, String type, String namesPath) {
		super("Value[" + type + "." + namesPath.replace('/', '.') + "]:");

		this.names = namesPath.split("\\/");
		this.types = new IXArchTypeMetadata[names.length];
		types[0] = xarch.getTypeMetadata(type);
		for (int i = 0; i < types.length; i++) {
			IXArchPropertyMetadata prop = types[0].getProperty(names[0]);
			if (prop.getMaxOccurs() > 1) {
				throw new IllegalArgumentException("Property must be singular");
			}
			//			if(prop.getMetadataType() == IXArchPropertyMetadata.ELEMENT && prop.isOptional())
			//				throw new IllegalArgumentException("Property must be required");
			if (i + 1 < types.length) {
				types[i + 1] = xarch.getTypeMetadata(prop.getType());
			}
		}
	}

	@Override
	public boolean canResolve(XArchFlatQueryInterface xarch, ObjRef objRef) {
		IXArchTypeMetadata type = xarch.getTypeMetadata(objRef);
		return xarch.isAssignable(types[0].getType(), type.getType());
	}

	@Override
	protected String getReferenceData(XArchFlatQueryInterface xarch, ObjRef objRef) {
		try {
			for (int i = 0; i < names.length - 1; i++) {
				if (objRef == null) {
					return null;
				}
				objRef = (ObjRef) xarch.get(objRef, names[i]);
			}
			if (objRef == null) {
				return null;
			}
			return (String) xarch.get(objRef, names[names.length - 1]);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isOldReference(XArchFlatQueryInterface xarch, ObjRef objRef, String oldReference) {
		// "Value[instance.Point" --> "Value[instance#Point"
		return oldReference.replaceFirst("\\.", "#").equals(getReference(xarch, objRef));
	}
}
