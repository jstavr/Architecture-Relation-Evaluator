/**
 * 
 */
package edu.uci.isr.archstudio4.comp.tracelink.models;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import edu.uci.isr.archstudio4.comp.tracelink.analysis.ITracelinkRule;

/**
 * @author dpurpura
 *
 */
public class XMLSerializer {
	public static ArrayList<ITracelinkRule> deserialize(String filename) throws FileNotFoundException{
		ArrayList<ITracelinkRule> rules = new ArrayList<ITracelinkRule>();
		
		XMLDecoder in = new XMLDecoder(
                new BufferedInputStream(
                    new FileInputStream(filename)));
		
		try {
			Object obj = in.readObject();
			
			while (obj != null) {
				if (obj instanceof ITracelinkRule) {
					rules.add((ITracelinkRule) obj);
				}
				
				obj = in.readObject();
			}
			
		}catch(ArrayIndexOutOfBoundsException e) {
			//do nothing
			//this exception happens when there are no more objects 
			//to be read in (see XMLDecoder javadocs for more info)
		}
		
		in.close();
		
		return rules;
	}
	
	public static void serialize(String filename, Collection<ITracelinkRule> rules) throws FileNotFoundException {
		XMLEncoder out = new XMLEncoder(
				new BufferedOutputStream(
                    new FileOutputStream(filename)));
		
		for (ITracelinkRule rule : rules)
			out.writeObject(rule);

		out.close();
	}
	
	//H added 7/21/08 for serializing selection items
	public static void serializeSelection(String filename, Collection<ISelectionModel> selectionItems) throws FileNotFoundException {
		XMLEncoder out = new XMLEncoder(
				new BufferedOutputStream(
                    new FileOutputStream(filename)));
		
		for (ISelectionModel selection : selectionItems)
			out.writeObject(selection);

		out.close();
	}
	
	public static Collection<ISelectionModel> deserializeSelection(String filename) throws FileNotFoundException{
		//ArrayList<ITracelinkRule> rules = new ArrayList<ITracelinkRule>();
		Collection<ISelectionModel> selectionList = new Vector<ISelectionModel>();
		
		XMLDecoder in = new XMLDecoder(
                new BufferedInputStream(
                    new FileInputStream(filename)));
		
		try {
			Object obj = in.readObject();
			
			while (obj != null) {
				if (obj instanceof ISelectionModel) {
					//rules.add((ITracelinkRule) obj);
					selectionList.add((ISelectionModel)obj);
				}
				
				obj = in.readObject();
			}
			
		}catch(ArrayIndexOutOfBoundsException e) {
			//do nothing
			//this exception happens when there are no more objects 
			//to be read in (see XMLDecoder javadocs for more info)
		}
		
		in.close();
		
		return selectionList;
	}
}
