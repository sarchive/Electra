package net.electra.math;

import java.util.Map;
import java.util.Stack;

import net.electra.math.operators.Associativity;
import net.electra.math.operators.Operator;
import net.electra.math.tokens.*;

// aka reverse polish notation
public class PostfixNotation extends Notation
{
	public PostfixNotation(InfixNotation infix)
	{
		Stack<Token<?>> stack = new Stack<Token<?>>();
		
		for (Token<?> token : infix.tokens())
		{
			Object value = token.value();
			
			if (value instanceof Parenthesis)
			{
				Parenthesis parenthesis = (Parenthesis)value;
				
				switch (parenthesis)
				{
					case OPEN:
						stack.push(token);
						break;
					case CLOSE:
						while (stack.size() > 0)
						{
							Token<?> peek = stack.peek();
							
							if (peek instanceof ParenthesisToken && (Parenthesis)peek.value() == Parenthesis.OPEN)
							{
								break;
							}
							
							tokens().add(stack.pop());
						}
						
						stack.pop();
						break;
				}
			}
			else if (value instanceof Operator)
			{
				Operator operator = (Operator)value;
				
				while (stack.size() > 0)
				{
					Token<?> peek = stack.peek();
					
					if (peek instanceof ParenthesisToken)
					{
						break;
					}
					
					Operator peekValue = (Operator)peek.value();
					
					if (operator.associativity() == Associativity.LEFT && operator.precedence() <= peekValue.precedence()
						|| operator.associativity() == Associativity.RIGHT && operator.precedence() < peekValue.precedence())
					{
						tokens().add(stack.pop());
					}
					else
					{
						break;
					}
				}
				
				stack.push(token);
			}
			else
			{
				tokens().add(token);
			}
		}
		
		while (stack.size() > 0)
		{
			if (stack.peek().value() instanceof Parenthesis && (Parenthesis)stack.peek().value() == Parenthesis.OPEN)
			{
				stack.pop();
			}
			else
			{
				tokens().add(stack.pop());
			}
		}
		
		expressionize();
	}

	@Override
	public double evaluate(Map<String, Double> variables)
	{
		Stack<Token<?>> stack = new Stack<Token<?>>();
		
		for (Token<?> token : tokens())
		{
			if (token instanceof OperatorToken)
			{
				OperatorToken operator = (OperatorToken)token;
				Token<?> second = stack.pop();
				
				if (second instanceof VariableToken)
				{
					second = new NumberToken(variables.get(second.value()));
				}
				
				Token<?> first = new NumberToken(0.0);
				
				if (operator.value() != Operator.NEGATIVE)
				{
					first = stack.pop();
					
					if (first instanceof VariableToken)
					{
						first = new NumberToken(variables.get(first.value()));
					}
				}
				
				stack.push(new NumberToken(operator.value().evaluate(((NumberToken)first).value(), ((NumberToken)second).value())));
			}
			else
			{
				stack.push(token);
			}
		}
		
		return ((NumberToken)stack.pop()).value();
	}
}
