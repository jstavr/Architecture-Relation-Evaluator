// Ping Chen
// MissingElementException.java

package edu.uci.isr.archstudio4.comp.booleaneval;

/**
 *
 * The MissingElementException is thrown whenever the program encounters
 * an architectural description that is lacking some necessary elements.
 */
public class MissingElementException extends Exception
{
    public MissingElementException( String msg )
    {
        super( msg );
    }
}
