package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;

import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.logics.editing.EditColorLogic;

public class StructureEditColorLogic
	extends EditColorLogic{

	final protected IPreferenceStore prefs;

	public StructureEditColorLogic(IPreferenceStore prefs){
		this.prefs = prefs;
	}

	@Override
	protected RGB getDefaultRGB(IBNAView view, IThing[] thingsToEdit){
		RGB defaultRGB = null;
		RGB defaultComponentRGB = StructureMapper.getDefaultComponentColor(prefs);
		RGB defaultConnectorRGB = StructureMapper.getDefaultConnectorColor(prefs);
		for(IThing element: thingsToEdit){
			IThing pt = view.getWorld().getBNAModel().getParentThing(element);
			if(pt != null){
				if(StructureMapper.isComponentAssemblyRootThing(pt)){
					if(defaultRGB == null || BNAUtils.nulleq(defaultRGB, defaultComponentRGB)){
						defaultRGB = defaultComponentRGB;
					}
					else{
						return null;
					}
				}
				else if(StructureMapper.isConnectorAssemblyRootThing(pt)){
					if(defaultRGB == null || BNAUtils.nulleq(defaultRGB, defaultConnectorRGB)){
						defaultRGB = defaultConnectorRGB;
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
