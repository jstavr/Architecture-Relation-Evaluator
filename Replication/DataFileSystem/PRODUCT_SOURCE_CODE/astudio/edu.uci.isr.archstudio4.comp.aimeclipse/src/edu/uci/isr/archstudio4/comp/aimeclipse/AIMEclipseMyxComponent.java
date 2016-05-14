package edu.uci.isr.archstudio4.comp.aimeclipse;

import edu.uci.isr.archstudio4.comp.aimeclipse.AbstractAIMEclipseMyxComponent;

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
 * method, remove "@generated" annotation from java doc comment before re-run
 * the generation if any change is made to the method.
 * </p>
 * 
 * @generated
 */
public class AIMEclipseMyxComponent extends AbstractAIMEclipseMyxComponent {

	/**
	 * @generated
	 */
	public AIMEclipseMyxComponent() {

		super();

	}

	/**
	 * @see edu.uci.isr.myx.fw.AbstractMyxSimpleBrick#init()
	 */
	@Override
	public void init() {
		super.init();
		aim = new AIMEclipseImpl(xarch);
	}

	/**
	 * @see edu.uci.isr.myx.fw.AbstractMyxSimpleBrick#begin()
	 */
	@Override
	public void begin() {
		super.begin();
	}

}
