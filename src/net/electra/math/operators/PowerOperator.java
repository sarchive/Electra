package net.electra.math.operators;

public class PowerOperator extends Operator
{
	public PowerOperator()
	{
		super('^', 9, Associativity.RIGHT);
	}

	@Override
	public double evaluate(double var1, double var2)
	{
		return Math.pow(var1, var2);
	}
}
