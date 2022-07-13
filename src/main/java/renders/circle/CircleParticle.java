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
	Point diff; //Difference from actual point on band
	static int diffSize = 20;
	int opacity;
	Color col;

	public CircleParticle(int bucket, CircleRender r) {
		this.bucket = bucket;
		this.r = r;
		this.size = Controller.random(1, 4);
		this.speed = Controller.randomD(0.8, 2);
		this.opacity = Controller.random(50, 255);
		this.col = new Color(255, 240, 255);

		if (bucket>160&&bucket<240) {
			this.speed = Controller.randomD(0.04, 0.8);
			System.out.println(this.speed);
		}

		//this.speed = 0.2;
		diff = new Point(Controller.random(-diffSize, diffSize), Controller.random(-diffSize, diffSize));
	}

	public void increment(double mag) {
		mag *= 4;
		if (this.mag<mag&&Math.abs(mag-this.mag)>3) { //Bump value up to mag
			this.mag = mag;
		}
		else { //Fall the particle
			this.mag -= speed;
		}

		if (this.mag<0) this.mag = 0;
	}

	public void draw(Graphics2D g, int xStart, int yStart) {
		//g.setColor(Color.PINK);
		g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), opacity));
		if (mag==0) g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 5));
		//Bottom half
		Point pos = r.getRealPosOnNormal(this, false);
		if (mag>0) { //Add random position adjustment for texture
			pos.x += diff.x;
			pos.y += diff.y;
		}
		g.fillOval((int) (xStart+pos.x-size/2), (int) (yStart+pos.y-size/2), size, size);

		//Top half of circle
		pos = r.getRealPosOnNormal(this, true);
		if (mag>0) { //Add random position adjustment for texture
			pos.x += diff.x;
			pos.y += diff.y;
		}
		g.fillOval((int) (xStart+pos.x-size/2), (int) (yStart+pos.y-size/2), size, size);
	}
}
