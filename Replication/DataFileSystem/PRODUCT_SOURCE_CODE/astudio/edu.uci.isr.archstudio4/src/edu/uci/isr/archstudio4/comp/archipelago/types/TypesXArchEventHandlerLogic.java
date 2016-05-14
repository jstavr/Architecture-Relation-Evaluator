package edu.uci.isr.archstudio4.comp.archipelago.types;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.IXArchEventHandlerLogic;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.logics.tracking.TypedThingTrackingLogic;
import edu.uci.isr.bna4.things.utility.EnvironmentPropertiesThing;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchPath;

public class TypesXArchEventHandlerLogic
	extends AbstractThingLogic
	implements IXArchEventHandlerLogic{

	protected final TypedThingTrackingLogic tttl;

	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;

	public TypesXArchEventHandlerLogic(ArchipelagoServices AS, ObjRef xArchRef, TypedThingTrackingLogic tttl){
		this.AS = AS;
		this.xArchRef = xArchRef;
		this.tttl = tttl;
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt, IBNAWorld world){
		ArchipelagoUtils.sendEventToInnerViews(evt, world, tttl);

		IBNAModel model = world.getBNAModel();
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if(sourcePath != null){
			sourcePathString = sourcePath.toTagsOnlyString();
		}

		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if(targetPath != null){
			targetPathString = targetPath.toTagsOnlyString();
		}

		//We only handle things that occur under xArch/archTypes
		if(sourcePathString == null || !sourcePathString.startsWith("xArch/archTypes")){
			return;
		}

		ObjRef sourceRef = evt.getSource();

		ObjRef[] srcAncestors = evt.getSourceAncestors();
		ObjRef[] targetAncestors;
		if(evt.getTarget() instanceof ObjRef){
			targetAncestors = new ObjRef[srcAncestors.length + 1];
			targetAncestors[0] = (ObjRef)evt.getTarget();
			System.arraycopy(srcAncestors, 0, targetAncestors, 1, srcAncestors.length);
		}
		else{
			targetAncestors = srcAncestors;
		}

		ObjRef archTypesRef = srcAncestors[srcAncestors.length - 2];
		ObjRef modifiedTypeRef;
		if(srcAncestors.length >= 3){
			modifiedTypeRef = srcAncestors[srcAncestors.length - 3];
		}
		else if(targetAncestors.length >= 3){
			modifiedTypeRef = targetAncestors[targetAncestors.length - 3];
		}
		else{
			return;
		}
		String modifiedTypeID = XadlUtils.getID(AS.xarch, modifiedTypeRef);

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(model);
		String editingTypeID = (String)ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		ObjRef editingTypeRef = AS.xarch.getByID(xArchRef, editingTypeID);

		if(!BNAUtils.nulleq(modifiedTypeID, editingTypeID)){
			//This is an event for an ArchType, but not the one we're currently editing
			//TODO: Process subarchitectures recursively
			return;
		}

		if(evt.getEventType() == XArchFlatEvent.CLEAR_EVENT || evt.getEventType() == XArchFlatEvent.REMOVE_EVENT){
			if(sourcePathString != null && sourcePathString.equals("xArch/archTypes")){
				if(targetPathString != null && targetPathString.equals("componentType")){
					AS.editor.clearEditor();
					AS.editor.displayDefaultEditor();
					return;
				}
				else if(targetPathString != null && targetPathString.equals("connectorType")){
					AS.editor.clearEditor();
					AS.editor.displayDefaultEditor();
					return;
				}
				else if(targetPathString != null && targetPathString.equals("interfaceType")){
					AS.editor.clearEditor();
					AS.editor.displayDefaultEditor();
					return;
				}
			}
			else if(sourcePathString != null && sourcePathString.startsWith("xArch/archTypes/componentType")){
				TypesMapper.updateComponentType(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
			else if(sourcePathString != null && sourcePathString.startsWith("xArch/archTypes/connectorType")){
				TypesMapper.updateConnectorType(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
			else if(sourcePathString != null && sourcePathString.startsWith("xArch/archTypes/interfaceType")){
				TypesMapper.updateInterfaceType(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
		}

		if(targetPathString == null){
			targetPath = sourcePath;
		}

		if(evt.getTarget() == null || !(evt.getTarget() instanceof ObjRef)){
			return;
		}
		//ObjRef[] targetAncestorRefs = AS.xarch.getAllAncestors((ObjRef)evt.getTarget());
		if(targetPathString != null && targetPathString.startsWith("xArch/archTypes/componentType")){
			TypesMapper.updateComponentType(AS, world, srcAncestors[srcAncestors.length - 3]);
		}
		else if(targetPathString != null && targetPathString.startsWith("xArch/archTypes/connectorType")){
			TypesMapper.updateConnectorType(AS, world, srcAncestors[srcAncestors.length - 3]);
		}
		else if(targetPathString != null && targetPathString.startsWith("xArch/archTypes/interfaceType")){
			TypesMapper.updateInterfaceType(AS, world, srcAncestors[srcAncestors.length - 3]);
		}
	}

}
