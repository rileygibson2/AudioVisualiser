package main.java.renders;

public abstract class Painter extends Thread {
	protected Render render;
	protected int spacing = 100;

	public Painter(Render render) {
		this.render = render;
	}
	
	@Override
	public abstract void run();
}
