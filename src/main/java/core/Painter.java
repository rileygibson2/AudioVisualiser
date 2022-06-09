package main.java.core;

public class Painter extends Thread {
	SpectrumRender render;
	int spacing = 100;

	public Painter(SpectrumRender render) {
		this.render = render;
	}

	@Override
	public void run() {
		int count = 0;
		while (render.paint) {
			try {Thread.sleep(20);}
			catch (InterruptedException er) {throw new Error("Sleep error");}
			count++;
			if (count>spacing) count = 0;
			
			//Paint
			render.repaint();
			
			//Increment
			render.increaseColorScroll(2);
			render.flickerStars();
			if (count%40==0) render.av.incrementTestMags();
			else render.av.resetTestMags();
			if (count%2==0) render.incrementVisualMags();
			
			if (render.blackout&&render.blackoutOp<255) render.blackoutOp++;
			if (!render.blackout&&render.blackoutOp>0) render.blackoutOp--;
		}
	}

}
