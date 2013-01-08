package net.electra;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

public class Logger extends PrintStream
{
	private final DateFormat format;
	
	public Logger(OutputStream out, DateFormat format)
	{
		super(out);
		this.format = format;
	}
	
	public void print(String message)
	{
		super.print("[" + format.format(new Date()) + "] " + message);
	}
}
