package main.java.renders;

public abstract class Painter extends Thread {
	protected Render render;
	protected int spacing = 100;

	public Painter(Render render) {
		this.render = render;
		this.start();
	}
	
	@Override
	public void run() {
		System.out.println("Painter - Starting paint on "+render.getName());
		int count = 0;
		
		while (render.paint) {
			//Sleep and calculate count
			try {Thread.sleep(20);}
			catch (InterruptedException er) {throw new Error("Sleep error");}
			count++;
			if (count>spacing) count = 0;
			
			render.av.rP.repaint();//Paint
			iterate(count); //Iterate
			
			//Increment strobe
			if (render.strobing&&count%2==0) render.strobeOn = !render.strobeOn;
			
			//Increase blackout
			if (render.blackout&&render.blackoutOp<255) {
				render.blackoutOp += 2;
				if (render.blackoutOp>255) render.blackoutOp = 255;
			}
			if (!render.blackout&&render.blackoutOp>0) {
				render.blackoutOp -= 2;
				if (render.blackoutOp<0) render.blackoutOp = 0;
			}
		}
		
		System.out.println("Painter - Killing paint on "+render.getName());
		render.av.rP.repaint(); //Trigger one last paint to make rP print black screen
	};
	
	public abstract void iterate(int count);
}
