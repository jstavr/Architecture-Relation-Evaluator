package edu.uci.isr.archstudio4.comp.aimeclipse;

import edu.uci.isr.archstudio4.comp.aim.AIMMyxComponent;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxLifecycleAdapter;

/**
 * Abstract base class of "AIMEclipseMyxComponent" brick.
 * <p>
 * Following methods are called automatically by the Myx framework. Clients can
 * override them as necessary.
 * <ul>
 * <li>init(): this brick is created</li>
 * <li>begin(): this brick is attached to others via links.</li>
 * <li>end(): this brick is detached.</li>
 * <li>destroy(): this brick is destroyed.</li>
 * </ul>
 * </p>
 * The brick interface service object(s):
 * <table border="1">
 * <tr>
 * <th>JavaInterface</th>
 * <th>service object</th>
 * <th>brick interface name</th>
 * <th>direction</th>
 * <th>connection timing</th>
 * <th>template type</th>
 * </tr>
 * <tr>
 * <td>XArchFlatInterface</td>
 * <td><code>xarch</code></td>
 * <td><code>xarch</code></td>
 * <td>outSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegate</td>
 * </tr>
 * <tr>
 * <td>IAIM</td>
 * <td><code>aim</code></td>
 * <td><code>aim</code></td>
 * <td>outSingleServiceObject</td>
 * <td>beforeBegin</td>
 * <td>delegate</td>
 * </tr>
 * </table>
 * <p>
 * In order to prevent the myx code generator from overwriting the content of
 * method, remove or change "@generated" annotation of java doc comment before
 * re-run the generation.
 * </p>
 * 
 * @generated
 */
public abstract class AbstractAIMEclipseMyxComponent extends AIMMyxComponent {

	/**
	 * @generated
	 */
	class PreMyxLifecycleProcessor extends MyxLifecycleAdapter {

		/**
		 * @generated
		 */
		@Override
		public void init() {
			// connectBeforeInit
		}

		/**
		 * @generated
		 */
		@Override
		public void begin() {
			// connectBeforeBegin 
		}
	}

	/**
	 * @generated
	 */
	public AbstractAIMEclipseMyxComponent() {

		super();

		addPreMyxLifecycleProcessor(new PreMyxLifecycleProcessor());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.myx2.fw.IMyxDynamicBrick#interfaceConnected(edu.uci.isr.myx2
	 * .fw.IMyxName, java.lang.Object)
	 */
	/**
	 * @generated
	 */
	public synchronized void interfaceConnected(IMyxName interfaceName, Object serviceObject) {

		super.interfaceConnected(interfaceName, serviceObject);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.myx.fw.IMyxDynamicBrick#interfaceDisconnecting(edu.uci.isr
	 * .myx.fw.IMyxName, java.lang.Object)
	 */
	/**
	 * @generated
	 */
	public synchronized void interfaceDisconnecting(IMyxName interfaceName, Object serviceObject) {
		super.interfaceDisconnecting(interfaceName, serviceObject);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.myx.fw.IMyxDynamicBrick#interfaceDisconnected(edu.uci.isr
	 * .myx.fw.IMyxName, java.lang.Object)
	 */
	/**
	 * @generated
	 */
	public void interfaceDisconnected(IMyxName interfaceName, Object serviceObject) {
		super.interfaceDisconnected(interfaceName, serviceObject);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci
	 * .isr.myx.fw.IMyxName)
	 */
	/**
	 * @generated
	 */
	public Object getServiceObject(IMyxName interfaceName) {

		return super.getServiceObject(interfaceName);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#begin()
	 */
	/**
	 * @generated
	 */
	public void begin() {
		super.begin();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#destroy()
	 */
	/**
	 * @generated
	 */
	public void destroy() {
		super.destroy();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#end()
	 */
	/**
	 * @generated
	 */
	public void end() {
		super.end();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#init()
	 */
	/**
	 * @generated
	 */
	public void init() {
		super.init();
	}

}
