package edu.uci.isr.archstudio4.comp.bootstrap;

import java.util.ArrayList;
import java.util.List;

import edu.uci.isr.archstudio4.comp.aim.IAIM;
import edu.uci.isr.myx.fw.IMyxBrickItems;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxLifecycleProcessor;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.IMyxProvidedServiceProvider;
import edu.uci.isr.myx.fw.IMyxRuntime;
import edu.uci.isr.myx.fw.MyxLifecycleAdapter;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFlatInterface;

/**
 * Abstract base class of "BootstrapMyxComponent" brick.
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
 * method, remove or change "@generated" annotation of java doc comment before
 * re-run the generation.
 * </p>
 * 
 * @generated
 */
public abstract class AbstractBootstrapMyxComponent

implements IMyxDynamicBrick, IMyxLifecycleProcessor, IMyxProvidedServiceProvider {

	/**
	 * Myx-interface name for outSingleServiceObject myx.
	 * <p>
	 * Through this IMyxName, this brick can use services reached through myx
	 * brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_OUT_MYX = MyxUtils.createName("edu.uci.isr.archstudio4.comp.bootstrap.BootstrapMyxComponent.myx");

	/**
	 * Myx-interface name for outSingleServiceObject aim.
	 * <p>
	 * Through this IMyxName, this brick can use services reached through aim
	 * brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_OUT_AIM = MyxUtils.createName("edu.uci.isr.archstudio4.comp.bootstrap.BootstrapMyxComponent.aim");

	/**
	 * Myx-interface name for outSingleServiceObject xarch.
	 * <p>
	 * Through this IMyxName, this brick can use services reached through xarch
	 * brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("edu.uci.isr.archstudio4.comp.bootstrap.BootstrapMyxComponent.xarch");

	/**
	 * the service object for outSingleServiceObject <code>myx</code>.
	 * 
	 * @generated
	 */
	protected IMyxRuntime myx = null;

	/**
	 * the service object for outSingleServiceObject <code>aim</code>.
	 * 
	 * @generated
	 */
	protected IAIM aim = null;

	/**
	 * the service object for outSingleServiceObject <code>xarch</code>.
	 * 
	 * @generated
	 */
	protected XArchFlatInterface xarch = null;

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
			if (myx == null) {
				throw new IllegalArgumentException("myx must be assigned a value (i.e., the interface must be connected) before calling init()");
			}
			if (aim == null) {
				throw new IllegalArgumentException("aim must be assigned a value (i.e., the interface must be connected) before calling init()");
			}
			if (xarch == null) {
				throw new IllegalArgumentException("xarch must be assigned a value (i.e., the interface must be connected) before calling init()");
			}
		}

		/**
		 * @generated
		 */
		@Override
		public void begin() {
			// connectBeforeBegin 

			if (myx == null) {
				throw new IllegalArgumentException("myx must be assigned a value before calling begin()");
			}

			if (aim == null) {
				throw new IllegalArgumentException("aim must be assigned a value before calling begin()");
			}

			if (xarch == null) {
				throw new IllegalArgumentException("xarch must be assigned a value before calling begin()");
			}
		}
	}

	/**
	 * @generated
	 */
	public AbstractBootstrapMyxComponent() {

		myxLifecycleProcessors.add(this);
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

		if (INTERFACE_NAME_OUT_MYX.equals(interfaceName)) {

			if (myx != null) {
				throw new IllegalArgumentException("myx is already connected.");
			}
			// sets the serviceObject to myx 
			myx = (IMyxRuntime) serviceObject;
			return;
		}

		if (INTERFACE_NAME_OUT_AIM.equals(interfaceName)) {

			if (aim != null) {
				throw new IllegalArgumentException("aim is already connected.");
			}
			// sets the serviceObject to aim 
			aim = (IAIM) serviceObject;
			return;
		}

		if (INTERFACE_NAME_OUT_XARCH.equals(interfaceName)) {

			if (xarch != null) {
				throw new IllegalArgumentException("xarch is already connected.");
			}
			// sets the serviceObject to xarch 
			xarch = (XArchFlatInterface) serviceObject;
			return;
		}

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

		if (INTERFACE_NAME_OUT_MYX.equals(interfaceName)) {
			if (myx == null) {
				throw new IllegalArgumentException("myx was not previously connected.");
			}
			// removes the serviceObject
			myx = null;
			return;
		}

		if (INTERFACE_NAME_OUT_AIM.equals(interfaceName)) {
			if (aim == null) {
				throw new IllegalArgumentException("aim was not previously connected.");
			}
			// removes the serviceObject
			aim = null;
			return;
		}

		if (INTERFACE_NAME_OUT_XARCH.equals(interfaceName)) {
			if (xarch == null) {
				throw new IllegalArgumentException("xarch was not previously connected.");
			}
			// removes the serviceObject
			xarch = null;
			return;
		}

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

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#begin()
	 */
	/**
	 * @generated
	 */
	public void begin() {

	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#destroy()
	 */
	/**
	 * @generated
	 */
	public void destroy() {

	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#end()
	 */
	/**
	 * @generated
	 */
	public void end() {

	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#init()
	 */
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
	 */
	public void init() {

	}

	/**
	 * A list of lifecycle processors. The order of method calling is
	 * <ol>
	 * <li>PreMyxLifecycleProcessor#init()</li>
	 * <li>this.init()</li>
	 * <li>PreMyxLifecycleProcessor#begin()</li>
	 * <li>this.begin()</li>
	 * <li>PreMyxLifecycleProcessor#end()</li>
	 * <li>this.end()</li>
	 * <li>PreMyxLifecycleProcessor#destroy()</li>
	 * <li>this.destroy()</li>
	 * 
	 * @generated
	 */
	private final List<IMyxLifecycleProcessor> myxLifecycleProcessors = new ArrayList<IMyxLifecycleProcessor>();

	/**
	 * Adds a preMyxLifecycleProcessor
	 * 
	 * @param preMyxLifecycleProcessor
	 */
	protected void addPreMyxLifecycleProcessor(IMyxLifecycleProcessor preMyxLifecycleProcessor) {

		// inserts a preMyxLifecycleProcessor into the head of the list
		this.myxLifecycleProcessors.add(0, preMyxLifecycleProcessor);
	}

	/**
	 * @generated
	 */
	private IMyxBrickItems brickItems = null;

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxBrick#setMyxBrickItems()
	 */
	/**
	 * @generated
	 */
	public final void setMyxBrickItems(IMyxBrickItems brickItems) {
		this.brickItems = brickItems;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxBrick#setMyxBrickItems()
	 */
	/**
	 * @generated
	 */
	public final IMyxBrickItems getMyxBrickItems() {
		return brickItems;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxBrick#getLifecycleProcessors()
	 */
	/**
	 * @generated
	 */
	public final IMyxLifecycleProcessor[] getLifecycleProcessors() {
		return myxLifecycleProcessors.toArray(new IMyxLifecycleProcessor[myxLifecycleProcessors.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxBrick#getProvidedServiceProvider()
	 */
	/**
	 * @generated
	 */
	public final IMyxProvidedServiceProvider getProvidedServiceProvider() {
		return this;
	}

}
