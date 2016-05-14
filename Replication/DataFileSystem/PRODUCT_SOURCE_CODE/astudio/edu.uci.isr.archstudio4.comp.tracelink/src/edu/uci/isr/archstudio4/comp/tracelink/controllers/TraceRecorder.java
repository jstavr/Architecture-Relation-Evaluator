/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.controllers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.eclipse.core.internal.resources.File;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import edu.uci.isr.archstudio4.comp.tracelink.analysis.IRulePart;
import edu.uci.isr.archstudio4.comp.tracelink.analysis.ITracelinkRule;
import edu.uci.isr.archstudio4.comp.tracelink.models.ITraceEndpointModel;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceEndpoint;
import edu.uci.isr.archstudio4.comp.tracelink.models.TraceLink;
import edu.uci.isr.archstudio4.comp.tracelink.models.XMLSerializer;
import edu.uci.isr.sysutils.ArrayUtils;
import edu.uci.isr.xarchflat.ObjRef;

/**
 * Class contains logic for recording trace link events
 * 
 * FIXME must click component after clicking start record, 
 *  should be able to use currently selected component
 *  
 * FIXME should send new endpoints to Import/new Tracelink 
 * component to limit the number of ways tracelinks can be created
 * 
 * @author dpurpura
 *
 */
public class TraceRecorder {

	private boolean isRecording;

	private XADLFacade xADLFacade;
	private ArrayList<ITracelinkRule> ruleSet;

	private TraceLink currTracelink;


	public TraceRecorder(XADLFacade xADLFacade) {
		this.isRecording = false;
		this.xADLFacade  = xADLFacade;
		this.ruleSet = new ArrayList<ITracelinkRule>();
	}


	/**
	 * @return the isRecording
	 */
	public boolean isRecording() {
		return isRecording;
	}


	public void setRecording(boolean isRecording) {
		if (isRecording) {
			currTracelink = new TraceLink();
			currTracelink.setRelationship("rationale");
		}
		else {
			serialize(currTracelink);
		}

		this.isRecording = isRecording;
	}

	
	/**
	 * @param facade the xADLFacade to set
	 */
	public void setXADLFacade(XADLFacade facade) {
		xADLFacade = facade;
	}


	/**
	 * @return the ruleSet
	 */
	public ArrayList<ITracelinkRule> getRuleSet() {
		return ruleSet;
	}


	/**
	 * @param ruleSet the ruleSet to set
	 */
	public void setRuleSet(ArrayList<ITracelinkRule> ruleSet) {
		this.ruleSet = ruleSet;
		
		System.out.println("Rules set! \n Rules added:");
		for (ITracelinkRule rule : ruleSet)
			System.out.println(rule);
	}
	
	public void setRuleSet(String filename) throws FileNotFoundException{
		setRuleSet(XMLSerializer.deserialize(filename));
	}


	public void record(String filename, ISelection selection) {
		if (!isRecording()) {
			setRecording(true);
		}

		String location = filename + "#";

		//TODO: remove the following later
		System.out.println("record timestamp " + new Date() );
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;

			currTracelink.addAllEndpoints(createEndpoints(ss, filename));
		}
		else if (selection instanceof ITextSelection) {
			ITextSelection ts  = (ITextSelection) selection;
			location += ts.getText();

			currTracelink.addEndpoint(createEndpoint(location));
		}
		else if (selection instanceof IMarkSelection) {
			IMarkSelection ms = (IMarkSelection) selection;

			try {
				location += ms.getDocument().get(ms.getOffset(), ms.getLength());
			}
			catch (BadLocationException ble) {
				System.err.println("TraceRecorder: Bad Location Exception: " + ms.getDocument());
			}

			currTracelink.addEndpoint(createEndpoint(location));
		}
	}

	private ITraceEndpointModel createEndpoint(String location) {
		if (existsEndpoint(location))
			return null;
		
		final String CAPTURE_MODE  = "auto_record";
		final String LOCATION_TYPE = "simple";

		ITraceEndpointModel endpoint = new TraceEndpoint();

		endpoint.setAuthor(System.getProperty("user.name"));
		endpoint.setCaptureMode(CAPTURE_MODE);
		endpoint.setLocationType(LOCATION_TYPE);
		endpoint.setLocationHref(location);
		endpoint.setTimestamp(new Date());

		applyRuleSet(endpoint);

		return endpoint;
	}


	private ArrayList<ITraceEndpointModel> createEndpoints(IStructuredSelection ss, String filename) {
		ArrayList<ITraceEndpointModel> endpoints = new ArrayList<ITraceEndpointModel>();

		Iterator<?> iterator = ss.iterator();
		Object obj;
		String location = "";
		ITraceEndpointModel endpoint;
		while (iterator.hasNext()) {
			obj = iterator.next();
			//System.out.println("selection item event" + o.getClass().getName());

			if (obj instanceof File) {
				location = ((File) obj).getFullPath().toPortableString();
			}
			else if (obj instanceof ObjRef) {
				location = "#" + xADLFacade.getID((ObjRef) obj);
			}
			
			endpoint = createEndpoint(location);
			if (endpoint != null)
				endpoints.add(endpoint);

		}

		return endpoints;
	}

	private void serialize(TraceLink tracelink) {
		xADLFacade.serializeTracelink(tracelink);
	}
	
	private void applyRuleSet(ITraceEndpointModel endpoint) {
		for (ITracelinkRule rule : ruleSet) {
			if (rule.isFollowingRule(endpoint))
				applyConsequent(currTracelink, rule.getConsequent());
		}
	}
	
	
	/*
	 * TODO move to ITracelink rule, to maintain better information hiding
	 */
	private void applyConsequent(TraceLink traceLink, IRulePart consequent) {
		//System.out.println("rules are true");
		
		//FIXME remove string matching -- change to enum
		if (consequent.getArchObject().equals("TraceLink")) {
			if (consequent.getCommand().equals("setDescription")) {
				traceLink.setDescription(ArrayUtils.join(consequent.getParameters(), ", "));
				System.out.println("setDescription: " + ArrayUtils.join(consequent.getParameters(), ", "));
			}else if (consequent.getCommand().equals("setRelationship")) {
				traceLink.setRelationship(ArrayUtils.join(consequent.getParameters(), ", "));
				System.out.println("setRelationship: " + ArrayUtils.join(consequent.getParameters(), ", "));
			}
		}
	}
	
	private boolean existsEndpoint(String location) {
		for (ITraceEndpointModel endpoint : currTracelink.getEndpointList()) {
			if (endpoint.getLocationHref().equalsIgnoreCase(location))
				return true;
		}
		return false;
	}



}
