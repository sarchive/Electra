package net.electra.net.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NetworkEventHeader
{
	int id();
	int length();
}