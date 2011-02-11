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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Batch {
	private final Codeword syncCW;
	private final Frame[] frames;
	private final Codeword[] codewords;
	private long nbErrors;
	private int badWords;

	/*
	private Batch(Codeword syncCW, Frame[] frames) {
		this.syncCW = syncCW;
		this.frames = frames;
	}
	*/
	
	public Batch(int[] bits, int pos) {
		codewords = new Codeword[17];
		syncCW = Codeword.create(POCSAG.readWord(bits, pos));
		nbErrors = syncCW.numErrors();
		badWords = syncCW.numBadWords();
		frames = new Frame[8];
		codewords[0] = syncCW;
		
		for(int i=0; i<8; i++) {
			frames[i] = new Frame(bits, pos+32+64*i);
			for(int j=0; j<2; j++) {
				codewords[1+j+i*2] = frames[i].getCodeword(j);
				nbErrors += codewords[1+j+i*2].numErrors();
				badWords += codewords[1+j+i*2].numBadWords();
			}
		}
	}
	
	public String toString() {
		String res = "Batch (" + nbErrors + " corr. errors):\n\tSync = " + syncCW + "\n";
		for(int i=0; i<8; i++) res += "\tFrame = " + frames[i] + "\n";
		return res;
	}
	
/*	public void text(TextDecodeBuffer buf) {
		for(int i=0; i<8; i++) frames[i].text(buf);
	}*/
	
	public Codeword getCodeword(int i) {
		return codewords[i];
	}
	
	public Element xml(Document doc) {
		Element xmlBatch = doc.createElement("batch");
		
		for(int i=0; i<codewords.length; i++)
			xmlBatch.appendChild(codewords[i].xml(doc));
		
		return xmlBatch;
	}
	
	public boolean isSynced() {
		return syncCW instanceof SyncCodeword;
	}

	public void toHTML(StringBuffer buf) {
		buf.append("Frame (" + nbErrors + " errors corrected, " + badWords + " bad words)<br>");
		buf.append("       sync = ");
		syncCW.toHTML(buf);
		buf.append("<br>");
		for(Frame f : frames) {
			buf.append("  ");
			f.toHTML(buf);
			buf.append("<br>");
		}
		buf.append("<br>");
	}
}
