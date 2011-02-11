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


package jpocsag.pocsag;

public class Frame {
	private final Codeword[] cw;
	
	public Frame(int[] bits, int pos) {
		cw = new Codeword[2];
		for(int i=0; i<2; i++) cw[i] = Codeword.create(POCSAG.readWord(bits, pos+i*32));
	}
	
	public String toString() {
		return "{ " + cw[0] + ", " + cw[1] + " }";
	}

	public void text(TextDecodeBuffer buf) {
		for(int i=0; i<2; i++) cw[i].text(buf);
	}
	
	public Codeword getCodeword(int i) {
		return cw[i];
	}

	public void toHTML(StringBuffer buf) {
		cw[0].toHTML(buf);
		buf.append(" ");
		cw[1].toHTML(buf);
	}
}
