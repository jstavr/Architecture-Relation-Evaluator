// Ping Chen
// VariantEvaluationException.java

package edu.uci.isr.archstudio4.comp.selector;

/*
 * This Exception is thrown when we encounter an exception while evaluating the variant
 * Types.  It is usually thrown due to the lack of variants or improper number of 
 * variants that were evaluated to true.
 */

public class VariantEvaluationException extends Exception
{
	public VariantEvaluationException( String msg )
	{
		super( msg );
	}
}