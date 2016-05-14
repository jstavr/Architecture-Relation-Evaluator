package edu.uci.isr.archstudio4.comp.versionpruner;


// Add support for xArchADT
import java.util.Arrays;
import java.util.Vector;

import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;


/**
 * This is the class that provides the implementation of the PruneVersions service. The 
 * PruneVersions service is responsible 
 * for taking an architecture and removing any unneeded archStructures and archTypes.
 * The end result is an architecture that consists only of archStructures, found in types
 * as substructure (where needed), and archTypes that are types of elements within archStructures.
 * Any disjoint archTypes and archStructures will then not be included.  This could be used, for
 * example, after an architecture has been run through a selector.  The larger architecture will
 * only have a subset selected from it and the rest will then be removed after being run through
 * the pruner.
 *
 * @author Christopher Van der Westhuizen <A HREF="mailto:vanderwe@uci.edu">(vanderwe@uci.edu)</A> 
 */

public class VersionPrunerImpl implements IVersionPruner
{
	// This inner helper class acts as a means to collect together all the arguments
	// that would be needed by most of the methods in PruneVersionsImpl.
	// The advantage to using this class is to group together variables that will
	// be used by a particular instance of the PruneVerions interface and allowing
	// for reentrant code.
	private class PruneVersionsArguments
	{
		public ObjRef arch;				// reference to the xArch element of the architecture being examined
		public String targetURI;		// URI of the target document
		public double lowerBound;		// lower bound of current pruning progress
		public double upperBound;		// upper bound of current pruning progress
		public double progressAmount;	// the amount by which to update the progress bar for every increment
	}
	
	// Interface through which DOM-based xArch libraries can be accessed
	protected XArchFlatInterface xarch; 	

	// Size of list for holding message listeners
	private static final int NUM_MESSAGES = 1;
	
	// Size for upper bound of progress bar
	private static final double UPPER_BOUND = 100;
	
	// List of message listeners
	protected Vector messageListeners = new Vector(NUM_MESSAGES);
	
	/**
	 * This is the constructor for PruneVersions
	 *
	 * @param xArch	A reference to the XArchFlatInterface that allows for the accessing of DOM-based xArch libraries
	 */
	public VersionPrunerImpl(XArchFlatInterface xArch)
	{
		xarch = xArch;
	}
	
	
	/**
	 * This is the function that is responsible for calling the "prune versions" algorithm.  The algorithm
	 * will visit all types and remove any links to nodes in versionGraphs.  Additionally, 
	 * the PruneVersions service will remove all archVersion tags in the xADL document.  The entire document
	 * is cloned before pruning so that pruning is performed on a copy of the document.  The result is a document
	 * containing only the structures and types used in the architecture without any version information. 
	 *
	 * @param archURI 		This is the URI of the xADL document that needs to be pruned
	 * @param targetArchURI This is the URI of the new xADL document that will be created and store the pruned architecture
	 *
	 * @throws InvalidURIException 	If the provided URI to open an xArch document is invalid
	 */	
	public void pruneVersions(String archURI, String targetArchURI)
		throws InvalidURIException
	{
		// Get a reference to the open architecture
		ObjRef origArch = xarch.getOpenXArch(archURI);
		if(origArch == null)
		{
			throw new InvalidURIException("Error: The URI \"" + archURI + "\" does not point to an open xArch");
		}
		
		if(targetArchURI == null || targetArchURI.length() == 0)
		{
			throw new InvalidURIException("Error: The provided target URI is invalid.  Please provide a non-empty URI");
		}
		
		// Create and initialize a PruneVersionsArguments object
		PruneVersionsArguments args = new PruneVersionsArguments();
		args.arch = xarch.cloneXArch(origArch, targetArchURI);
		args.targetURI = targetArchURI;
		args.lowerBound = 0;
		args.upperBound = UPPER_BOUND;
		
		// Now perform pruneVersions algorithm
		pruneArch(args);
	}
			
	// This method calls helper functions that take care of pruning version information from a xADL document
	// This is performed in two steps: 1. Removing version info from types; 2. Removing archVersion elements
	// from the xADL document.
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private void pruneArch(PruneVersionsArguments arguments)
	{
		// Fire initial progress message
		//fireMessageSent(createNewStatusMessage(arguments));
		
		// Go through all types and remove links
		removeTypeVersionLinks(arguments);
		
		// Remove all archVersions elements		
		removeArchVersions(arguments);

		// Fire complete progress message
		arguments.lowerBound = arguments.upperBound;
		//fireMessageSent(createNewStatusMessage(arguments));
	}
	
	// This method is responsible for removing version information from types
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private void removeTypeVersionLinks(PruneVersionsArguments arguments)
	{
		ObjRef typesContextRef = xarch.createContext(arguments.arch, "types");
		ObjRef [] archTypes = xarch.getAllElements(typesContextRef, "archTypes", arguments.arch);
		ObjRef [] componentTypeRefs;
		ObjRef [] connectorTypeRefs;
		ObjRef [] interfaceTypeRefs;
		
		Vector compTypes = new Vector();
		Vector connTypes = new Vector();
		Vector intTypes = new Vector();
		
		// Value to hold total number of Types in document
		int numElements = 0;
		
		// Calculate number of types to be used in progressAmount computation
		// The majority of the work being done is in updating Types.  Minimal
		// work is required for the removal of archVersions and, as such,
		// it is only necessary to perform progress updates based on Type operations
		for(int i = 0; i < archTypes.length; ++i)
		{
			componentTypeRefs = xarch.getAll(archTypes[i], "ComponentType");
			numElements += componentTypeRefs.length;
			compTypes.addAll(Arrays.asList(componentTypeRefs));
			
			connectorTypeRefs = xarch.getAll(archTypes[i], "ConnectorType");
			numElements += connectorTypeRefs.length;
			connTypes.addAll(Arrays.asList(connectorTypeRefs));
			
			interfaceTypeRefs = xarch.getAll(archTypes[i], "InterfaceType");
			numElements += interfaceTypeRefs.length;
			intTypes.addAll(Arrays.asList(interfaceTypeRefs));
		}
		
		// Convert all Vectors of Types into ObjRef arrays
		componentTypeRefs = new ObjRef[compTypes.size()];
		compTypes.toArray(componentTypeRefs);
		
		connectorTypeRefs = new ObjRef[connTypes.size()];
		connTypes.toArray(connectorTypeRefs);
		
		interfaceTypeRefs = new ObjRef[intTypes.size()];
		intTypes.toArray(interfaceTypeRefs);
		
		// Calculate progress amount value that will change with each operation in the prune versions algorithm
		arguments.progressAmount = UPPER_BOUND / numElements;

		// Remove each of the three different types {components, connectors, interfaces}
		removeTypeVersionLinksHelper(componentTypeRefs, "ComponentType", arguments);
		removeTypeVersionLinksHelper(connectorTypeRefs, "ConnectorType", arguments);
		removeTypeVersionLinksHelper(interfaceTypeRefs, "InterfaceType", arguments);
	}
	
	// This method assists the removeTypeVersionLinks method by inspecting a set of ComponentTypes, ConnectorTypes, and InterfaceTypes and then
	// clearing the versionGraphNode element of the respective type
	//
	// elementTypes - Collection of types that are to be inspected
	// elementName - String name representing what types are passed into the method: either ComponentTyeps, ConnectorTypes, or InterfaceTypes
    // arguments - A collection of arguments specific to this xArch pruning
	private void removeTypeVersionLinksHelper(ObjRef [] elementTypes, String elementName, PruneVersionsArguments arguments)
	{
		for(int i = 0; i < elementTypes.length; ++i)
		{
			// If this is a ImplVers type, i.e. with versioning information
			if(xarch.isInstanceOf(elementTypes[i], "versions#VariantComponentTypeImplVers") ||
			   xarch.isInstanceOf(elementTypes[i], "versions#VariantConnectorTypeImplVers") ||
			   xarch.isInstanceOf(elementTypes[i], "versions#InterfaceTypeImplVers"))
			{
				// Clear the VersionGraphNode element
				xarch.clear(elementTypes[i], "VersionGraphNode");
			}
			
			arguments.lowerBound += arguments.progressAmount;
			//fireMessageSent(createNewStatusMessage(arguments));
		}
	}

	// This method is responsible for removing all archVersion elements in the document
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private void removeArchVersions(PruneVersionsArguments arguments)
	{
		ObjRef versionsContextRef = xarch.createContext(arguments.arch, "versions");
		ObjRef [] archVersions = xarch.getAllElements(versionsContextRef, "archVersions", arguments.arch);
		
		if(archVersions != null)
		{
			xarch.remove(arguments.arch, "Object", archVersions);
		}
	}

}