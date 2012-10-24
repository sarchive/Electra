package net.electra.math.operators;

public class SubtractionOperator extends Operator
{
	public SubtractionOperator()
	{
		super('-', 5, Associativity.LEFT);
	}

	@Override
	public double evaluate(double var1, double var2)
	{
		return var1 - var2;
	}
}
