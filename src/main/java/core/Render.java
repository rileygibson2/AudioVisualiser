package main.java.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Render extends JPanel {
	
	AudioVisualiser av;
	public static JFrame frame;
	public static int sW = 700, sH = 700;
	static Painter painter;
	public boolean paint = true;
	
	int zones;
	int zoneW;
	int zoneOpacity = 20;
	
	int target;
	
	public void setup() {
		zones = (int) (av.maxFrequency/av.frequencyResolution)/AudioVisualiser.groupBlocks;
		zoneW = sW/zones;
		System.out.println("zones = "+zones+", "+zoneW);
		
		target = 400;
	}
	
	@Override
	public void paintComponent(Graphics g1d) {
		Graphics2D g = (Graphics2D) g1d;
		//Reset screen
		//g.setColor(Color.BLACK);
		//g.fillRect(0, 0, sW, sH);
		
		drawZones(g);
	}
	
	public void drawZones(Graphics2D g) {
		if (av.magnitudes==null) return;
		
		/*for (int z=0; z<zones; z++) {
			if (z%2==0) g.setColor(new Color(0, 0, 255, zoneOpacity));
			else g.setColor(new Color(255, 0, 0, zoneOpacity));
			g.fillRect(z*zoneW, 0, zoneW, sH);
		}*/
		
		g.setColor(Color.RED);
		for (int m=0; m<av.magnitudes.length; m++) {
			int freq = (int) (av.frequencyResolution*av.groupBlocks)*m;
			
			if (freq>target-1000&&freq<target+1000) g.setColor(Color.GREEN);
			else g.setColor(Color.RED);
			
			//System.out.println(freq+", "+av.maxFrequency);
			int val = (int) (av.magnitudes[m]*10);
			g.fillRect(m*zoneW, sH-val, zoneW, val);
		}
	}

	public Render(AudioVisualiser av) {
		this.av = av;
		//Render.painter = new Painter(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		this.paint = true;
		painter.start();
	}

	public static Render initialise(AudioVisualiser av) {
		Render panel = new Render(av);
		
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
}
