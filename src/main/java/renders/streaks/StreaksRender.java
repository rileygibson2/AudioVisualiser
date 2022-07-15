package main.java.renders.streaks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import main.java.core.Controller;
import main.java.renders.Button;
import main.java.renders.Painter;
import main.java.renders.Render;

public class StreaksRender extends Render {

	//Buckets
	List<StreaksBucket> buckets;

	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;

	//Magnitude formatting
	public final int numBuckets = 40; //Number of spectrum buckets
	public final int maxAmp = 500; //Range of possible amplitudes for each bucket
	public Integer[] bucketShuffle; //Used to randomise which visual bucket an amplitude bucket is sent too.

	public Point origin;
	public boolean rushSpeed;

	private void setup() {
		origin = new Point((int) (sW+(0.16*sW)), sH);
		buckets = new ArrayList<StreaksBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new StreaksBucket(i, this)); 

		//Initialise random bucket mapping
		bucketShuffle = new Integer[numBuckets];
		for (int i=0; i<numBuckets; i++) bucketShuffle[i] = i;
		List<Integer> list = Arrays.asList(bucketShuffle);
		Collections.shuffle(list);
		list.toArray(bucketShuffle);

		//Add specialised button
		buttons.add(new Button("B/S", Color.GREEN, Color.BLACK, "toggleBlackStrobe", "isBlackStrobing", true, this));
		buttons.add(new Button("W/S", Color.GREEN, Color.BLACK, "toggleWhiteStrobe", "isWhiteStrobing", true, this));
		buttons.add(new Button("Speed", Color.GREEN, Color.BLACK, "toggleRushSpeed", "isRushSpeed", true, this));
	}

	public void paint(Graphics2D g) {
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		//Elements
		drawStreaks(g);
	}

	/**
	 * Node - Smooth downwards magnitude transitions are handled by the bucket 
	 * class for this render.
	 */
	public void incrementVisualMags(boolean increaseFall) {
		if (av.magnitudes==null) return;
		double realMags[] = cutandAverageMags(0, 100);

		//Update buckets
		for (int i=0; i<numBuckets; i++) {
			/*
			 * Use bucketShuffle to send mags to different bucket. This helps
			 * make it look less like the bass is all coming from one place,
			 * makes it look less frequency analyser and more cool. 
			 */
			buckets.get(i).updateMag(realMags[bucketShuffle[i]]);
		}
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

	/**
	 * Fetches the gradient of a bucket, based on the number of that bucket.
	 * 
	 * The line of a bucket is just the line going through the origin point,
	 * of a certain gradient. This method that line gradient based on a falling
	 * off function. This allows the lines to appear evenly spaced, as
	 * having a linear increase in gradient for lines from a single
	 * origin point will make them bunch up.
	 * 
	 * @param bucket - the bucket to fetch the line gradient for
	 */
	public double getGradient(int bucket) {
		double m = 0;
		int count = 0;

		while (count<bucket) {
			m += ((0.1*Math.pow(m, 3))+0.05)/2;
			count++;
		}
		return m;
	}

	/**
	 * Returns a real y point in xy space when given the gradient for a
	 * bucket's line and a position on that line.
	 * 
	 * @param m - the gradient of the line
	 * @param x - the position on the line
	 * @return - the real y point
	 */
	public int getRealPositionOnLine(double m, int x) {
		int y = (int) ((m*(x-origin.x))+origin.y);
		return y;
	}

	private void drawStreaks(Graphics2D g) {
		for (StreaksBucket b : buckets) b.drawStreak(g);
	}
	
	public void toggleRushSpeed() {if (windowVisible()) this.rushSpeed = !this.rushSpeed;}

	public boolean isRushSpeed() {return this.rushSpeed;}
	
	public Painter getPainter() {return new ReflectivePainter(this);}

	public StreaksRender(Controller av) {
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
		((StreaksRender) render).incrementVisualMags(true);
	}

}
