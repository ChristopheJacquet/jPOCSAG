/*
	jPOCSAG, a POCSAG paging decoder written in Java.
    Copyright (C) 2009-2011 Christophe Jacquet, F8FTK.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package jpocsag.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import jpocsag.pocsag.CaptureInfoLogger;


public class CaptureInfoPanel extends JPanel implements CaptureInfoLogger {
	private static final long serialVersionUID = 1566672059061059341L;
	
	private final int nbLevels;
	private final int squareWidth;
	private final State[][] levels;
	private boolean synced;
	private int highThreshold, lowThreshold;

	public CaptureInfoPanel(int nbLevels, int width) {
		this.nbLevels = nbLevels;
		this.squareWidth = width - 10;
		levels = new State[nbLevels][nbLevels];
		reset();
		
		setPreferredSize(new Dimension(width, width));
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//System.out.println("paintComponent");
		
		Graphics2D g2d = (Graphics2D)g;
		
		AffineTransform origTransform = g2d.getTransform();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.translate(5, 5);
		double scale = ((double)squareWidth) / nbLevels;
		g2d.scale(scale, scale);
		
		for(int h=0; h<nbLevels; h++)
			for(int l=0; l<nbLevels; l++) {
				g.setColor(levels[h][l].color);
				g.fillRect(l, h, 1, 1);
			}
		
		if(synced) {
			g.setColor(Color.MAGENTA);
			int rad = nbLevels / 10;
			g.drawLine(lowThreshold-rad, highThreshold, lowThreshold+rad, highThreshold);
			g.drawLine(lowThreshold, highThreshold-rad, lowThreshold, highThreshold+rad);
		}
			
		g2d.setTransform(origTransform);
		g2d.setColor(synced ? Color.GREEN : Color.RED);
		g2d.drawString(synced ? "SYNCED" : "NOT SYNCED", 5, 20+squareWidth);
	}
	
	public void reset() {
		for(int i=0; i<nbLevels; i++) {
			for(int j=0; j<nbLevels; j++) {
				levels[i][j] = State.NOK;
			}
		}
	}
	
	public void setLevelState(int highLevel, int lowLevel, State s) {
		levels[highLevel][lowLevel] = s;
		//System.out.println("setLevelState" + highLevel + ", " + lowLevel);
	}

	public void notSynced() {
		synced = false;
	}

	public void setThresholds(int high, int low) {
		synced = true;
		highThreshold = high;
		lowThreshold = low;
	}
}
