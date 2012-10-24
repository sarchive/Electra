package net.electra.math.operators;

import java.lang.reflect.Field;
import java.util.HashMap;

public abstract class Operator
{
	public static final HashMap<Character, Operator> operators = new HashMap<Character, Operator>();
	public static final NegationOperator NEGATIVE = new NegationOperator();
	public static final PowerOperator POWER = new PowerOperator();
	public static final MultiplicationOperator MULTIPLICATION = new MultiplicationOperator();
	public static final DivisionOperator DIVISION = new DivisionOperator();
	public static final ModuloOperator MODULUS = new ModuloOperator();
	public static final AdditionOperator ADDITION = new AdditionOperator();
	public static final SubtractionOperator SUBTRACTION = new SubtractionOperator();
	
	static
	{
		Field[] fields = Operator.class.getDeclaredFields();

		try
		{
			for (Field field : fields)
			{
				if (field.getType().getSuperclass() == Operator.class)
				{
					Operator operator = (Operator)field.get(null);
					operators.put(operator.sign(), operator);
				}
			}
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Operator forName(String name) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
	{
		return (Operator)Operator.class.getDeclaredField(name.toUpperCase()).get(null);
	}
	
	public static Operator get(char value)
	{
		return operators.get(value);
	}
	
	public static boolean is(char value)
	{
		return operators.containsKey(value);
	}
	
	private final Associativity associativity;
	private final int precedence;
	private final char sign;
	
	public Operator(char sign, int precedence, Associativity associativity)
	{
		this.sign = sign;
		this.precedence = precedence;
		this.associativity = associativity;
	}
	
	public abstract double evaluate(double var1, double var2);
	
	public char sign()
	{
		return sign;
	}
	
	public int precedence()
	{
		return precedence;
	}
	
	public Associativity associativity()
	{
		return associativity;
	}
	
	@Override
	public String toString()
	{
		return sign + "";
	}
}
