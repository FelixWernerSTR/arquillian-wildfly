package de.fewe.arquillian;

import java.io.PrintStream;


public interface IGreeter {

	String createGreeting(String string);

	void greet(PrintStream out, String string);

}
