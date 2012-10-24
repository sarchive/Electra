package net.electra.math.tokens;

public class Token<T>
{
	private final T value;
	
	public Token(T value)
	{
		this.value = value;
	}
	
	public T value()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return value.toString();
	}
}
