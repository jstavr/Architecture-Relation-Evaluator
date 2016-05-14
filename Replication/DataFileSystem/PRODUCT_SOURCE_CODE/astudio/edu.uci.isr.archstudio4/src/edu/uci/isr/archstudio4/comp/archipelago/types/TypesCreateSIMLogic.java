package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchActionConstants;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.bna4.AbstractThingLogic;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IBNAMenuListener;
import edu.uci.isr.bna4.IBNAMouseListener;
import edu.uci.isr.bna4.IBNAMouseMoveListener;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.facets.IHasWorld;
import edu.uci.isr.bna4.things.glass.EndpointGlassThing;
import edu.uci.isr.bna4.things.shapes.SplineThing;
import edu.uci.isr.bna4.things.utility.WorldThing;
import edu.uci.isr.bna4.things.utility.WorldThingPeer;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xarchflat.ObjRef;

public class TypesCreateSIMLogic extends AbstractThingLogic implements IBNAMenuListener, IBNAMouseListener, IBNAMouseMoveListener{
	protected ArchipelagoServices AS = null;
	protected ObjRef xArchRef = null;
	
	protected EndpointAssembly outerSignatureAssembly = null;
	protected ObjRef outerSignatureRef = null;
	protected SplineThing indicatorSpline = null;
	
	public TypesCreateSIMLogic(ArchipelagoServices services, ObjRef xArchRef){
		this.AS = services;
		this.xArchRef = xArchRef;
	}
	
	public boolean matches(IBNAView view, IThing t){
		if(t instanceof EndpointGlassThing){
			IThing pt = view.getWorld().getBNAModel().getParentThing(t);
			if(pt != null){
				return TypesMapper.isSignatureAssemblyRootThing(pt);
			}
		}
		return false;
	}
	
	public String getXArchID(IBNAView view, IThing t){
		if(t instanceof EndpointGlassThing){
			IThing parentThing = view.getWorld().getBNAModel().getParentThing(t);
			return parentThing.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
		}
		return null;
	}
	
	public void fillMenu(IBNAView view, IMenuManager m, int localX, int localY, IThing t, int worldX, int worldY){
		if(matches(view, t)){
			for(IAction action : getActions(view, t, worldX, worldY)){
				m.add(action);
			}
			m.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
	}
	
	protected IAction[] getActions(IBNAView view, IThing t, int worldX, int worldY){
		final IBNAView fview = view;
		final IThing ft = t;
		final int fworldX = worldX;
		final int fworldY = worldY;
		
		final String eltXArchID = getXArchID(view, t);
		if(eltXArchID == null){
			return new IAction[0];
		}
		
		ObjRef eltRef = AS.xarch.getByID(xArchRef, eltXArchID);
		if(eltRef == null){
			return new IAction[0];
		}
		
		ObjRef brickTypeRef = AS.xarch.getParent(eltRef);
		if(brickTypeRef == null){
			return new IAction[0];
		}
		
		ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(brickTypeRef, "subArchitecture");
		if(subArchitectureRef == null){
			return new IAction[0];
		}
		
		ObjRef archStructureRef = XadlUtils.resolveXLink(AS.xarch, subArchitectureRef, "archStructure");
		if(archStructureRef == null){
			return new IAction[0];
		}
		
		outerSignatureRef = eltRef;
		
		Action action = new Action("New Signature-Interface Mapping..."){
			public void run(){
				Point p1 = BNAUtils.getCentralPoint(ft);
				if(p1 == null) p1 = new Point(fworldX, fworldY);
				
				indicatorSpline = new SplineThing();
				indicatorSpline.setEndpoint1(p1);
				indicatorSpline.setEndpoint2(p1);
				indicatorSpline.setLineWidth(2);
				indicatorSpline.setColor(new RGB(0,0,255));
				
				fview.getWorld().getBNAModel().addThing(indicatorSpline, ft);
			}
		};
		
		return new IAction[]{action};
	}
	
	protected Point endpoint2 = new Point(0,0);
	public void mouseMove(IBNAView view, MouseEvent e, IThing t, int worldX, int worldY){
		if(indicatorSpline != null){
			endpoint2.x = worldX;
			endpoint2.y = worldY;
			indicatorSpline.setEndpoint2(endpoint2);
		}
	}
	
	public void mouseDown(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
		if(indicatorSpline != null){
			if(evt.button == 1){
				if((t instanceof IHasWorld) && (outerSignatureRef != null)){
					ObjRef brickTypeRef = AS.xarch.getParent(outerSignatureRef);
					if(brickTypeRef != null){
						String brickTypeID = XadlUtils.getID(AS.xarch, brickTypeRef);
						if(brickTypeID != null){
							IThing brickAssemblyRootThing = ArchipelagoUtils.findThing(view.getWorld().getBNAModel(), brickTypeID);
							IAssembly assembly = AssemblyUtils.getAssemblyWithRoot(brickAssemblyRootThing);
							if(assembly instanceof BoxAssembly){
								BoxAssembly brickAssembly = (BoxAssembly)assembly;
								WorldThing vt = brickAssembly.getWorldThing();
								//See if we clicked somewhere on the view thing that is in the box containing the endpoint
								if((vt != null) && (vt.equals(t))){
									WorldThingPeer vtp = (WorldThingPeer)view.getPeer(vt);
									if(vtp != null){
										IBNAView internalView = vtp.getInnerView();
										if(internalView != null){
											IThing clickedThing = internalView.getThingAt(evt.x, evt.y);
											if((clickedThing != null) && (clickedThing instanceof EndpointGlassThing)){
												IThing pt = internalView.getWorld().getBNAModel().getParentThing(clickedThing);
												if((pt != null) && (StructureMapper.isInterfaceAssemblyRootThing(pt))){
													String innerInterfaceID = pt.getProperty(ArchipelagoUtils.XARCH_ID_PROPERTY_NAME);
													if(innerInterfaceID != null){
														ObjRef innerInterfaceRef = AS.xarch.getByID(xArchRef, innerInterfaceID);
														if(innerInterfaceRef != null){
															String outerSignatureID = XadlUtils.getID(AS.xarch, outerSignatureRef);
															if(outerSignatureID != null){
																ObjRef subArchitectureRef = (ObjRef)AS.xarch.get(brickTypeRef, "subArchitecture");
																if(subArchitectureRef != null){
																	ObjRef typesContextRef = AS.xarch.createContext(xArchRef, "types");
																	ObjRef simRef = AS.xarch.create(typesContextRef, "signatureInterfaceMapping");
																	AS.xarch.set(simRef, "id", BNAUtils.generateUID("signatureInterfaceMapping"));
																	XadlUtils.setDescription(AS.xarch, simRef, "[New Signature-Interface Mapping]");
																	XadlUtils.setXLink(AS.xarch, simRef, "outerSignature", outerSignatureID);
																	XadlUtils.setXLink(AS.xarch, simRef, "innerInterface", innerInterfaceID);
																	AS.xarch.add(subArchitectureRef, "signatureInterfaceMapping", simRef);
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				outerSignatureAssembly = null;
				outerSignatureRef = null;
				view.getWorld().getBNAModel().removeThing(indicatorSpline);
			}
			else if(evt.button == 3){
				outerSignatureAssembly = null;
				outerSignatureRef = null;
				view.getWorld().getBNAModel().removeThing(indicatorSpline);
			}
		}
	}
	
	public void mouseUp(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}
	
	public void mouseClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}
	
	public void mouseDoubleClick(IBNAView view, MouseEvent evt, IThing t, int worldX, int worldY){
	}
}
