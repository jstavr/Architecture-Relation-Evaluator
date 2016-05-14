package edu.uci.isr.archstudio4.comp.archipelago.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.draw2d.geometry.Point;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoUtils;
import edu.uci.isr.archstudio4.comp.archipelago.types.StructureMapper;
import edu.uci.isr.archstudio4.comp.archipelago.types.TypesMapper;
import edu.uci.isr.archstudio4.util.ArchstudioResources;
import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.FlyToUtils;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.IBNAView;
import edu.uci.isr.bna4.IBNAWorld;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly;
import edu.uci.isr.bna4.assemblies.IAssembly;
import edu.uci.isr.bna4.assemblies.SplineAssembly;
import edu.uci.isr.bna4.facets.IHasAssemblyData;
import edu.uci.isr.bna4.facets.IHasText;
import edu.uci.isr.bna4.logics.information.ToolTipLogic;
import edu.uci.isr.bna4.things.utility.WorldThing;
import edu.uci.isr.bna4.things.utility.WorldThingPeer;
import edu.uci.isr.widgets.swt.DefaultFindResult;
import edu.uci.isr.widgets.swt.IFindResult;
import edu.uci.isr.widgets.swt.IFinder;

public class ArchipelagoFinder
	implements IFinder<IBNAView>{

	protected ArchipelagoServices AS = null;

	public ArchipelagoFinder(ArchipelagoServices AS){
		this.AS = AS;
	}

	@SuppressWarnings("unchecked")
	public IFindResult[] find(IBNAView context, String search){
		List<IFindResult> resultList = new ArrayList<IFindResult>();
		find(context, context.getWorld().getBNAModel(), search, "", resultList);
		Collections.sort(resultList);
		return resultList.toArray(new IFindResult[resultList.size()]);
	}

	public void selected(IFindResult selectedResult){
		Object o = selectedResult.getData();
		if(o != null && o instanceof FindResultData){
			final FindResultData frd = (FindResultData)o;
			FlyToUtils.flyTo(frd.view, frd.p.x, frd.p.y);

			IThing t = frd.t;
			ArchipelagoUtils.pulseNotify(frd.view.getWorld().getBNAModel(), t);
		}
	}

	protected void find(IBNAView context, IBNAModel m, String search, String prefix, List<IFindResult> resultList){
		IThing[] things = m.getAllThings();
		for(IThing t: things){
			IFindResult r = null;
			if(t instanceof IHasAssemblyData){
				IAssembly a = ((IHasAssemblyData)t).getAssembly();
				if(a instanceof BoxAssembly && StructureMapper.isComponentAssemblyRootThing(t)){
					IHasText textThing = ((BoxAssembly)a).getBoxedLabelThing();
					if(textThing != null){
						if(matches(search, textThing.getText())){
							r = createFindResult(context, textThing, prefix, textThing.getText(), AS.resources.getImageDescriptor(ArchstudioResources.ICON_COMPONENT));
						}
					}
					find(context, (BoxAssembly)a, search, resultList);
				}
				else if(a instanceof BoxAssembly && StructureMapper.isConnectorAssemblyRootThing(t)){
					IHasText textThing = ((BoxAssembly)a).getBoxedLabelThing();
					if(textThing != null){
						if(matches(search, textThing.getText())){
							r = createFindResult(context, textThing, prefix, textThing.getText(), AS.resources.getImageDescriptor(ArchstudioResources.ICON_CONNECTOR));
						}
					}
					find(context, (BoxAssembly)a, search, resultList);
				}
				else if(a instanceof EndpointAssembly && StructureMapper.isInterfaceAssemblyRootThing(t)){
					IThing toolTipThing = ((EndpointAssembly)a).getEndpointGlassThing();
					if(toolTipThing != null){
						String toolTipText = ToolTipLogic.getToolTip(toolTipThing);
						if(matches(search, toolTipText)){
							r = createFindResult(context, toolTipThing, prefix, toolTipText, AS.resources.getImageDescriptor(ArchstudioResources.ICON_INTERFACE));
						}
					}
				}
				else if(a instanceof SplineAssembly && StructureMapper.isLinkAssemblyRootThing(t)){
					IThing toolTipThing = ((SplineAssembly)a).getSplineGlassThing();
					if(toolTipThing != null){
						String toolTipText = ToolTipLogic.getToolTip(toolTipThing);
						if(matches(search, toolTipText)){
							r = createFindResult(context, toolTipThing, prefix, toolTipText, AS.resources.getImageDescriptor(ArchstudioResources.ICON_LINK));
						}
					}
				}
				else if(a instanceof BoxAssembly && TypesMapper.isComponentTypeAssemblyRootThing(t)){
					IHasText textThing = ((BoxAssembly)a).getBoxedLabelThing();
					if(textThing != null){
						if(matches(search, textThing.getText())){
							r = createFindResult(context, textThing, prefix, textThing.getText(), AS.resources.getImageDescriptor(ArchstudioResources.ICON_COMPONENT_TYPE));
						}
					}
					find(context, (BoxAssembly)a, search, resultList);
				}
				else if(a instanceof BoxAssembly && TypesMapper.isConnectorTypeAssemblyRootThing(t)){
					IHasText textThing = ((BoxAssembly)a).getBoxedLabelThing();
					if(textThing != null){
						if(matches(search, textThing.getText())){
							r = createFindResult(context, textThing, prefix, textThing.getText(), AS.resources.getImageDescriptor(ArchstudioResources.ICON_CONNECTOR_TYPE));
						}
					}
					find(context, (BoxAssembly)a, search, resultList);
				}
				else if(a instanceof EndpointAssembly && TypesMapper.isSignatureAssemblyRootThing(t)){
					IThing toolTipThing = ((EndpointAssembly)a).getEndpointGlassThing();
					if(toolTipThing != null){
						String toolTipText = ToolTipLogic.getToolTip(toolTipThing);
						if(matches(search, toolTipText)){
							r = createFindResult(context, toolTipThing, prefix, toolTipText, AS.resources.getImageDescriptor(ArchstudioResources.ICON_INTERFACE));
						}
					}
				}
				else if(a instanceof EndpointAssembly && TypesMapper.isInterfaceTypeAssemblyRootThing(t)){
					IThing toolTipThing = ((EndpointAssembly)a).getEndpointGlassThing();
					if(toolTipThing != null){
						String toolTipText = ToolTipLogic.getToolTip(toolTipThing);
						if(matches(search, toolTipText)){
							r = createFindResult(context, toolTipThing, prefix, toolTipText, AS.resources.getImageDescriptor(ArchstudioResources.ICON_INTERFACE));
						}
					}
				}
			}

			if(r != null){
				resultList.add(r);
			}
		}
	}

	protected static boolean matches(String query, String target){
		if(query == null || target == null){
			return false;
		}
		query = query.toLowerCase();
		target = target.toLowerCase();
		return target.indexOf(query) != -1;
	}

	protected void find(IBNAView context, BoxAssembly ba, String search, List<IFindResult> resultList){
		String prefix = "[No Description]/";
		if(ba.getBoxedLabelThing() != null){
			String text = ba.getBoxedLabelThing().getText();
			if(text != null){
				prefix = text + "/";
			}
		}

		WorldThing wt = ba.getWorldThing();

		WorldThingPeer wtp = (WorldThingPeer)context.getPeer(wt);

		if(wt != null){
			IBNAWorld world = wt.getWorld();
			if(world != null){
				IBNAModel internalModel = world.getBNAModel();
				if(internalModel != null && wtp.getInnerView() != null)
					find(wtp.getInnerView(), internalModel, search, prefix, resultList);
			}
		}
	}

	protected IFindResult createFindResult(IBNAView view, IThing t, String prefix, String text, ImageDescriptor image){
		if(text == null){
			return null;
		}
		Point p = BNAUtils.getCentralPoint(t);
		if(p == null){
			return null;
		}
		if(prefix == null){
			prefix = "";
		}
		return new DefaultFindResult(new FindResultData(view, t, p), prefix + text, image);
	}

	class FindResultData{

		public IBNAView view;
		public IThing t;
		public Point p;

		public FindResultData(IBNAView view, IThing t, Point p){
			this.view = view;
			this.t = t;
			this.p = p;
		}
	}

}
