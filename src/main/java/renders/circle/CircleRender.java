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

import main.java.core.Button;
import main.java.core.Controller;
import main.java.core.Painter;
import main.java.core.Render;

public class CircleRender extends Render {

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
	public boolean unfocused;
	public boolean ring;
	int color;
	int thickness; //Thickness of present particles

	private void setup() {
		radius = 200;
		increments = 1;
		numBuckets = (radius/increments)+1;
		xStart = (int) ((sW-radius*2)/2);
		yStart = (int) ((sH-radius*2)/2)+radius;
		color = 0;
		ring = true;
		thickness = 1;
		makeParticles();

		//Add specialised button
		buttons.add(new Button("B/S", Color.GREEN, Color.BLACK, "toggleBlackStrobe", "isBlackStrobing", true, this));
		buttons.add(new Button("Focus", Color.GREEN, Color.BLACK, "toggleFocus", "isUnfocused", false, this));
		buttons.add(new Button("Ring", Color.GREEN, Color.BLACK, "toggleRing", "isRing", false, this));
		buttons.add(new Button("Thick", new Color(252, 186, 3), null, "toggleThickness", "cheatTrue", false, this));
		buttons.add(new Button("Color", new Color(252, 186, 3), null, "toggleColor", "cheatTrue", false, this));
	}
	
	/**
	 * Make particles. Note that circle is split into four quandrants,
	 * each with mirrored actions. So there will be twice number of
	 * particle buckets as there are actual freuqency buckets, and then
	 * that one half will be mirrored above to make the top half of the
	 * circle.
	 */
	public void makeParticles() {
		particles = new ArrayList<Set<CircleParticle>>();
		for (int i=0; i<numBuckets*2; i++) {
			Set<CircleParticle> band = new HashSet<>();
			for (int z=0; z<8*thickness; z++) band.add(new CircleParticle(i, this));
			particles.add(band);
		}
		updateColors();
	}
	
	public void updateColors() {
		//Update all the particles
		for (Set<CircleParticle> bucket : particles) {
			for (CircleParticle p : bucket) p.setColor(colors[color][0], colors[color][1]);
		}
	}

	public void toggleFocus() {if (windowVisible()) this.unfocused = !this.unfocused;}

	public boolean isUnfocused() {return this.unfocused;}
	
	public void toggleRing() {if (windowVisible()) this.ring = !this.ring;}

	public boolean isRing() {return this.ring;}

	public void toggleColor() {
		color++;
		if (color>=colors.length) color = 0;
		updateColors();
	}
	
	public void toggleThickness() {
		thickness++;
		if (thickness>4) thickness = 1;
		makeParticles();
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
		double realMags[] = cutandAverageMags(0, 201);

		for (int i=0; i<particles.size(); i++) {
			//Increment all the particles in this band
			for (CircleParticle p : particles.get(i)) {
				if (i>radius) p.increment(realMags[(realMags.length)-(i-radius)]);
				else p.increment(realMags[i]);
			}
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
				Point a1 = new Point(-500, (int) (((-400d-radius)/(x-radius))*y));
				Point a2 = new Point(200, (int) (((200d-radius)/(x-radius))*y));

				if (x>radius) { //Reflect with values after 1 radius
					a1 = new Point(-a1.x+2*radius, -a1.y);
					a2 = new Point(-a2.x+2*radius, -a2.y);
				}

				Color col = colors[color][2];
				g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), unfocused ? 10 : 15));
				g.drawLine(xStart+a1.x, yStart+a1.y, xStart+a2.x, yStart+a2.y);
			}
		}
	}

	private void drawCircle(Graphics2D g) {
		//Draw all particles
		for (Set<CircleParticle> band : particles) {
			for (CircleParticle p : band) p.draw(g, xStart, yStart);
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
		int adj = ((radius)/numBuckets);
		if (adj<1) adj = 1;
		int x = adj*c.bucket;
		int y = (int) (Math.sqrt(Math.pow(radius, 2)-Math.pow((x-radius), 2)));
		if (inverted) y = -y;
		
		//Find point on normal line
		double nX = x-c.mag;
		if (x>radius) nX = x+c.mag;
		double nY = ((nX-radius)/(x-radius))*y;

		return new Point((int) nX, (int) nY);
	}

	public Painter getPainter() {return new ReflectivePainter(this);}

	public CircleRender(Controller av) {
		super(av, "Circle");
		setup();
	}
	
	//0 is min, 1 is max, 2 is backdrop
	Color[][] colors = {
			{Color.WHITE, Color.WHITE, Color.WHITE},
			{Color.RED, Color.WHITE, Color.RED},
			{Color.GREEN, Color.WHITE, new Color(200, 255, 200)},
			{Color.BLUE, Color.WHITE, Color.BLUE},
			{new Color(255, 0, 255), Color.WHITE, new Color(255, 0, 200)},
			{Color.RED, new Color(255, 100, 100), Color.RED},
			{new Color(247, 205, 136), Color.WHITE, Color.WHITE}
	};
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
