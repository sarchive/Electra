package net.electra.math.operators;

public class MultiplicationOperator extends Operator
{
	public MultiplicationOperator()
	{
		super('*', 8, Associativity.LEFT);
	}

	@Override
	public double evaluate(double var1, double var2)
	{
		return var1 * var2;
	}
}
