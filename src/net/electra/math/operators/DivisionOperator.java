package net.electra.math.operators;

public class DivisionOperator extends Operator
{
	public DivisionOperator()
	{
		super('/', 8, Associativity.LEFT);
	}

	@Override
	public double evaluate(double var1, double var2)
	{
		return var1 / var2;
	}
}
