package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;

import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.logics.editing.EditColorLogic;

public class TypesEditColorLogic
	extends EditColorLogic{

	final protected IPreferenceStore prefs;

	public TypesEditColorLogic(IPreferenceStore prefs){
		this.prefs = prefs;
	}

	@Override
	protected RGB getDefaultRGB(IBNAView view, IThing[] thingsToEdit){
		RGB defaultRGB = null;
		RGB defaultComponentTypeRGB = TypesMapper.getDefaultComponentTypeColor(prefs);
		RGB defaultConnectorTypeRGB = TypesMapper.getDefaultConnectorTypeColor(prefs);
		for(IThing element: thingsToEdit){
			IThing pt = view.getWorld().getBNAModel().getParentThing(element);
			if(pt != null){
				if(TypesMapper.isComponentTypeAssemblyRootThing(pt)){
					if(defaultRGB == null || BNAUtils.nulleq(defaultRGB, defaultComponentTypeRGB)){
						defaultRGB = defaultComponentTypeRGB;
					}
					else{
						return null;
					}
				}
				else if(TypesMapper.isConnectorTypeAssemblyRootThing(pt)){
					if(defaultRGB == null || BNAUtils.nulleq(defaultRGB, defaultConnectorTypeRGB)){
						defaultRGB = defaultConnectorTypeRGB;
					}
					else{
						return null;
					}
				}
			}
		}
		return defaultRGB;
	}
}
