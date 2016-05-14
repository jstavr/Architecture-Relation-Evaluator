package edu.uci.isr.archstudio4.comp.bootstrap;

import edu.uci.isr.archstudio4.comp.bootstrap.AbstractBootstrapMyxComponent;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * Myx brick component. Following methods are called automatically. They should
 * be overrided as necessary.
 * <ul>
 * <li>init(): this brick is created</li>
 * <li>begin(): this brick is attached to others via links.</li>
 * <li>end(): this brick is detached.</li>
 * <li>destroy(): this brick is destroyed.</li>
 * </ul>
 * The brick interface service object variable(s):
 * <table border="1">
 * <tr>
 * <th>JavaInterface</th>
 * <th>service object</th>
 * <th>brick interface</th>
 * <th>direction</th>
 * <th>connection timing</th>
 * <th>template type</th>
 * </tr>
 * <tr>
 * <td>IMyxRuntime</td>
 * <td><code>myx</code></td>
 * <td><code>myx</code></td>
 * <td>outSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegate</td>
 * </tr>
 * <tr>
 * <td>IAIM</td>
 * <td><code>aim</code></td>
 * <td><code>aim</code></td>
 * <td>outSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegate</td>
 * </tr>
 * <tr>
 * <td>XArchFlatInterface</td>
 * <td><code>xarch</code></td>
 * <td><code>xarch</code></td>
 * <td>outSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegate</td>
 * </tr>
 * </table>
 * <p>
 * In order to prevent the myx code generator from overwriting the content of
 * method, remove "@generated" annotation from java doc comment before re-run
 * the generation if any change is made to the method.
 * </p>
 * 
 * @generated
 */
public class BootstrapMyxComponent extends AbstractBootstrapMyxComponent {

	public static final String ARCHITECTURE_NAME = "main";
	public static final String ARCHITECTURE_FILE = "file";
	public static final String ARCHITECTURE_CONTENTS = "contents";
	public static final String ARCHITECTURE_STRUCTURE = "structure";

	/**
	 * @generated
	 */
	public BootstrapMyxComponent() {

	}

	/**
	 * The following variable(s) will be assigned values before this method is
	 * called.
	 * <ul>
	 * <li>{@link #myx}</li>
	 * <li>{@link #aim}</li>
	 * <li>{@link #xarch}</li>
	 * </ul>
	 * 
	 * @generated
	 * @see edu.uci.isr.myx.fw.AbstractMyxSimpleBrick#init()
	 */
	@Override
	public void init() {
		super.init();
	}

	/**
	 * @see edu.uci.isr.myx.fw.AbstractMyxSimpleBrick#begin()
	 */
	@Override
	public void begin() {
		super.begin();
		String xadlFileContents = null;
		String xadlURLFileString = MyxUtils.getInitProperties(this).getProperty(BootstrapMyxComponent.ARCHITECTURE_FILE);
		if (xadlURLFileString == null) {
			xadlFileContents = MyxUtils.getInitProperties(this).getProperty(BootstrapMyxComponent.ARCHITECTURE_CONTENTS);
			if (xadlFileContents == null) {
				throw new IllegalArgumentException("File parameter missing to bootstrap component.");
			}
		}

		String structureDescription = MyxUtils.getInitProperties(this).getProperty(BootstrapMyxComponent.ARCHITECTURE_STRUCTURE);
		if (structureDescription == null) {
			structureDescription = BootstrapMyxComponent.ARCHITECTURE_NAME;
		}

		ObjRef xArchRef = null;
		try {
			if (xadlURLFileString == null) {
				xArchRef = xarch.parseFromString("urn:arch" + Math.random(), xadlFileContents);
			}
			else {
				try {
					xArchRef = xarch.parseFromURL(xadlURLFileString);
				}
				catch (Exception e) {
					xArchRef = xarch.parseFromFile(xadlURLFileString);
				}
			}

			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef[] structureRefs = xarch.getAllElements(typesContextRef, "archStructure", xArchRef);

			if (structureRefs.length == 0) {
				throw new RuntimeException("Architecture has no structures to instantiate");
			}

			ObjRef structureRef = null;
			for (ObjRef structureRef2 : structureRefs) {
				String description = XadlUtils.getDescription(xarch, structureRef2);
				if (description != null && description.equals(structureDescription)) {
					structureRef = structureRef2;
					break;
				}
			}
			if (structureRef == null) {
				structureRef = structureRefs[0];
			}

			aim.instantiate(myx, BootstrapMyxComponent.ARCHITECTURE_NAME, xArchRef, structureRef);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
