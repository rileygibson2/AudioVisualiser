package main.java.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class Render {

	protected Controller av;
	public int sW;
	public int sH;
	protected Painter painter;
	public boolean paint; //Kill switch for paint thread
	protected boolean painting; //Whether a paint is currently occuring, used for synchronisation
	private String name;

	protected double[] visualMags; //What the magnitudes are in the render, allows for the transition down magnitudes

	//Blackout & Strobe
	protected boolean blackout = false;
	protected int blackoutOp = 0;
	protected boolean strobing, strobeOn, whiteStrobe;

	//Gui Buttons
	public List<Button> buttons;

	public Render(Controller av, String name) {
		this.av = av;
		this.name = name;
		this.sW = av.rP.sW;
		this.sH = av.rP.sH;

		//Set up basic functionality buttons
		buttons = new ArrayList<Button>();
		buttons.add(new Button("Visible", Color.GREEN, Color.RED, "toggleWindow", "windowVisible", false, this));
		buttons.add(new Button("B/O", Color.RED, Color.BLACK, "toggleBlackout", "isBO", false, this));
	}

	/*case BO: if (r.windowVisible()) r.toggleBlackout(); break;
	case Visible: r.toggleWindow(); break;
	case WStrobe: r.setStrobe(false, false); break;
	case BStrobe: r.setStrobe(false, false); break;*/

	public String getName() {return this.name;}

	public boolean isBO() {return this.blackout;}

	public boolean windowVisible() {return av.currentRender==this;}

	public boolean isBlackStrobing() {return this.strobing&&!this.whiteStrobe;}

	public boolean isWhiteStrobing() {return this.strobing&&this.whiteStrobe;}

	public void paint(Graphics g1d) {
		painting = true;
		Graphics2D g = (Graphics2D) g1d;

		//Handle black strobe in on phase of strobe
		if (strobing&&!whiteStrobe&&strobeOn) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, sW, sH);
		}
		/* Below will paint when not strobing, or when it is black
		 * strobing, but in an off phase of the strobe, or when strobing
		 * but it's a white strobe (which is handled by render children).
		 */
		else if (!strobing||(strobing&&!strobeOn)||(strobing&&whiteStrobe)) {
			paint(g);
			if (blackoutOp>0) { //Blackout
				g.setColor(new Color(0, 0, 0, blackoutOp));
				g.fillRect(0, 0, sW, sH);
			}
		}
		painting = false;
	}

	public abstract void paint(Graphics2D g);

	public void toggleBlackout() {if (windowVisible()) this.blackout = !this.blackout;}

	public void toggleWindow() {
		if (av.currentRender==this) { //Toggle off
			av.currentRender = null;
			paint = false; //Will kill paint thread
		}
		else { //Toggle on
			if (av.currentRender!=null) { //Notify old render
				av.currentRender.toggleWindow();
			}
			av.currentRender = this;
			av.rP.frame.setTitle(name);
			
			strobing = false;
			//Wait for old paint thread to die then start new paint thread
			if (painter!=null&&painter.isAlive()) try {painter.join();} catch (InterruptedException e) {throw new Error("Problem waiting for paint thread to die");}
			paint = true;
			painter = getPainter();
		}
	}

	public void toggleWhiteStrobe() {
		if (windowVisible()) {
			this.strobing = !this.strobing;
			this.whiteStrobe = !this.whiteStrobe;
			this.strobeOn = false;
		}
	}

	public void toggleBlackStrobe() {
		if (windowVisible()) {
			this.strobing = !this.strobing;
			this.whiteStrobe = false;
			this.strobeOn = false;
		}
	}

	public abstract Painter getPainter();

	//Dummy method used in reflection for buttons which do not require an on check 
	public boolean cheatTrue() {return true;}
}
