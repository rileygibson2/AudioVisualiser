package main.java.renders.albumcover;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import main.java.core.Button;
import main.java.core.Controller;
import main.java.core.Painter;
import main.java.core.Render;

public class AlbumCoverRender extends Render {

	//Color scroll
	private int stage; //What stage of the color scroll we are in
	private int point; //At what point in that stage are we in

	//Cell
	private int ceiling; //Max height of spectrum cells
	private int cellW, cellXOff, cellH, cellYOff;
	static final int curve = 12;

	//Stars
	private Set<Star> stars;
	private int numStars = 100;
	private Rectangle starSpace = new Rectangle(0, (int) (sH*0.4), sW, sH); //Bounds for stars

	//Magnitude formatting
	public final int numBuckets = 21; //Number of spectrum buckets
	public final int maxAmp = 45; //Range of possible amplitudes for each bucket

	private void setup() {
		ceiling = (int) (sH*0.8);
		cellW = sW/numBuckets;
		cellXOff = (int) (cellW*0.1);
		cellW = (int) (cellW*0.8);

		cellH = ceiling/maxAmp;
		cellYOff = (int) (cellH*0.15);
		cellH = (int) (cellH*0.7);

		visualMags = new double[numBuckets];
		stage = 1;
		point = 0;
		stars = new HashSet<Star>();
		for (int i=0; i<numStars; i++) stars.add(new Star(starSpace, this));

		//Add specialised button
		buttons.add(new Button("B/S", Color.GREEN, Color.BLACK, "toggleBlackStrobe", "isBlackStrobing", true, this));
		buttons.add(new Button("W/S", Color.GREEN, Color.BLACK, "toggleWhiteStrobe", "isWhiteStrobing", true, this));
	}

	public void paint(Graphics2D g) {
		//Background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, sW, sH);

		//Elements
		drawStars(g);
		drawBuckets(g);
	}

	public void flickerStars() {
		for (Star s : stars) {
			s.flicker();
			//Maybe reposition this star if it is invisible
			if (Controller.random(0, 200)==0&&s.haloCut<0.05) s.reposition();
		}
	}

	/**
	 * Allows for the transition element between different magnitudes.
	 * Means that buckets will not jump down when magnitude goes from high to low,
	 * but will jump with a low to high transition.
	 */
	public void incrementVisualMags(boolean increaseFall) {
		//Cut unwanted frequencys and average into set number of buckets
		if (av.magnitudes==null) return;
		double realMags[] = cutandAverageMags(0, 100);

		for (int i=0; i<numBuckets; i++) {
			if (increaseFall&&visualMags[i]>realMags[i]) visualMags[i]--;
			if (visualMags[i]<realMags[i]) visualMags[i] = realMags[i];
		}
	}

	public double[] cutandAverageMags(int mincut, int maxcut) {
		if (mincut+maxcut>av.magnitudes.length||mincut<0||mincut>maxcut) throw new Error("Windowing error during cutting and averaging");
		double[] averaged = new double[numBuckets];
		double sum = 0;
		int count = 0;
		int bucketCut = (maxcut-mincut)/(numBuckets+1);
		int bucketCount = 0;

		for (int i=mincut; i<maxcut; i++) {
			if (i==mincut+((bucketCount+1)*bucketCut)) {
				if (bucketCount>=numBuckets) break;

				if ((sum)>maxAmp) averaged[bucketCount] = maxAmp;
				else averaged[bucketCount] = sum;
				sum = 0;
				count = 0;
				bucketCount++;
			}

			if (av.magnitudes[i]>0) sum += av.magnitudes[i];
			count++;
		}
		return averaged;
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
		double mag;
		int op, j;
		Color c = colorScroll(0);

		for (int i=0; i<numBuckets; i++) {
			c = colorScroll((numBuckets-i)*30);
			if (strobing&&whiteStrobe&&strobeOn) c = Color.WHITE;
			
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

	public Painter getPainter() {return new AlbumCoverPainter(this);}

	public AlbumCoverRender(Controller av) {
		super(av, "Album Cover");
		setup();
	}
}

class AlbumCoverPainter extends Painter {

	public AlbumCoverPainter(Render render) {super(render);}

	public void iterate(int count) {
		AlbumCoverRender r = (AlbumCoverRender) render;

		//Increment
		r.increaseColorScroll(2);
		r.flickerStars();
		/* Split up the 'falling' of values that are no longer as tall 
		 * and the updating of magnitudes. This is because we want to
		 * see the peaks as they happen, but we also want the falling to
		 * look slow and controlled and visible.
		 */
		//if (count%2==0) r.incrementVisualMags(true); //Update values and do the falling animation
		//else
		r.incrementVisualMags(true); //Just update values
	}

}
