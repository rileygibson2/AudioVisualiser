package main.java.renders.circle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.java.core.Controller;
import main.java.renders.Painter;
import main.java.renders.Render;

public class CircleRender extends Render implements KeyListener {

	//Buckets
	private int radius, increments, xStart, yStart;
	static final int curve = 12;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public int numBuckets; //Number of spectrum buckets
	public final int maxAmp = 200; //Range of possible amplitudes for each bucket

	List<Set<CircleParticle>> particles;
	
	private void setup() {
		radius = 200;
		increments = 1;
		numBuckets = (radius*2)/increments+1;
		xStart = (int) ((sW-radius*2)/2);
		yStart = (int) ((sH-radius*2)/2)+radius;
		
		//Make particles
		particles = new ArrayList<Set<CircleParticle>>();
		for (int i=0; i<numBuckets; i++) {
			Set<CircleParticle> band = new HashSet<>();
			for (int z=0; z<1; z++) band.add(new CircleParticle(i, this));
			particles.add(band);
		}
	}

	public void paint(Graphics2D g) {
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		//Elements
		drawBackdrop(g);
		drawCircle(g);
	}

	/**
	 * Transition between higher and lower magnitudes is done in each particle
	 * in this render, not here. This is due to different particles having
	 * different speeds.
	 */
	public void incrementVisualMags(boolean increaseFall) {
		//Cut unwanted frequencys and average into set number of buckets
		if (av.magnitudes==null) return;
		double realMags[] = cutandAverageMags(0, 800);

		for (int i=0; i<numBuckets; i++) {
			//Increment all the particles in this band
			for (CircleParticle p : particles.get(i)) p.increment(realMags[i]);
		}
	}

	public double[] cutandAverageMags(int mincut, int maxcut) {
		if (mincut+maxcut>av.magnitudes.length||mincut<0||mincut>maxcut) throw new Error("Windowing error during cutting and averaging");
		double[] averaged = new double[numBuckets];
		double sum = 0;
		int count = 0;
		int bucketCut = (maxcut-mincut)/numBuckets;
		int bucketCount = 1;

		for (int i=mincut; i<maxcut; i++) {
			if (i==mincut+(bucketCount*bucketCut)) {
				if (bucketCount>=numBuckets) break;

				if ((sum)>maxAmp) averaged[bucketCount-1] = maxAmp;
				else averaged[bucketCount-1] = sum;
				sum = 0;
				count = 0;
				bucketCount++;
			}

			if (av.magnitudes[i]>0) sum += av.magnitudes[i];
			count++;
		}
		return averaged;
	}

	private void drawBackdrop(Graphics2D g) {
		for (int x=1; x<radius*2; x++) {
			if (x==radius) continue; //Skip ugly lines
			int y = (int) (Math.sqrt(Math.pow(radius, 2)-Math.pow((x-radius), 2)));

			//Find normal
			for (int z=0; z<2; z++) { //Do twice, once inverted for top half
				if (z==1) y = -y;
				Point a1 = new Point(-400, (int) (((-400d-radius)/(x-radius))*y));
				Point a2 = new Point(200, (int) (((200d-radius)/(x-radius))*y));

				if (x>radius) { //Reflect with values after 1 radius
					a1 = new Point(-a1.x+2*radius, -a1.y);
					a2 = new Point(-a2.x+2*radius, -a2.y);
				}

				g.setColor(new Color(255, 0, 0, 25));
				g.drawLine(xStart+a1.x, yStart+a1.y, xStart+a2.x, yStart+a2.y);
			}
		}
	}

	private void drawCircle(Graphics2D g) {
		/*int i = 0;
		for (int x=0; x<radius*2; x+=increments, i++) {
			int y = (int) (Math.sqrt(Math.pow(radius, 2)-Math.pow((x-radius), 2)));
			

			for (int z=0; z<2; z++) { //Do twice, once inverted
				if (z==1) y = -y;

				double am = visualMags[i]*3;
				//if (z==1) am = visualMags[visualMags.length-1-i]*3;

				//Find point on normal line
				double nX = x-am;
				if (x>radius) nX = x+am;
				double nY = ((nX-radius)/(x-radius))*y;

				g.setColor(Color.PINK);
				g.fillOval((int) (xStart+nX-1), (int) (yStart+nY-1), 2, 2);
			}
		}*/
		g.setColor(new Color(255, 255, 255, 100));
		g.drawOval(xStart, yStart-radius, radius*2, radius*2);
		
		//Draw all particles
		for (Set<CircleParticle> band : particles) {
			for(CircleParticle p : band) p.draw(g, xStart, yStart);
		}
	}

	/**
	 * Gives an absolute xy position for a band based on the normal of the
	 * band's position on the circle. Used so that individual particles can
	 * figure out where they are meant to be based on their assigned band.
	 * 
	 * @param band
	 * @param inverted
	 * @return
	 */
	public Point getRealPosOnNormal(CircleParticle c, boolean inverted) {
		int adj = ((radius*2)/numBuckets);
		if (adj<1) adj = 1;
		int x = adj*c.bucket;
		int y = (int) (Math.sqrt(Math.pow(radius, 2)-Math.pow((x-radius), 2)));

		if (inverted) y = -y;

		//c.mag = 0;
		
		//Find point on normal line
		double nX = x-c.mag;
		if (x>radius) nX = x+c.mag;
		double nY = ((nX-radius)/(x-radius))*y;

		return new Point((int) nX, (int) nY);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_B:
			blackout = !blackout;
			break;
		}
	}

	public Painter getPainter() {return new ReflectivePainter(this);}

	public CircleRender(Controller av) {
		super(av, "Circle", 1450, 900);
		setup();
		addKeyListener(this);
	}

	public static CircleRender initialise(Controller av) {
		CircleRender panel = new CircleRender(av);
		initialise(panel);
		return panel;
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}

class ReflectivePainter extends Painter {

	public ReflectivePainter(Render render) {
		super(render);
	}

	public void iterate(int count) {
		//Increment
		((CircleRender) render).incrementVisualMags(true);
	}

}
