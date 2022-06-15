package main.java.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BasicRender extends Render {
	
	private int zones;
	private int zoneW;
	private void setup() {
		zones = av.buckets;
		zoneW = sW/zones;
		System.out.println("zones = "+zones+", "+zoneW);
	}
	
	@Override
	public void paintComponent(Graphics g1d) {
		Graphics2D g = (Graphics2D) g1d;
		//Reset screen
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, sW, sH);
		
		drawZones(g);
	}
	
	private void drawZones(Graphics2D g) {
		if (av.magnitudes==null) return;
		
		/*for (int z=0; z<zones; z++) {
			if (z%2==0) g.setColor(new Color(0, 0, 255, zoneOpacity));
			else g.setColor(new Color(255, 0, 0, zoneOpacity));
			g.fillRect(z*zoneW, 0, zoneW, sH);
		}*/
		
		g.setColor(Color.RED);
		for (int i=0; i<av.magnitudes.length; i++) {
			if (i>av.buckets) break;
			int val = (int) (av.magnitudes[i]*10);
			g.fillRect(i*zoneW, sH-val, zoneW, val);
		}
	}

	public BasicRender(AudioVisualiser av) {
		super(av, 700, 700);
		Render.painter = new BasicPainter(this);
		setup();
		startPaint();
	}

	public static BasicRender initialise(AudioVisualiser av) {
		BasicRender panel = new BasicRender(av);
		initialise(panel);
		return panel;
	}
}

class BasicPainter extends Painter {

	public BasicPainter(Render render) {
		super(render);
	}

	@Override
	public void run() {
		while (render.paint) {
			try {Thread.sleep(20);}
			catch (InterruptedException er) {throw new Error("Sleep error");}
			render.repaint(); //Paint
		}
	}

}
