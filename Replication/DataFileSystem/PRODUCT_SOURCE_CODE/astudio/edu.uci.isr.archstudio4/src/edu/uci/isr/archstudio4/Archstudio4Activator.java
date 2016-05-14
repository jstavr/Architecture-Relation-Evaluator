package edu.uci.isr.archstudio4;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class Archstudio4Activator extends AbstractUIPlugin {

	protected boolean appInited = false;
	
	//The shared instance.
	private static Archstudio4Activator plugin;
	
	/**
	 * The constructor.
	 */
	public Archstudio4Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		initApp();
		if(isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption("edu.uci.isr.archstudio4/lifecycle"))){
			System.err.println("ArchStudio plugin started");
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		if(isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption("edu.uci.isr.archstudio4/lifecycle"))){
			System.err.println("ArchStudio plugin stopped");
		}
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Archstudio4Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("edu.uci.isr.archstudio4", path);
	}
	
	public synchronized void initApp(){
		if(appInited){
			return;
		}
		edu.uci.isr.myx.fw.MyxClassLoaders.addClassLoader(getClass().getClassLoader());
		//Get the contents of the archstudio.xml file
		String contents = readXMLFile("edu/uci/isr/archstudio4/archstudio4.xml");
		Properties p = new Properties();
		p.setProperty("contents", contents);
		//System.err.println("Initting app");
		Bootstrap b = new Bootstrap(p);
		appInited = true;
	}

	public String readXMLFile(String resourceName){
		try{
			InputStream resourceInputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte buf[] = new byte[2048];
			while(true){
				int len = resourceInputStream.read(buf, 0, buf.length);
				if(len != -1){
					baos.write(buf, 0, len);
				}
				else{
					resourceInputStream.close();
					baos.flush();
					baos.close();
					return baos.toString();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	

}
