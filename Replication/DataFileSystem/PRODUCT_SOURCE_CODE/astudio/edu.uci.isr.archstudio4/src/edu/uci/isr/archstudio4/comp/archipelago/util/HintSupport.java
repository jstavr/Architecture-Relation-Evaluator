package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.util.HashSet;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class HintSupport
    implements IPropertyCoder{

	protected static HintSupport __instance = null;

	protected HintSupport(){
	}

	public void writeTarget(ArchipelagoServices AS, ObjRef xArchRef, ObjRef eltRef, ObjRef targetRef){
		if(targetRef != null){
			String targetID = XadlUtils.getID(AS.xarch, targetRef);
			if(targetID != null){
				XadlUtils.setXLink(AS.xarch, eltRef, "target", targetID);
			}
		}
	}

	public void writeProperty(ArchipelagoServices AS, ObjRef xArchRef, ObjRef eltRef, String name, Object value){
		if(value == null){
			return;
		}

		ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
		ObjRef propertyRef = AS.xarch.create(hintsContextRef, "property");

		AS.xarch.set(propertyRef, "name", name);
		ObjRef propertyValueRef = AS.xarch.create(hintsContextRef, "propertyValue");
		if(encode(this, AS, xArchRef, propertyValueRef, value)){
			AS.xarch.set(propertyRef, "value", propertyValueRef);
			AS.xarch.add(eltRef, "property", propertyRef);
		}
	}

	public ObjRef readTarget(ArchipelagoServices AS, ObjRef xArchRef, ObjRef eltRef){
		if(eltRef != null){
			return XadlUtils.resolveXLink(AS.xarch, eltRef, "target");
		}
		return null;
	}

	public Object readProperty(ArchipelagoServices AS, ObjRef xArchRef, ObjRef eltRef, String name){
		ObjRef[] propertyRefs = AS.xarch.getAll(eltRef, "property");
		for(ObjRef propertyRef: propertyRefs){
			String n = (String)AS.xarch.get(propertyRef, "name");
			if(n != null && n.equals(name)){
				ObjRef propertyValueRef = (ObjRef)AS.xarch.get(propertyRef, "value");
				try{
					Object value = decode(this, AS, xArchRef, propertyValueRef);
					if (value instanceof org.eclipse.swt.graphics.Point) {
						value = BNAUtils.toPoint((org.eclipse.swt.graphics.Point) value);
					}
					else if (value instanceof org.eclipse.swt.graphics.Point[]) {
						value = BNAUtils.toPoints((org.eclipse.swt.graphics.Point[]) value);
					}
					else if (value instanceof org.eclipse.swt.graphics.Rectangle) {
						value = BNAUtils.toRectangle((org.eclipse.swt.graphics.Rectangle) value);
					}
					return value;
				}
				catch(PropertyDecodeException pde){
				}
			}
		}
		return null;
	}

	public Class classForName(String name) throws ClassNotFoundException{
		try{
			Class c = Class.forName(name);
			return c;
		}
		catch(ClassNotFoundException cnfe){
			for(ClassLoader cl: classLoaders){
				try{
					Class c = Class.forName(name, true, cl);
					return c;
				}
				catch(ClassNotFoundException cnfe2){
				}
			}
			for(IPropertyCoder pc: propertyCoders){
				try{
					Class c = Class.forName(name, true, pc.getClass().getClassLoader());
					return c;
				}
				catch(ClassNotFoundException cnfe2){
				}
			}
			throw cnfe;
		}
	}

	protected Set<ClassLoader> classLoaderSet = new HashSet<ClassLoader>();
	protected ClassLoader[] classLoaders = new ClassLoader[0];

	public void registerClassLoader(ClassLoader cl){
		classLoaderSet.add(cl);
		classLoaders = classLoaderSet.toArray(new ClassLoader[classLoaderSet.size()]);
	}

	public void unregisterClassloader(ClassLoader cl){
		classLoaderSet.remove(cl);
		classLoaders = classLoaderSet.toArray(new ClassLoader[classLoaderSet.size()]);
	}

	protected Set<IPropertyCoder> propertyCoderSet = new HashSet<IPropertyCoder>();
	protected IPropertyCoder[] propertyCoders = new IPropertyCoder[0];

	public void registerPropertyCoder(IPropertyCoder coder){
		propertyCoderSet.add(coder);
		propertyCoders = propertyCoderSet.toArray(new IPropertyCoder[propertyCoderSet.size()]);
	}

	public void unregisterPropertyCoder(IPropertyCoder coder){
		propertyCoderSet.remove(coder);
		propertyCoders = propertyCoderSet.toArray(new IPropertyCoder[propertyCoderSet.size()]);
	}

	public boolean encode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef, Object propertyValue){
		for(IPropertyCoder pc: propertyCoders){
			if(pc.encode(this, AS, xArchRef, propertyValueRef, propertyValue)){
				return true;
			}
		}
		return false;
	}

	public Object decode(IPropertyCoder masterCoder, ArchipelagoServices AS, ObjRef xArchRef, ObjRef propertyValueRef) throws PropertyDecodeException{
		for(IPropertyCoder pc: propertyCoders){
			Object o = pc.decode(this, AS, xArchRef, propertyValueRef);
			if(o != null){
				return o;
			}
		}
		return null;
	}

	public static HintSupport getInstance(){
		if(__instance == null){
			__instance = new HintSupport();
			__instance.registerPropertyCoder(new BasicPropertyCoder());
			__instance.registerPropertyCoder(new BasicSWTPropertyCoder());
			__instance.registerPropertyCoder(new ArrayPropertyCoder());
			__instance.registerPropertyCoder(new EnumPropertyCoder());
		}
		return __instance;
	}

	public static ObjRef getHintsRootRef(ArchipelagoServices AS, ObjRef xArchRef){
		ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
		ObjRef rootRef = AS.xarch.getElement(hintsContextRef, "renderingHints3", xArchRef);
		if(rootRef != null){
			return rootRef;
		}
		rootRef = AS.xarch.createElement(hintsContextRef, "renderingHints3");
		AS.xarch.add(xArchRef, "Object", rootRef);
		return rootRef;
	}

	public static ObjRef getArchipelagoHintsBundleRef(ArchipelagoServices AS, ObjRef xArchRef){
		ObjRef hintsRootRef = getHintsRootRef(AS, xArchRef);
		ObjRef[] bundleRefs = AS.xarch.getAll(hintsRootRef, "hintedElement");
		for(ObjRef bundleRef: bundleRefs){
			if(AS.xarch.isInstanceOf(bundleRef, "hints3#HintBundle")){
				String maintainer = (String)AS.xarch.get(bundleRef, "maintainer");
				String version = (String)AS.xarch.get(bundleRef, "version");

				if(maintainer != null && version != null){
					if(maintainer.equals("edu.uci.isr.archstudio4.comp.archipelago") && version.equals("4.0.0")){
						return bundleRef;
					}
				}
			}
		}
		ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
		ObjRef bundleRef = AS.xarch.create(hintsContextRef, "hintBundle");
		AS.xarch.set(bundleRef, "maintainer", "edu.uci.isr.archstudio4.comp.archipelago");
		AS.xarch.set(bundleRef, "version", "4.0.0");

		AS.xarch.add(hintsRootRef, "hintedElement", bundleRef);
		return bundleRef;
	}

	/*
	 * public static ObjRef findHintedElementRef(ArchipelagoServices AS, ObjRef
	 * xArchRef, ObjRef bundleRef, String targetID){ ObjRef[] topLevelRefs =
	 * AS.xarch.getAll(bundleRef, "hintedElement"); String targetHref = "#" +
	 * targetID; for(int i = 0; i < topLevelRefs.length; i++){ ObjRef targetRef =
	 * (ObjRef)AS.xarch.get(topLevelRefs[i], "target"); if(targetRef != null){
	 * String href = XadlUtils.getHref(AS.xarch, targetRef); if((href != null) &&
	 * (href.equals(targetHref))){ return topLevelRefs[i]; } } } return null; }
	 */

	public static ObjRef findChildHintedElementRef(ArchipelagoServices AS, ObjRef xArchRef, ObjRef rootRef, ObjRef targetRef){
		ObjRef[] subRefs = AS.xarch.getAll(rootRef, "hintedElement");
		for(ObjRef subRef: subRefs){
			ObjRef tRef = XadlUtils.resolveXLink(AS.xarch, subRef, "target");
			if(equals(AS, targetRef, tRef)){
				return subRef;
			}
		}
		return null;
	}

	public static ObjRef findHintedElementRef(ArchipelagoServices AS, ObjRef xArchRef, ObjRef rootRef, ObjRef targetRef){
		ObjRef tRef = XadlUtils.resolveXLink(AS.xarch, rootRef, "target");
		if(equals(AS, targetRef, tRef)){
			return rootRef;
		}

		ObjRef[] subRefs = AS.xarch.getAll(rootRef, "hintedElement");
		for(ObjRef subRef: subRefs){
			ObjRef matchRef = findHintedElementRef(AS, xArchRef, subRef, targetRef);
			if(matchRef != null){
				return matchRef;
			}
		}
		return null;
	}

	private static boolean equals(ArchipelagoServices AS, ObjRef ref1, ObjRef ref2){
		if(ref1 == ref2){
			return true;
		}
		if(ref1.equals(ref2)){
			return true;
		}

		try{
			String id1 = XadlUtils.getID(AS.xarch, ref1);
			String id2 = XadlUtils.getID(AS.xarch, ref2);

			if(id1 != null && id2 != null && id1.equals(id2)){
				return true;
			}
		}
		catch(Exception e){
		}
		return false;
	}

}
