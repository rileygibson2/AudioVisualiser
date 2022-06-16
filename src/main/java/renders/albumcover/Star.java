package main.java.renders.albumcover;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import main.java.core.AudioVisualiser;

public class Star {
	//Basic
	AlbumCoverRender render;
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
	private double flickerSpeed = 0.015;
	
	public Star(Rectangle bounds, AlbumCoverRender render) {
		this.render = render;
		this.bounds = bounds; //Stored for repositioning
		this.size = 2;
		this.pos = new Point(AudioVisualiser.random(bounds.x, bounds.width), AudioVisualiser.random(bounds.y, bounds.height));
		this.c = new Color(200, 200, 200);
		
		this.haloCut = AudioVisualiser.random(0, 80)/100d; //Randomise starting point for twinkle
		this.flickerSpeed = AudioVisualiser.random(5, 20)/1000d;
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
		haloCut += flickerDir*flickerSpeed; //Move halo
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
