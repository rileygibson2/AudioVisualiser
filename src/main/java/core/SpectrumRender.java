package main.java.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SpectrumRender extends JPanel implements KeyListener {
	
	AudioVisualiser av;
	public static JFrame frame;
	public static int sW = 1450, sH = 900;
	static Painter painter;
	public boolean paint; //Kill switch for paint thread
	public boolean painting; //Whether a paint is currently occuring, used for synchronisation
	
	//Color scroll
	int stage; //What stage of the color scroll we are in
	int point; //At what point in that stage are we in
	
	//Cell
	int[] visualMags; //What the magnitudes are in the render, allows for the transition down magnitudes
	int ceiling; //Max height of spectrum cells
	int cellW, cellXOff, cellH, cellYOff;
	static final int curve = 12;
	
	//Stars
	public Set<Star> stars;
	
	//Blackout
	public boolean blackout = false;
	int blackoutOp = 0;
	
	public void setup() {
		ceiling = (int) (sH*0.8);
		cellW = sW/av.buckets;
		cellXOff = (int) (cellW*0.1);
		cellW = (int) (cellW*0.8);
		
		cellH = ceiling/av.ampRange;
		cellYOff = (int) (cellH*0.15);
		cellH = (int) (cellH*0.7);
		
		visualMags = new int[av.ampRange];
		stage = 1;
		point = 0;
		stars = new HashSet<Star>();
		for (int i=0; i<100; i++) {
			stars.add(new Star(new Rectangle(0, sH/2, sW, sH), this));
		}
	}
	
	@Override
	public void paintComponent(Graphics g1d) {
		painting = true;
		Graphics2D g = (Graphics2D) g1d;
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);
		
		//Elements
		drawStars(g);
		drawBuckets(g);
		
		//Blackout
		if (blackoutOp>0) {
			g.setColor(new Color(0, 0, 0, blackoutOp));
			g.fillRect(0, 0, sW, sH);
		}
		painting = false;
	}
	
	public void flickerStars() {
		for (Star s : stars) {
			s.flicker();
			//Maybe reposition this star if it is invisible
			if (AudioVisualiser.random(0, 200)==0&&s.haloCut<0.05) s.reposition();
		}
	}
	
	/**
	 * Allows for the transition element between different magnitudes.
	 * Means that buckets will not jump down when magnitude goes from high to low,
	 * but will jump with a low to high transition.
	 */
	public void incrementVisualMags() {
		for (int i=0; i<av.buckets; i++) {
			if (visualMags[i]>av.testMags[i]) visualMags[i]--;
			if (visualMags[i]<av.testMags[i]) visualMags[i] = av.testMags[i];
		}
	}
	
	/**
	 * Returns the color of the given stage and point in stage of a color
	 * scroll. Also takes an offset to give a color a specified distance
	 * earlier or later than the current point in the scroll.
	 * 
	 * @param stage - what stage (1-6) of the scroll we are in
	 * @param point - what point within that stage we are in
	 * @param offset - what offset from the current point and stage we are in
	 * @return
	 */
	private Color colorScroll(int offset) {
		int stage = this.stage;
		int point = this.point;
		
		//Handle offset
		if (offset>0) {
			double diff = (double) offset/255;
			stage = stage+(int) Math.floor(diff);
			point += (255*(diff-Math.floor(diff)));
			
			//Validate
			if (point>255) {
				stage++;
				point -= 255;
			}
			if (stage>6) stage -= 6;
		}
		
		Color c = null;
		switch(stage) {
		case 1: c = new Color(255, point, 0); break;
		case 2: c = new Color(255-point, 255, 0); break;
		case 3: c = new Color(0, 255, point); break;
		case 4: c = new Color(0, 255-point, 255); break;
		case 5: c = new Color(point, 0, 255); break;
		case 6: c = new Color(255, 0, 255-point);
		}
		return c;
	}
	
	public void increaseColorScroll(int inc) {
		point += inc;
		//Validate
		if (point>255) {
			stage++;
			point -= 255;
		}
		if (stage>6) stage -= 6;
	}
	
	private void drawStars(Graphics2D g) {
		Color c;
		double opSoFar; //Used to find the opacity already present in a glow ring. This stops elements behind star from being hidden by an absolute opacity.
		for (Star s : stars) {
			c = s.c;
			opSoFar = 0;
			
			//Glow effect
			for (int i=s.glowCut; i>0; i--) {
				//Draw ring
				//double op = (s.glowCut-i)/100d;
				double op = ((s.glowCut-i)/100d)-opSoFar;
				
				opSoFar += op;
				g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255*op)));
				g.fillOval(s.pos.x-(s.size+i)/2, sH-(s.pos.y+(s.size+i)/2), s.size+i, s.size+i);
			}
			
			//Halo effect
			for (int i=0; i<s.haloLength; i++) {
				g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255*((s.haloLength-i)*s.haloIncrements))));
				g.fillRect(s.pos.x-i, sH-(s.pos.y), 1, 1); //To left
				g.fillRect(s.pos.x+i, sH-(s.pos.y), 1, 1); //To right
				g.fillRect(s.pos.x, sH-(s.pos.y-i), 1, 1); //To bottom
				g.fillRect(s.pos.x, sH-(s.pos.y+i), 1, 1); //To top
			}
			
			//Star center
			g.setColor(c);
			g.fillOval(s.pos.x-(s.size/2), sH-(s.pos.y+(s.size/2)), s.size, s.size);
		}
	}
	
	private void drawBuckets(Graphics2D g) {
		int mag, op, j;
		Color c = colorScroll(0);
	
		for (int i=0; i<av.buckets; i++) {
			c = colorScroll((av.buckets-i)*30);
			mag = visualMags[i];
			g.setColor(c);
			for (j=0; j<mag; j++) { //Draw full cells
				g.fillRoundRect(i*(cellW+(2*cellXOff))+cellXOff, sH-((j+1)*(cellH+(2*cellYOff))+cellYOff), cellW, cellH, curve, curve);
			}
			op = 65;
			while (op>0) { //Draw shadow cells
				g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), op));
				g.fillRoundRect(i*(cellW+(2*cellXOff))+cellXOff, sH-((j+1)*(cellH+(2*cellYOff))+cellYOff), cellW, cellH, curve, curve);
				j++;
				op -= 4;
			}
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_B:
			blackout = !blackout;
			break;
		}
	}

	public SpectrumRender(AudioVisualiser av) {
		this.av = av;
		setup();
		SpectrumRender.painter = new Painter(this);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		this.paint = true;
		painter.start();
	}

	public static SpectrumRender initialise(AudioVisualiser av) {
		SpectrumRender panel = new SpectrumRender(av);
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame();
				panel.setPreferredSize(new Dimension(sW, sH));
				frame.getContentPane().add(panel);

				//Label and build
				frame.setTitle("AudioVisualiser");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
		
		return panel;
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}
