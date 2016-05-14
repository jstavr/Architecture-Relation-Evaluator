/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.publishextract;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import edu.uci.isr.archstudio4.comp.tracelink.analysis.ILinkDiff;
import edu.uci.isr.archstudio4.comp.tracelink.controllers.IXADLFacade;
import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxRegistry;
import edu.uci.isr.myx.fw.MyxUtils;

/**
 * @author Hazel
 */
public class PublishExtractLinkView
	extends AbstractMyxSimpleBrick
	implements IPublishExtractLinkView{

	public static final IMyxName INTERFACE_NAME_OUT_EDITTRACELINKS = MyxUtils.createName("edittracelinks");
	public static final IMyxName INTERFACE_NAME_OUT_GETDIFF = MyxUtils.createName("getdiff");
	public static final IMyxName INTERFACE_NAME_IN_INVOKEPUBLISHEXTRACTVIEW = MyxUtils.createName("invokepublishextractview");

	protected MyxRegistry myxr = MyxRegistry.getSharedInstance();
	//protected SimpleTracelinkController controller;
	protected ILinkDiff linkDiff;

	protected IXADLFacade xadlFacade;

	
	public void begin(){

		xadlFacade = (IXADLFacade)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_EDITTRACELINKS);

		linkDiff = (ILinkDiff)MyxUtils.getFirstRequiredServiceObject(this, INTERFACE_NAME_OUT_GETDIFF);

		//xADLFacade = new XADLFacade(controller.getXArchFlatInterface());

		myxr.register(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uci.isr.myx.fw.IMyxProvidedServiceProvider#getServiceObject(edu.uci.isr.myx.fw.IMyxName)
	 */
	public Object getServiceObject(IMyxName interfaceName){
		if(interfaceName.equals(INTERFACE_NAME_IN_INVOKEPUBLISHEXTRACTVIEW)){
			return this;
		}
		else{
			return null;
		}
	}

	public void invokePublishExtractView(Shell parent){
		System.out.println("PublishExtractLinkView*******************");
		System.out.println("connected to : " + xadlFacade.toString());
		System.out.println("connected to : " + linkDiff.toString());

		if(parent == null){
			return;
		}

		Shell shell = new Shell(parent);

		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Import ArchTraceLinks");
		fd.setFilterPath(System.getProperty("user.home"));
		String[] filterExt = {"*.xml", "*.*"};
		fd.setFilterExtensions(filterExt);

		String filename = fd.open();

		if(filename == null){
			System.err.println("No such file");
			return;
		}

		File file = new File(filename);
		if(!file.exists()){
			System.err.println("No such file");
			return;
		}

		/*
		 * try {
		 * //xadlFacade.setXArchRef(controller.getXArchFlatInterface().parseFromFile(filename));
		 * xadlFacade.setXArchRef(filename); //this may not work
		 * ITracelinkModel[] links =
		 * linkDiff.getDiff(xadlFacade.editTraceLinks(),
		 * xadlFacade.getTracelinks()); shell.setText ("Tracelink Import");
		 * shell.setSize (600, 400); shell.setLayout(new FillLayout());
		 * ExpandBar bar = new ExpandBar(shell, SWT.NONE); Composite view = new
		 * ExtractViewer(bar, xadlFacade, links); ExpandItem item = new
		 * ExpandItem(bar, SWT.NONE); item.setText("Link Diff");
		 * item.setHeight(view.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		 * item.setControl(view); item.setExpanded(true); shell.open (); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); } catch (SAXException e) {
		 * e.printStackTrace(); }
		 */
	}

}
