package main.java.renders.reflective;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.renders.Col;

public class ReflectiveBucket {

	private Point pos;
	private int w;
	private double mag;

	//Peak
	private double peak;
	private int maxPeakLife = 10;
	private int peakLife;

	private Col col, pCol;
	private double rDim = 0.25; //Amount color is dimmed in reflection

	public ReflectiveBucket(Point pos, int w) {
		this.pos = pos;
		this.w = w;
		this.col = new Col(28, 181, 246);
		this.pCol = new Col(50, 50, 50);
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

	public void drawBucket(Graphics2D g, Color override) {
		int h = (int) (mag*8);
		Color col = this.col.getColor();
		if (override!=null) col = override;
		
		//Draw main gradient
		for (int y=0; y<h; y++) {
			int op = h-y;
			if (op>255) op = 255; if (op<0) op = 0;

			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
			g.fillRect(pos.x, pos.y-y, w, 1); //Main
			g.setColor(new Color((int) (col.getRed()*rDim), (int) (col.getGreen()*rDim), (int) (col.getBlue()*rDim), op));
			g.fillRect(pos.x, pos.y+y, w, 1); //Reflection
		}

		//Draw biggest peak gradient
		/*if (biggestPeak>0) { //Avoid initial flash
			int op = 255- (255/maxPeakLife)*peakLife; //Fade peak out

			g.setColor(new Color(pCol.getRed(), pCol.getGreen(), pCol.getBlue(), op));
			g.fillRect(pos.x, (int) (pos.y-(biggestPeak*8)), w, 1);
			g.setColor(new Color((int) (col.getRed()*rDim), (int) (col.getGreen()*rDim), (int) (col.getBlue()*rDim), op));
			g.fillRect(pos.x, (int) (pos.y+(biggestPeak*8)), w, 1);

		}*/
	}
}
