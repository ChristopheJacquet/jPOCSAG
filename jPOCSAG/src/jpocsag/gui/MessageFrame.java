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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import jpocsag.pocsag.Message;


public class MessageFrame extends JFrame {
	private static final long serialVersionUID = 463955903504300506L;

	private JLabel contents;
	
	public void setMessage(Message message) {
		contents.setText(message.toHTML());
		repaint();
		setVisible(true);
	}
	
	public MessageFrame() {
		super("Message details");
		setPreferredSize(new Dimension(350, 400));
		setLayout(new BorderLayout());
		contents = new JLabel();
		contents.setFont(new Font("Monospaced", Font.PLAIN, contents.getFont().getSize()));
		add(new JScrollPane(contents, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		pack();
	}
}