package edu.uci.isr.archstudio4.comp.xarchcs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.sysutils.Tuple;
import edu.uci.isr.sysutils.UIDGenerator;
import edu.uci.isr.widgets.swt.OverlayImageIcon;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public class ChangeSetUtils {

	private static Image removedImage = null;
	private static Image addedImage = null;
	private static Image modifiedImage = null;

	public static Image getOverlayImageIcon(XArchChangeSetInterface xArchCS, IExplicitADT explicitADT, Image image, Object element) {

		if (removedImage == null) {
			removedImage = XArchCSActivator.getDefault().getImageRegistry().get("res/icons/removed_item.gif");
			addedImage = XArchCSActivator.getDefault().getImageRegistry().get("res/icons/added_item.gif");
			modifiedImage = XArchCSActivator.getDefault().getImageRegistry().get("res/icons/modified_item.gif");
		}
		
		Image[] overlayImage = new Image[1];
		overlayImage[0] = null;
		if (element instanceof ObjRef) {
			ObjRef objRef = (ObjRef) element;
			ChangeStatus changeStatus = xArchCS.getChangeStatus(objRef, explicitADT.getExplicitChangeSetRefs(xArchCS.getXArch(objRef)));

			switch (changeStatus) {
			case ADDED:
				overlayImage[0] = addedImage;
				break;
			case REMOVED:
				overlayImage[0] = removedImage;
				break;
			case MODIFIED:
				overlayImage[0] = modifiedImage;
				break;
			}
			if (overlayImage[0] != null) {
				String key = ""+imageHash(image) +":"+ imageHash(overlayImage[0]);
				Image composedImage = XArchCSActivator.getDefault().getImageRegistry().get(key);
				if(composedImage == null){
					composedImage = new OverlayImageIcon(image, overlayImage, new int[] { 0 }).createImage();
					XArchCSActivator.getDefault().getImageRegistry().put(key, composedImage);
				}
				return composedImage;
			}
		}
		return image;
	}

	protected static int imageHash(Image image){
		ImageData imageData = image.getImageData();
		return Arrays.hashCode(imageData.data) ^ Arrays.hashCode(imageData.alphaData);
	}
	
	public static Object[] filterOutDetatched(XArchChangeSetInterface xArchCS, IExplicitADT explicitADT, Object[] objects) {
		if (objects.length == 0) {
			return objects;
		}

		ObjRef[] explicitChangeSets = null;
		List<Object> nondetachcedChangeSets = new ArrayList<Object>();

		for (Object o : objects) {
			if (o instanceof ObjRef) {
				if (explicitChangeSets == null) {
					explicitChangeSets = explicitADT.getExplicitChangeSetRefs(xArchCS.getXArch((ObjRef) o));
				}
				if (ChangeStatus.DETACHED == xArchCS.getChangeStatus((ObjRef) o, explicitChangeSets)) {
					continue;
				}
			}
			nondetachcedChangeSets.add(o);
		}

		return nondetachcedChangeSets.toArray(new Object[nondetachcedChangeSets.size()]);
	}

	public static ObjRef[] resolveExternalChangeSetRefs(XArchFlatQueryInterface xarch, ObjRef[] changeSetRefs) {
		ObjRef[] newChangeSetRefs = new ObjRef[changeSetRefs.length];
		for (int i = 0; i < changeSetRefs.length; i++) {
			newChangeSetRefs[i] = resolveExternalChangeSetRef(xarch, changeSetRefs[i]);
		}
		return newChangeSetRefs;
	}

	public static ObjRef resolveExternalChangeSetRef(XArchFlatQueryInterface xarch, ObjRef changeSetRef) {
		if (changeSetRef != null && xarch.isInstanceOf(changeSetRef, "changesets#ChangeSetLink")) {
			changeSetRef = XadlUtils.resolveXLink(xarch, changeSetRef, "externalLink");
		}
		return changeSetRef;
	}

	public static ObjRef[] unresolveExternalChangeSetRefs(XArchFlatQueryInterface xarch, ObjRef xArchRef, ObjRef[] changeSetRefs) {
		if (changeSetRefs.length == 0) {
			return changeSetRefs;
		}

		Map<ObjRef, ObjRef> unresolver = new HashMap<ObjRef, ObjRef>();
		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "ArchChangeSets", xArchRef);
		ObjRef[] unresolvedChangeSetRefs = xarch.getAll(archChangeSetsRef, "changeSet");
		ObjRef[] resolvedChangeSetRefs = resolveExternalChangeSetRefs(xarch, unresolvedChangeSetRefs);
		for (int i = 0, size = unresolvedChangeSetRefs.length; i < size; i++) {
			unresolver.put(resolvedChangeSetRefs[i], unresolvedChangeSetRefs[i]);
		}
		ObjRef[] newChangeSetRefs = new ObjRef[changeSetRefs.length];
		for (int i = 0; i < changeSetRefs.length; i++) {
			if (unresolver.get(changeSetRefs[i]) != null)
				newChangeSetRefs[i] = unresolver.get(changeSetRefs[i]);
			else
				newChangeSetRefs[i] = changeSetRefs[i];
		}
		return newChangeSetRefs;
	}

	public static void unresolveExternalChangeSetRefs(XArchFlatQueryInterface xarch, ObjRef xArchRef, Collection<ObjRef> changeSetRefs) {
		if (changeSetRefs.size() == 0) {
			return;
		}

		ObjRef[] unresolvedChangeSetRefs = unresolveExternalChangeSetRefs(xarch, xArchRef, changeSetRefs.toArray(new ObjRef[changeSetRefs.size()]));
		changeSetRefs.clear();
		changeSetRefs.addAll(Arrays.asList(unresolvedChangeSetRefs));
	}

	public static ObjRef unresolveExternalChangeSetRef(XArchFlatQueryInterface xarch, ObjRef xArchRef, ObjRef changeSetRef) {
		return unresolveExternalChangeSetRefs(xarch, xArchRef, new ObjRef[] { changeSetRef })[0];
	}

	public static ObjRef[] resolveExternalRelationshipRefs(XArchFlatQueryInterface xarch, ObjRef[] relationshipRefs) {
		ObjRef[] newRelationshipRefs = new ObjRef[relationshipRefs.length];
		for (int i = 0; i < relationshipRefs.length; i++) {
			newRelationshipRefs[i] = resolveExternalRelationshipRef(xarch, relationshipRefs[i]);
		}
		return newRelationshipRefs;
	}

	public static ObjRef resolveExternalRelationshipRef(XArchFlatQueryInterface xarch, ObjRef relationshipRef) {
		if (xarch.isInstanceOf(relationshipRef, "changesets#RelationshipLink")) {
			relationshipRef = XadlUtils.resolveXLink(xarch, relationshipRef, "externalLink");
		}
		return relationshipRef;
	}

	public static ObjRef[] unresolveExternalRelationshipRefs(XArchFlatQueryInterface xarch, ObjRef xArchRef, ObjRef[] relationshipRefs) {
		if (relationshipRefs.length == 0) {
			return relationshipRefs;
		}

		Map<ObjRef, ObjRef> unresolver = new HashMap<ObjRef, ObjRef>();
		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "ArchChangeSets", xArchRef);
		ObjRef[] unresolvedRelationshipRefs = xarch.getAll(archChangeSetsRef, "relationship");
		ObjRef[] resolvedRelationshipRefs = resolveExternalRelationshipRefs(xarch, unresolvedRelationshipRefs);
		for (int i = 0, size = unresolvedRelationshipRefs.length; i < size; i++) {
			unresolver.put(resolvedRelationshipRefs[i], unresolvedRelationshipRefs[i]);
		}
		ObjRef[] newRelationshipRefs = new ObjRef[relationshipRefs.length];
		for (int i = 0; i < relationshipRefs.length; i++) {
			newRelationshipRefs[i] = unresolver.get(relationshipRefs[i]);
		}
		return newRelationshipRefs;
	}

	public static ObjRef createChangeSet(XArchFlatInterface xarch, ObjRef xArchRef, boolean resolve, boolean saveIfExternal) throws URISyntaxException,
	        IOException {
		return createPossiblyExternalElement(xarch, xArchRef, "ChangeSet", "ChangeSet", resolve, saveIfExternal);
	}

	public static ObjRef createRelationship(XArchFlatInterface xarch, ObjRef xArchRef, String relationshipType, boolean resolve, boolean saveIfExternal)
	        throws URISyntaxException, IOException {
		return createPossiblyExternalElement(xarch, xArchRef, relationshipType + "Relationship", "Relationship", resolve, saveIfExternal);
	}

	private static ObjRef createPossiblyExternalElement(XArchFlatInterface xarch, ObjRef xArchRef, String elementType, String elementMainType, boolean resolve,
	        boolean saveIfExternal) throws URISyntaxException, IOException {
		String id = UIDGenerator.generateUID(elementType);

		ObjRef changesetsContextRef = xarch.createContext(xArchRef, "changesets");
		ObjRef archChangeSetsRef = xarch.getElement(changesetsContextRef, "archChangeSets", xArchRef);
		ObjRef elementRef = null;

		if (archChangeSetsRef != null) {

			String externalLinkHref = (String) xarch.get(archChangeSetsRef, "externalLinkHref");
			if (externalLinkHref != null && externalLinkHref.length() > 0) {
				URI uri = new URI(xarch.getXArchURI(xArchRef)).resolve(externalLinkHref + "/" + id + ".xml");
				ObjRef externalXArchRef = xarch.createXArch(uri.toString());
				ObjRef externalChangesetsContextRef = xarch.createContext(externalXArchRef, "changesets");
				ObjRef externalArchChangeSetsRef = xarch.createElement(externalChangesetsContextRef, "ArchChangeSets");
				xarch.add(externalXArchRef, "Object", externalArchChangeSetsRef);

				elementRef = xarch.create(externalChangesetsContextRef, elementType);
				xarch.set(elementRef, "id", id);
				xarch.add(externalArchChangeSetsRef, elementMainType, elementRef);

				ObjRef elementLinkRef = xarch.create(changesetsContextRef, elementMainType + "Link");
				XadlUtils.setXLinkByHref(xarch, elementLinkRef, "externalLink", externalLinkHref + "/" + id + ".xml#" + id);
				xarch.add(archChangeSetsRef, elementMainType, elementLinkRef);

				if (saveIfExternal) {
					xarch.writeToURL(externalXArchRef, uri.toString());
				}

				return resolve ? elementRef : elementLinkRef;
			}
			else {
				elementRef = xarch.create(changesetsContextRef, elementType);
				xarch.set(elementRef, "id", id);
				xarch.add(archChangeSetsRef, elementMainType, elementRef);

				return elementRef;
			}
		}

		return null;
	}
}
