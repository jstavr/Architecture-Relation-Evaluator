package edu.uci.isr.archstudio4.comp.aimlauncher;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import edu.uci.isr.archstudio4.comp.aim.IAIM;
import edu.uci.isr.archstudio4.comp.resources.IResources;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioEditor;
import edu.uci.isr.archstudio4.editors.AbstractArchstudioOutlinePage;
import edu.uci.isr.ljm.LJMProxyFactory;
import edu.uci.isr.ljm.LJMServer;
import edu.uci.isr.myx.fw.IMyxRuntime;
import edu.uci.isr.myx.fw.IMyxRuntimeRegistry;
import edu.uci.isr.widgets.swt.SWTWidgetUtils;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.IXArchPropertyMetadata;
import edu.uci.isr.xarchflat.IXArchTypeMetadata;
import edu.uci.isr.xarchflat.ObjRef;

public class AIMLauncherEditor extends AbstractArchstudioEditor
	implements IMyxRuntimeRegistry{

	class ViewContentProvider
		implements IStructuredContentProvider{
		public ViewContentProvider(){
		}

		public Object[] getElements(Object inputElement){
			List<String[]> l = new ArrayList<String[]>();
			IXArchTypeMetadata typeMetadata = xarch.getTypeMetadata((ObjRef)inputElement);
			for(IXArchPropertyMetadata propMetadata: typeMetadata.getProperties()){
				if(propMetadata.getMetadataType() == IXArchPropertyMetadata.ATTRIBUTE){
					l.add(new String[]{propMetadata.getName(), (String)xarch.get((ObjRef)inputElement, propMetadata.getName())});
				}
			}
			return l.toArray();
		}

		public void dispose(){
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		}
	}

	class ViewLabelProvider extends LabelProvider
		implements ITableLabelProvider{
		public String getColumnText(Object obj, int index){
			return ((String[])obj)[index];
		}

		public Image getColumnImage(Object obj, int index){
			return null;
		}

		public Image getImage(Object obj){
			return null;
		}
	}

	class ViewCellModifier
		implements ICellModifier{
		protected ObjRef ref;

		public ViewCellModifier(ObjRef ref){
			this.ref = ref;
		}

		public boolean canModify(Object element, String property){
			return true;
		}

		public Object getValue(Object element, String property){
			if(element instanceof String[]){
				String[] elts = (String[])element;
				if(elts == null)
					return "";
				if(elts[1] == null)
					return "";
				return elts[1].toString();
			}
			return null;
		}

		public void modify(Object element, String property, Object value){
			// SWT bug workaround
			if(element instanceof Item){
				element = ((Item)element).getData();
			}
			if(element instanceof String[]){
				String[] elts = (String[])element;
				String propertyName = elts[0].toString();

				String oldValue = null;
				if(elts[1] != null)
					oldValue = elts[1].toString();

				String newValue = null;
				if(value != null)
					newValue = value.toString();

				if((oldValue == null) && (newValue == null)){
					// Do nothing
				}
				else if((oldValue != null) && (newValue == null)){
					xarch.clear(ref, propertyName);
				}
				else if((oldValue == null) && (newValue != null)){
					xarch.set(ref, propertyName, newValue);
				}
				else{
					// Both non-null:
					if(!oldValue.equals(newValue)){
						xarch.set(ref, propertyName, newValue);
					}
				}
			}
		}
	}

	long launchCount = 0;

	IAIM aim = null;

	LJMServer server;

	Map<String, LaunchData> launchDatas = Collections.synchronizedMap(new HashMap<String, LaunchData>());

	private class LaunchData{
		String name;
		ObjRef xArchRef = null;
		ObjRef structureRef = null;
		ILaunch launch = null;
		IMyxRuntime runtime = null;
	}

	public AIMLauncherEditor(){
		super(AIMLauncherMyxComponent.class, AIMLauncherMyxComponent.EDITOR_NAME);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
		super.init(site, input);

		setBannerInfo(((AIMLauncherMyxComponent)comp).getIcon(), "Architecture Instantiation Manager");
		setHasBanner(true);

		aim = ((AIMLauncherMyxComponent)comp).getAIM();

		try{
			server = new LJMServer();
			server.deploy("AIMLauncher", this);
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void dispose(){
		server.destroy();
		super.dispose();
	}

	protected AbstractArchstudioOutlinePage createOutlinePage(){
		return new AIMLauncherOutlinePage(xarch, xArchRef, resources);
	}

	public void createEditorContents(final Composite parent){

		parent.setLayout(new GridLayout(1, true));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ObjRef[] selectedRefs = null;
		if(outlinePage != null){
			selectedRefs = ((AIMLauncherOutlinePage)outlinePage).getSelectedRefs();
		}

		if((selectedRefs == null) || (selectedRefs.length == 0)){
			Label lNothingSelected = new Label(parent, SWT.LEFT);
			lNothingSelected.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			lNothingSelected.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
			lNothingSelected.setText("Select one or more elements in the outline view.");
		}
		else{
			ObjRef selectedRef = selectedRefs[0];
			if((selectedRefs.length == 1) && (xarch.getParent(selectedRef) == null)){
				// It's an xArch element
				Label lNothingSelected = new Label(parent, SWT.LEFT);
				lNothingSelected.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				lNothingSelected.setFont(resources.getPlatformFont(IResources.PLATFORM_DEFAULT_FONT_ID));
				lNothingSelected.setText("Select one or more structures in the outline view.");
			}
			else{
				for(int i = 0; i < selectedRefs.length; i++){
					final ObjRef structureRef = selectedRefs[i];

					Label lElement = new Label(parent, SWT.LEFT);
					lElement.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					lElement.setFont(resources.getPlatformFont(IResources.PLATFORM_HEADER_FONT_ID));
					lElement.setText(XadlUtils.getDescription(xarch, structureRef));

					final Button bInstantiate = new Button(parent, SWT.NONE);
					bInstantiate.setImage(resources.getImage(AIMLauncherMyxComponent.IMAGE_AIMLAUNCHER_GO_ICON));
					bInstantiate.setText("Instantiate");
					bInstantiate.addSelectionListener(new SelectionAdapter(){
						@Override
						public void widgetSelected(SelectionEvent e){
							bInstantiate.setEnabled(false);
							bInstantiate.setText("Instantiating...");
							parent.layout(true, true);
							Thread t = new Thread(new Runnable(){
								public void run(){
									try{
										launchArchitecture(structureRef);
									}
									finally{
										SWTWidgetUtils.async(parent, new Runnable(){
											public void run(){
												bInstantiate.setEnabled(true);
												bInstantiate.setText("Instantiate");
												parent.layout(true, true);
											}
										});
									}
								}
							});
							t.start();
						}
					});

					break;
				}
			}
		}
	}

	protected void launchArchitecture(final ObjRef structureRef){
		// for an example for creating java launch configurations, see:
		// http://www.eclipse.org/articles/Article-Java-launch/launching-java.html

		try{
			final LaunchData launchData = new LaunchData();
			launchData.name = "Launch:" + (++launchCount);
			launchData.xArchRef = xarch.getXArch(structureRef);
			launchData.structureRef = structureRef;
			launchDatas.put(launchData.name, launchData);

			URI xArchURI = URI.create(xarch.getXArchURI(launchData.xArchRef));
			IProject xArchProject = null;
			for(IContainer container : ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(xArchURI))
			{
				xArchProject = container.getProject();
				break;
			}
			if(xArchProject == null){
				throw new RuntimeException("XArch document must be located within a project. "+xArchURI);
			}

			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType jType = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
			ILaunchConfigurationWorkingCopy workingConfig = jType.newInstance(null, "AIM Launcher");

			workingConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, xArchProject.getName());
			workingConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "edu.uci.isr.myx.fw.MyxRemoteRuntime");
			workingConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "-registryHost localhost -registryPort " + server.getPort() + " -registryName AIMLauncher -runtimeHost localhost -runtimeName \"" + launchData.name + "\"");

			launchData.launch = workingConfig.launch(ILaunchManager.DEBUG_MODE, null, true, true);

			Thread t = new Thread(new Runnable(){
				public void run(){
					while(true){
						try{
							if(launchData.launch.isTerminated())
								break;
							Thread.sleep(1000);
						}
						catch(InterruptedException e){
						}
					}
					launchDatas.remove(launchData.name);
				}
			});
			t.setDaemon(true);
			t.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void addLJMRuntime(String name, String host, int port){
		addRuntime(name, (IMyxRuntime)LJMProxyFactory.createProxy(host, port, name, new Class[]{IMyxRuntime.class}));
	}

	public void addRuntime(String name, IMyxRuntime runtime){
		final LaunchData launchData = launchDatas.get(name);
		if(launchData != null && launchData.runtime == null){
			try{
				launchData.runtime = runtime;
				aim.instantiate(launchData.runtime, name, xarch.getXArch(launchData.structureRef), launchData.structureRef);
//				aim.begin(launchData.runtime, name);
			}
			catch(Exception e){
				e.printStackTrace();
				try{
					launchData.launch.terminate();
				}
				catch(Exception e2){
					e2.printStackTrace();
				}
			}
		}
	}

	public void removeRuntime(String name){
		final LaunchData launchData = launchDatas.get(name);
		if(launchData != null && launchData.runtime != null){
			try{
//				aim.end(launchData.runtime, name);
				aim.destroy(launchData.runtime, name);
			}
			catch(Exception e){
				e.printStackTrace();
				try{
					launchData.launch.terminate();
				}
				catch(Exception e2){
					e2.printStackTrace();
				}
			}
		}
	}
}
