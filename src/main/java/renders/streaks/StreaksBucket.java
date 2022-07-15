package main.java.renders.streaks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

import main.java.core.Controller;

public class StreaksBucket {

	private StreaksRender r;
	private double mag;
	private double gradient;
	private int speed;
	private Color col;

	private double[] buffer; //The buffer which allows values from the past to be displayed on the line
	private final int bufferSize = 200; //Max size of buffer

	public StreaksBucket(int bucket, StreaksRender r) {
		this.r = r;
		this.gradient = r.getGradient(bucket);
		this.buffer = new double[bufferSize];
		this.speed = Controller.random(1, 2);
		setColor(new Color(200, 200, 255), new Color(255, 255, 255));
	}

	public void setColor(Color c1, Color c2) {
		int r = Controller.random(c1.getRed(), c2.getRed());
		int g = Controller.random(c1.getGreen(), c2.getGreen());
		int b = Controller.random(c1.getBlue(), c2.getBlue());
		this.col = new Color(r, g, b);
	}

	public void updateMag(double mag) {
		//Change value
		if (mag>this.mag) {
			this.mag = mag;
		}
		else this.mag--;
		if (this.mag<0) this.mag = 0;

		//Check speed
		int speed = this.speed;
		if (r.rushSpeed) speed *= 8;
		
		//Update buffer
		for (int z=0; z<speed; z++) { //Do multiple for different speeds
			for (int i=bufferSize-2; i>=0; i--) {
				buffer[i+1] = buffer[i]; //Shift values right
				if (i==0) buffer[i] = this.mag; //Add new value
			}
		}
	}

	public void drawStreak(Graphics2D g) {
		//Allow for a randomised white strobe
		if (r.isWhiteStrobing()&&Controller.random(0, 20)!=0) return;
		
		//Draw buffer
		for (int i=0; i<bufferSize; i++) {
			
			double x = r.sW-(i*((double) r.sW/bufferSize)); //Move along the line
			int y = r.getRealPositionOnLine(gradient, (int) x);
			y += 50;
			int size = (int) getSize(x)+2;
			int op = (int) buffer[i]*5;
			if (op>255) op = 255;
			
			//Glow
			if (op>50) {
				op /= 8;
				for (int z=0; z<10; z++) {
					op -= 1;
					if (op<=0) break;
					g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
					size = (int) getSize(x)+2+z;
					g.fillRect((int) x-(size/2), r.sH-y-(size/2), size, size);
				}
			}
			
			//Dot
			op = (int) buffer[i]*5;
			if (op>255) op = 255;
			size = (int) getSize(x)+2;
			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
			g.fillOval((int) x-(size/2), r.sH-y-(size/2), size, size);
		}

	}

	/**
	 * Falling off function for size of dot to give impression of distance
	 * as it moves closer to end of line at origin point size.
	 * 
	 * @param x
	 * @return
	 */
	public double getSize(double x) {
		return -Math.pow(2, 0.0026*x)+12;
	}
}
