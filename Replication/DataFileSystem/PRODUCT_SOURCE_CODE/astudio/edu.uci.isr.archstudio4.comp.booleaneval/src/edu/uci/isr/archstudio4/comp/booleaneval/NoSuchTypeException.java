// Ping Chen
// NoSuchTypeException.java

package edu.uci.isr.archstudio4.comp.booleaneval;

/**
 *
 * NoSuchTypeException is thrown when a type is encountered
 * that is not one of the valid types that is allowed in the symbol table.
 */
public class NoSuchTypeException extends Exception
{
    public NoSuchTypeException( String msg )
    {
        super( msg );
    }
}
