package edu.uci.isr.archstudio4.comp.aimeclipse;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.uci.isr.archstudio4.comp.aim.ArchitectureInstantiationException;
import edu.uci.isr.archstudio4.comp.aim.IAIM;
import edu.uci.isr.myx.fw.EMyxInterfaceDirection;
import edu.uci.isr.myx.fw.IMyxBrickDescription;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.IMyxRuntime;
import edu.uci.isr.myx.fw.IMyxWeld;
import edu.uci.isr.myx.fw.MyxBrickCreationException;
import edu.uci.isr.myx.fw.MyxBrickLoadException;
import edu.uci.isr.myx.fw.MyxJavaClassBrickDescription;
import edu.uci.isr.myx.fw.MyxJavaClassInterfaceDescription;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.myx2.eclipse.MyxPluginJavaClassBrickDescription;
import edu.uci.isr.myx2.eclipse.extension.ConnectionTiming;
import edu.uci.isr.myx2.eclipse.extension.IInterface;
import edu.uci.isr.myx2.eclipse.extension.IMyxBrickExtension;
import edu.uci.isr.myx2.eclipse.extension.MyxBrickExtensionFactory;
import edu.uci.isr.myx2.eclipse.extension.MyxBrickExtensionUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class AIMEclipseImpl implements IAIM {

	protected XArchFlatInterface xarch = null;

	protected IMyxBrickDescription containerBrickDescription;

	public AIMEclipseImpl(XArchFlatInterface xarch) {
		this.xarch = xarch;
		containerBrickDescription = MyxUtils.getContainerBrickDescription();
	}

	public synchronized void setXArch(XArchFlatInterface xarch) {
		this.xarch = xarch;
	}

	public synchronized void instantiate(IMyxRuntime myx, String name, ObjRef xArchRef, ObjRef structureRef) throws ArchitectureInstantiationException {
		IMyxName containerName = MyxUtils.createName(name);

		try {

			myx.addBrick(new IMyxName[0], containerName, containerBrickDescription);
			instantiate(myx, xArchRef, structureRef, new IMyxName[] { containerName });
			begin(myx, structureRef, new IMyxName[] { containerName });
			//			myx.begin(new IMyxName[0], containerName);
		}
		catch (MyxBrickLoadException mble) {
			throw new ArchitectureInstantiationException("Myx cannot load brick", mble);
		}
		catch (MyxBrickCreationException mbce) {
			throw new ArchitectureInstantiationException("Myx cannot create brick", mbce);
		}
	}


	public synchronized void destroy(IMyxRuntime myx, String name) {
		IMyxName containerName = MyxUtils.createName(name);
		try {
			myx.end(new IMyxName[0], containerName);
		}
		finally {
			try {
				myx.destroy(new IMyxName[0], containerName);
			}
			finally {
				myx.removeBrick(new IMyxName[0], containerName);
			}
		}
	}

	private static class AIMInterfaceData {

		public EMyxInterfaceDirection myxDirection;
		public IMyxName[] containerPath;
		public IMyxName brickName;
		public IMyxName interfaceName;
		public String[] serviceObjectInterfaceNames;

		/* Following are not used for non-mapped interfaces */

		public IMyxName internalBrickName;
		public IMyxName internalBrickInterfaceName;
	}

	private class InitializationOrderInfo {

		public ObjRef brickRef;
		public Collection<ObjRef> initLinkRefs = new HashSet<ObjRef>();
		public Collection<ObjRef> beginLinkRefs = new HashSet<ObjRef>();
		public Collection<ObjRef> laterLinkRefs = new HashSet<ObjRef>();

		// these are only valid while sorting topologically
		public int overallOrder = 0;
		public Set<ObjRef> dependents = new HashSet<ObjRef>();
		public Set<ObjRef> dependencies = new HashSet<ObjRef>();

		public InitializationOrderInfo(ObjRef brickRef) {
			this.brickRef = brickRef;
		}

		private List<String> getDescriptions(Collection<ObjRef> objRefs) {
			List<String> s = new ArrayList<String>();
			for (ObjRef objRef : objRefs) {
				s.add(XadlUtils.getDescription(xarch, objRef));
			}
			return s;
		}

		@Override
		public String toString() {
			String EOL = System.getProperty("line.separator");
			StringBuffer sb = new StringBuffer();
			sb.append(XadlUtils.getDescription(xarch, brickRef)).append(" <- ").append(getDescriptions(dependents)).append(EOL);
			sb.append(" - init:  ").append(getDescriptions(initLinkRefs)).append(EOL);
			sb.append(" - begin: ").append(getDescriptions(beginLinkRefs)).append(EOL);
			sb.append(" - later: ").append(getDescriptions(laterLinkRefs)).append(EOL);
			return sb.toString();
		}
	}

	private List<InitializationOrderInfo> getBrickInstantiationOrder(ObjRef structureRef) throws ArchitectureInstantiationException {

		Set<ObjRef> brickRefs = new HashSet<ObjRef>();
		brickRefs.addAll(Arrays.asList(xarch.getAll(structureRef, "component")));
		brickRefs.addAll(Arrays.asList(xarch.getAll(structureRef, "connector")));

		Map<ObjRef, InitializationOrderInfo> dependencyInfos = new HashMap<ObjRef, InitializationOrderInfo>();
		for (ObjRef objRef : brickRefs) {
			dependencyInfos.put(objRef, new InitializationOrderInfo(objRef));
		}

		List<ObjRef> unknownLinks = new ArrayList<ObjRef>();
		for (ObjRef linkRef : xarch.getAll(structureRef, "link")) {
			ObjRef[] pointRefs = xarch.getAll(linkRef, "point");
			if (pointRefs.length != 2) {
				throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef) + " must have exactly two points.");
			}

			ObjRef[] pBrickRefs = new ObjRef[2];
			EMyxInterfaceDirection[] pDirections = new EMyxInterfaceDirection[2];
			//String[] pServiceTypes = new String[2];
			ConnectionTiming[] pConnTimings = new ConnectionTiming[2];
			for (int i = 0; i < pointRefs.length; i++) {
				ObjRef interfaceRef = XadlUtils.resolveXLink(xarch, pointRefs[i], "anchorOnInterface");
				if (interfaceRef == null) {
					throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef) + " has an invalid endpoint link.");
				}
				ObjRef directionRef = (ObjRef) xarch.get(interfaceRef, "direction");
				if (directionRef == null) {
					throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, interfaceRef) + " has an invalid direction.");
				}
				String direction = (String) xarch.get(directionRef, "value");
				if (direction.equals("in")) {
					pDirections[i] = EMyxInterfaceDirection.IN;
				}
				else if (direction.equals("out")) {
					pDirections[i] = EMyxInterfaceDirection.OUT;
				}
				else {
					throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, interfaceRef)
					        + " has an invalid (non in/out) direction.");
				}

				pBrickRefs[i] = xarch.getParent(interfaceRef);
				if (!brickRefs.contains(pBrickRefs[i])) {
					throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef)
					        + " points to a brick that is not in structure " + XadlUtils.getDescription(xarch, structureRef) + ".");
				}

				ObjRef brickRef = xarch.getParent(interfaceRef);

				// get the brick type information from the eclipse extension point, 
				// if it is using that mechanism
				IMyxBrickExtension extBrick = getExtensionBrick(xarch, brickRef);
				if (extBrick != null) {
					// XXX: If the user uses eclipse extension point mechanism,
					// only the component and interface are defined in xadl,
					// and component type (Brick) and interface type (BrickSignature) are 
					// defined in the eclipse extension point.
					String intfUrl = AIMEclipseImpl.getHref(xarch, interfaceRef);
					if (intfUrl != null) {
						Collection<IInterface> extIntfs = new HashSet<IInterface>();
						extIntfs.addAll(extBrick.getInterfaces());
						extIntfs.addAll(extBrick.getAncestorsExtensionInterfaces());
						for (IInterface extIntf : extIntfs) {
							// equal to the name of myx interface name
							String extIntfUrl = extIntf.getExtensionPointUrl().toString();
							if (extIntfUrl.equals(intfUrl)) {
								//pServiceTypes[i] = brickSignature.getConnectionTiming().toServiceType();
								pConnTimings[i] = extIntf.getConnectionTiming();
							}
						}
					}
				}
				else {
					// Conventional xadl
					ObjRef signatureType = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
					if (signatureType == null) {
						throw new ArchitectureInstantiationException("Missing signature on interface " + XadlUtils.getDescription(xarch, pointRefs[i]));
					}
					ObjRef signatureServiceType = (ObjRef) xarch.get(signatureType, "serviceType");
					if (signatureServiceType != null) {
						//pServiceTypes[i] = (String) xarch.get(signatureServiceType, "value");
						pConnTimings[i] = ConnectionTiming.fromServiceType((String) xarch.get(signatureServiceType, "value"));
					}
				}
			}

			//			if (pServiceTypes[0] != null && pServiceTypes[0].equals(pServiceTypes[1])) {
			//				throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef)
			//				        + " must have compatible signature service types.");
			//			}

			for (int current = 0; current < 2; current++) {
				// when current = 0, peer = 1.
				// when current = 1, peer = 0.
				int peer = 1 - current;

				InitializationOrderInfo initOrderInfo = dependencyInfos.get(pBrickRefs[current]);
				InitializationOrderInfo peerInitOrderInfo = dependencyInfos.get(pBrickRefs[peer]);

				//if ("requires".equals(pServiceTypes[current])) {
				if (ConnectionTiming.CONNECT_BEFORE_INIT == pConnTimings[current]) {
					initOrderInfo.dependencies.add(pBrickRefs[peer]);
					if (pDirections[current] == EMyxInterfaceDirection.OUT) {
						initOrderInfo.initLinkRefs.add(linkRef);
					}
					else {
						initOrderInfo.beginLinkRefs.add(linkRef);
					}
					peerInitOrderInfo.dependents.add(pBrickRefs[current]);
					peerInitOrderInfo.laterLinkRefs.add(linkRef);
					break;
				}
				//else if ("provides".equals(pServiceTypes[current])) {
				else if (ConnectionTiming.CONNECT_BEFORE_BEGIN == pConnTimings[current]) {
					initOrderInfo.dependents.add(pBrickRefs[peer]);
					initOrderInfo.laterLinkRefs.add(linkRef);
					peerInitOrderInfo.dependencies.add(pBrickRefs[current]);
					if (pDirections[peer] == EMyxInterfaceDirection.OUT) {
						peerInitOrderInfo.initLinkRefs.add(linkRef);
					}
					else {
						peerInitOrderInfo.beginLinkRefs.add(linkRef);
					}
					break;
				}

				if (current == 1) {
					unknownLinks.add(linkRef);
				}
			}
		}

		List<InitializationOrderInfo> sortedObjRefs = new ArrayList<InitializationOrderInfo>();
		{
			// q = independent nodes
			// n = independent node
			// m = a node that n depends on
			Collection<ObjRef> q = new HashSet<ObjRef>();
			for (InitializationOrderInfo di : dependencyInfos.values()) {
				if (di.dependencies.size() == 0) {
					q.add(di.brickRef);
				}
			}
			while (q.size() > 0) {
				ObjRef n = q.iterator().next();
				InitializationOrderInfo nDI = dependencyInfos.remove(n);
				//System.err.println(nDI);
				q.remove(n);
				nDI.overallOrder = sortedObjRefs.size();
				sortedObjRefs.add(nDI);
				for (ObjRef m : nDI.dependents) {
					InitializationOrderInfo mDI = dependencyInfos.get(m);
					mDI.dependencies.remove(n);
					if (mDI.dependencies.size() == 0) {
						q.add(m);
					}
				}
			}
			for (InitializationOrderInfo di : dependencyInfos.values()) {
				if (di.dependents.size() > 0 || di.dependencies.size() > 0) {
					//System.err.println("-----");
					List<String> cycle = new ArrayList<String>();
					do {
						//System.err.println(di);
						cycle.add(XadlUtils.getDescription(xarch, di.brickRef));
						if (di.dependents.size() > 0) {
							di = dependencyInfos.get(di.dependents.iterator().next());
						}
						else {
							di = null;
						}
					} while (di != null);

					throw new ArchitectureInstantiationException("Structure " + XadlUtils.getDescription(xarch, structureRef) + " contains a dependency cycle "
					        + cycle + ".");
				}
			}
		}

		dependencyInfos.clear();
		for (InitializationOrderInfo di : sortedObjRefs) {
			dependencyInfos.put(di.brickRef, di);
		}

		for (ObjRef linkRef : unknownLinks) {
			ObjRef[] pointRefs = xarch.getAll(linkRef, "point");
			ObjRef[] pBrickRefs = new ObjRef[2];
			InitializationOrderInfo[] pDIs = new InitializationOrderInfo[2];
			for (int i = 0; i < pointRefs.length; i++) {
				ObjRef interfaceRef = XadlUtils.resolveXLink(xarch, pointRefs[i], "anchorOnInterface");
				pBrickRefs[i] = xarch.getParent(interfaceRef);
				pDIs[i] = dependencyInfos.get(pBrickRefs[i]);
			}
			if (pDIs[0].overallOrder < pDIs[1].overallOrder) {
				pDIs[1].beginLinkRefs.add(linkRef);
				pDIs[0].laterLinkRefs.add(linkRef);
			}
			else {
				pDIs[0].beginLinkRefs.add(linkRef);
				pDIs[1].laterLinkRefs.add(linkRef);
			}
		}

		//		for(InitializationOrderInfo di: sortedObjRefs){
		//			System.err.println(di);
		//		}

		return sortedObjRefs;
	}

	/**
	 * Reads the brick information from the eclipse extension point
	 * 
	 * @param xarch
	 * @param brickRef
	 * @return brick, or null if the corresponding data is not found.
	 */
	private IMyxBrickExtension getExtensionBrick(XArchFlatInterface xarch, ObjRef brickRef) {

		//if (extUrlToExtBrickMap == null) {
		//	return null;
		//}
		// gets the type href of brickRef
		ObjRef brickTypeEltRef = (ObjRef) xarch.get(brickRef, "type");
		String brickTypeUrl = null;
		if (brickTypeEltRef != null) {
			brickTypeUrl = XadlUtils.getHref(xarch, brickTypeEltRef);
		}
		if (brickTypeUrl == null) {
			return null;
		}

		try {
			return MyxBrickExtensionFactory.parseBrickConfigurationElement(new URL(brickTypeUrl));
		}
		catch (Exception e) {
		}
		return null;
	}


	private void instantiate(IMyxRuntime myx, ObjRef xArchRef, ObjRef structureRef, IMyxName[] containerPath) throws ArchitectureInstantiationException {
		//System.err.println("initing");
		for (InitializationOrderInfo ioi : getBrickInstantiationOrder(structureRef)) {
			//System.err.println(ioi);
			ObjRef brickRef = ioi.brickRef;
			String brickID = XadlUtils.getID(xarch, brickRef);
			if (brickID == null) {
				throw new ArchitectureInstantiationException("Brick missing ID: " + brickRef);
			}

			//get brick type (ComponentType/ConnectorType) of the brick
			ObjRef brickTypeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");

			// get the extension brick information from the eclipse extension point,
			// if it is using that mechanism
			IMyxBrickExtension extBrick = getExtensionBrick(xarch, brickRef);

			// if the brick type is not defined and the extension brick is defined,
			// use the extension brick
			if (brickTypeRef == null && extBrick != null) {
				// only the component and its interfaces are defined in the xadl,

				//TODO: how to get the instantiation class

				//looks for defaultImplClass first
				String javaClassName = extBrick.getFQDefaultImplClassName();
				if (javaClassName == null) {
					//looks for base class
					javaClassName = extBrick.getFQBaseClassName();
				}
				if (javaClassName == null) {
					throw new ArchitectureInstantiationException("Brick missing instantiation java class: " + brickRef);
				}

				// instantiate the brick
				IMyxBrickDescription myxBrickDescription = new MyxPluginJavaClassBrickDescription(new Properties(), javaClassName, extBrick.getSymbolicName());
				IMyxName myxBrickName = MyxUtils.createName(brickID);
				try {
					myx.addBrick(containerPath, myxBrickName, myxBrickDescription);
				}
				catch (MyxBrickLoadException mble) {
					throw new ArchitectureInstantiationException("Myx cannot load brick", mble);
				}
				catch (MyxBrickCreationException mbce) {
					throw new ArchitectureInstantiationException("Myx cannot create brick", mbce);
				}

				//create the brick interfaces
				Collection<IInterface> extIntfs = new HashSet<IInterface>();
				extIntfs.addAll(extBrick.getInterfaces());
				extIntfs.addAll(extBrick.getAncestorsExtensionInterfaces());
				for (IInterface extIntf : extIntfs) {
					AIMInterfaceData aid = new AIMInterfaceData();
					aid.myxDirection = extIntf.getDirection().toEMyxInterfaceDirection();
					aid.containerPath = containerPath;
					aid.brickName = MyxUtils.createName(brickID);
					aid.interfaceName = MyxUtils.createName(extIntf.getId());
					aid.serviceObjectInterfaceNames = new String[] { extIntf.getFQJavaInterfaceName() };

					MyxJavaClassInterfaceDescription aidDesc = new MyxJavaClassInterfaceDescription(aid.serviceObjectInterfaceNames);
					myx.addInterface(aid.containerPath, aid.brickName, aid.interfaceName, aidDesc, aid.myxDirection);
				}

				// weld beforeInit interfaces, then call init(), then weld beforeBegin/none interfaces
				for (ObjRef linkRef : ioi.initLinkRefs) {
					weldLink(myx, xArchRef, structureRef, containerPath, linkRef);
				}
				myx.init(containerPath, myxBrickName);
				for (ObjRef linkRef : ioi.beginLinkRefs) {
					weldLink(myx, xArchRef, structureRef, containerPath, linkRef);
				}

				continue;
			}

			//Reads classes from xadl
			if (brickTypeRef == null) {
				throw new ArchitectureInstantiationException("Brick type link missing or invalid: " + brickID);
			}

			ObjRef subArchitectureRef = (ObjRef) xarch.get(brickTypeRef, "subArchitecture");
			if (subArchitectureRef != null) {
				//Process subarchitecture
				//Create the container:
				IMyxName containerName = MyxUtils.createName(brickID);
				try {
					myx.addBrick(containerPath, containerName, containerBrickDescription);
				}
				catch (MyxBrickLoadException mble) {
					throw new ArchitectureInstantiationException("Myx cannot load brick", mble);
				}
				catch (MyxBrickCreationException mbce) {
					throw new ArchitectureInstantiationException("Myx cannot create brick", mbce);
				}

				//Instantiate the substructure into that container.
				ObjRef innerStructureRef = XadlUtils.resolveXLink(xarch, subArchitectureRef, "archStructure");
				if (innerStructureRef == null) {
					throw new ArchitectureInstantiationException("Brick type " + XadlUtils.getDescription(xarch, brickTypeRef)
					        + " has invalid subarchitecture structure link.");
				}
				//This is easy enough; we just recurse.
				IMyxName[] innerContainerPath = new IMyxName[containerPath.length + 1];
				for (int yj = 0; yj < containerPath.length; yj++) {
					innerContainerPath[yj] = containerPath[yj];
				}
				innerContainerPath[containerPath.length] = containerName;
				instantiate(myx, xArchRef, innerStructureRef, innerContainerPath);

				//Okay, the container is created and added; its inner structure is all
				//set up, now we have to go about creating and mapping all its interfaces.
				ObjRef[] interfaceRefs = xarch.getAll(brickRef, "interface");
				for (ObjRef element : interfaceRefs) {
					//AIMInterfaceData aid = parseAndValidateMappedInterfaceData(containerPath, brickRef, element, brickTypeRef);
					AIMInterfaceData aid = parseAndValidateInterfaceData(containerPath, brickRef, element);
					ObjRef saRef = (ObjRef) xarch.get(brickTypeRef, "subArchitecture");
					if (saRef == null) {
						throw new ArchitectureInstantiationException("Can't find subarchitecture for brick " + XadlUtils.getDescription(xarch, brickRef));
					}
					ObjRef interfacesSignatureRef = XadlUtils.resolveXLink(xarch, element, "signature");
					if (interfacesSignatureRef == null) {
						throw new ArchitectureInstantiationException("Invalid or missing signature link on interface "
						        + XadlUtils.getDescription(xarch, element));
					}
					ObjRef[] simRefs = xarch.getAll(subArchitectureRef, "signatureInterfaceMapping");
					for (ObjRef e : simRefs) {
						if (mapSigToIntf(e, aid, interfacesSignatureRef, element) == null) {
							continue;
						}
						MyxJavaClassInterfaceDescription aidDesc = new MyxJavaClassInterfaceDescription(aid.serviceObjectInterfaceNames);
						myx.addContainerInterface(aid.containerPath, aid.brickName, aid.interfaceName, aidDesc, aid.myxDirection, aid.internalBrickName,
						        aid.internalBrickInterfaceName);
					}
					//MyxJavaClassInterfaceDescription aidDesc = new MyxJavaClassInterfaceDescription(aid.serviceObjectInterfaceNames);
					//myx.addContainerInterface(aid.containerPath, aid.brickName, aid.interfaceName, aidDesc, aid.myxDirection, aid.internalBrickName, aid.internalBrickInterfaceName);
				}
				//added by yongjie
				for (ObjRef linkRef : ioi.beginLinkRefs) {
					weldLink(myx, xArchRef, structureRef, containerPath, linkRef);
				}
			}
			else {
				//Process atomic type
				ObjRef[] implementationRefs = xarch.getAll(brickTypeRef, "implementation");
				if (implementationRefs == null || implementationRefs.length == 0) {
					throw new ArchitectureInstantiationException("Brick type " + XadlUtils.getDescription(xarch, brickTypeRef)
					        + " must have either a subarchitecture or an implementation.");
				}
				ObjRef javaImplementationRef = null;
				for (ObjRef element : implementationRefs) {
					if (xarch.isInstanceOf(element, "javaimplementation#JavaImplementation")) {
						javaImplementationRef = element;
						break;
					}
				}
				if (javaImplementationRef == null) {
					throw new ArchitectureInstantiationException("Could not find Java implementation on brick type: "
					        + XadlUtils.getDescription(xarch, brickTypeRef));
				}
				ObjRef mainClassRef = (ObjRef) xarch.get(javaImplementationRef, "mainClass");
				if (mainClassRef == null) {
					throw new ArchitectureInstantiationException("Java implementation lacks main class on brick type: "
					        + XadlUtils.getDescription(xarch, brickTypeRef));
				}
				ObjRef mainClassNameRef = (ObjRef) xarch.get(mainClassRef, "javaClassName");
				if (mainClassNameRef == null) {
					throw new ArchitectureInstantiationException("Java implementation lacks main class name on brick type: "
					        + XadlUtils.getDescription(xarch, brickTypeRef));
				}
				String mainClassName = (String) xarch.get(mainClassNameRef, "value");
				if (mainClassName == null) {
					throw new ArchitectureInstantiationException("Java implementation lacks main class name on brick type: "
					        + XadlUtils.getDescription(xarch, brickTypeRef));
				}
				//We have the main class name; let's get the properties (if any)
				Properties initializationParameters = getProperties(mainClassRef);

				IMyxBrickDescription myxBrickDescription = new MyxJavaClassBrickDescription(initializationParameters, mainClassName);
				IMyxName myxBrickName = MyxUtils.createName(brickID);
				try {
					myx.addBrick(containerPath, myxBrickName, myxBrickDescription);
				}
				catch (MyxBrickLoadException mble) {
					throw new ArchitectureInstantiationException("Myx cannot load brick", mble);
				}
				catch (MyxBrickCreationException mbce) {
					throw new ArchitectureInstantiationException("Myx cannot create brick", mbce);
				}

				//Okay, the brick is created and added; now we have to go about
				//creating all its interfaces.
				ObjRef[] interfaceRefs = xarch.getAll(brickRef, "interface");
				for (ObjRef element : interfaceRefs) {
					AIMInterfaceData aid = parseAndValidateInterfaceData(containerPath, brickRef, element);
					MyxJavaClassInterfaceDescription aidDesc = new MyxJavaClassInterfaceDescription(aid.serviceObjectInterfaceNames);
					myx.addInterface(aid.containerPath, aid.brickName, aid.interfaceName, aidDesc, aid.myxDirection);
				}
				//System.err.println("initing " + myxBrickName);
				for (ObjRef linkRef : ioi.initLinkRefs) {
					weldLink(myx, xArchRef, structureRef, containerPath, linkRef);
				}
				System.err.println("Initing: "+myxBrickName);
				myx.init(containerPath, myxBrickName);
				System.err.println("Done:    "+myxBrickName);
				for (ObjRef linkRef : ioi.beginLinkRefs) {
					weldLink(myx, xArchRef, structureRef, containerPath, linkRef);
				}
			}
		}
		//System.err.println("beginning");
		//System.err.println("done");
	}

	private void begin(IMyxRuntime myx, ObjRef structureRef, IMyxName[] containerPath) throws ArchitectureInstantiationException {
		for (InitializationOrderInfo ioi : getBrickInstantiationOrder(structureRef)) {
			ObjRef brickRef = ioi.brickRef;
			//System.err.println(XadlUtils.getDescription(xarch, ioi.brickRef));
			String brickID = XadlUtils.getID(xarch, brickRef);
			IMyxName myxBrickName = MyxUtils.createName(brickID);
			myx.begin(containerPath, myxBrickName);
		}
	}

	/**
	 * Returns the href value of the given object
	 * 
	 * @param xarch
	 * @param objRef
	 * @return
	 */
	private static String getHref(XArchFlatInterface xarch, ObjRef objRef) {

		if (xarch.isInstanceOf(objRef, "types#Component") || xarch.isInstanceOf(objRef, "types#Connector") || xarch.isInstanceOf(objRef, "types#interface")) {
			ObjRef typeRef = (ObjRef) xarch.get(objRef, "type");
			if (typeRef != null) {
				return XadlUtils.getHref(xarch, typeRef);
			}
		}

		return null;
	}

	private static String getSigImplNameForExtBrickInterface(XArchFlatInterface xarch, ObjRef interfaceRef) {
		return MyxBrickExtensionUtils.getIdFromExtensionURI(MyxBrickExtensionUtils.toUrl(AIMEclipseImpl.getHref(xarch, interfaceRef)));
	}

	private void weldLink(IMyxRuntime myx, ObjRef xArchRef, ObjRef structureRef, IMyxName[] containerPath, ObjRef linkRef)
	        throws ArchitectureInstantiationException {
		//Process the links
		ObjRef[] pointRefs = xarch.getAll(linkRef, "point");
		if (pointRefs.length != 2) {
			throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef) + " must have exactly two points.");
		}

		ObjRef providedInterfaceRef = null;
		ObjRef requiredInterfaceRef = null;
		for (int p = 0; p < pointRefs.length; p++) {
			ObjRef interfaceRef = XadlUtils.resolveXLink(xarch, pointRefs[p], "anchorOnInterface");
			if (interfaceRef == null) {
				throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef) + " has an invalid endpoint (" + p + ") link.");
			}
			String direction = XadlUtils.getDirection(xarch, interfaceRef);
			if (direction == null) {
				throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, interfaceRef) + " has an invalid direction.");
			}
			else if (direction.equals("in")) {
				if (providedInterfaceRef != null) {
					throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef) + " connects two provided interfaces.");
				}
				providedInterfaceRef = interfaceRef;
			}
			else if (direction.equals("out")) {
				if (requiredInterfaceRef != null) {
					throw new ArchitectureInstantiationException("Link " + XadlUtils.getDescription(xarch, linkRef) + " connects two required interfaces.");
				}
				requiredInterfaceRef = interfaceRef;
			}
			else {
				throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, interfaceRef)
				        + " has an invalid (non in/out) direction.");
			}
		}
		//Okay, we have both the provided and required interface refs.
		ObjRef providedBrickRef = xarch.getParent(providedInterfaceRef);
		ObjRef requiredBrickRef = xarch.getParent(requiredInterfaceRef);

		String providedBrickID = XadlUtils.getID(xarch, providedBrickRef);
		if (providedBrickID == null) {
			throw new ArchitectureInstantiationException("Brick " + XadlUtils.getDescription(xarch, providedBrickRef) + " missing ID.");
		}

		String requiredBrickID = XadlUtils.getID(xarch, requiredBrickRef);
		if (requiredBrickID == null) {
			throw new ArchitectureInstantiationException("Brick " + XadlUtils.getDescription(xarch, requiredBrickRef) + " missing ID.");
		}

		String providedInterfaceID = XadlUtils.getID(xarch, providedInterfaceRef);
		if (providedInterfaceID == null) {
			throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, providedInterfaceRef) + " on brick "
			        + XadlUtils.getDescription(xarch, providedBrickRef) + " missing ID.");
		}

		String requiredInterfaceID = XadlUtils.getID(xarch, requiredInterfaceRef);
		if (requiredInterfaceID == null) {
			throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, requiredInterfaceRef) + " on brick "
			        + XadlUtils.getDescription(xarch, requiredBrickRef) + " missing ID.");
		}

		String requiredSignatureImplementationName;
		ObjRef requiredInterfaceSignatureLink = (ObjRef) xarch.get(requiredInterfaceRef, "signature");
		if (requiredInterfaceSignatureLink == null) {
			// XXX: If the user uses eclipse extension point mechanism,
			// only the component and interface are defined in xadl,
			// and component type (Brick) and interface type (BrickSignature) are 
			// defined in the eclipse extension point.
			// Uses interface id for the signature implementation name.
			requiredSignatureImplementationName = AIMEclipseImpl.getSigImplNameForExtBrickInterface(xarch, requiredInterfaceRef);
		}
		else {
			ObjRef requiredSignatureRef = XadlUtils.resolveXLink(xarch, requiredInterfaceRef, "signature");
			if (requiredSignatureRef == null) {
				throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, requiredInterfaceRef) + " on brick "
				        + XadlUtils.getDescription(xarch, requiredBrickRef) + " has a missing or invalid signature.");
			}

			requiredSignatureImplementationName = getLookupImplementationName(requiredSignatureRef);
			if (requiredSignatureImplementationName == null) {
				throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, requiredSignatureRef) + " on brick "
				        + XadlUtils.getDescription(xarch, requiredBrickRef) + " has no lookup implementation for its corresponding signature.");
			}
		}

		String providedSignatureImplementationName;
		ObjRef providedInterfaceSignatureLink = (ObjRef) xarch.get(providedInterfaceRef, "signature");
		if (providedInterfaceSignatureLink == null) {
			// XXX: If the user uses eclipse extension point mechanism,
			// only the component and interface are defined in xadl,
			// and component type (Brick) and interface type (BrickSignature) are 
			// defined in the eclipse extension point.
			// Uses interface id for the signature implementation name.
			providedSignatureImplementationName = AIMEclipseImpl.getSigImplNameForExtBrickInterface(xarch, providedInterfaceRef);
		}
		else {
			ObjRef providedSignatureRef = XadlUtils.resolveXLink(xarch, providedInterfaceRef, "signature");
			if (providedSignatureRef == null) {
				throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, providedInterfaceRef) + " on brick "
				        + XadlUtils.getDescription(xarch, providedBrickRef) + " has a missing or invalid signature.");
			}

			providedSignatureImplementationName = getLookupImplementationName(providedSignatureRef);
			if (providedSignatureImplementationName == null) {
				throw new ArchitectureInstantiationException("Interface " + XadlUtils.getDescription(xarch, providedSignatureRef) + " on brick "
				        + XadlUtils.getDescription(xarch, providedBrickRef) + " has no lookup implementation for its corresponding signature.");
			}
		}

		IMyxWeld weld = myx.createWeld(
		/* Required */
		containerPath, MyxUtils.createName(requiredBrickID), MyxUtils.createName(requiredSignatureImplementationName),
		/* Provided */
		containerPath, MyxUtils.createName(providedBrickID), MyxUtils.createName(providedSignatureImplementationName));

		try {
			myx.addWeld(weld);
		}
		catch (Exception e) {
			throw new ArchitectureInstantiationException("Problem adding weld or invalid weld: " + weld.toString(), e);
		}
	}

	private AIMInterfaceData parseAndValidateInterfaceData(IMyxName[] containerPath, ObjRef brickRef, ObjRef interfaceRef)
	        throws ArchitectureInstantiationException {
		String interfaceID = XadlUtils.getID(xarch, interfaceRef);
		if (interfaceID == null) {
			throw new ArchitectureInstantiationException("Missing ID on interface: " + XadlUtils.getDescription(xarch, interfaceRef) + " on brick "
			        + XadlUtils.getDescription(xarch, brickRef));
		}
		//We have to get the implementation lookup name for the interface,
		//which should be hiding on its type.
		ObjRef signatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
		if (signatureRef == null) {
			throw new ArchitectureInstantiationException("Missing signature for interface " + XadlUtils.getDescription(xarch, interfaceRef) + " on brick "
			        + XadlUtils.getDescription(xarch, brickRef));
		}
		String lookupImplementationName = getLookupImplementationName(signatureRef);
		if (lookupImplementationName == null) {
			throw new ArchitectureInstantiationException("Missing lookup implementation name on signature " + XadlUtils.getDescription(xarch, signatureRef));
		}
		String direction = XadlUtils.getDirection(xarch, interfaceRef);
		if (direction == null) {
			throw new ArchitectureInstantiationException("Missing direction on interface " + XadlUtils.getDescription(xarch, interfaceRef));
		}
		EMyxInterfaceDirection myxDirection = null;
		if (direction.equals("in")) {
			myxDirection = EMyxInterfaceDirection.IN;
		}
		else if (direction.equals("out")) {
			myxDirection = EMyxInterfaceDirection.OUT;
		}
		else {
			throw new ArchitectureInstantiationException("Invalid direction (not in/out) on interface " + XadlUtils.getDescription(xarch, interfaceRef));
		}
		List<String> serviceObjectInterfaceNames = new ArrayList<String>();
		ObjRef ifaceTypeRef = XadlUtils.resolveXLink(xarch, interfaceRef, "type");
		ObjRef[] implementationRefs = xarch.getAll(ifaceTypeRef, "implementation");
		for (ObjRef javaImplementationRef : implementationRefs) {
			if (xarch.isInstanceOf(javaImplementationRef, "javaimplementation#JavaImplementation")) {
				ObjRef mainClassRef = (ObjRef) xarch.get(javaImplementationRef, "mainClass");
				if (mainClassRef != null) {
					ObjRef mainClassNameRef = (ObjRef) xarch.get(mainClassRef, "javaClassName");
					if (mainClassNameRef != null) {
						String mainClassName = (String) xarch.get(mainClassNameRef, "value");
						if (mainClassName != null) {
							serviceObjectInterfaceNames.add(mainClassName);
						}
					}
				}
				for (ObjRef auxClassRef : xarch.getAll(javaImplementationRef, "auxClass")) {
					if (auxClassRef != null) {
						ObjRef auxClassNameRef = (ObjRef) xarch.get(auxClassRef, "javaClassName");
						if (auxClassNameRef != null) {
							String auxClassName = (String) xarch.get(auxClassNameRef, "value");
							if (auxClassName != null) {
								serviceObjectInterfaceNames.add(auxClassName);
							}
						}
					}
				}
			}
		}

		AIMInterfaceData aid = new AIMInterfaceData();
		aid.myxDirection = myxDirection;
		aid.containerPath = containerPath;
		aid.brickName = MyxUtils.createName(XadlUtils.getID(xarch, brickRef));
		aid.interfaceName = MyxUtils.createName(lookupImplementationName);
		aid.serviceObjectInterfaceNames = serviceObjectInterfaceNames.toArray(new String[serviceObjectInterfaceNames.size()]);

		return aid;
	}

	//	private AIMInterfaceData parseAndValidateMappedInterfaceData(IMyxName[] containerPath, ObjRef brickRef, ObjRef interfaceRef, ObjRef brickTypeRef)
	//	        throws ArchitectureInstantiationException {
	//		AIMInterfaceData aid = parseAndValidateInterfaceData(containerPath, brickRef, interfaceRef);
	//		ObjRef subArchitectureRef = (ObjRef) xarch.get(brickTypeRef, "subArchitecture");
	//		if (subArchitectureRef == null) {
	//			throw new ArchitectureInstantiationException("Can't find subarchitecture for brick " + XadlUtils.getDescription(xarch, brickRef));
	//		}
	//		ObjRef interfacesSignatureRef = XadlUtils.resolveXLink(xarch, interfaceRef, "signature");
	//		if (interfacesSignatureRef == null) {
	//			throw new ArchitectureInstantiationException("Invalid or missing signature link on interface " + XadlUtils.getDescription(xarch, interfaceRef));
	//		}
	//
	//		ObjRef[] simRefs = xarch.getAll(subArchitectureRef, "signatureInterfaceMapping");
	//		for (ObjRef element : simRefs) {
	//			ObjRef outerSignatureRef = XadlUtils.resolveXLink(xarch, element, "outerSignature");
	//			if (outerSignatureRef == null) {
	//				throw new ArchitectureInstantiationException("Invalid or missing outerSignature link on signature-interface mapping "
	//				        + XadlUtils.getDescription(xarch, element));
	//			}
	//			ObjRef innerInterfaceRef = XadlUtils.resolveXLink(xarch, element, "innerInterface");
	//			if (innerInterfaceRef == null) {
	//				throw new ArchitectureInstantiationException("Invalid or missing innerInterface link on signature-interface mapping "
	//				        + XadlUtils.getDescription(xarch, element));
	//			}
	//			//changed by yongjie: change "type" to "signature"
	//			ObjRef innerInterfaceTypeRef = XadlUtils.resolveXLink(xarch, innerInterfaceRef, "signature");
	//			if (innerInterfaceTypeRef == null) {
	//				throw new ArchitectureInstantiationException("Missing interface type on interface " + XadlUtils.getDescription(xarch, innerInterfaceRef));
	//			}
	//			String innerInterfaceLookupImplementationName = getLookupImplementationName(innerInterfaceTypeRef);
	//			if (innerInterfaceLookupImplementationName == null) {
	//				throw new ArchitectureInstantiationException("Missing lookup implementation on interface type "
	//				        + XadlUtils.getDescription(xarch, innerInterfaceTypeRef));
	//			}
	//
	//			//Is this a SIM to our target signature?
	//			if (interfacesSignatureRef.equals(outerSignatureRef) || xarch.isEqual(interfacesSignatureRef, outerSignatureRef)) {
	//				ObjRef innerBrickRef = xarch.getParent(innerInterfaceRef);
	//				if (innerBrickRef == null) {
	//					throw new ArchitectureInstantiationException("Invalid or missing parent on interface " + XadlUtils.getDescription(xarch, innerInterfaceRef));
	//				}
	//				String innerBrickID = XadlUtils.getID(xarch, innerBrickRef);
	//				if (innerBrickID == null) {
	//					throw new ArchitectureInstantiationException("Missing ID on brick " + XadlUtils.getDescription(xarch, innerBrickRef));
	//				}
	//				String innerInterfaceID = XadlUtils.getID(xarch, innerInterfaceRef);
	//				if (innerInterfaceID == null) {
	//					throw new ArchitectureInstantiationException("Missing ID on interface " + XadlUtils.getDescription(xarch, innerInterfaceRef));
	//				}
	//				aid.internalBrickName = MyxUtils.createName(innerBrickID);
	//				//aid.internalBrickInterfaceName = MyxUtils.createName(innerInterfaceID);
	//				aid.internalBrickInterfaceName = MyxUtils.createName(innerInterfaceLookupImplementationName);
	//				return aid;
	//			}
	//		}
	//		throw new ArchitectureInstantiationException("Could not find matching signature-interface mapping for interface "
	//		        + XadlUtils.getDescription(xarch, interfaceRef) + " on brick " + XadlUtils.getDescription(xarch, brickRef));
	//	}

	private AIMInterfaceData mapSigToIntf(ObjRef element, AIMInterfaceData aid, ObjRef interfacesSignatureRef, ObjRef interfaceRef)
	        throws ArchitectureInstantiationException {
		ObjRef outerSignatureRef = XadlUtils.resolveXLink(xarch, element, "outerSignature");
		if (outerSignatureRef == null) {
			throw new ArchitectureInstantiationException("Invalid or missing outerSignature link on signature-interface mapping "
			        + XadlUtils.getDescription(xarch, element));
		}
		ObjRef innerInterfaceRef = XadlUtils.resolveXLink(xarch, element, "innerInterface");
		if (innerInterfaceRef == null) {
			throw new ArchitectureInstantiationException("Invalid or missing innerInterface link on signature-interface mapping "
			        + XadlUtils.getDescription(xarch, element));
		}
		//changed by yongjie: change "type" to "signature"
		ObjRef innerInterfaceTypeRef = XadlUtils.resolveXLink(xarch, innerInterfaceRef, "signature");
		if (innerInterfaceTypeRef == null) {
			throw new ArchitectureInstantiationException("Missing interface type on interface " + XadlUtils.getDescription(xarch, innerInterfaceRef));
		}
		String innerInterfaceLookupImplementationName = getLookupImplementationName(innerInterfaceTypeRef);
		if (innerInterfaceLookupImplementationName == null) {
			throw new ArchitectureInstantiationException("Missing lookup implementation on interface type "
			        + XadlUtils.getDescription(xarch, innerInterfaceTypeRef));
		}

		//Is this a SIM to our target signature?
		if (interfacesSignatureRef.equals(outerSignatureRef) || xarch.isEqual(interfacesSignatureRef, outerSignatureRef)) {
			ObjRef innerBrickRef = xarch.getParent(innerInterfaceRef);
			if (innerBrickRef == null) {
				throw new ArchitectureInstantiationException("Invalid or missing parent on interface " + XadlUtils.getDescription(xarch, innerInterfaceRef));
			}
			String innerBrickID = XadlUtils.getID(xarch, innerBrickRef);
			if (innerBrickID == null) {
				throw new ArchitectureInstantiationException("Missing ID on brick " + XadlUtils.getDescription(xarch, innerBrickRef));
			}
			String innerInterfaceID = XadlUtils.getID(xarch, innerInterfaceRef);
			if (innerInterfaceID == null) {
				throw new ArchitectureInstantiationException("Missing ID on interface " + XadlUtils.getDescription(xarch, innerInterfaceRef));
			}
			aid.internalBrickName = MyxUtils.createName(innerBrickID);
			//aid.internalBrickInterfaceName = MyxUtils.createName(innerInterfaceID);
			aid.internalBrickInterfaceName = MyxUtils.createName(innerInterfaceLookupImplementationName);
			return aid;
		}
		else {
			return null;
		}
		//throw new ArchitectureInstantiationException("Could not find matching signature-interface mapping for interface " + XadlUtils.getDescription(xarch, interfaceRef));
	}

	private Properties getProperties(ObjRef javaClassFileRef) {
		if (xarch.isInstanceOf(javaClassFileRef, "javainitparams#JavaClassFileParams")) {
			Properties p = new Properties();
			ObjRef[] initializationParameterRefs = xarch.getAll(javaClassFileRef, "initializationParameter");
			for (ObjRef element : initializationParameterRefs) {
				String name = (String) xarch.get(element, "name");
				String value = (String) xarch.get(element, "value");
				p.put(name, value);
			}
			return p;
		}
		else {
			return null;
		}
	}

	private String getLookupImplementationName(ObjRef typeRef) {
		ObjRef[] implementationRefs = xarch.getAll(typeRef, "implementation");
		if (implementationRefs == null || implementationRefs.length == 0) {
			return null;
		}
		ObjRef lookupImplementationRef = null;
		for (ObjRef element : implementationRefs) {
			if (xarch.isInstanceOf(element, "lookupimplementation#LookupImplementation")) {
				lookupImplementationRef = element;
				break;
			}
		}
		if (lookupImplementationRef == null) {
			return null;
		}
		ObjRef lookupNameRef = (ObjRef) xarch.get(lookupImplementationRef, "name");
		if (lookupNameRef == null) {
			return null;
		}
		return (String) xarch.get(lookupNameRef, "value");
	}
}
