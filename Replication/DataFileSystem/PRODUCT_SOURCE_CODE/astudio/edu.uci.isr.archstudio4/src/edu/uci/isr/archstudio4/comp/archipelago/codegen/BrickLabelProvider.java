package edu.uci.isr.archstudio4.comp.archipelago.codegen;

import java.util.Collection;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.archstudio4.util.XadlTreeUtils;

/**
 * A label provider that provides the names of bricks defined at the eclipse
 * extension point.
 * 
 * @author Nobu Takeo nobu.takeo@gmail.com, nobu.takeo@uci.edu
 */
public class BrickLabelProvider extends LabelProvider implements ILabelProvider {

	protected IResources resources;

	public BrickLabelProvider(IResources resources) {
		this.resources = resources;
		ArchstudioResources.init(resources);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof BrickLabel) {
			BrickLabel brickLabel = (BrickLabel) element;
			return brickLabel.toLabelString();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof Collection) {
			return XadlTreeUtils.getIconForType(resources, XadlTreeUtils.DOCUMENT);
		}
		else if (element instanceof BrickLabel) {
			return XadlTreeUtils.getIconForType(resources, XadlTreeUtils.COMPONENT_TYPE);
		}
		return super.getImage(element);
	}

}
