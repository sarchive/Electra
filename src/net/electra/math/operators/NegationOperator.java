package net.electra.math.operators;

public class NegationOperator extends Operator
{
	public NegationOperator()
	{
		super('-', 10, Associativity.RIGHT);
	}

	@Override
	public double evaluate(double var1, double var2)
	{
		return -var2;
	}
}
