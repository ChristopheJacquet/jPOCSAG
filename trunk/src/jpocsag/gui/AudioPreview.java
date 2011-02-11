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

import javax.swing.JPanel;

public class AudioPreview extends JPanel {
	private static final long serialVersionUID = -5621483040429207150L;

	// 100 samples per second
	private final static int NUM_SAMPLES = 2000;
	
	private byte[] samples = new byte[NUM_SAMPLES];
	private int posEnd = 0;
	private int length = 0;
	
	public AudioPreview() {
		setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
	}
	
	public synchronized void addSamples(byte[] someSamples) {
		if(someSamples.length > NUM_SAMPLES - posEnd) {
			//System.out.println("NUM_SAMPLES=" + NUM_SAMPLES + ", posEnd=" + posEnd + ", someSamples.length=" + someSamples.length);
			System.arraycopy(someSamples, 0, samples, posEnd, NUM_SAMPLES - posEnd);
			System.arraycopy(someSamples, NUM_SAMPLES - posEnd, samples, 0, someSamples.length - (NUM_SAMPLES - posEnd));
		} else
			System.arraycopy(someSamples, 0, samples, posEnd, someSamples.length);
			
			
		posEnd = (posEnd + someSamples.length) % NUM_SAMPLES;
		length += someSamples.length;
		if(length > NUM_SAMPLES) length = NUM_SAMPLES;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GREEN);
		int xMax = getWidth() - 1;
		
		synchronized(this) {
			for(int i = decModulo(posEnd, NUM_SAMPLES), j=0; j<length && j<xMax; j++, i = decModulo(i, NUM_SAMPLES)) {
				g.drawLine(xMax - j, 48 - samples[i], xMax - j, 48);
			}
		}
	}
	
	private int decModulo(int val, int mod) {
		val--;
		while(val<0) val += mod;
		while(val>=mod) val-= mod;
		return val;
	}
}
