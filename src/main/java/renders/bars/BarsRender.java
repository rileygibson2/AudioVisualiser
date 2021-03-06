package main.java.renders.bars;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import main.java.core.Button;
import main.java.core.Controller;
import main.java.core.Painter;
import main.java.core.Render;

public class BarsRender extends Render {

	//Buckets
	List<BarsBucket> buckets;
	private int bucketXStart, bucketW, bucketY;
	static final int curve = 12;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public final int numBuckets = 200; //Number of spectrum buckets
	public final int maxAmp = 40; //Range of possible amplitudes for each bucket

	private boolean inverted = false;
	public boolean inner = true;
	public int color;

	public Color colors[] = {new Color(255, 255, 0), new Color(255, 255, 255)};

	private void setup() {
		color = 1;
		bucketW = ((sW/2))/numBuckets; //Only use half the space so to mirror accross center
		bucketY = (int) (sH*0.5);
		bucketXStart = sW/2-bucketW*numBuckets;

		buckets = new ArrayList<BarsBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new BarsBucket((int) (bucketW*0.9), this)); 

		visualMags = new double[numBuckets];

		//Add specialised button
		buttons.add(new Button("B/S", Color.GREEN, Color.BLACK, "toggleBlackStrobe", "isBlackStrobing", true, this));
		buttons.add(new Button("W/S", Color.GREEN, Color.BLACK, "toggleWhiteStrobe", "isWhiteStrobing", true, this));
		buttons.add(new Button("Invert", Color.GREEN, Color.BLACK, "toggleInvert", "isInverted", false, this));
		buttons.add(new Button("Inner", Color.GREEN, Color.BLACK, "toggleInner", "isInner", false, this));
		buttons.add(new Button("Color", new Color(252, 186, 3), null, "toggleColor", "cheatTrue", false, this));
	}

	public void paint(Graphics2D g) {
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		//Elements
		drawBlocks(g);
	}

	/**
	 * Allows for the transition element between different magnitudes.
	 * Means that buckets will not jump down when magnitude goes from high to low,
	 * but will jump with a low to high transition.
	 */
	public void incrementVisualMags(boolean increaseFall) {
		if (av.magnitudes==null) return;
		double realMags[] = cutandAverageMags(0, numBuckets);

		for (int i=0; i<numBuckets; i++) {
			if (increaseFall&&visualMags[i]>realMags[i]) visualMags[i]--;
			if (visualMags[i]<realMags[i]) visualMags[i] = realMags[i];
		}

		//Update buckets
		for (int i=0; i<numBuckets; i++) buckets.get(i).updateMag(visualMags[i]);
	}

	public double[] cutandAverageMags(int mincut, int maxcut) {
		//Cut unwanted frequencys and average into set number of buckets
		if (mincut+maxcut>av.magnitudes.length||mincut<0||mincut>maxcut) throw new Error("Windowing error during cutting and averaging");
		double[] averaged = new double[numBuckets];
		double sum = 0;
		int bucketCut = (maxcut-mincut)/numBuckets;
		int bucketCount = 1;

		for (int i=mincut; i<maxcut; i++) {
			if (i==mincut+(bucketCount*bucketCut)) {
				if (bucketCount>=numBuckets) break;

				if ((sum)>maxAmp) averaged[bucketCount-1] = maxAmp;
				else averaged[bucketCount-1] = sum;
				sum = 0;
				bucketCount++;
			}

			if (av.magnitudes[i]>0) sum += av.magnitudes[i];
		}
		return averaged;
	}

	private void drawBlocks(Graphics2D g) {
		int i = 0;
		if (inverted) i = buckets.size()-1;

		Color override = null;
		if (strobing&&whiteStrobe&&strobeOn) override = Color.WHITE;

		for (BarsBucket b : buckets) {
			b.drawBucket(g, new Point((i*bucketW)+bucketXStart, bucketY), override); //Draw left
			b.drawBucket(g, new Point(sW-((i*bucketW)+bucketXStart), bucketY), override); //Draw reflected right

			if (inverted) i--;
			else i++;
		}
	}

	public void toggleInvert() {if (windowVisible()) inverted = !inverted;}

	public boolean isInverted() {return inverted;}

	public void toggleInner() {if (windowVisible()) inner = !inner;}

	public boolean isInner() {return inner;}

	public void toggleColor() {
		if (inner) {
			color++;
			if (color>=colors.length) color = 0;
		}
	}

	public Painter getPainter() {return new ReflectivePainter(this);}

	public BarsRender(Controller av) {
		super(av, "Bars");
		setup();
	}
}

class ReflectivePainter extends Painter {

	public ReflectivePainter(Render render) {
		super(render);
	}

	public void iterate(int count) {
		//Increment
		((BarsRender) render).incrementVisualMags(true);
	}

}
