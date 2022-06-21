package main.java.renders;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.java.core.Controller;

public abstract class Render extends JPanel implements KeyListener {

	protected Controller av;
	protected JFrame frame;
	protected int sW;
	public int sH;
	private boolean windowVisible = false;
	protected Painter painter;
	public boolean paint; //Kill switch for paint thread
	protected boolean painting; //Whether a paint is currently occuring, used for synchronisation
	private String name;

	protected double[] visualMags; //What the magnitudes are in the render, allows for the transition down magnitudes

	//Blackout & Strobe
	protected boolean blackout = false;
	protected int blackoutOp = 0;
	protected boolean strobing, strobeOn, whiteStrobe;

	public Render(Controller av, String name, int sW, int sH) {
		this.av = av;
		this.name = name;
		this.sW = sW;
		this.sH = sH;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
	}

	public String getName() {return this.name;}

	public boolean getBO() {return this.blackout;}

	public boolean windowVisible() {return this.windowVisible;}

	public boolean isStrobing() {return this.strobing;}
	
	public boolean isWhiteStrobe() {return this.whiteStrobe;}

	@Override
	public void paintComponent(Graphics g1d) {
		painting = true;
		Graphics2D g = (Graphics2D) g1d;

		//Strobe
		if (strobing&&strobeOn) {
			if (whiteStrobe) g.setColor(Color.WHITE);
			else g.setColor(Color.BLACK);
			g.fillRect(0, 0, sW, sH);
		}
		else if (!strobing||(strobing&&!strobeOn)) {
			paint(g);
			//Blackout
			if (blackoutOp>0) {
				g.setColor(new Color(0, 0, 0, blackoutOp));
				g.fillRect(0, 0, sW, sH);
			}
		}
		painting = false;
	}

	public abstract void paint(Graphics2D g);

	public void toggleBlackout() {this.blackout = !this.blackout;}

	public void toggleWindow() {
		if (windowVisible) {
			windowVisible = false;
			paint = false;
		}
		else {
			windowVisible = true;
			strobing = false;
			//Wait for old paint thread to die then start new paint thread
			if (painter!=null&&painter.isAlive()) try {painter.join();} catch (InterruptedException e) {throw new Error("Problem waiting for paint thread to die");}
			paint = true;
			painter = getPainter();
		}
		this.frame.setVisible(windowVisible);
	}

	public void setStrobe(boolean strobing, boolean whiteStrobe) {
		this.strobing = strobing;
		this.whiteStrobe = whiteStrobe;
		this.strobeOn = false;
	}

	public abstract Painter getPainter();

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_B:
			toggleBlackout();
			break;
		}
	}

	public static void initialise(Render panel) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.frame = new JFrame();
				panel.setPreferredSize(new Dimension(panel.sW, panel.sH));
				panel.frame.getContentPane().add(panel);

				//Label and build
				panel.frame.setTitle(panel.name);
				panel.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				panel.frame.setVisible(false);
				panel.frame.pack();
			}
		});
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}
