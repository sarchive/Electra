package net.electra.math.tokens;

import net.electra.math.operators.Operator;

public class OperatorToken extends Token<Operator>
{
	public OperatorToken(Operator value)
	{
		super(value);
	}
}
