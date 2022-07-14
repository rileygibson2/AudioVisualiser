package main.java.renders.streaks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import main.java.core.Controller;
import main.java.renders.Button;
import main.java.renders.Painter;
import main.java.renders.Render;

public class Streaks extends Render {

	//Buckets
	List<StreaksBucket> buckets;
	private int bucketXStart, bucketW, bucketY;
	static final int curve = 12;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public final int numBuckets = 200; //Number of spectrum buckets
	public final int maxAmp = 40; //Range of possible amplitudes for each bucket

	private void setup() {
		buckets = new ArrayList<StreaksBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new StreaksBucket((int) (bucketW*0.9), this)); 

		visualMags = new double[numBuckets];

		//Add specialised button
	}

	public void paint(Graphics2D g) {
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		//Elements
		drawStreaks(g);
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

	private void drawStreaks(Graphics2D g) {
		
	}

	public Painter getPainter() {return new ReflectivePainter(this);}

	public Streaks(Controller av) {
		super(av, "Streaks");
		setup();
	}
}

class ReflectivePainter extends Painter {

	public ReflectivePainter(Render render) {
		super(render);
	}

	public void iterate(int count) {
		//Increment
		((Streaks) render).incrementVisualMags(true);
	}

}
