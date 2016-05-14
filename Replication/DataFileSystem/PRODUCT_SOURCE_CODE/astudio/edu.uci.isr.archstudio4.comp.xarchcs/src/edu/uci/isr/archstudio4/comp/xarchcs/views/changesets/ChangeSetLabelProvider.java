package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import edu.uci.isr.archstudio4.comp.xarchcs.ChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.XArchCSActivator;
import edu.uci.isr.archstudio4.comp.xarchcs.changesetsync.IChangeSetSync.ChangeStatus;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTListener;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent.ExplicitEventType;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetListener;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetUtils;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetEvent.ChangeSetEventType;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.NoSuchObjectException;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatListener;

public class ChangeSetLabelProvider extends LabelProvider implements IColorProvider, IFontProvider, ITableLabelProvider, XArchFlatListener, XArchChangeSetListener,
        ExplicitADTListener, IChangeSetStateListener {

	public static final String APPLY_PROPERTY = "Apply";
	public static final String EXPLICIT_PROPERTY = "View";
	public static final String CHANGE_SET_PROPERTY = "Change Set";

	protected final ColumnViewer viewer;
	protected final XArchChangeSetInterface xarch;
	protected final IChangeSetState changeSetState;
	protected final Set<String> properties = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] { APPLY_PROPERTY, EXPLICIT_PROPERTY,
	        CHANGE_SET_PROPERTY })));

	Font activeChangeSetFont = null;
	
	public ChangeSetLabelProvider(ColumnViewer viewer, XArchChangeSetInterface xarch, IChangeSetState changeSetState) {
		this.viewer = viewer;
		this.xarch = xarch;
		this.changeSetState = changeSetState;
		changeSetState.addChangeSetStateListener(this);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return properties.contains(property);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		try {
			Object columnProperty = SWTWidgetUtils.getColumnProperty(viewer, columnIndex);
			ImageRegistry ir = XArchCSActivator.getDefault().getImageRegistry();

			if (APPLY_PROPERTY.equals(columnProperty)) {
				ObjRef changeSetRef = (ObjRef) element;
				switch (changeSetState.getChangeSetState(changeSetRef)) {
				case APPLIED:
					return ir.get("res/icons/applied.gif");
				case IMPLIED:
					return ir.get("res/icons/applied_implied.gif");
				case UNAPPLIED:
					return ir.get("res/icons/unapplied.gif");
				case EXCLUDED:
					return ir.get("res/icons/unapplied_excluded.gif");
				}
			}
			else if (EXPLICIT_PROPERTY.equals(columnProperty)) {
				ObjRef changeSetRef = (ObjRef) element;
				if (changeSetState.isExplicit(changeSetRef))
					return ir.get("res/icons/explicit.gif");
				else
					return ir.get("res/icons/implicit.gif");
			}
			else if (CHANGE_SET_PROPERTY.equals(columnProperty)) {
				return null;
			}
		}
		catch (NoSuchObjectException e) {
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		try {
			Object columnProperty = SWTWidgetUtils.getColumnProperty(viewer, columnIndex);

			if (APPLY_PROPERTY.equals(columnProperty)) {
				return null;
			}
			else if (EXPLICIT_PROPERTY.equals(columnProperty)) {
				return null;
			}
			else if (CHANGE_SET_PROPERTY.equals(columnProperty)) {
				ObjRef changeSetRef = ChangeSetUtils.resolveExternalChangeSetRef(xarch, (ObjRef) element);
				String value = XadlUtils.getDescription(xarch, changeSetRef);
				if (value != null) {
					return value;
				}
				return changeSetRef.toString();
			}
		}
		catch (NoSuchObjectException e) {
		}
		return null;
	}

	public void handleXArchFlatEvent(final XArchFlatEvent evt) {
		if (evt.getIsAttached() && !evt.getIsExtra()) {
			if (evt.getSourceTargetPath().toTagsOnlyString().startsWith("xArch/archChangeSets/changeSet/description")) {
				SWTWidgetUtils.async(viewer, new Runnable() {

					public void run() {
						fireLabelProviderChanged(new LabelProviderChangedEvent(ChangeSetLabelProvider.this,
						        evt.getSourceAncestors()[evt.getSourceAncestors().length - 3]));
					}
				});
			}
		}
	}

	boolean updateApplied = false;

	protected void updateApplied() {
		updateApplied = true;
		SWTWidgetUtils.async(viewer, new Runnable() {

			public void run() {
				if (updateApplied) {
					updateApplied = false;
					fireLabelProviderChanged(new LabelProviderChangedEvent(ChangeSetLabelProvider.this));
				}
			}
		});
	}

	boolean lableProviderNeedsUpdate = false;

	public void handleXArchChangeSetEvent(XArchChangeSetEvent evt) {
		if (evt.getEventType() == ChangeSetEventType.UPDATED_APPLIED_CHANGE_SETS) {
			updateApplied();
		}
		if (evt.getEventType() == ChangeSetEventType.UPDATED_ACTIVE_CHANGE_SET) {
			lableProviderNeedsUpdate = true;
			SWTWidgetUtils.async(viewer, new Runnable() {

				public void run() {
					if (lableProviderNeedsUpdate) {
						lableProviderNeedsUpdate = true;
						fireLabelProviderChanged(new LabelProviderChangedEvent(ChangeSetLabelProvider.this));
					}
				}
			});
		}
	}

	public void handleChangeSetStateEvent(ChangeSetStateEvent event) {
		updateApplied();
	}

	public void handleExplicitEvent(ExplicitADTEvent evt) {
		if (evt.getEventType() == ExplicitEventType.UPDATED_EXPLICIT_CHANGE_SETS) {
			lableProviderNeedsUpdate = true;
			SWTWidgetUtils.async(viewer, new Runnable() {

				public void run() {
					if (lableProviderNeedsUpdate) {
						lableProviderNeedsUpdate = false;
						fireLabelProviderChanged(new LabelProviderChangedEvent(ChangeSetLabelProvider.this));
					}
				}
			});
		}
	}

	public void setChangeColors(ObjRef objRef) {
		changeSetRefToColor.clear();
		if (objRef != null) {
			ObjRef xArchRef = xarch.getXArch(objRef);
			if (objRef != null && xarch.getChangeSetsEnabled(xArchRef)) {
				ObjRef[] changeSetRefs = XArchChangeSetUtils.getOrderedChangeSets(xarch, xArchRef, "appliedChangeSets", false).toArray(new ObjRef[0]);
				ChangeStatus[] changeStatuses = xarch.getAllChangeStatus(objRef, changeSetRefs);
				Display display = PlatformUI.getWorkbench().getDisplay();
				for (int i = 0; i < changeSetRefs.length; i++) {
					ObjRef changeSetRef = changeSetRefs[i];
					ChangeStatus changeStatus = changeStatuses[i];
					Color color = null;
					if (changeStatus != null) {
						switch (changeStatus) {
						case ADDED:
							color = display.getSystemColor(SWT.COLOR_DARK_CYAN);
							break;
						case MODIFIED:
							color = display.getSystemColor(SWT.COLOR_MAGENTA);
							break;
						case REMOVED:
							color = display.getSystemColor(SWT.COLOR_RED);
							break;
						default:
							color = display.getSystemColor(SWT.COLOR_GRAY);
							break;
						}
					}
					changeSetRefToColor.put(changeSetRef, color);
				}
			}
		}
		fireLabelProviderChanged(new LabelProviderChangedEvent(ChangeSetLabelProvider.this));
	}

	private Map<ObjRef, Color> changeSetRefToColor = new HashMap<ObjRef, Color>();

	public Color getBackground(Object element) {
//		ObjRef changeSetRef = (ObjRef) element;
//		return changeSetRefToColor.get(changeSetRef) == null ? 
//				null : PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WHITE);
		return null;
	}

	public Color getForeground(Object element) { 
		ObjRef changeSetRef = (ObjRef) element;
		return changeSetRefToColor.get(changeSetRef);
	}

	public Font getFont(Object element) {
		ObjRef xArchRef = (ObjRef)viewer.getInput();
		ObjRef changeSetRef = (ObjRef) element;
		if(xArchRef != null && changeSetRef != null){
			if(changeSetRef.equals(xarch.getActiveChangeSetRef(xArchRef))){
				if(activeChangeSetFont == null){
					Display d = PlatformUI.getWorkbench().getDisplay();
					FontData[] fontDatas = d.getSystemFont().getFontData();
					if(fontDatas != null && fontDatas.length > 0){
						for(FontData f : fontDatas){
							f.setStyle(f.getStyle() | SWT.BOLD);
						}
						activeChangeSetFont = new Font(d, fontDatas);
					}
				}
				return activeChangeSetFont;
			}
		}
	    return null;
	}
}
