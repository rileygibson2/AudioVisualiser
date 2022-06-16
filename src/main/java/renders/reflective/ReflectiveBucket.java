package main.java.renders.reflective;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class ReflectiveBucket {

	ReflectiveBlocksRender r;
	Point pos;
	int w;
	double mag;
	
	Color col;
	Color reflectCol;
	
	public ReflectiveBucket(Point pos, int w, ReflectiveBlocksRender r) {
		this.pos = pos;
		this.w = w;
		this.r = r;
		this.col = new Color(28, 181, 246);
		double mult = 0.2;
		this.reflectCol = new Color((int) (col.getRed()*mult), (int) (col.getGreen()*mult), (int) (col.getBlue()*mult));
	}
	
	public void drawBucket(Graphics2D g) {
		int h = (int) (mag*8);
		
		/*//Draw bucket
		g.setColor(col);
		g.fillRoundRect(pos.x, (pos.y-h), w, h, 2, 2);
		//Draw Reflection
		g.setColor(reflectCol);
		g.fillRoundRect(pos.x, pos.y, w, h, 2, 2);*/
		
		
		for (int y=0; y<h; y++) {
			int op = h-y;
			if (op>255) op = 255;
			if (op<0) op = 0;
			
			g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), op));
			g.fillRect(pos.x, (pos.y-y), w, 1); //Main
			//op -= 100;
			if (op<0) op = 0;
			g.setColor(new Color(reflectCol.getRed(), reflectCol.getGreen(), reflectCol.getBlue(), op));
			g.fillRect(pos.x, pos.y+y, w, 1); //Reflection
		}
	}
}
