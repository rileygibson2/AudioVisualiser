package main.java.renders;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

public class Button {

	Render parent;
	public String name;
	public Color onCol;
	public Color offCol;
	public boolean limitPress;
	String actionMethod;
	String checkMethod;

	public boolean isOn() {
		try {
			Object returned = parent.getClass().getMethod(checkMethod).invoke(parent);
			return (Boolean) returned;
		} catch (NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {throw new Error("Problem with reflection: "+e.toString());}
	}

	public void toggleAction() {
		try {
			parent.getClass().getMethod(actionMethod).invoke(parent);
		} catch (NoSuchMethodException|SecurityException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {throw new Error("Problem with reflection: "+e.toString());}
	}

	public Button(String name, Color onCol, Color offCol, String actionMethod, String checkMethod, boolean limitPress, Render parent) {
		this.name = name;
		this.onCol = onCol;
		this.offCol = offCol;
		this.actionMethod = actionMethod;
		this.checkMethod = checkMethod;
		this.parent = parent;
		this.limitPress = limitPress;
	}
}
