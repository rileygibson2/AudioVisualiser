package main.java.core;

import java.awt.Color;

/**
 * Represents a scalable color stored in percentages.
 * 
 * @author thesmileyone
 */
public class Col {
	double r, g, b;
	
	public Col(int r, int g, int b) {
		this.r = r/255d;
		this.g = g/255d;
		this.b = b/255d;
	}
	
	public Color getColor() {
		return new Color((int) (r*255), (int) (g*255), (int) (b*255));
	}
	
	public Color getColor(double mult) {
		int rd = (int) ((r*mult)*255);
		int gr = (int) ((g*mult)*255);
		int bl = (int) ((b*mult)*255);
		if (rd>255) rd = 255; if (rd<0) rd = 0;
		if (gr>255) gr = 255; if (gr<0) gr = 0;
		if (bl>255) bl = 255; if (bl<0) bl = 0;
		return new Color(rd, gr, bl);
	}
}
