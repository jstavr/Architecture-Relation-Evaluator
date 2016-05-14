package edu.uci.isr.archstudio4.comp.archipelago.activitydiagrams.logic.util;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.logics.tracking.ModelBoundsTrackingLogic;
import edu.uci.isr.bna4.logics.util.ExportPPTLogic;
import edu.uci.isr.xarchflat.ObjRef;

import edu.uci.isr.xadlutils.XadlUtils;

public class ExportPPTLogic2 extends ExportPPTLogic {
	
	ArchipelagoServices AS;
	ObjRef activityDiagramRef;
	

	public ExportPPTLogic2(ModelBoundsTrackingLogic mbtl, ArchipelagoServices AS, ObjRef activityDiagramRef) {
		super(mbtl);
		this.AS = AS;
		this.activityDiagramRef = activityDiagramRef;
	}
	
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(t == null){
			final IBNAView fview = view;
			final IThing it = t;
			IAction saveAsPTTAction = new Action("Save as PTT..."){

				@Override
				public void run(){
					
					System.out.println("PPT2 Save action: Start");
					
					
					if (activityDiagramRef != null){
						//System.out.println("description =" +XadlUtils.getDescription(AS.xarch, activityDiagramRef));
						String title = XadlUtils.getDescription(AS.xarch, activityDiagramRef);
						saveAsPPT(fview, "ppt", "Microsoft PowerPoint 97-2003 Format (*.ppt)", title, SWT.IMAGE_JPEG);
					}else{
						//System.out.println("activityDiagramRef NULL!");
					}
					
					
					
					
					//saveAsPPT(fview, "ppt", "Microsoft PowerPoint 97-2003 Format (*.ppt)", SWT.IMAGE_JPEG);
					System.out.println("PPT2 Save action: End");
				}
			};
			m.add(saveAsPTTAction);

			
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}

}
