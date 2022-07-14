package main.java.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class RenderPanel extends JPanel implements KeyListener {

	private Controller av;
	public JFrame frame;
	public int sW, sH;

	public RenderPanel(Controller av, int sW, int sH) {
		this.av = av;
		this.sW = sW;
		this.sH = sH;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(this);
		repaint();
	}

	@Override
	public void paintComponent(Graphics g1d) {
		if (av.currentRender==null) {
			g1d.setColor(Color.RED);
			g1d.fillRect(0, 0, sW, sH);
		}
		else av.currentRender.paint(g1d);
	}

	@Override
	public void keyPressed(KeyEvent e) {av.gui.keyPressed(e);}
	
	@Override
	public void keyReleased(KeyEvent e) {av.gui.keyReleased(e);}

	public static RenderPanel initialise(Controller av, int sW, int sH) {
		RenderPanel panel = new RenderPanel(av, sW, sH);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.frame = new JFrame();
				panel.setPreferredSize(new Dimension(sW, sH));
				panel.frame.getContentPane().add(panel);

				//Label and build
				panel.frame.setTitle("Render");
				panel.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				panel.frame.setVisible(true);
				panel.frame.pack();
			}
		});

		return panel;
	}

	@Override
	public void keyTyped(KeyEvent e) {}
}
