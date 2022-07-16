package main.java.renders.reflective;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import main.java.core.Button;
import main.java.core.Controller;
import main.java.core.Painter;
import main.java.core.Render;

public class ReflectiveRender extends Render {

	//Buckets
	List<ReflectiveBucket> buckets;
	private int bucketXStart, bucketW, bucketXOff, bucketY;
	static final int curve = 12;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public final int numBuckets = 18; //Number of spectrum buckets
	public final int maxAmp = 40; //Range of possible amplitudes for each bucket

	private void setup() {
		bucketXStart = (int) (sW*0.1);
		bucketW = (sW-(bucketXStart*2))/numBuckets;
		bucketXOff = (int) (bucketW*0.1);
		bucketY = (int) (sH*0.6);

		buckets = new ArrayList<ReflectiveBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new ReflectiveBucket(new Point(bucketXStart+(i*bucketW)+bucketXOff, bucketY), (int) (bucketW*0.8))); 

		visualMags = new double[numBuckets];

		//Add specialised button
		buttons.add(new Button("B/S", Color.GREEN, Color.BLACK, "toggleBlackStrobe", "isBlackStrobing", true, this));
		buttons.add(new Button("W/S", Color.GREEN, Color.BLACK, "toggleWhiteStrobe", "isWhiteStrobing", true, this));
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
		//Cut unwanted frequencys and average into set number of buckets
		if (av.magnitudes==null) return;
		double realMags[] = cutandAverageMags(0, 100);

		for (int i=0; i<numBuckets; i++) {
			if (increaseFall&&visualMags[i]>realMags[i]) visualMags[i]--;
			if (visualMags[i]<realMags[i]) visualMags[i] = realMags[i];
		}

		//Update buckets
		for (int i=0; i<numBuckets; i++) buckets.get(i).updateMag(visualMags[i]);
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

	private void drawBlocks(Graphics2D g) {
		Color override = null;
		if (strobing&&whiteStrobe&&strobeOn) override = Color.WHITE;
		for (ReflectiveBucket b : buckets) b.drawBucket(g, override);
	}

	public Painter getPainter() {return new ReflectivePainter(this);}

	public ReflectiveRender(Controller av) {
		super(av, "Reflective");
		setup();
	}
}

class ReflectivePainter extends Painter {

	public ReflectivePainter(Render render) {
		super(render);
	}

	public void iterate(int count) {
		//Increment
		((ReflectiveRender) render).incrementVisualMags(true);
	}

}
