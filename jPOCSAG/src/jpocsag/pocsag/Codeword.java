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


public abstract class Codeword {
	protected final long receivedWord;
	protected final long word;
	protected final long errors;
	protected final boolean good;
	
	protected final static long MASK_TYPE = (1l << 31);
	protected final static long MASK_PARITY = 0x000007FF;
	
	protected Codeword(BCHDecoder.Correction corr) {
		this.receivedWord = corr.initialCW;
		this.word = corr.cw;
		this.errors = corr.nbCorr;
		this.good = corr.good && Long.bitCount(word) % 2 == 0;
		//errors = 0;
	}
	
	public static Codeword create(long initialWord) {
		BCHDecoder.Correction corr = BCHDecoder.correct(initialWord);
		long word = corr.cw;
		if(word == POCSAG.SYNC_CODEWORD) return new SyncCodeword(corr);
		if(word == POCSAG.IDLE_CODEWORD) return new IdleCodeword(corr);
		if((word & MASK_TYPE) != 0) return new MessageCodeword(corr);
		else return new AddressCodeword(corr);
	}
	
	public String toString() {
		return "[" + Long.toString(word, 16) + "]";
	}

	public void text(TextDecodeBuffer buf) {
	}
	
	protected String contents() {
		if(good)
			return Long.toString(receivedWord, 16) + "/" + errors + " > " + Long.toString(word, 16);
		else
			return "BAD: " + Long.toString(word, 16);
	}
	
	public long numErrors() {
		return errors;
	}
	
	public int numBadWords() {
		return good ? 0 : 1;
	}
	
	public abstract String getType();

	public Element xml(Document doc) {
		Element xmlCW = doc.createElement("codeword");
		xmlCW.setAttribute("type", getType());
		xmlCW.setAttribute("value", Long.toHexString(word));
		return xmlCW;
	}

	public void toHTML(StringBuffer buf) {
		buf.append("<font ");
		if(good) {
			if(errors>0) buf.append("color='blue'");
		} else buf.append("color='red'");
		buf.append(">").append(String.format("%08X", word)).append("-").append(getType()).append("</font>");
	}
}
