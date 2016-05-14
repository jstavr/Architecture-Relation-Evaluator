// Ping Chen
// SymbolTable.java

package edu.uci.isr.archstudio4.comp.booleaneval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * SymbolTable, this will act as the customized symbol table used to pass in
 * variables and their associated value. Namely, it is a container class that
 * stores a name-value pair. The value is wrapped by an internal class
 * TypeValuePair. TypeValuePair stores not only the value, but it stores the
 * type as well. The symbol table is implemented with the java predefined
 * hashtable. It uses the name as the key.
 * 
 * @see archstudio.comp.booleaneval.TypeParser
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 * @updated by Matt Critchlow and Lewis Chiang August, 2002
 * 
 */

public class SymbolTable implements java.io.Serializable{

	/**
	 * This is the integer value to represent type Double
	 */
	public static final int DOUBLE = 100;

	/**
	 * This integer value represents the type String
	 */
	public static final int STRING = 101;

	/**
	 * This integer value represents the type Date
	 */
	public static final int DATE = 102;

	/**
	 * Entry will store a name-type-value binding. The type of
	 * the value is stored as an integer constant The only valid types are doubles
	 * (real numbers), strings, and dates.
	 */
	protected static class Entry{
		private String name;
		private int type;
		private Object value;

		public Entry(String name, int newType, Object newVal){
			this.name = name;
			this.value = newVal;
			this.type = newType;
		}
		
		public String getName(){
			return name;
		}

		public void setName(String name){
			this.name = name;
		}

		public int getType(){
			return type;
		}

		public void setType(int type){
			this.type = type;
		}

		public Object getValue(){
			return value;
		}

		public void setValue(Object value){
			this.value = value;
		}

		public String toString(){
			return "( " + name + ", " + value.toString() + " )";
		}
	}

	private List entryList = new ArrayList(10);

	/**
	 * Default constructor
	 */
	public SymbolTable(){
	}
	
	private void storeEntry(Entry entryToStore){
		int index = 0;
		for(Iterator it = entryList.iterator(); it.hasNext(); ){
			Entry e = (Entry)it.next();
			if(e.getName().equals(entryToStore.getName())){
				entryList.set(index, entryToStore);
				return;
			}
			index++;
		}
		entryList.add(entryToStore);
	}
	
	private Entry getEntry(String variableName){
		for(Iterator it = entryList.iterator(); it.hasNext(); ){
			Entry e = (Entry)it.next();
			if(e.getName().equals(variableName)){
				return e;
			}
		}
		return null;
	}

	/**
	 * This function stores the name-value pair into the symbol table. It will
	 * override any previous value associated with this variable.
	 * 
	 * @param variable
	 *          The name of the variable
	 * @param value
	 *          The value associated with the variable name that is passed in.
	 * 
	 * @exception NoSuchTypeException
	 *              This exception is thrown if the type passed in is not one of
	 *              the valid data types
	 * 
	 */
	public void put(String variable, String value) throws NoSuchTypeException{
		Object val = TypeParser.parse(value);

		if(val instanceof Date){
			storeEntry(new Entry(variable, DATE, val));
		}
		else if(val instanceof Double){
			storeEntry(new Entry(variable, DOUBLE, val));
		}
		else if(val instanceof String){
			storeEntry(new Entry(variable, STRING, val));
		}
	}

	public void putString(String variable, String value){
		storeEntry(new Entry(variable, STRING, value));
	}
	
	public void putDouble(String variable, double value){
		storeEntry(new Entry(variable, DOUBLE, new Double(value)));
	}
	
	public void putDate(String variable, java.util.Date value){
		storeEntry(new Entry(variable, DATE, value));
	}
	
	/**
	 * This function retrieves the value associated with a particular variable
	 * name. This is the general get function that will return the object.
	 * 
	 * @param variable
	 *          The variable whose value you wish to retrieve.
	 * @return Object representing the value associated with the variable passed
	 *         in. Null if it doesn't exist
	 */
	public Object get(String variable){
		Entry e = getEntry(variable);
		if(e == null){
			return null;
		}
		return e.getValue();
	}

	/**
	 * This is a specialized get functin that returns Doubles.
	 * 
	 * @param variable
	 *          The name of the double variable whose value you wish to retrieve.
	 * @return A Double object to represent the value of the variable passed in.
	 * @exception TypeMismatchException
	 *              This exception is thrown when the variable is not of type
	 *              Double.
	 */
	public Double getDouble(String variable) throws TypeMismatchException{
		Object o = get(variable);
		if(o == null) return null;
		if(!(o instanceof Double)){
			throw new TypeMismatchException("Variable " + variable + " is not of type Double.");
		}
		return (Double)o;
	}

	/**
	 * This is a specialized get functin that returns Strings.
	 * 
	 * @param variable
	 *          The name of the string variable whose value you wish to retrieve.
	 * @return A String object to represent the value of the variable passed in.
	 *         Null if it doesn't exist.
	 * @exception TypeMismatchException
	 *              This exception is thrown when the variable is not of type
	 *              String.
	 */
	public String getString(String variable) throws TypeMismatchException{
		Object o = get(variable);
		if(o == null) return null;
		if(!(o instanceof String)){
			throw new TypeMismatchException("Variable " + variable + " is not of type String.");
		}
		return (String)o;
	}

	/**
	 * This is a specialized get functin that only returns Dates.
	 * 
	 * @param variable
	 *          The name of the Date variable whose value you wish to retrieve.
	 * @return A Date object to represent the value of the variable passed in.
	 *         Null if the variable doesn't exist
	 * @exception TypeMismatchException
	 *              This exception is thrown when the variable is not of type
	 *              Date.
	 */
	public Date getDate(String variable) throws TypeMismatchException{
		Object o = get(variable);
		if(o == null) return null;
		if(!(o instanceof Date)){
			throw new TypeMismatchException("Variable " + variable + " is not of type Date.");
		}
		return (Date)o;
	}

	/**
	 * This function will return an integer representation of the type of the
	 * variable.
	 * 
	 * @param variable
	 *          The variable whose type you wish to get
	 * @return DOUBLE if the variable is of type Double. STRING if the variable is
	 *         of type String. DATE if the variable is of type Date.
	 * @exception NoSuchVariableException
	 *              Thrown if the variable is not in the symbol table
	 */
	public int getType(String variable) throws NoSuchVariableException{
		Entry e = getEntry(variable);
		if(e == null){
			throw new NoSuchVariableException("Unable to find variable: " + variable);
		}
		return e.getType();
	}
	
	public void renameVariable(String oldName, String newName){
		Entry e = getEntry(oldName);
		if(e != null){
			e.setName(newName);
		}
	}

	/**
	 * This function removes the name-value pair associated with the variable. It
	 * will do nothing if the variable does not exist.
	 * 
	 * @param variable
	 *          The name of the variable whose value you wish to remove.
	 */
	public void remove(String variable){
		Entry e = getEntry(variable);
		entryList.remove(e);
	}

	/**
	 * This function checks to see if a particular variable is stored within the
	 * symbol table.
	 * 
	 * @param variable
	 *          The name of the variable to check
	 * @return True if the variable is in the symbol table, false otherwise
	 */
	public boolean isPresent(String variable){
		return getEntry(variable) != null;
	}

	/**
	 * This function returns the size of the symbol table
	 * 
	 * @return The size
	 */
	public int size(){
		return entryList.size();
	}

	// this is mainly for debugging purpose
	public String toString(){
		StringBuffer output = new StringBuffer();
		for(Iterator it = entryList.iterator(); it.hasNext(); ){
			output.append(it.next().toString());
			output.append(System.getProperty("line.separator"));
		}
		return output.toString();
	}

	// writeFile takes in the filepath to write the contents of the symbol table
	// to
	// as well as a prepared String[] for printing
	public boolean writeFile(String fileToWrite, String[] file){
		try{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				fileToWrite)));
			for(int i = 0; i < size(); i++){
				String temp = file[i];
				out.println(temp);
			}
			out.close();
		}
		catch(Exception e){
			System.out.println(e);
			return false;
		}
		return true;
	}

	public boolean isEmpty(){
		return entryList.isEmpty();
	}

	public void clearTable(){
		entryList.clear();
	}

	// listTable() stores each name-value pair into a string array which will
	// be used for print out the contents of the table to a file in writeFile()
	public String[] listTable(){
		try{
			String[] lines = new String[entryList.size()];
			Object tempValue = null;
			
			Entry[] entries = (Entry[])entryList.toArray(new Entry[entryList.size()]);
			for(int i = 0; i < entries.length; i++){
				int valueType = entries[i].getType();
				if(valueType == DATE){
					Date date = (Date)entries[i].getValue();
					tempValue = DateFormat.getDateTimeInstance().format(date);
					lines[i] = entries[i].getName() + " = #" + tempValue + "#";
				}
				if(valueType == STRING){
					String string = (String)entries[i].getValue();
					lines[i] = entries[i].getName() + " = \"" + string + "\"";
				}
				if(valueType == DOUBLE){
					Double d = (Double)entries[i].getValue();
					lines[i] = entries[i].getName() + " = " + d.toString();
				}
			}
			return lines;
		}
		catch(Exception e){
			System.out.println(e);
			return null;
		}
	}

	// getVariable() returns all the keys(names) of the symbol table in the form
	// of a
	// string array
	public String[] getVariables(){
		Entry[] entries = (Entry[])entryList.toArray(new Entry[entryList.size()]);
		String[] names = new String[entries.length];
		for(int i = 0; i < entries.length; i++){
			names[i] = entries[i].getName();
		}
		return names;
	}
	
	public static String typeToString(int type){ 
		if(type == DATE) return "Date";
		if(type == DOUBLE) return "Numeric";
		if(type == STRING) return "String";
		return "Unknown";
	}
	
	public static void parse(InputStream is, SymbolTable symTab) throws java.io.IOException, NoSuchTypeException{
		BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
		
		int lineNum = 1;  // this is to help printing out error messages
		String line;
		
		line = br.readLine( );
		// makes sure we haven't reached the end of file
		while( line != null )
		{
			line = line.trim();    // removes extra white space at beginning and end
			// ignores empty lines and comments
			if( !line.equals( "" ) && !line.startsWith( "//" ) )
			{
				// the tokenizer WILL return =
				StringTokenizer st = new StringTokenizer( line, "=", true );

				if( st.countTokens() >= 2 )	
				{
					String name = st.nextToken( ).trim( );
					st.nextToken( ); // dumps the first =
					
					String variable = st.nextToken( );
					// this will concat the rest of the tokens into the variable
					// string
					while( st.hasMoreTokens( ) )
						variable += st.nextToken( );
					// makes sure that variable doesn't have starting or ending white spaces
					symTab.put( name, variable.trim( ) );
				}
				else
					throw new IOException( "Error parsing input file on line " + lineNum +
						": " + line );
				
			}
			line = br.readLine( );
			lineNum++;
		}
		br.close( );
	}
}