package main.java.core;

public class Painter extends Thread {
	Render render;

	public Painter(Render render) {
		this.render = render;
	}

	@Override
	public void run() {
		while (render.paint) {
			try {Thread.sleep(10);}
			catch (InterruptedException er) {throw new Error("Sleep error");}
			render.repaint();
		}
	}

}
