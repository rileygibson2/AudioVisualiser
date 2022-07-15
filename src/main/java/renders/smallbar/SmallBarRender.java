package main.java.renders.smallbar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.java.core.Controller;
import main.java.renders.Button;
import main.java.renders.Painter;
import main.java.renders.Render;

public class SmallBarRender extends Render {

	//Buckets
	List<SmallBarBucket> buckets;
	private int bucketXStart, bucketW, bucketXOff, bucketY;
	static final int curve = 12;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public final int numBuckets = 6; //Number of spectrum buckets
	public final int maxAmp = 250; //Range of possible amplitudes for each bucket

	private void setup() {
		bucketXStart = (int) (sW*0.35);
		bucketW = (sW-(bucketXStart*2))/numBuckets;
		bucketXOff = (int) (bucketW*0.08);
		bucketW -= bucketXOff*2;
		bucketY = (int) (sH*0.5);

		buckets = new ArrayList<SmallBarBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new SmallBarBucket(new Point(bucketXStart+(i*(bucketW+bucketXOff*2)), bucketY), bucketW)); 

		visualMags = new double[numBuckets];

		//Add specialised button
		//buttons.add(new Button("B/S", Color.GREEN, Color.BLACK, "toggleBlackStrobe", "isBlackStrobing", true, this));
		//buttons.add(new Button("W/S", Color.GREEN, Color.BLACK, "toggleWhiteStrobe", "isWhiteStrobing", true, this));
	}

	public void paint(Graphics2D g) {
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		//Elements
		drawBars(g);
	}

	/**
	 * Allows for the transition element between different magnitudes.
	 * Means that buckets will not jump down when magnitude goes from high to low,
	 * but will jump with a low to high transition.
	 */
	public void incrementVisualMags(boolean increaseFall) {
		//Cut unwanted frequencys and average into set number of buckets
		if (av.magnitudes==null) return;
		double realMags[] = cutandAverageMags(0, 900);

		for (int i=0; i<numBuckets; i++) {
			if (increaseFall&&visualMags[i]>realMags[i]) visualMags[i]-=4;
			if (visualMags[i]<realMags[i]) {
				visualMags[i] += (realMags[i]-visualMags[i])*0.1;
			}
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
				
				if ((sum*6)>maxAmp) {
					if (bucketCount==1) averaged[bucketCount-1] = maxAmp*0.8;
					else averaged[bucketCount-1] = maxAmp;
				}
				else {
					if (bucketCount==1) averaged[bucketCount-1] = sum;
					else averaged[bucketCount-1] = sum*6;
				}
				
				sum = 0;
				count = 0;
				bucketCount++;
			}

			if (av.magnitudes[i]>0) sum += av.magnitudes[i];
			count++;
		}
		
		System.out.println(Arrays.toString(averaged));
		return averaged;
	}

	private void drawBars(Graphics2D g) {
		Color override = null;
		if (strobing&&whiteStrobe&&strobeOn) override = Color.WHITE;
		for (SmallBarBucket b : buckets) b.drawBucket(g, override);
	}

	public Painter getPainter() {return new ReflectivePainter(this);}

	public SmallBarRender(Controller av) {
		super(av, "SmallBar");
		setup();
	}
}

class ReflectivePainter extends Painter {

	public ReflectivePainter(Render render) {
		super(render);
	}

	public void iterate(int count) {
		//Increment
		((SmallBarRender) render).incrementVisualMags(true);
	}

}
