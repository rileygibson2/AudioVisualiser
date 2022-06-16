package main.java.renders;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.java.core.AudioVisualiser;

public abstract class Render extends JPanel {

	protected AudioVisualiser av;
	protected static JFrame frame;
	protected int sW;
	public int sH;
	protected static Painter painter;
	public boolean paint; //Kill switch for paint thread
	protected boolean painting; //Whether a paint is currently occuring, used for synchronisation
	
	protected double[] visualMags; //What the magnitudes are in the render, allows for the transition down magnitudes
	
	public Render(AudioVisualiser av, int sW, int sH) {
		this.av = av;
		this.sW = sW;
		this.sH = sH;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
	}
	
	public void startPaint() {
		this.paint = true;
		painter.start();
	}
	
	@Override
	public abstract void paintComponent(Graphics g1d);
	
	public static void initialise(Render panel) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame();
				panel.setPreferredSize(new Dimension(panel.sW, panel.sH));
				frame.getContentPane().add(panel);

				//Label and build
				frame.setTitle("AudioVisualiser");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
	}
}
