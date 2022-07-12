package main.java.renders.greenblocks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.renders.Col;

public class GreenBucket {

	private int w;
	private double mag;

	//Peak
	private double peak;
	private int maxPeakLife = 10;
	private int peakLife;

	private Col col;
	private double rDim = 0.2; //Amount color is dimmed in reflection

	public GreenBucket(int w) {
		this.w = w;
		this.col = new Col(54, 247, 0);
	}

	public void updateMag(double mag) {
		this.mag = mag;
		//Update peak;
		if (mag>peak&&mag>30) {
			peak = mag;
			peakLife = 0;
		}
		else if (peakLife>=maxPeakLife) {
			peak = 0;
			peakLife = 0;
		}
		else peakLife++;
	}

	public void drawBucket(Graphics2D g, Point pos) {
		int h = (int) (mag*10);
		Color col = this.col.getColor();
		
		//Draw main gradient
		for (int y=0; y<h; y++) {
			int op = h-y;
			if (op>255) op = 255; if (op<0) op = 0;

			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
			g.fillRect(pos.x, (pos.y-y), w, 1); //Main
			//g.setColor(new Color((int) (col.getRed()*rDim), (int) (col.getGreen()*rDim), (int) (col.getBlue()*rDim), op));
			g.fillRect(pos.x, pos.y+y, w, 1); //Reflection
		}
	}
}
