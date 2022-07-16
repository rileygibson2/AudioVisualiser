package main.java.renders.bars;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.core.Col;

public class BarsBucket {

	private BarsRender r;
	private int w;
	private double mag;

	//Peak
	private double peak;
	private int maxPeakLife = 10;
	private int peakLife;

	private Col col;
	private double rDim = 0.2; //Amount color is dimmed in reflection

	public BarsBucket(int w, BarsRender r) {
		this.r = r;
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

	public void drawBucket(Graphics2D g, Point pos, Color override) {
		int h = (int) (mag*10);
		Color col = this.col.getColor();
		if (override!=null) col = override;

		//Draw main gradient
		for (int y=0; y<h; y++) {
			int op = h-y;
			if (op>255) op = 255; if (op<0) op = 0;

			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
			g.fillRect(pos.x, (pos.y-y), w, 1); //Main
			g.fillRect(pos.x, pos.y+y, w, 1); //Reflection
		}

		//Draw white base
		if (r.inner&&h>100) { //If bucket has a magnitude
			for (int y=0; y<(h/5); y++) {
				int op = (255/(h/5))*((h/5)-y);
				op*=0.8;
				if (op>255) op = 255; if (op<0) op = 0;
				g.setColor(new Color(r.colors[r.color].getRed(), r.colors[r.color].getGreen(), r.colors[r.color].getBlue(), op));
				g.fillRect(pos.x, (pos.y-y), w, 1); //Main
				g.fillRect(pos.x, pos.y+y, w, 1); //Reflection
			}
		}
	}
}
