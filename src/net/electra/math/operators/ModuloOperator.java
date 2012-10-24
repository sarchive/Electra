package net.electra.math.operators;

public class ModuloOperator extends Operator
{
	public ModuloOperator()
	{
		super('%', 8, Associativity.LEFT);
	}

	@Override
	public double evaluate(double var1, double var2)
	{
		return var1 % var2;
	}
}
