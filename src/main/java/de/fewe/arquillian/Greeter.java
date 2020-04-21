package de.fewe.arquillian;

import java.io.PrintStream;

import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless
@Remote
public class Greeter implements IGreeter{
    public void greet(PrintStream to, String name) {
        to.println(createGreeting(name));
    }

    public String createGreeting(String name) {
        return "Hello, " + name + "!";
    }
}