package edu.uci.isr.archstudio4.comp.xarchcs;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class XArchCSActivator extends AbstractUIPlugin{

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.uci.isr.archstudio4.comp.xarchcs";

	// The shared instance
	private static XArchCSActivator plugin;

	/**
	 * The constructor
	 */
	public XArchCSActivator(){
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception{
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static XArchCSActivator getDefault(){
		return plugin;
	}

	ImageRegistry dynamicImageRegistry = null;

	@Override
	public ImageRegistry getImageRegistry(){
		if(dynamicImageRegistry == null)
			dynamicImageRegistry = new ImageRegistry(){
				final ImageRegistry imageRegistry = XArchCSActivator.super.getImageRegistry();

				public void dispose(){
					imageRegistry.dispose();
				}

				public Image get(String key){
					Image i = imageRegistry.get(key);
					if(i == null){
						ImageDescriptor id = imageDescriptorFromPlugin(PLUGIN_ID, key);
						if(id != null){
							imageRegistry.put(key, id);
							i = imageRegistry.get(key);
						}
					}
					return i;
				}

				public ImageDescriptor getDescriptor(String key){
					ImageDescriptor id = imageRegistry.getDescriptor(key);
					if(id == null){
						id = imageDescriptorFromPlugin(PLUGIN_ID, key);
						if(id != null){
							imageRegistry.put(key, id);
						}
					}
					return id;
				}

				public void put(String key, Image image){
					imageRegistry.put(key, image);
				}

				public void put(String key, ImageDescriptor descriptor){
					imageRegistry.put(key, descriptor);
				}

				public void remove(String key){
					imageRegistry.remove(key);
				}

				public String toString(){
					return imageRegistry.toString();
				}
			};
		return dynamicImageRegistry;
	}
}
