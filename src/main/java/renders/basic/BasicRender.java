package main.java.renders.basic;

import java.awt.Color;
import java.awt.Graphics2D;

import main.java.core.Controller;
import main.java.renders.Painter;
import main.java.renders.Render;

public class BasicRender extends Render {
	
	private int zones;
	private int zoneW;
	private void setup() {
		zones = 700;
		zoneW = sW/zones;
	}
	
	public void paint(Graphics2D g) {
		//Reset screen
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);
		
		drawZones(g);
	}
	
	private void drawZones(Graphics2D g) {
		if (av.magnitudes==null) return;
		
		/*for (int z=0; z<zones; z++) {
			if (z%2==0) g.setColor(new Color(0, 0, 255, zoneOpacity));
			else g.setColor(new Color(255, 0, 0, zoneOpacity));
			g.fillRect(z*zoneW, 0, zoneW, sH);
		}*/
		
		g.setColor(Color.WHITE);
		for (int i=0; i<av.magnitudes.length; i++) {
			if (i>av.magnitudes.length) break;
			int val = (int) (av.magnitudes[i]*10);
			g.fillRect(i*zoneW, sH-val, zoneW, val);
		}
	}
	
	public Painter getPainter() {return new BasicPainter(this);}

	public BasicRender(Controller av) {
		super(av, "Basic", 700, 700);
		setup();
	}

	public static BasicRender initialise(Controller av) {
		BasicRender panel = new BasicRender(av);
		initialise(panel);
		return panel;
	}
}

class BasicPainter extends Painter {

	public BasicPainter(Render render) {super(render);}

	public void iterate(int count) {}
}
