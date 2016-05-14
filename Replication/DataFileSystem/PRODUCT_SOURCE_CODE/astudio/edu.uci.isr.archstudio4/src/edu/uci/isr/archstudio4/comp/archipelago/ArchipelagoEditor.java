package edu.uci.isr.archstudio4.comp.archipelago;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import edu.uci.isr.archstudio4.comp.booleannotation.IBooleanNotation;
import edu.uci.isr.archstudio4.comp.copypaste.ICopyPasteManager;
import edu.uci.isr.archstudio4.comp.guardtracker.IGuardTracker;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.comp.selector.ISelector;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.ExplicitADTEvent;
import edu.uci.isr.archstudio4.comp.xarchcs.explicitadt.IExplicitADT;
import edu.uci.isr.archstudio4.comp.xarchcs.xarchcs.XArchChangeSetInterface;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditor;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.archstudio4.graphlayout.IGraphLayout;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFileEvent;
import edu.uci.isr.xarchflat.XArchFlatEvent;

public class ArchipelagoEditor
	extends AbstractArchstudioEditor{

	protected IPreferenceStore prefs = null;
	protected IGraphLayout graphLayout = null;
	protected IBooleanNotation bni = null;
	protected IGuardTracker guardTracker = null;
	protected ISelector selector = null;
	protected XArchChangeSetInterface xarchcs = null;
	protected IExplicitADT explicit = null;
	protected ICopyPasteManager copyPasteManager = null;

	public ArchipelagoEditor(){
		super(ArchipelagoMyxComponent.class, ArchipelagoMyxComponent.EDITOR_NAME);
		prefs = ((ArchipelagoMyxComponent)comp).getPreferences();
		graphLayout = ((ArchipelagoMyxComponent)comp).getGraphLayout();
		bni = ((ArchipelagoMyxComponent)comp).getBooleanNotation();
		guardTracker = ((ArchipelagoMyxComponent)comp).getGuardTracker();
		selector = ((ArchipelagoMyxComponent)comp).getSelector();
		xarchcs = (XArchChangeSetInterface)((ArchipelagoMyxComponent)comp).getXArchADT();
		explicit = ((ArchipelagoMyxComponent)comp).getExplicit();
		copyPasteManager = ((ArchipelagoMyxComponent)comp).getCopyPasteManager();
		//ArchlightUtils.initResources(resources);

		setBannerInfo(((ArchipelagoMyxComponent)comp).getIcon(), "Graphical Architecture Editor");
		setHasBanner(true);
		setUpdateOnSelectionChange(false);
		setUpdateEditorOnXArchFlatEvent(false);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
		super.init(site, input);

		setSite(site);
		setInput(input);
		//setupToolbar(site);
	}

	@Override
	protected AbstractArchstudioOutlinePage createOutlinePage(){
		return new ArchipelagoOutlinePage(this, xarch, getXArchRef(), resources, fileman, prefs, graphLayout, bni, guardTracker, selector, xarchcs, explicit, this.getSite());
	}

	/*
	 * protected void setupToolbar(IEditorSite site){ IActionBars bars =
	 * site.getActionBars(); IToolBarManager manager = bars.getToolBarManager();
	 * IAction[] actions = getToolbarActions(); for(int i = 0; i <
	 * actions.length; i++){ manager.add(actions[i]); } }
	 */

	@Override
	public void createEditorContents(Composite c){
		//System.out.println("create editor contents");
		Label l = new Label(c, SWT.NONE);
		l.setText("Double-click a node in the outline view to begin.");
		l.setFont(resources.getFont(IResources.PLATFORM_HEADER_FONT_ID));
		l.setBackground(c.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}

	@Override
	public void setFocus(){
		// parent.getChildren()[0].setFocus();
	}

	@Override
	public void doHandleXArchFlatEvent(XArchFlatEvent evt){
		if(outlinePage != null){
			((ArchipelagoOutlinePage)outlinePage).handleXArchFlatEvent(evt);
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		if(outlinePage != null){
			((ArchipelagoOutlinePage)outlinePage).handleXArchFileEvent(evt);
		}
	}

	@Override
	public void handleExplicitEvent(ExplicitADTEvent evt){
		if(outlinePage != null){
			((ArchipelagoOutlinePage)outlinePage).handleExplicitEvent(evt);
		}
	}
	
	@Override
	public void fileDirtyStateChanged(ObjRef xArchRef, boolean dirty){
		super.fileDirtyStateChanged(xArchRef, dirty);
		if(outlinePage != null){
			((ArchipelagoOutlinePage)outlinePage).fileDirtyStateChanged(xArchRef, dirty);
		}
	}

	@Override
	public void fileSaving(ObjRef xArchRef, IProgressMonitor monitor){
		super.fileSaving(xArchRef, monitor);
		if(outlinePage != null){
			((ArchipelagoOutlinePage)outlinePage).fileSaving(xArchRef, monitor);
		}
	}

	public XArchChangeSetInterface getXarchCS(){
		return xarchcs;
	}
}
