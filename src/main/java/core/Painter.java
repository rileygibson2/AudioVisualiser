package main.java.core;

public abstract class Painter extends Thread {
	Render render;
	int spacing = 100;

	public Painter(Render render) {
		this.render = render;
	}
	
	@Override
	public abstract void run();
}
