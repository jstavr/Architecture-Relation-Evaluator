package edu.uci.isr.archstudio4.rationale.views;

import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.myx.fw.IMyxBrickItems;
import edu.uci.isr.myx.fw.IMyxDynamicBrick;
import edu.uci.isr.myx.fw.IMyxLifecycleProcessor;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.IMyxProvidedServiceProvider;
import edu.uci.isr.myx.fw.MyxLifecycleAdapter;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFileListener;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class of "RationaleViewMyxComponent" brick.
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
 * <td>XArchFlatListener</td>
 * <td>this</td>
 * <td><code>xarchflatevents</code></td>
 * <td>inSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegateToMyxRegistry</td>
 * </tr>
 * <tr>
 * <td>XArchFileListener</td>
 * <td>this</td>
 * <td><code>xarchfileevents</code></td>
 * <td>inSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegateToMyxRegistry</td>
 * </tr>
 * <tr>
 * <td>ExplicitADTListener</td>
 * <td>this</td>
 * <td><code>explicitevents</code></td>
 * <td>inSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegateToMyxRegistry</td>
 * </tr>
 * <tr>
 * <td>XArchChangeSetListener</td>
 * <td>this</td>
 * <td><code>changesetevents</code></td>
 * <td>inSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegateToMyxRegistry</td>
 * </tr>
 * <tr>
 * <td>XArchChangeSetInterface</td>
 * <td><code>xarchcs</code></td>
 * <td><code>xarchcs</code></td>
 * <td>outSingleServiceObject</td>
 * <td>beforeInit</td>
 * <td>delegate</td>
 * </tr>
 * <tr>
 * <td>IExplicitADT</td>
 * <td><code>explicitadt</code></td>
 * <td><code>explicitadt</code></td>
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
public abstract class AbstractRationaleViewMyxComponent

implements IMyxDynamicBrick, IMyxLifecycleProcessor, IMyxProvidedServiceProvider, XArchFileListener, XArchFlatListener, ExplicitADTListener,
        XArchChangeSetListener {

	/**
	 * Myx-interface name for inSingleServiceObject xarchflatevents.
	 * <p>
	 * Through this IMyxName, other bricks can use services provided by
	 * xarchflatevents brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_IN_XARCHFLATEVENTS = MyxUtils
	        .createName("edu.uci.isr.archstudio4.rationale.views.RationaleViewMyxComponent.xarchflatevents");

	/**
	 * Myx-interface name for inSingleServiceObject xarchfileevents.
	 * <p>
	 * Through this IMyxName, other bricks can use services provided by
	 * xarchfileevents brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_IN_XARCHFILEEVENTS = MyxUtils
	        .createName("edu.uci.isr.archstudio4.rationale.views.RationaleViewMyxComponent.xarchfileevents");

	/**
	 * Myx-interface name for inSingleServiceObject explicitevents.
	 * <p>
	 * Through this IMyxName, other bricks can use services provided by
	 * explicitevents brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_IN_EXPLICITEVENTS = MyxUtils
	        .createName("edu.uci.isr.archstudio4.rationale.views.RationaleViewMyxComponent.explicitevents");

	/**
	 * Myx-interface name for inSingleServiceObject changesetevents.
	 * <p>
	 * Through this IMyxName, other bricks can use services provided by
	 * changesetevents brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_IN_CHANGESETEVENTS = MyxUtils
	        .createName("edu.uci.isr.archstudio4.rationale.views.RationaleViewMyxComponent.changesetevents");

	/**
	 * Myx-interface name for outSingleServiceObject xarch.
	 * <p>
	 * Through this IMyxName, this brick can use services reached through xarch
	 * brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_OUT_XARCH = MyxUtils.createName("edu.uci.isr.archstudio4.rationale.views.RationaleViewMyxComponent.xarch");

	/**
	 * Myx-interface name for outSingleServiceObject explicitadt.
	 * <p>
	 * Through this IMyxName, this brick can use services reached through
	 * explicitadt brick interface.
	 * </p>
	 * 
	 * @generated
	 */
	public static final IMyxName INTERFACE_NAME_OUT_EXPLICITADT = MyxUtils
	        .createName("edu.uci.isr.archstudio4.rationale.views.RationaleViewMyxComponent.explicitadt");

	/**
	 * the service object for outSingleServiceObject <code>xarch</code>.
	 * 
	 * @generated
	 */
	protected XArchChangeSetInterface xarch = null;

	/**
	 * the service object for outSingleServiceObject <code>explicitadt</code>.
	 * 
	 * @generated
	 */
	protected IExplicitADT explicitadt = null;

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
			if (xarch == null) {
				throw new IllegalArgumentException("xarch must be assigned a value (i.e., the interface must be connected) before calling init()");
			}
			if (explicitadt == null) {
				throw new IllegalArgumentException("explicitadt must be assigned a value (i.e., the interface must be connected) before calling init()");
			}
		}

		/**
		 * @generated
		 */
		@Override
		public void begin() {
			// connectBeforeBegin 

			if (xarch == null) {
				throw new IllegalArgumentException("xarch must be assigned a value before calling begin()");
			}

			if (explicitadt == null) {
				throw new IllegalArgumentException("explicitadt must be assigned a value before calling begin()");
			}
		}
	}

	/**
	 * @generated
	 */
	public AbstractRationaleViewMyxComponent() {

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

		if (INTERFACE_NAME_OUT_XARCH.equals(interfaceName)) {

			if (xarch != null) {
				throw new IllegalArgumentException("xarch is already connected.");
			}
			// sets the serviceObject to xarch 
			xarch = (XArchChangeSetInterface) serviceObject;
			return;
		}

		if (INTERFACE_NAME_OUT_EXPLICITADT.equals(interfaceName)) {

			if (explicitadt != null) {
				throw new IllegalArgumentException("explicitadt is already connected.");
			}
			// sets the serviceObject to explicitadt 
			explicitadt = (IExplicitADT) serviceObject;
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

		if (INTERFACE_NAME_OUT_XARCH.equals(interfaceName)) {
			if (xarch == null) {
				throw new IllegalArgumentException("xarch was not previously connected.");
			}
			// removes the serviceObject
			xarch = null;
			return;
		}

		if (INTERFACE_NAME_OUT_EXPLICITADT.equals(interfaceName)) {
			if (explicitadt == null) {
				throw new IllegalArgumentException("explicitadt was not previously connected.");
			}
			// removes the serviceObject
			explicitadt = null;
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

		if (INTERFACE_NAME_IN_XARCHFLATEVENTS.equals(interfaceName)) {

			return this;
		}
		if (INTERFACE_NAME_IN_XARCHFILEEVENTS.equals(interfaceName)) {

			return this;
		}
		if (INTERFACE_NAME_IN_EXPLICITEVENTS.equals(interfaceName)) {

			return this;
		}
		if (INTERFACE_NAME_IN_CHANGESETEVENTS.equals(interfaceName)) {

			return this;
		}
		return null;
	}

	/**
	 * The myx registry, which contains all objects interested in events from
	 * this brick.
	 * 
	 * @generated
	 */
	MyxRegistry myxr = MyxRegistry.getSharedInstance();

	/**
	 * Implementation of xarchfileevents.
	 * <table border="1">
	 * <tr>
	 * <th>direction</th>
	 * <th>connection timing</th>
	 * <th>template type</th>
	 * </tr>
	 * <tr>
	 * <td>inSingleServiceObject</td>
	 * <td>beforeInit</td>
	 * <td>delegateToMyxRegistry</td>
	 * </tr>
	 * </table>
	 * 
	 * @see XArchFileListener#handleXArchFileEvent(XArchFileEvent evt)
	 * @generated
	 */
	public void handleXArchFileEvent(XArchFileEvent evt) {
		for (XArchFileListener o : myxr.getObjects(this, XArchFileListener.class)) {
			try {
				o.handleXArchFileEvent(evt);
			}
			catch (Throwable t) {
				// TODO: Handle exceptions better.
				t.printStackTrace();
			}
		}
	}

	/**
	 * Implementation of xarchflatevents.
	 * <table border="1">
	 * <tr>
	 * <th>direction</th>
	 * <th>connection timing</th>
	 * <th>template type</th>
	 * </tr>
	 * <tr>
	 * <td>inSingleServiceObject</td>
	 * <td>beforeInit</td>
	 * <td>delegateToMyxRegistry</td>
	 * </tr>
	 * </table>
	 * 
	 * @see XArchFlatListener#handleXArchFlatEvent(XArchFlatEvent evt)
	 * @generated
	 */
	public void handleXArchFlatEvent(XArchFlatEvent evt) {
		for (XArchFlatListener o : myxr.getObjects(this, XArchFlatListener.class)) {
			try {
				o.handleXArchFlatEvent(evt);
			}
			catch (Throwable t) {
				// TODO: Handle exceptions better.
				t.printStackTrace();
			}
		}
	}

	/**
	 * Implementation of explicitevents.
	 * <table border="1">
	 * <tr>
	 * <th>direction</th>
	 * <th>connection timing</th>
	 * <th>template type</th>
	 * </tr>
	 * <tr>
	 * <td>inSingleServiceObject</td>
	 * <td>beforeInit</td>
	 * <td>delegateToMyxRegistry</td>
	 * </tr>
	 * </table>
	 * 
	 * @see ExplicitADTListener#handleExplicitEvent(ExplicitADTEvent evt)
	 * @generated
	 */
	public void handleExplicitEvent(ExplicitADTEvent evt) {
		for (ExplicitADTListener o : myxr.getObjects(this, ExplicitADTListener.class)) {
			try {
				o.handleExplicitEvent(evt);
			}
			catch (Throwable t) {
				// TODO: Handle exceptions better.
				t.printStackTrace();
			}
		}
	}

	/**
	 * Implementation of changesetevents.
	 * <table border="1">
	 * <tr>
	 * <th>direction</th>
	 * <th>connection timing</th>
	 * <th>template type</th>
	 * </tr>
	 * <tr>
	 * <td>inSingleServiceObject</td>
	 * <td>beforeInit</td>
	 * <td>delegateToMyxRegistry</td>
	 * </tr>
	 * </table>
	 * 
	 * @see XArchChangeSetListener#handleXArchChangeSetEvent(XArchChangeSetEvent
	 *      evt)
	 * @generated
	 */
	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt) {
		for (XArchChangeSetListener o : myxr.getObjects(this, XArchChangeSetListener.class)) {
			try {
				o.handleXArchChangeSetEvent(evt);
			}
			catch (Throwable t) {
				// TODO: Handle exceptions better.
				t.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#begin()
	 */
	/**
	 * @generated
	 */
	public void begin() {

		myxr.register(this);
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
		myxr.unregister(this);

	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.isr.myx.fw.IMyxLifecycleProcessor#init()
	 */
	/**
	 * The following variable(s) will be assigned values before this method is
	 * called.
	 * <ul>
	 * <li>{@link #xarchcs}</li>
	 * <li>{@link #explicitadt}</li>
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
