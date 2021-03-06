package main.java.renders.circle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import main.java.core.Controller;

public class CircleParticle {
	CircleRender r;
	int size;
	int glowSize;
	double speed;
	int bucket;
	double mag; //The magnitude for the particle
	Point diff; //Difference from actual point on band

	Color col;
	int opacity; //Opacity of particle
	int glowOpacity; //Opacity of glow around particle
	int fadeOutOp; //Extra opacity to allow particle to fade out at 0 mag position

	//Statics
	static int diffSize = 20;

	public CircleParticle(int bucket, CircleRender r) {
		this.bucket = bucket;
		this.r = r;
		this.size = Controller.random(1, 4);
		this.glowSize = Controller.random(10, 20);
		this.speed = Controller.randomD(0.8, 2);
		this.opacity = Controller.random(50, 255);
		this.glowOpacity = Controller.random(5, 20);
		diff = new Point(Controller.random(-diffSize, diffSize), Controller.random(-diffSize, diffSize));

		//Faster speed for circle extremes
		if (bucket>160&&bucket<240) this.speed = Controller.randomD(0.04, 0.8);
	}

	public void setColor(Color c1, Color c2) {
		int r = Controller.random(c1.getRed(), c2.getRed());
		int g = Controller.random(c1.getGreen(), c2.getGreen());
		int b = Controller.random(c1.getBlue(), c2.getBlue());
		this.col = new Color(r, g, b);
	}

	public void increment(double mag) {
		//Add the multipliers to different parts of the circle
		if (bucket>160&&bucket<240) mag*=4;
		else mag *= 8;
		
		if (this.mag<mag&&Math.abs(mag-this.mag)>3) { //Bump value up to mag
			this.mag = mag;
			fadeOutOp = opacity;
		}
		else { //Fall the particle
			this.mag -= speed;
		}

		if (this.mag<0) this.mag = 0;
		if (this.mag==0) fadeOutOp -= 20;
	}
	
	public void draw(Graphics2D g, int xStart, int yStart) {
		for (int i=0; i<2; i++) { //Do twice, once mirrored
			Point pos = r.getRealPosOnNormal(this, (i==0) ? false : true);
			int op = opacity;
			
			if (mag>0||fadeOutOp>0) { //Don't draw dot at no magnitude position (except when fading out)
				if (mag==0) op = fadeOutOp;
				g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
				
				//Adding position adjustment
				pos.x += diff.x; pos.y += diff.y;
				if (!r.unfocused) g.fillOval((int) (xStart+pos.x-size/2), (int) (yStart+pos.y-size/2), size, size);
			}	
			//Glow
			if (mag>0||r.ring) {
				g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), glowOpacity));
				if (mag==0) g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (glowOpacity-10<0) ? 5 : glowOpacity-10));
				g.fillOval((int) (xStart+pos.x-glowSize/4), (int) (yStart+pos.y-glowSize/2), glowSize, glowSize);
			}
		}
	}
}
