package main.java.core;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

public class Star {
	//Basic
	SpectrumRender render;
	Rectangle bounds;
	public int size;
	public Point pos;
	public Color c;
	
	//Glow and Halo
	public int glowCut = 30; //The maximum opacity of the glow
	public int haloLength = 30;
	public double haloIncrements;
	public double haloCut;
	
	private int flickerDir = -1; //Used to control direction of flickering and modulation
	
	public Star(Rectangle bounds, SpectrumRender render) {
		this.render = render;
		this.bounds = bounds; //Stored for repositioning
		this.size = 2;
		this.pos = new Point(AudioVisualiser.random(bounds.x, bounds.width), AudioVisualiser.random(bounds.y, bounds.height));
		this.c = new Color(200, 200, 200);
		
		haloCut = AudioVisualiser.random(0, 80)/100d; //Randomise starting point for twinkle
		calculateIncrements();
	}
	
	public void reposition() {
		this.pos = new Point(AudioVisualiser.random(bounds.x, bounds.width), AudioVisualiser.random(bounds.y, bounds.height));
	}
	
	/**
	 * Saves render computation at every paint
	 */
	private void calculateIncrements() {
		this.haloIncrements = haloCut/haloLength;
	}
	
	public void flicker() {
		haloCut += flickerDir*0.015; //Move halo
		if (haloCut%0.05<0.01) {
			glowCut += flickerDir; //Move glow but less frequently
		}
		if (haloCut>=0.8) {
			haloCut = 0.8;
			flickerDir = -flickerDir;
		}
		if (haloCut<0) {
			haloCut = 0;
			flickerDir = -flickerDir;
		}
		if (glowCut>30) glowCut = 30;
		calculateIncrements();
	}
}
