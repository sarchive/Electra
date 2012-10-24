package net.electra;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

public class TimeLogger extends PrintStream
{
	private final DateFormat format;
	
	public TimeLogger(OutputStream out, DateFormat format)
	{
		super(out);
		this.format = format;
	}
	
	@Override
	public void println()
	{
		super.println("test");
	}
	
	@Override
	public void println(String message)
	{
		super.println("[" + format.format(new Date()) + "] " + message);
	}
	
	@Override
	public void println(Object object)
	{
		println(object.toString());
	}
}
