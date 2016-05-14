package edu.uci.isr.archstudio4.comp.pruner;

/**
 * Interface that provides the Pruning service.  ArchPrune is responsible for taking a selector
 * file that has extraneous Types and ArchStructures and then subsequently removing any those
 * elements.
 *
 * @author Christopher Van der Westhuizen
 */

public interface IPruner
{
	/**
	 * This is the function that is responsible for calling the pruning algorithm.  The algorithm
	 * will hierarchically traverse through the architectural elements and discover which elements
	 * are not linked up to architecture and need to be "pruned."  All elements in the document
	 * are cloned before pruning so that the original document is not altered.  The result is a document
     * containing only the structures and types used in the architecture. Additionally, the pruner 
     * removes any version graph whose internal nodes are not referenced by any type included in the 
     * selected architecture.
	 *
	 * @param archURI 		This is the URL of the xADL document that needs to be pruned
	 * @param targetArchURI This is the URL of the new xADL document that will be created and store the pruned architecture
	 * @param startingID 	This is the ID of the element that the pruning algorithm should start from
	 * @param isStructural 	If this is true then the startingID is that of an archStruct, otherwise it is an
	 *							ID to a type
	 *
	 * @throws InvalidURIException 			If the provided URL to open an xArch document is invalid
	 * @throws InvalidElementIDException 	If the provided starting ID is invalid
	 * @throws MissingElementException		If an expected element is missing
	 * @throws MissingAttributeException	If an expected attribute on an element is missing
	 * @throws BrokenLinkException			If a link between elements does not match up or exist
	 */	
	public void prune(String archURI, String targetArchURI, String startingID, boolean isStructural)
		throws InvalidURIException, InvalidElementIDException, MissingElementException, 
				MissingAttributeException, BrokenLinkException;
}