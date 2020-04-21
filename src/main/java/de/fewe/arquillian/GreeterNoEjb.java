package de.fewe.arquillian;

import java.io.PrintStream;

public class GreeterNoEjb implements IGreeter{
    public void greet(PrintStream to, String name) {
        to.println(createGreeting(name));
    }

    public String createGreeting(String name) {
        return "Hello, " + name + "!";
    }
}