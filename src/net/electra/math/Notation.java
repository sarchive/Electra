package net.electra.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.electra.math.tokens.Token;
import net.electra.math.tokens.VariableToken;

public abstract class Notation
{
	private Map<String, Double> variables = new HashMap<String, Double>();
	private ArrayList<Token<?>> tokens = new ArrayList<Token<?>>();
	private String expression = "";
	
	public void expressionize()
	{
		for (Token<?> token : tokens)
		{
			expression += token + " ";
		}
		
		expression = expression.trim();
	}
	
	public double evaluate()
	{
		return evaluate(variables);
	}
	
	public abstract double evaluate(Map<String, Double> variables);
	
	public boolean hasVariables()
	{
		for (Token<?> token : tokens)
		{
			if (token.getClass().equals(VariableToken.class)) // instanceof doesn't work and i don't know why
			{
				return true;
			}
		}
		
		return false;
	}
	
	public String[] variableNames()
	{
		ArrayList<String> names = new ArrayList<String>();
		
		for (Token<?> token : tokens)
		{
			if (token.getClass().equals(VariableToken.class)) // instanceof doesn't work and i don't know why
			{
				names.add(((VariableToken)token).value());
			}
		}
		
		return names.toArray(new String[0]);
	}
	
	public Map<String, Double> variables()
	{
		return variables;
	}
	
	public ArrayList<Token<?>> tokens()
	{
		return tokens;
	}
	
	public String expression()
	{
		return expression;
	}
}
