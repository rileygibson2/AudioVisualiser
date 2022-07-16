package main.java.renders.smallbar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.core.Col;
import main.java.core.Controller;

public class SmallBarBucket {

	private Point pos;
	private int w;
	private double mag;
	private Color col;
	private int lowest = 30; //Lowest mag a bucket can have

	public SmallBarBucket(Point pos, int w, Color col) {
		this.pos = pos;
		this.w = w;
		this.col = col;
		//setColor(new Color(0, 100, 255), new Color(100, 255, 255));
	}
	
	public void setColor(Color c1, Color c2) {
		int r = Controller.random(c1.getRed(), c2.getRed());
		int g = Controller.random(c1.getGreen(), c2.getGreen());
		int b = Controller.random(c1.getBlue(), c2.getBlue());
		this.col = new Color(r, g, b);
	}

	public void updateMag(double mag) {this.mag = mag;}

	public void drawBucket(Graphics2D g, Color override) {
		int h = (int) mag;
		if (h<lowest) h = lowest;
		Color col = this.col;
		if (override!=null) col = override;
		g.setColor(col);
		
		g.fillRoundRect(pos.x, pos.y-h, w, h*2, 65, 65); //Bucket

	}
}
