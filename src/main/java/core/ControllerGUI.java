package main.java.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.java.renders.Button;
import main.java.renders.Render;

public class ControllerGUI extends JPanel implements MouseListener, KeyListener {

	static JFrame frame;
	static int sW = 500;
	static int sH = 700;

	private Controller c;
	//private enum Button {Visible, BO, WStrobe, BStrobe};

	//Drawing variables
	private int rowStart, rowH, rowOffset;

	public ControllerGUI(Controller c) {
		this.c = c;
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addMouseListener(this);
		addKeyListener(this);

		rowStart = (int) (0.1*sH);
		rowH = (int) (0.1*sH);
		rowOffset = (int) (0.1*rowH);
		rowH = (int) (0.8*rowH);
	}


	@Override
	public void paintComponent(Graphics g1d) {
		Graphics2D g = (Graphics2D) g1d;
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		drawHeader(g);

		//Rows
		int y = rowStart;
		for (Render r : c.renders) {
			drawRow(g, r, 0, y);
			y += rowH+rowOffset*2;
		}
	}

	public void drawHeader(Graphics2D g) {
		//Title
		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", Font.BOLD, 40));
		g.drawString("Control", (int) (sW*0.05), 50);

		//Control button
		g.setFont(new Font("Verdana", Font.BOLD, 15));
		int x = (int) (sW*0.42);
		g.setColor(c.capture ? new Color(252, 186, 3) : Color.BLACK);
		g.fillRoundRect((int) x, 13, (int) (sW*0.18), (int) (rowH*0.8), 10, 10);
		g.setColor(Color.WHITE);
		g.drawRoundRect((int) x, 13, (int) (sW*0.18), (int) (rowH*0.8), 10, 10);
		g.drawString("CAPTURE", (int) (x+sW*0.015), (int) (13+rowH*0.5));
	}

	public void drawRow(Graphics2D g, Render r, int x, int y) {
		//Borders
		g.setColor(Color.WHITE);
		g.fillRect(0, y, sW, 2);
		g.fillRect(0, y+rowH+rowOffset*2, sW, 2);
		x = (int) (sW*0.05);
		y += rowOffset;

		//Name
		g.setFont(new Font("Verdana", Font.BOLD, 20));
		g.drawString(r.getName(), x, y+40);

		//Buttons
		g.setFont(new Font("Verdana", Font.BOLD, 10));
		int c = 1;
		y += (int) (rowH*0.1);

		for (Button b : r.buttons) {
			Color col = b.isOn() ? b.onCol : b.offCol;
			x = (int) (sW-((sW*0.12)*c));
			g.setColor(col);
			g.fillRoundRect((int) x, y, (int) (sW*0.1), (int) (rowH*0.8), 10, 10);
			g.setColor(Color.WHITE);
			g.drawRoundRect((int) x, y, (int) (sW*0.1), (int) (rowH*0.8), 10, 10);
			g.drawString(b.name, (int) (x+sW*0.015), (int) (y+rowH*0.5));
			c++;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Render r = null;
		Button b = null;
		try {
			r = c.renders.get((e.getY()-rowStart)/(rowH+rowOffset*2));
			b = r.buttons.get((int) ((sW-e.getX())/(sW*0.12)));
		} catch (IndexOutOfBoundsException e1) {};

		if (r!=null&&b!=null) {
			if (b.limitPress) b.toggleAction();
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getY()<rowStart) { //Capture button
			if (e.getX()>sW*0.42&&e.getX()<sW*0.6) c.capture = !c.capture;
			repaint();
			return;
		}

		Render r = null;
		Button b = null;
		try {
			r = c.renders.get((e.getY()-rowStart)/(rowH+rowOffset*2));
			b = r.buttons.get((int) ((sW-e.getX())/(sW*0.12)));
		} catch (IndexOutOfBoundsException e1) {};

		if (r!=null&&b!=null) b.toggleAction();
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_Z : c.currentRender.toggleBlackStrobe(); break;
		case KeyEvent.VK_X : c.currentRender.toggleWhiteStrobe(); break;
		case KeyEvent.VK_SPACE : c.capture = false;
		}
		repaint();
	}


	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_Z :
			c.currentRender.toggleBlackStrobe();
			if (c.currentRender.isBlackStrobing()) c.currentRender.toggleBlackStrobe();
			break;
		case KeyEvent.VK_X :
			c.currentRender.toggleWhiteStrobe();
			if (c.currentRender.isWhiteStrobing()) c.currentRender.toggleWhiteStrobe();
			break;
		case KeyEvent.VK_SPACE : c.capture = true;
		}
		repaint();
	}

	public static ControllerGUI initialise(Controller c) {
		ControllerGUI panel = new ControllerGUI(c);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame();
				frame.setAlwaysOnTop(true);
				panel.setPreferredSize(new Dimension(sW, panel.rowStart+c.renders.size()*(panel.rowH+panel.rowOffset*2)));
				frame.getContentPane().add(panel);

				//Label and build
				frame.setTitle("Controller");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
		return panel;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}
}
