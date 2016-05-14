package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.swt.graphics.Image;

public interface IArchipelagoLabelProvider{
	public String getText(Object element, String textFromPreviousProvider);
	public Image getImage(Object element, Image imageFromPreviousProvider);
}
