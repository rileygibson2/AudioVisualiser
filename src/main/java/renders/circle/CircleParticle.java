package main.java.renders.circle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.core.Controller;

public class CircleParticle {
	CircleRender r;
	int size;
	double speed;
	
	int bucket;
	double mag; //The magnitude for the particle
	/*
	 * The val here is used to defeat the difference in increments at different
	 * points on the circle. Means particles can descend at a visually appealing
	 * rate no matter their normal line
	 */
	
	
	Point diff; //Difference from actual point on band
	
	public CircleParticle(int bucket, CircleRender r) {
		this.bucket = bucket;
		this.r = r;
		this.size = Controller.random(1, 4);
		this.speed = Controller.randomD(0.3, 1);
		
		if (bucket>160&&bucket<240) {
			this.speed = Controller.randomD(0.04, 0.8);
			System.out.println(this.speed);
		}
		
		//this.speed = 0.2;
		diff = new Point(Controller.random(-10, 10), Controller.random(-10, 10));
	}
	
	public void increment(double mag) {
		if (this.mag<mag&&Math.abs(mag-this.mag)>3) { //Bump value up to mag
			this.mag = mag;
		}
		else { //Fall the particle
			this.mag -= speed;
		}
		
		if (this.mag<0) this.mag = 0;
	}
	
	public void draw(Graphics2D g, int xStart, int yStart) {
		Point pos = r.getRealPosOnNormal(this, false);
		
		g.setColor(Color.PINK);
		g.fillOval((int) (xStart+pos.x-size/2), (int) (yStart+pos.y-size/2), size, size);
	}
}
