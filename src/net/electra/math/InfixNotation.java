package net.electra.math;

import java.util.Map;

import net.electra.math.operators.Operator;
import net.electra.math.tokens.*;

public class InfixNotation extends Notation
{
	public InfixNotation(String expression)
	{
		expression = "(" + expression.replace(" ", "") + ")";
		String tempTokenValue = "";
		boolean tempTokenIsVar = false;
		
		for (int i = 0; i < expression.length(); i++)
		{
			char current = expression.charAt(i);
			
			if (Operator.is(current) || Parenthesis.is(current))
			{
				if (tempTokenValue.length() > 0)
				{
					if (!tempTokenIsVar)
					{
						tokens().add(new NumberToken(Double.parseDouble(tempTokenValue)));
					}
					else
					{
						tokens().add(new VariableToken(tempTokenValue));
					}
					
					tempTokenValue = "";
					tempTokenIsVar = false;
				}
				
				if (Parenthesis.is(current))
				{
					tokens().add(new ParenthesisToken(Parenthesis.get(current)));
				}
				else
				{
					Operator operator = Operator.get(current);
					
					if (operator == Operator.SUBTRACTION || operator == Operator.NEGATIVE)
					{
						Token<?> last = tokens().get(tokens().size() - 1);
						operator = Operator.SUBTRACTION;

						if (last instanceof ParenthesisToken && ((ParenthesisToken)last).value() == Parenthesis.OPEN)
						{
							operator = Operator.NEGATIVE;
						}
						else if (last instanceof OperatorToken)
						{
							operator = Operator.NEGATIVE;
						}
					}
					
					tokens().add(new OperatorToken(operator));
				}
			}
			else
			{
				if (!Character.isDigit(current))
				{
					tempTokenIsVar = true;
				}
				
				tempTokenValue += current;
			}
		}
		
		expressionize();
	}

	@Override
	public double evaluate(Map<String, Double> variables)
	{
		return new PostfixNotation(this).evaluate(variables);
	}
}
