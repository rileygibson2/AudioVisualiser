package main.java.renders.reflective;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import main.java.core.AudioVisualiser;
import main.java.renders.Painter;
import main.java.renders.Render;

public class ReflectiveBlocksRender extends Render implements KeyListener {
		
	//Buckets
	List<ReflectiveBucket> buckets;
	private int ceiling; //Max height of spectrum cells
	private int bucketXStart, bucketW, bucketXOff, bucketY;
	static final int curve = 12;
	
	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;
	
	//Magnitude formatting
	public final int numBuckets = 15; //Number of spectrum buckets
	public final int maxAmp = 40; //Range of possible amplitudes for each bucket
	
	private void setup() {
		bucketXStart = (int) (sW*0.1);
		bucketW = (sW-(bucketXStart*2))/numBuckets;
		bucketXOff = (int) (bucketW*0.1);
		bucketY = (int) (sH*0.7);
		
		buckets = new ArrayList<ReflectiveBucket>();
		for (int i=0; i<numBuckets; i++) buckets.add(new ReflectiveBucket(new Point(bucketXStart+(i*bucketW)+bucketXOff, bucketY), (int) (bucketW*0.8), this)); 
		
		visualMags = new double[numBuckets];
	}
	
	@Override
	public void paintComponent(Graphics g1d) {
		painting = true;
		Graphics2D g = (Graphics2D) g1d;
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);
		
		//Elements
		drawBlocks(g);
		
		//Blackout
		if (blackoutOp>0) {
			g.setColor(new Color(0, 0, 0, blackoutOp));
			g.fillRect(0, 0, sW, sH);
		}
		painting = false;
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
		for (int i=0; i<numBuckets; i++) buckets.get(i).mag = visualMags[i];
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
		for (ReflectiveBucket b : buckets) b.drawBucket(g);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_B:
			blackout = !blackout;
			break;
		}
	}

	public ReflectiveBlocksRender(AudioVisualiser av) {
		super(av, 1450, 900);
		Render.painter = new ReflectivePainter(this);
		setup();
		addKeyListener(this);
		
		startPaint();
	}

	public static ReflectiveBlocksRender initialise(AudioVisualiser av) {
		ReflectiveBlocksRender panel = new ReflectiveBlocksRender(av);
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

	@Override
	public void run() {
		ReflectiveBlocksRender r = (ReflectiveBlocksRender) render;
		
		int count = 0;
		while (render.paint) {
			try {Thread.sleep(20);}
			catch (InterruptedException er) {throw new Error("Sleep error");}
			count++;
			if (count>spacing) count = 0;
			
			//Paint
			r.repaint();
			
			//Increment
			r.incrementVisualMags(true); //Just update values
			
			if (r.blackout&&r.blackoutOp<255) r.blackoutOp++;
			if (!r.blackout&&r.blackoutOp>0) r.blackoutOp--;
		}
	}

}
