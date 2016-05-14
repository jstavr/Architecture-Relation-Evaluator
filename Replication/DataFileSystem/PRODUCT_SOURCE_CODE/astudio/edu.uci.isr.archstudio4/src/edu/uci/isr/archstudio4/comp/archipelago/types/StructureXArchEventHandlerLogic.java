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

public class StructureXArchEventHandlerLogic extends AbstractThingLogic implements IXArchEventHandlerLogic {

	protected final TypedThingTrackingLogic tttl;

	protected ArchipelagoServices AS = null;

	public StructureXArchEventHandlerLogic(ArchipelagoServices AS, TypedThingTrackingLogic tttl) {
		this.AS = AS;
		this.tttl = tttl;
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt, IBNAWorld world) {
		ArchipelagoUtils.sendEventToInnerViews(evt, world, tttl);

		IBNAModel model = world.getBNAModel();

		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if (sourcePath != null) {
			sourcePathString = sourcePath.toTagsOnlyString();
		}

		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if (targetPath != null) {
			targetPathString = targetPath.toTagsOnlyString();
		}

		//See if we're removing the currently editing archstructure;
		//if so, then clear the editor and display the default editor
		if (evt.getEventType() == XArchFlatEvent.REMOVE_EVENT) {
			if (sourcePathString != null && sourcePathString.equals("xArch")) {
				if (targetPathString != null && targetPathString.equals("archStructure")) {
					String targetID = XadlUtils.getID(AS.xarch, (ObjRef) evt.getTarget());
					if (targetID != null) {
						EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(model);
						String editingArchStructureID = (String) ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
						if (editingArchStructureID != null && editingArchStructureID.equals(targetID)) {
							/*
							 * This isn't the right place for this. Instead this
							 * should be handled by whatever created the editor.
							 * Handling it hear causes the editor to close if
							 * one of the substructures is removed.
							 */
							//AS.editor.clearEditor();
							//AS.editor.displayDefaultEditor();
							//return;
						}
					}
				}
			}
		}

		//We only handle things that occur under xArch/archStructure
		if (sourcePathString == null || !sourcePathString.startsWith("xArch/archStructure")) {
			return;
		}

		ObjRef sourceRef = evt.getSource();

		ObjRef[] srcAncestors = evt.getSourceAncestors();
		ObjRef[] targetAncestors;
		if (evt.getTarget() instanceof ObjRef) {
			targetAncestors = new ObjRef[srcAncestors.length + 1];
			targetAncestors[0] = (ObjRef) evt.getTarget();
			System.arraycopy(srcAncestors, 0, targetAncestors, 1, srcAncestors.length);
		}
		else {
			targetAncestors = srcAncestors;
		}

		ObjRef archStructureRef = srcAncestors[srcAncestors.length - 2];
		String archStructureID = XadlUtils.getID(AS.xarch, archStructureRef);

		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(model);
		String editingArchStructureID = (String) ept.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);

		if (!BNAUtils.nulleq(archStructureID, editingArchStructureID)) {
			return;
		}

		//This is an event that occurred on the structure associated with the passed in model

		if (evt.getEventType() == XArchFlatEvent.CLEAR_EVENT || evt.getEventType() == XArchFlatEvent.REMOVE_EVENT) {
			if (sourcePathString != null && sourcePathString.equals("xArch/archStructure")) {
				if (targetPathString != null && targetPathString.equals("component")) {
					StructureMapper.removeOrphanedBricks(AS, world, archStructureRef);
				}
				else if (targetPathString != null && targetPathString.equals("connector")) {
					StructureMapper.removeOrphanedBricks(AS, world, archStructureRef);
				}
			}
			else if (sourcePathString != null && sourcePathString.startsWith("xArch/archStructure/component")) {
				StructureMapper.updateComponent(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
			else if (sourcePathString != null && sourcePathString.startsWith("xArch/archStructure/connector")) {
				StructureMapper.updateConnector(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
		}

		if (targetPathString == null) {
			targetPath = sourcePath;
		}

		if (evt.getTarget() == null || !(evt.getTarget() instanceof ObjRef)) {
			if (sourcePathString != null && sourcePathString.startsWith("xArch/archStructure/component")) {
				StructureMapper.updateComponent(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
			else if (sourcePathString != null && sourcePathString.startsWith("xArch/archStructure/connector")) {
				StructureMapper.updateConnector(AS, world, srcAncestors[srcAncestors.length - 3]);
			}
			return;
		}
		if (targetPathString != null && targetPathString.startsWith("xArch/archStructure/component")) {
			StructureMapper.updateComponent(AS, world, targetAncestors[targetAncestors.length - 3]);
		}
		else if (targetPathString != null && targetPathString.startsWith("xArch/archStructure/connector")) {
			StructureMapper.updateConnector(AS, world, targetAncestors[targetAncestors.length - 3]);
		}
	}

}
