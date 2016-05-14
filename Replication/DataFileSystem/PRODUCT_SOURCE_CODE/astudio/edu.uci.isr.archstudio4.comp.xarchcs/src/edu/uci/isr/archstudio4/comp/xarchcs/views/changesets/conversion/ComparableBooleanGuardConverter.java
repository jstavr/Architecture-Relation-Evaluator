package edu.uci.isr.archstudio4.comp.xarchcs.views.changesets.conversion;

import java.util.Arrays;

import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatInterface;

public class ComparableBooleanGuardConverter{

	public static String booleanGuardToString(XArchFlatInterface xarch, ObjRef optionalRef){
		ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
		if(guardRef != null){
			if(xarch.isInstanceOf(guardRef, "boolguard#BooleanGuard")){
				ObjRef booleanExpRef = (ObjRef)xarch.get(guardRef, "BooleanExp");
				if(booleanExpRef != null){
					return convertBooleanExpression(xarch, booleanExpRef);
				}
			}
		}
		return null;
	}

	private static String convertBooleanExpression(XArchFlatInterface xarch, ObjRef booleanExp){
		ObjRef expression = null;

		expression = (ObjRef)xarch.get(booleanExp, "And");
		if(expression != null){
			ObjRef left = (ObjRef)xarch.get(expression, "BooleanExp1");
			ObjRef right = (ObjRef)xarch.get(expression, "BooleanExp2");

			String[] operands = new String[]{convertBooleanExpression(xarch, left), convertBooleanExpression(xarch, right)};
			Arrays.sort(operands);

			String ret = "(" + operands[0] + " && " + operands[1] + ")";

			return ret;
		}

		expression = (ObjRef)xarch.get(booleanExp, "Or");
		if(expression != null){
			ObjRef left = (ObjRef)xarch.get(expression, "BooleanExp1");
			ObjRef right = (ObjRef)xarch.get(expression, "BooleanExp2");

			String[] operands = new String[]{convertBooleanExpression(xarch, left), convertBooleanExpression(xarch, right)};
			Arrays.sort(operands);

			String ret = "(" + operands[0] + " || " + operands[1] + ")";

			return ret;
		}

		expression = (ObjRef)xarch.get(booleanExp, "Not");
		if(expression != null){
			ObjRef parameter = (ObjRef)xarch.get(expression, "BooleanExp");

			String ret = "!(" + convertBooleanExpression(xarch, parameter) + ")";

			return ret;
		}

		expression = (ObjRef)xarch.get(booleanExp, "Equals");
		if(expression != null){
			return convertRelationalExpression(xarch, expression, "==", "==");
		}

		expression = (ObjRef)xarch.get(booleanExp, "NotEquals");
		if(expression != null){
			return convertRelationalExpression(xarch, expression, "!=", "!=");
		}

		expression = (ObjRef)xarch.get(booleanExp, "GreaterThan");
		if(expression != null){
			return convertRelationalExpression(xarch, expression, ">", "<");
		}

		expression = (ObjRef)xarch.get(booleanExp, "GreaterThanOrEquals");
		if(expression != null){
			return convertRelationalExpression(xarch, expression, ">=", "<=");
		}

		expression = (ObjRef)xarch.get(booleanExp, "LessThan");
		if(expression != null){
			return convertRelationalExpression(xarch, expression, "<", ">");
		}

		expression = (ObjRef)xarch.get(booleanExp, "LessThanOrEquals");
		if(expression != null){
			return convertRelationalExpression(xarch, expression, "<=", ">=");
		}

		expression = (ObjRef)xarch.get(booleanExp, "InSet");
		if(expression != null){
			ObjRef param = (ObjRef)xarch.get(expression, "Symbol");
			String paramName = (String)xarch.get(param, "Value");

			String ret = paramName + " @ {";
			ObjRef[] values = xarch.getAll(expression, "Value");
			String[] valueStrings = new String[values.length];
			for(int i = 0; i != values.length; ++i){
				valueStrings[i] = (String)xarch.get(values[i], "Value");
			}
			Arrays.sort(valueStrings);
			for(int i = 0; i != values.length; ++i){
				if(i > 0){
					ret += ", ";
				}
				ret += valueStrings[i];
			}
			ret += "}";

			return ret;
		}

		expression = (ObjRef)xarch.get(booleanExp, "InRange");
		if(expression != null){
			ObjRef param = (ObjRef)xarch.get(expression, "Symbol");
			String paramName = (String)xarch.get(param, "Value");

			String ret = paramName + " @ [";

			ObjRef[] values = xarch.getAll(expression, "Value");
			String value1 = (String)xarch.get(values[0], "Value");
			String value2 = (String)xarch.get(values[1], "Value");

			ret = ret + value1 + ", " + value2 + "]";
			return ret;
		}

		return "";
	}

	private static String convertRelationalExpression(XArchFlatInterface xarch, ObjRef expression, String symbol, String reverseSymbol){
		ObjRef left;
		ObjRef right;
		String leftValue;
		String rightValue;

		left = (ObjRef)xarch.get(expression, "Symbol");
		leftValue = (String)xarch.get(left, "Value");

		right = (ObjRef)xarch.get(expression, "Symbol2");
		if(right != null){
			rightValue = (String)xarch.get(right, "Value");
		}
		else{
			right = (ObjRef)xarch.get(expression, "Value");
			rightValue = (String)xarch.get(right, "Value");
		}

		String ret = leftValue + " " + symbol + " " + rightValue;
		if(leftValue.compareTo(rightValue) > 0){
			ret = leftValue + " " + reverseSymbol + " " + rightValue;
		}
		return ret;
	}

}
