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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import jpocsag.pocsag.Address;
import jpocsag.pocsag.Message;


public class MessageListCellRenderer extends JPanel implements ListCellRenderer {
	private static final long serialVersionUID = -3366516173008523423L;
	
	private final static Color COL_DATE = new Color(0f, 0f, .5f);
	private final static Color COL_ADDR = new Color(0f, .5f, 0f);
	private final static Color COL_MSG = Color.BLACK;
	private final static Color COL_BACKSEL = new Color(1f, 1f, .7f);
	
	private final static Graphics2D gBase = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
	private final static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private Message message;

	public MessageListCellRenderer() {
		setOpaque(true);
	}
	
	private Dimension render(Graphics2D gImg) {
		gImg.setFont(new Font("Monospaced", Font.PLAIN, gImg.getFont().getSize()));
		
		gImg.setColor(COL_DATE);
		Rectangle2D r = drawString(gImg, dateFormat.format(message.getDate()), 0, 0);
		int left = (int)r.getWidth() + 10;
		int y2ndLine = (int)r.getHeight() + 2;
		
		gImg.setColor(COL_ADDR);
		String s = "";
		for(Address a : message.getAddresses()) {
			s += a.toString() + "    ";
		}
		r = drawString(gImg, s, left, 0);
		int right = (int)r.getWidth() + left;
		
		gImg.setColor(COL_MSG);
		if(message.empty()) {
			s = "<empty>";
			gImg.setFont(gImg.getFont().deriveFont(Font.ITALIC));
		} else {
			s = message.text();
		}
		r = drawString(gImg, s, left, y2ndLine);
		int right2 = (int)r.getWidth() + left;
		right = Math.max(right, right2);

		return new Dimension(right, y2ndLine + (int)r.getHeight() + 5);
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		message = (Message)value;
		
		setBackground(isSelected ? COL_BACKSEL : Color.WHITE);
		
		Dimension d = render(gBase);
		
		setPreferredSize(d);
		
		return this;
	}

	private Rectangle2D drawString(Graphics2D g, String s, int x, int y) {
		Rectangle2D r = g.getFont().getStringBounds(s, g.getFontRenderContext());
		g.drawString(s, x, y + (int)r.getHeight());
		return r;

	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		
		render(g2d);

	}
}
