package edu.uci.isr.archstudio4;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.Bundle;

import edu.uci.isr.archstudio4.comp.aim.IAIM;
import edu.uci.isr.archstudio4.comp.aimeclipse.AIMEclipseImpl;
import edu.uci.isr.myx.fw.IMyxImplementation;
import edu.uci.isr.myx.fw.IMyxName;
import edu.uci.isr.myx.fw.IMyxRuntime;
import edu.uci.isr.myx.fw.MyxUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatImpl;

public class Bootstrap {

	public static final IMyxName XARCHADT_NAME = MyxUtils.createName("xArchADT");
	public static final IMyxName AIM_NAME = MyxUtils.createName("AIM");
	public static final IMyxName MYXRUNTIME_NAME = MyxUtils.createName("myxruntime");
	public static final IMyxName BOOTSTRAP_NAME = MyxUtils.createName("bootstrap");

	protected IMyxImplementation myximpl;

	public static void main(String[] args) {
		Bootstrap.printHeader();
		Properties p = Bootstrap.parseArgs(args);
		new Bootstrap(p);
	}

	private static void printHeader() {
		System.out.println();
		System.out.println("------------------------------------------------------------------------");
		System.out.println("ArchStudio 4 Bootstrapper");
		System.out.println("Copyright (C) 2006 The Regents of the University of California.");
		System.out.println("All rights reserved worldwide.");
		System.out.println();
	}

	private static Properties parseArgs(String[] args) {
		String xadlFile = null;
		String structureName = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-s")) {
				if (++i == args.length) {
					Bootstrap.argError();
				}
				if (structureName != null) {
					Bootstrap.argError();
				}
				structureName = args[i];
			}
			else {
				if (xadlFile != null) {
					Bootstrap.argError();
				}
				xadlFile = args[i];
			}
		}
		if (xadlFile == null) {
			Bootstrap.argError();
		}

		Properties p = new Properties();
		p.setProperty("file", xadlFile);
		if (structureName != null) {
			p.setProperty("structure", structureName);
		}
		return p;
	}

	private static void argError() {
		System.err.println();
		System.err.println("Argument error. Usage is:");
		System.err.println("  java " + Bootstrap.class.getName() + " file|URL [-s structureName]");
		System.err.println();
		System.err.println("  where:");
		System.err.println("    file: the name of the xADL file to bootstrap");
		System.err.println("    URL: the URL of the xADL file to bootstrap");
		System.err.println("    structureName: the name of the structure to bootstrap");
		System.err.println();
		System.exit(-2);
	}

	public Bootstrap(Properties props) {
		myximpl = MyxUtils.getDefaultImplementation();
		doBootstrap(props);
	}

	private void doBootstrap(Properties p) {
		// System.err.println("in dobootstrap");
		try {
			String xadlFileContents = p.getProperty("contents");
			String xadlURLFile = p.getProperty("file");
			String structureName = p.getProperty("structure");

			IMyxRuntime myx = MyxUtils.getDefaultImplementation().createRuntime();
			for (final Bundle bundle : Archstudio4Activator.getDefault().getBundle().getBundleContext().getBundles()) {
				myx.addClassLoader(new ClassLoader() {
					@Override
					protected URL findResource(String name) {
						return bundle.getResource(name);
					}

					@Override
					protected Class<?> findClass(String name) throws ClassNotFoundException {
						return bundle.loadClass(name);
					}

					@SuppressWarnings("unchecked")
					@Override
					protected Enumeration<URL> findResources(String name) throws IOException {
						return bundle.getResources(name);
					}
				});
			}

			XArchFlatImpl xarch = new XArchFlatImpl();
			IAIM aim = new AIMEclipseImpl(xarch);

			ObjRef xArchRef = null;
			try {
				if (xadlFileContents != null) {
					xArchRef = xarch.parseFromString("urn:arch" + Math.random(), xadlFileContents);
				}
			}
			catch (Exception e) {
			}
			if (xArchRef == null) {
				try {
					xArchRef = xarch.parseFromURL(xadlURLFile);
				}
				catch (Exception e) {
					xArchRef = xarch.parseFromFile(xadlURLFile);
				}
			}

			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef[] structureRefs = xarch.getAllElements(typesContextRef, "archStructure", xArchRef);

			if (structureRefs.length == 0) {
				throw new RuntimeException("Architecture has no structures to instantiate");
			}

			ObjRef structureRef = null;
			for (ObjRef structureRef2 : structureRefs) {
				String description = XadlUtils.getDescription(xarch, structureRef2);
				if (description != null && description.equals(structureName)) {
					structureRef = structureRef2;
					break;
				}
			}
			if (structureRef == null) {
				structureRef = structureRefs[0];
			}

			aim.instantiate(myx, "main", xArchRef, structureRef);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-3);
		}
	}

}
