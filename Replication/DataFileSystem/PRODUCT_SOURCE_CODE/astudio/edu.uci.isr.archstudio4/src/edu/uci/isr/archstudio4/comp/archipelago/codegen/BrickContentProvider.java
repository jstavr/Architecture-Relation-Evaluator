package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A tree content provider that provides brick info defined at the eclipse
 * extension point. This class is used to show brick types in a selection
 * window, when the user selects "Assign extension points..." or
 * "Convert ExtensionPoint to xADL..." on a component/connector in Archipelago.
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class BrickContentProvider implements ITreeContentProvider {
	protected static final Object[] EMPTY_ARRAY = new Object[0];

	private final BrickLabelList brickLabelList;

	public BrickContentProvider(BrickLabelList brickLabelList) {
		this.brickLabelList = brickLabelList;
	}

	public Object[] getChildren(Object parentElement) {

		if(parentElement instanceof BrickLabelList) {
			return ((BrickLabelList)parentElement).getBrickLabels().toArray();
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		if(element instanceof BrickLabel) {
			return brickLabelList;
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
