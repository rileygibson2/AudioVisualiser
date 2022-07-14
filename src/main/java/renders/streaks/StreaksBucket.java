package main.java.renders.streaks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;

import main.java.core.Controller;
import main.java.renders.Col;

public class StreaksBucket {

	private StreaksRender r;
	private double mag;
	private int bucket;
	private double gradient;
	private int speed;
	private Color col;

	double buffer[]; //The buffer which allows values from the past to be displayed on the line
	final int bufferSize = 200;

	public StreaksBucket(int bucket, StreaksRender r) {
		this.r = r;
		this.bucket = bucket;
		this.gradient = r.getGradient(bucket);
		this.buffer = new double[bufferSize];
		this.speed = Controller.random(1, 2);
		setColor(new Color(100, 255, 100), Color.WHITE);
	}
	
	public void setColor(Color c1, Color c2) {
		int r = Controller.random(c1.getRed(), c2.getRed());
		int g = Controller.random(c1.getGreen(), c2.getGreen());
		int b = Controller.random(c1.getBlue(), c2.getBlue());
		this.col = new Color(r, g, b);
	}

	public void updateMag(double mag) {
		if (mag>this.mag) this.mag = mag;
		else this.mag--;
		if (this.mag<0) this.mag = 0;

		//Update buffer
		for (int z=0; z<speed; z++) { //Do multiple for different speeds
			for (int i=bufferSize-2; i>=0; i--) {
				buffer[i+1] = buffer[i]; //Shift values right
				if (i==0) buffer[i] = this.mag; //Add new value
			}
		}
	}

	public void drawBucket(Graphics2D g) {
		//Draw buffer
		for (int i=0; i<bufferSize; i++) {
			int x = r.sW-(i*(r.sW/bufferSize)); //Move along the line
			int y = r.getRealPositionOnLine(gradient, x);

			int op = (int) buffer[i]*5;
			if (op>255) op = 255;
			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));

			int size = (int) getSize(x)+2;
			g.fillOval(x-(size/2), r.sH-y-(size/2), size, size);

			//Glow
			if (op>80) op = 80;
			for (int z=0; z<10; z++) {
				op -= 10;
				if (op<=0) break;
				g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));

				size = (int) getSize(x)+2+z;
				g.fillOval(x-(size/2), r.sH-y-(size/2), size, size);
			}
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
