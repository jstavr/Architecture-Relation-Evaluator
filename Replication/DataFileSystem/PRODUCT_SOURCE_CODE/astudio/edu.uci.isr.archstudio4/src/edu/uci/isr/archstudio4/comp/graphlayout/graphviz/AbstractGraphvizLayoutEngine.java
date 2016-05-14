package edu.uci.isr.archstudio4.comp.graphlayout.graphviz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import edu.uci.isr.archstudio4.comp.graphlayout.AliasTable;
import edu.uci.isr.archstudio4.comp.graphlayout.ILayoutEngine;
import edu.uci.isr.archstudio4.graphlayout.GraphLayout;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutException;
import edu.uci.isr.archstudio4.graphlayout.GraphLayoutParameters;
import edu.uci.isr.widgets.swt.constants.Orientation;
import edu.uci.isr.xadlutils.XadlUtils;
import edu.uci.isr.xadlutils.XadlUtils.LinkInfo;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public abstract class AbstractGraphvizLayoutEngine implements ILayoutEngine{

	public static final String EOL = System.getProperty("line.separator");
	
	public GraphLayout layoutGraph(XArchFlatInterface xarch, IPreferenceStore prefs, ObjRef rootRef, GraphLayoutParameters params) throws GraphLayoutException{		
		StringBuffer sb = new StringBuffer();
		AliasTable at = new AliasTable();
		createGraph(sb, at, xarch, rootRef, params);
		String output = runLayoutTool(xarch, prefs, sb.toString());
		GraphLayout gl = processOutput(at, xarch, rootRef, params, output);
		return gl;
	}
	
	protected abstract String runLayoutTool(XArchFlatInterface xarch, IPreferenceStore prefs, String toolInput) throws GraphLayoutException;
	
	protected void createGraph(StringBuffer sb, AliasTable at, XArchFlatInterface xarch, ObjRef rootRef, GraphLayoutParameters params) throws GraphLayoutException{
		if(rootRef == null) throw new IllegalArgumentException("Null root reference in graph layout.");
		
		if(!xarch.isInstanceOf(rootRef, "types#ArchStructure")){
			throw new GraphLayoutException("Invalid root reference in graph layout; must be an ArchStructure");
		}
		
		sb.append("digraph arch {" + EOL);
		createGraphParameters(sb, at, xarch, rootRef, params);
		sb.append(EOL);
		createBricks(sb, at, xarch, rootRef, params);
		sb.append(EOL);
		createLinks(sb, at, xarch, rootRef, params);
		sb.append(EOL);
		sb.append("}" + EOL);
	}
	
	protected void createGraphParameters(StringBuffer sb, AliasTable at, XArchFlatInterface xarch, ObjRef rootRef, GraphLayoutParameters params) throws GraphLayoutException{
		Object o = params.getProperty("nodeSep");
	  if((o != null) && (o instanceof Double)){
	  	sb.append("  nodesep = ");
	  	sb.append(((Double)o).toString());
	  	sb.append(";");
	  	sb.append(EOL);
	  }
	  
	  o = params.getProperty("rankSep");
	  if((o != null) && (o instanceof Double)){
	  	sb.append("  ranksep = ");
	  	sb.append(((Double)o).toString());
	  	sb.append(";");
	  	sb.append(EOL);
	  }
	}
	
	protected void createBricks(StringBuffer sb, AliasTable at, XArchFlatInterface xarch, ObjRef rootRef, GraphLayoutParameters params) throws GraphLayoutException{
		ObjRef[] componentRefs = xarch.getAll(rootRef, "component");
		for(ObjRef componentRef : componentRefs){
			createBrick(sb, at, xarch, componentRef, params, true);
		}
		sb.append(EOL);
		ObjRef[] connectorRefs = xarch.getAll(rootRef, "connector");
		for(ObjRef connectorRef : connectorRefs){
			createBrick(sb, at, xarch, connectorRef, params, false);
		}
	}
	
	protected void createBrick(StringBuffer sb, AliasTable at, XArchFlatInterface xarch, ObjRef brickRef, GraphLayoutParameters params, boolean isComponent) throws GraphLayoutException{
		double rw = isComponent ? params.getRelativeComponentWidth() : params.getRelativeConnectorWidth();
		double rh = isComponent ? params.getRelativeComponentHeight() : params.getRelativeConnectorHeight();
		
		String brickXArchID = XadlUtils.getID(xarch, brickRef);
		if(brickXArchID != null){
			String brickAlias = at.getAlias(brickXArchID);
		  sb.append("  ");
		  sb.append(brickAlias);
		  sb.append(" ");
			if(shouldOrientInterfaces(params) && hasOrientedInterface(xarch, brickRef)){
				//the brick will be a quartered record
			  sb.append("[shape=record,width=");
			  sb.append(rw);
			  sb.append(",height=");
			  sb.append(rh);
			  sb.append(",label=\"{<__n> | { <__w> | <__c> | <__e> } | <__s>}\"");
			}
			else{
				//The brick will be a box.
			  sb.append("[shape=box,width=");
			  sb.append(rw);
			  sb.append(",height=");
			  sb.append(rh);
			}
		  sb.append("];");
			sb.append(EOL);
		}
	}

	protected void createLinks(StringBuffer sb, AliasTable at, XArchFlatInterface xarch, ObjRef rootRef, GraphLayoutParameters params) throws GraphLayoutException{
		ObjRef[] linkRefs = xarch.getAll(rootRef, "link");
		for(ObjRef linkRef : linkRefs){
			createLink(sb, at, xarch, linkRef, params);
		}
	}

	protected void createLink(StringBuffer sb, AliasTable at, XArchFlatInterface xarch, ObjRef linkRef, GraphLayoutParameters params) throws GraphLayoutException{
		String linkXArchID = XadlUtils.getID(xarch, linkRef);
		if(linkXArchID == null){
			return;
		}

		LinkInfo linkInfo = XadlUtils.getLinkInfo(xarch, linkRef, true);
		if(linkInfo == null){
			return;
		}
		
		ObjRef linkEndpoint1Target = linkInfo.getPoint1Target();
		if(linkEndpoint1Target == null){
			return;
		}
		if(!xarch.isInstanceOf(linkEndpoint1Target, "types#Interface")){
			return;
		}
		ObjRef linkEndpoint1Parent = xarch.getParent(linkEndpoint1Target);
		if(linkEndpoint1Parent == null){
			return;
		}
		if(!xarch.isInstanceOf(linkEndpoint1Parent, "types#Component") &&
			!xarch.isInstanceOf(linkEndpoint1Parent, "types#Connector")){
			return;
		}
		String linkEndpoint1ParentXArchID = XadlUtils.getID(xarch, linkEndpoint1Parent);
		if(linkEndpoint1ParentXArchID == null){
			return;
		}

		ObjRef linkEndpoint2Target = linkInfo.getPoint2Target();
		if(linkEndpoint2Target == null){
			return;
		}
		if(!xarch.isInstanceOf(linkEndpoint2Target, "types#Interface")){
			return;
		}
		ObjRef linkEndpoint2Parent = xarch.getParent(linkEndpoint2Target);
		if(linkEndpoint2Parent == null){
			return;
		}
		if(!xarch.isInstanceOf(linkEndpoint2Parent, "types#Component") &&
			!xarch.isInstanceOf(linkEndpoint2Parent, "types#Connector")){
			return;
		}
		String linkEndpoint2ParentXArchID = XadlUtils.getID(xarch, linkEndpoint2Parent);
		if(linkEndpoint2ParentXArchID == null){
			return;
		}
		
		//OK, the link is valid.
		sb.append("  ");
		sb.append(at.getAlias(linkEndpoint1ParentXArchID));
		
		if(shouldOrientInterfaces(params) && hasOrientedInterface(xarch, linkEndpoint1Parent)){
			//The brick is quartered.
			Orientation linkEndpoint1Orientation = guessInterfaceOrientation(xarch, linkEndpoint1Target);
			switch(linkEndpoint1Orientation){
				case NORTH:
					sb.append(":__n");
					break;
				case EAST:
					sb.append(":__e");
					break;
				case SOUTH:
					sb.append(":__s");
					break;
				case WEST:
					sb.append(":__w");
					break;
				default:
					sb.append(":__c");
			}
		}
		
		sb.append(" -> ");
		
		sb.append(at.getAlias(linkEndpoint2ParentXArchID));
		
		if(shouldOrientInterfaces(params) && hasOrientedInterface(xarch, linkEndpoint2Parent)){
			//The brick is quartered.
			Orientation linkEndpoint2Orientation = guessInterfaceOrientation(xarch, linkEndpoint2Target);
			switch(linkEndpoint2Orientation){
				case NORTH:
					sb.append(":__n");
					break;
				case EAST:
					sb.append(":__e");
					break;
				case SOUTH:
					sb.append(":__s");
					break;
				case WEST:
					sb.append(":__w");
					break;
				default:
					sb.append(":__c");
			}
		}
		
		sb.append(" [label=\"");
		sb.append(at.getAlias(linkXArchID));
		sb.append("\"];");
		
		sb.append(EOL);
	}
	//------------
	
	public GraphLayout processOutput(AliasTable at, XArchFlatInterface xarch, ObjRef rootRef, GraphLayoutParameters params, String output) throws GraphLayoutException{
		try{
			GraphLayout gl = new GraphLayout();
			
			BufferedReader br = new BufferedReader(new StringReader(output));
			
			double scale = params.getScale();
			while(true){
				String line = br.readLine().trim();
				if(line.startsWith("stop")){
					return gl;
				}
				else if(line.startsWith("node")){
					StringTokenizer tok = new StringTokenizer(line);
					String nodeToken = tok.nextToken();
					String eltToken = tok.nextToken();
					String xToken = tok.nextToken();
					String yToken = tok.nextToken();
					String widthToken = tok.nextToken();
					String heightToken = tok.nextToken();
					
					GraphLayout.Node node = new GraphLayout.Node();
					node.setNodeId(at.getTruename(eltToken));
					
					double xd = Double.parseDouble(xToken);
					double yd = Double.parseDouble(yToken);
					double widthd = Double.parseDouble(widthToken);
					double heightd = Double.parseDouble(heightToken);

					//We want a bounds rectangle, (xd,yd) is the CENTER
					//of the box, so we have to offset it by half the width
					//and half the height to get the UL coordinate.
					
					xd -= (widthd / 2.0d);
					yd -= (heightd / 2.0d);

					xd *= scale;
					int x = (int)Math.round(xd);
					
					yd *= scale;
					int y = (int)Math.round(yd);
					
					widthd *= scale;
					int width = (int)Math.round(widthd);
					
					heightd *= scale;
					int height = (int)Math.round(heightd);
					
					Rectangle bounds = new Rectangle(x, y, width, height);
					node.setBounds(bounds);
					
					gl.addNode(node);
				}
				else if(line.startsWith("edge")){
					StringTokenizer tok = new StringTokenizer(line);
					String edgeToken = tok.nextToken();
					String endpt1Token = tok.nextToken();
					String endpt2Token = tok.nextToken();
					String numPointsToken = tok.nextToken();
					int numPoints = Integer.parseInt(numPointsToken);
					
					String[] xTokens = new String[numPoints];
					String[] yTokens = new String[numPoints];
					
					for(int i = 0; i < numPoints; i++){
						xTokens[i] = tok.nextToken();
						yTokens[i] = tok.nextToken();
					}
					
					String edgeIdToken = tok.nextToken();
					
					GraphLayout.Edge edge = new GraphLayout.Edge();
					
					edge.setEdgeId(at.getTruename(edgeIdToken));
					
					int colon1Index = endpt1Token.indexOf(":");
					String node1Token = null;
					String port1Token = null;

					if(colon1Index != -1){
						node1Token = endpt1Token.substring(0, colon1Index);
						port1Token = endpt1Token.substring(colon1Index + 1);
					}
					/*else{
						int secondEIndex = endpt1Token.indexOf("e", 1);
						node1Token = endpt1Token.substring(0, secondEIndex);
						port1Token = endpt1Token.substring(secondEIndex);
					}*/
					
					int colon2Index = endpt2Token.indexOf(":");
					String node2Token = null;
					String port2Token = null;

					if(colon2Index != -1){
						node2Token = endpt2Token.substring(0, colon2Index);
						port2Token = endpt2Token.substring(colon2Index + 1);
					}
					/*else{
						int secondEIndex = endpt2Token.indexOf("e", 1);
						node2Token = endpt2Token.substring(0, secondEIndex);
						port2Token = endpt2Token.substring(secondEIndex);
					}*/
					edge.setEndpoint1(at.getTruename(node1Token), at.getTruename(port1Token));
					edge.setEndpoint2(at.getTruename(node2Token), at.getTruename(port2Token));
											
					for(int i = 0; i < numPoints; i++){
						double xd = Double.parseDouble(xTokens[i]);
						xd *= scale;
						int x = (int)Math.round(xd);
						
						double yd = Double.parseDouble(yTokens[i]);
						yd *= scale;
						int y = (int)Math.round(yd);
						
						Point p = new Point(x, y); 
						edge.addPoint(p);
					}
					
					gl.addEdge(edge);
				}					
			}
		}
		catch(IOException e){
			throw new GraphLayoutException("This shouldn't happen.");
		}
		catch(Exception e2){
			throw new GraphLayoutException("Exception while parsing Graphviz output", e2);
		}
	}
	
	//------------
	
	protected static boolean hasOrientedInterface(XArchFlatInterface xarch, ObjRef brickRef){
		ObjRef[] interfaceRefs = xarch.getAll(brickRef, "interface");
		for(int i = 0; i < interfaceRefs.length; i++){
			Orientation o = guessInterfaceOrientation(xarch, interfaceRefs[i]);
			if((o != null) && (!o.equals(Orientation.NONE))){
				return true;
			}
		}
		return false;
	}
	
	protected static Orientation guessInterfaceOrientation(XArchFlatInterface xarch, ObjRef interfaceRef){
		String description = XadlUtils.getDescription(xarch, interfaceRef);
		if(description == null){
			return Orientation.NONE;
		}
		
		description = description.toLowerCase();
		if(description.indexOf("top") != -1){
			return Orientation.NORTH;
		}
		if(description.indexOf("bottom") != -1){
			return Orientation.SOUTH;
		}
		if(description.indexOf("left") != -1){
			return Orientation.WEST;
		}
		if(description.indexOf("right") != -1){
			return Orientation.EAST;
		}
		if(description.indexOf("peer") != -1){
			return Orientation.EAST;
		}
		if(description.indexOf("north") != -1){
			return Orientation.NORTH;
		}
		if(description.indexOf("east") != -1){
			return Orientation.EAST;
		}
		if(description.indexOf("west") != -1){
			return Orientation.WEST;
		}
		if(description.indexOf("south") != -1){
			return Orientation.SOUTH;
		}
		return Orientation.NONE;
	}
	
	protected static boolean shouldOrientInterfaces(GraphLayoutParameters params){ 
		Object o = params.getProperty("orientInterfaces");
		if(o instanceof Boolean){
			Boolean b = (Boolean)o;
			if(b == null){
				return false;
			}
			return b.booleanValue();
		}
		return false;
	}
}
