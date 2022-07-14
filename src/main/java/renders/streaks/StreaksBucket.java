package main.java.renders.streaks;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.renders.Col;

public class StreaksBucket {

	private Streaks r;
	private int w;
	private double mag;

	private Col col;

	public StreaksBucket(int w, Streaks r) {
		this.r = r;
		this.w = w;
		this.col = new Col(54, 247, 0);
	}

	public void updateMag(double mag) {
		
	}

	public void drawBucket(Graphics2D g, Point pos, Color override) {
		int h = (int) (mag*10);
		Color col = this.col.getColor();
		if (override!=null) col = override;

	}
}
