package edu.uci.isr.archstudio4.comp.eclipsecontentstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import edu.uci.isr.myx.fw.AbstractMyxSimpleBrick;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.sysutils.SystemUtils;
import edu.uci.isr.xarchflat.IContentStore;

public class EclipseContentStoreMyxComponent extends AbstractMyxSimpleBrick {

	public static final IMyxName INTERFACE_CONTENTSTORE = MyxUtils.createName("contentstore");

	IContentStore contentStore = new IContentStore() {

		public boolean canProcessURI(String uriString) {
			try {
				URI uri = new URI(uriString);
				return "platform".equalsIgnoreCase(uri.getScheme());
			}
			catch (Exception e) {
			}
			return false;
		};

		public byte[] retrieveContents(String uriString) throws java.io.IOException {
			try {
				return SystemUtils.blt(new URI(uriString).toURL().openConnection().getInputStream());
			}
			catch (IOException e) {
				throw e;
			}
			catch (Throwable t) {
				throw new IOException(t.getMessage());
			}
		};

		public void storeContents(String uriString, byte[] contents) throws java.io.IOException {
			try {
				URI uri = new URI(uriString);

				// save the resource if it happens to be an eclipse-specific URI
				// see: http://lmap.blogspot.com/2008/03/platform-scheme-uri.html
				if ("platform".equalsIgnoreCase(uri.getScheme())) {
					if (uri.getSchemeSpecificPart().startsWith("/resource")) {
						IPath path = new Path(uri.getSchemeSpecificPart()).removeFirstSegments(1);
						IFile f = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
						if (!f.exists()) {
							f.create(new ByteArrayInputStream(contents), true, null);
						}
						else {
							f.setContents(new ByteArrayInputStream(contents), true, false, null);
						}
					}
				}

			}
			catch (Throwable t) {
				throw new IOException(t.getMessage());
			}
			throw new IOException("Cannot store " + uriString);
		};

		public void contentsStored(String uriString) {
			try {
				URI uri = new URI(uriString);

				// inform eclipse that the resource has changed
				for (IFile file : ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri)) {
					file.refreshLocal(IResource.DEPTH_ONE, null);
				}
			}
			catch (Throwable t) {
				// ignore it
			}
		}
	};

	public Object getServiceObject(IMyxName interfaceName) {
		if (interfaceName.equals(EclipseContentStoreMyxComponent.INTERFACE_CONTENTSTORE)) {
			return contentStore;
		}
		return null;
	}
}
