package edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAModelEvent;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNASynchronousModelListener;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.ThingEvent;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.logics.tracking.ThingPropertyTrackingLogic;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.NoSuchObjectException;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatEvent;
import edu.uci.isr.xarchflat.XArchFlatInterface;
import edu.uci.isr.xarchflat.XArchFlatListener;
import edu.uci.isr.xarchflat.XArchPath;
import edu.uci.isr.xarchflat.utils.IXArchRelativePathTrackerListener;
import edu.uci.isr.xarchflat.utils.XArchRelativePathTracker;

public abstract class SingleAssemblyXadlMappingLogic<T extends IAssembly>
	extends AbstractThingLogic
	implements IBNASynchronousModelListener, XArchFlatListener, IXArchRelativePathTrackerListener{

	protected final static String OBJREF_PROPERTY_NAME = "objRef";

	protected final XArchFlatInterface xarch;
	protected final ObjRef xArchRef;
	protected final XArchRelativePathTracker pathTracker;

	protected final ThingPropertyTrackingLogic tptl;
	protected final Class<T> assemblyClass;
	protected final IThing parentThing;
	protected final Object kind;

	private int updatingAssembly = 0;

	public SingleAssemblyXadlMappingLogic(XArchFlatInterface xarch, ObjRef rootObjRef, String relativePath, ThingPropertyTrackingLogic tptl, Class<T> assemblyClass, IThing parentThing, Object kind){
		this.xarch = xarch;
		this.xArchRef = xarch.getXArch(rootObjRef);
		this.pathTracker = new XArchRelativePathTracker(xarch, rootObjRef, relativePath, false);
		this.tptl = tptl;
		this.assemblyClass = assemblyClass;
		this.parentThing = parentThing;
		this.kind = kind;
		pathTracker.addTrackerListener(this);
	}

	@Override
	public void init(){
		super.init();
		pathTracker.startScanning();
	}

	@Override
	public void destroy(){
		pathTracker.stopScanning();
		super.destroy();
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		if(bnaWorld != null){
			pathTracker.handleXArchFlatEvent(evt);
		}
	}

	public void processAdd(ObjRef objRef, ObjRef[] relativeAncestorRefs){
		IBNAModel model = getBNAModel();
		if(model != null){
			updatingAssembly++;
			try{
				T assembly = addAssembly(model, objRef, relativeAncestorRefs);
				updateAssembly(model, objRef, relativeAncestorRefs, assembly, null, null);
				assembly.getRootThing().setProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME, XadlUtils.getID(xarch, objRef));
				assembly.getRootThing().setProperty(OBJREF_PROPERTY_NAME, objRef);
			}
			finally{
				updatingAssembly--;
			}
		}
	}

	protected abstract T addAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs);

	@SuppressWarnings("unchecked")
	public void processUpdate(ObjRef objRef, ObjRef[] relativeAncestorRefs, XArchFlatEvent evt, XArchPath relativeSourceTargetPath){
		IBNAModel model = getBNAModel();
		if(model != null){
			IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(tptl.getThings(OBJREF_PROPERTY_NAME, objRef), kind);
			if(assemblyClass.isInstance(assembly)){
				updatingAssembly++;
				try{
					updateAssembly(model, objRef, relativeAncestorRefs, (T)assembly, evt, relativeSourceTargetPath);
				}
				catch(NoSuchObjectException e){
					pathTracker.stopScanning();
				}
				finally{
					updatingAssembly--;
				}
			}
		}
	}

	protected abstract void updateAssembly(IBNAModel model, ObjRef objRef, ObjRef[] relativeAncestorRefs, T assembly, XArchFlatEvent evt, XArchPath relativeSourceTargetPath);

	public void processRemove(ObjRef objRef, ObjRef[] relativeAncestorRefs){
		IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(tptl.getThings(OBJREF_PROPERTY_NAME, objRef), kind);
		if(assemblyClass.isInstance(assembly)){
			updatingAssembly++;
			try{
				assembly.remove(true);
			}
			finally{
				updatingAssembly--;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void bnaModelChangedSync(BNAModelEvent evt){
		if(updatingAssembly == 0){
			switch(evt.getEventType()){
			case THING_CHANGED:
				IThing thing = evt.getTargetThing();
				ThingEvent te = evt.getThingEvent();
				Object propertyName = te.getPropertyName();
				IAssembly assembly = AssemblyUtils.getAssemblyWithPart(thing);
				if(assemblyClass.isInstance(assembly) && kind.equals(assembly.getKind())){
					ObjRef objRef = assembly.getRootThing().getProperty(OBJREF_PROPERTY_NAME);
					if(objRef != null){
						try{
							storeAssemblyData(evt.getSource(), objRef, (T)assembly, evt, new BNAPath(thing, propertyName));
						}
						catch(NoSuchObjectException e){
							pathTracker.stopScanning();
						}
					}
				}
			}
		}
	}

	protected abstract void storeAssemblyData(IBNAModel model, ObjRef objRef, T assembly, BNAModelEvent evt, BNAPath relativeBNAPath);
}
