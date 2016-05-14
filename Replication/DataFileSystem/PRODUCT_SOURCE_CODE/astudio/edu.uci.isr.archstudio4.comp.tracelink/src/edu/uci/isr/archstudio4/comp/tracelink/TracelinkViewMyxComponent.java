package edu.uci.isr.archstudio4.comp.tracelink;

import java.util.Collection;

import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.addtracelinks.IManualLinkView;
import edu.uci.isr.archstudio4.comp.tracelink.addtracelinks.IRecordLinkView;
import edu.uci.isr.archstudio4.comp.tracelink.addtracelinks.IRecoverLinkView;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.ITracelinkController;
import edu.uci.isr.archstudio4.comp.tracelink.hypermedia.IHypermediaAdapter;
import edu.uci.isr.archstudio4.comp.tracelink.models.ISelectionModel;
import edu.uci.isr.archstudio4.comp.tracelink.preferences.IPreferencesView;
import edu.uci.isr.archstudio4.comp.tracelink.publishextract.IPublishExtractLinkView;
import edu.uci.isr.archstudio4.comp.tracelink.reports.IReportView;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xarch.IXArch;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.proxy.XArchFlatProxyUtils;

public class TracelinkViewMyxComponent
	extends AbstractMyxSimpleBrick{

	//implements XArchFileListener, XArchFlatListener{

	MyxRegistry myxr = MyxRegistry.getSharedInstance();

	private IManualLinkView manualLinkView;
	private IRecordLinkView recordLinkView;
	private IRecoverLinkView recoverLinkView;
	private IPublishExtractLinkView peLinkView;
	private IReportView reportView;
	private IPreferencesView prefView;
	private IHypermediaAdapter hAdapter;
	protected ITracelinkController controller;

	//private XArchFlatInterface xarch;
	//private TraceLinkImpl tracelink;

	/*
	 * public static final IMyxName INTERFACE_NAME_OUT_XARCH =
	 * MyxUtils.createName("xarchcs"); // the following interfaces are currently
	 * not being used public static final IMyxName INTERFACE_NAME_IN_FILEEVENTS =
	 * MyxUtils.createName("xarchfileevents"); public static final IMyxName
	 * INTERFACE_NAME_IN_FLATEVENTS = MyxUtils.createName("xarchflatevents");
	 * public static final IMyxName INTERFACE_NAME_OUT_TRACELINK =
	 * MyxUtils.createName("tracelink");
	 */

	public static final IMyxName INTERFACE_NAME_OUT_INVOKEMANUALVIEW = MyxUtils.createName("invokemanualview");
	public static final IMyxName INTERFACE_NAME_OUT_INVOKERECORDVIEW = MyxUtils.createName("invokerecordview");
	public static final IMyxName INTERFACE_NAME_OUT_INVOKERECOVERVIEW = MyxUtils.createName("invokerecoverview");
	public static final IMyxName INTERFACE_NAME_OUT_INVOKEPUBLISHEXTRACTVIEW = MyxUtils.createName("invokepublishextractview");
	public static final IMyxName INTERFACE_NAME_OUT_INVOKEREPORTVIEW = MyxUtils.createName("invokereportview");
	public static final IMyxName INTERFACE_NAME_OUT_INVOKEPREFVIEW = MyxUtils.createName("invokeprefview");
	public static final IMyxName INTERFACE_NAME_OUT_INVOKEHADAPTER = MyxUtils.createName("invokehadapter");
	public static final IMyxName INTERFACE_NAME_OUT_UPDATEVIEWS = MyxUtils.createName("updateviews");

	
	public void begin(){
		//xarch = (XArchFlatInterface) MyxUtils.getFirstRequiredServiceObject(
		//		this, INTERFACE_NAME_OUT_XARCH);

		manualLinkView = (IManualLinkView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKEMANUALVIEW);
		recordLinkView = (IRecordLinkView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKERECORDVIEW);
		recoverLinkView = (IRecoverLinkView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKERECOVERVIEW);
		peLinkView = (IPublishExtractLinkView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKEPUBLISHEXTRACTVIEW);
		reportView = (IReportView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKEREPORTVIEW);
		prefView = (IPreferencesView)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKEPREFVIEW);
		hAdapter = (IHypermediaAdapter)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_INVOKEHADAPTER);
		controller = (ITracelinkController)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_UPDATEVIEWS);

		myxr.register(this);

		System.out.println("TraceLinkView*******************");
		System.out.println("connected to : " + manualLinkView.toString());
		System.out.println("connected to : " + recordLinkView.toString());
		System.out.println("connected to : " + recoverLinkView.toString());
		System.out.println("connected to : " + peLinkView.toString());
		System.out.println("connected to : " + reportView.toString());
		System.out.println("connected to : " + prefView.toString());
		System.out.println("connected to : " + hAdapter.toString());
		System.out.println("connected to : " + controller.toString());
		//manualLinkView.invokeManualView();		
		//recordLinkView.invokeRecordView();
		//recoverLinkView.invokeRecoverView();
		peLinkView.invokePublishExtractView(null);
		//reportView.invokeReportView();
		prefView.invokePrefView(null);
		//hAdapter.invokeHAdapter();

	}

	// This component is at the very bottom and no interfaces are going in.
	// Thus, always return null.
	public Object getServiceObject(IMyxName interfaceName){
		return null;
	}

	/*
	 * // public void handleXArchFileEvent(XArchFileEvent evt){
	 * for(Object o: myxr.getObjects(this).clone()){ if(o instanceof
	 * XArchFileListener){ ((XArchFileListener)o).handleXArchFileEvent(evt); } } }
	 * // public void handleXArchFlatEvent(XArchFlatEvent evt){
	 * for(Object o: myxr.getObjects(this).clone()){ if(o instanceof
	 * XArchFlatListener){ ((XArchFlatListener)o).handleXArchFlatEvent(evt); } } }
	 * public XArchFlatInterface getXArchFlatInterface() { return xarch; }
	 */

	//H: 8/7/08
	public void invokeRecordView(Shell shell, Collection<ISelectionModel> selectionList, String archSelectedID){
		recordLinkView.invokeRecordView(shell, selectionList, archSelectedID);
	}

	//H: 5/27/08
	public void invokeRecoverView(Shell shell, String archSelectedID){
		recoverLinkView.invokeRecoverView(shell, archSelectedID);
	}

	//H: 5/31/08
	public IHypermediaAdapter getHAdapter(){
		return hAdapter;
	}

	//H: 5/31/08
	public void invokeManualView(Shell shell, String archSelectedID){
		manualLinkView.invokeManualView(shell, archSelectedID);
	}

	public void invokeReportView(Shell shell, String[] attributeNames){
		reportView.invokeReportView(shell, attributeNames);
	}

	//H: 7/21/08
	public IRecordLinkView getRecordView(){
		return recordLinkView;
	}

	public ITracelinkController getController(){
		return controller;
	}

	public static void main(String[] args){
		XArchFlatInterface xarch = null;
		ObjRef xArchRef = null;
		IXArch xArch = XArchFlatProxyUtils.proxy(xarch, xArchRef);
	}

}
