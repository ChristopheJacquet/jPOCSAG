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

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import jpocsag.pocsag.Capture;


public class CaptureRenderer extends JPanel {
	private static final long serialVersionUID = 5800196208580631443L;
	
	private Capture capture = null;
	
	public CaptureRenderer() {
		setPreferredSize(new Dimension(100, 210));
	}
	
	public synchronized void setCapture(Capture capture) {
		this.capture = capture;
		setPreferredSize(capture.getDimension());
		revalidate();
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(capture == null) return;
		
		synchronized(this) {
			capture.paint(g);
		}
	}
}
