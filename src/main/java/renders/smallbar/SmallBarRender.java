package main.java.renders.smallbar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import main.java.core.Controller;
import main.java.core.Painter;
import main.java.core.Render;

public class SmallBarRender extends Render {

	//Buckets
	List<SmallBarBucket> buckets;
	private int bucketXStart, bucketW, bucketXOff, bucketY;
	static final int curve = 12;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public final int numBuckets = 7; //Number of spectrum buckets
	public final int maxAmp = 250; //Range of possible amplitudes for each bucket

	private final Color colors[] = {
			new Color(216, 200, 32),
			new Color(37, 184, 204),
			new Color(37, 203, 129),
			new Color(227, 224, 223),
			new Color(158, 46, 53),
			new Color(220, 159, 30),
			new Color(23, 44, 160)
	};
	
	private void setup() {
		bucketXStart = (int) (sW*0.31);
		bucketW = (sW-(bucketXStart*2))/numBuckets;
		bucketXOff = (int) (bucketW*0.08);
		bucketW -= bucketXOff*2;
		bucketY = (int) (sH*0.5);

		buckets = new ArrayList<SmallBarBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new SmallBarBucket(new Point(bucketXStart+(i*(bucketW+bucketXOff*2)), bucketY), bucketW, colors[i])); 

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
		double realMags[] = cutandAverageMags(0, 100);

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
		int bucketCut = (maxcut-mincut)/(numBuckets+1);
		int bucketCount = 0;

		for (int i=mincut; i<maxcut; i++) {
			if (i>=mincut+((bucketCount+1)*bucketCut)) {
				if (bucketCount>=numBuckets) break;
				
				if (bucketCount==0) averaged[bucketCount] = sum;
				else averaged[bucketCount] = sum*6;
				if (averaged[bucketCount]>maxAmp) averaged[bucketCount] = maxAmp; 
				
				sum = 0;
				bucketCount++;
			}

			if (av.magnitudes[i]>0) sum += av.magnitudes[i];
		}
		
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
