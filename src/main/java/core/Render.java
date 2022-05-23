package main.java.core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Render extends JPanel {
	
	public static JFrame frame;
	public static int sW = 700, sH = 700;
	
	static Painter painter;
	public boolean paint = true;
	
	private void setup() {
		
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		
	}

	public Render() {
		Render.painter = new Painter(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		setup();
		this.paint = true;
		painter.start();
	}

	public static void initialise() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame();
				Render panel = new Render();
				panel.setPreferredSize(new Dimension(sW, sH));
				frame.getContentPane().add(panel);

				//Label and build
				frame.setTitle("Dot Dynamics");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
	}
}
