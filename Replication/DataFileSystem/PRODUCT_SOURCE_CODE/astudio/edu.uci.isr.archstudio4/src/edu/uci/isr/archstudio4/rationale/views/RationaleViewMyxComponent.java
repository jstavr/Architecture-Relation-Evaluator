package edu.uci.isr.archstudio4.rationale.views;

import edu.uci.isr.archstudio4.rationale.views.AbstractRationaleViewMyxComponent;

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
 * method, remove "@generated" annotation from java doc comment before re-run
 * the generation if any change is made to the method.
 * </p>
 * 
 * @generated
 */
public class RationaleViewMyxComponent extends AbstractRationaleViewMyxComponent {

	/**
	 * @generated
	 */
	public RationaleViewMyxComponent() {

	}

	/**
	 * The following variable(s) will be assigned values before this method is
	 * called.
	 * <ul>
	 * <li>{@link #xarchcs}</li>
	 * <li>{@link #explicitadt}</li>
	 * </ul>
	 * 
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
	}

}
