package net.electra.math.tokens;

public enum Parenthesis
{
	OPEN('('),
	CLOSE(')');
	
	private final char value;
	
	private Parenthesis(char value)
	{
		this.value = value;
	}
	
	public char value()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
	
	public static Parenthesis get(char value)
	{
		for (Parenthesis operator : Parenthesis.values())
		{
			if (operator.value == value)
			{
				return operator;
			}
		}
		
		return null;
	}
	
	public static boolean is(char value)
	{
		for (Parenthesis operator : Parenthesis.values())
		{
			if (operator.value == value)
			{
				return true;
			}
		}
		
		return false;
	}
}
