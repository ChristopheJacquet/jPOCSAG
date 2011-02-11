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

public class TextDecodeBuffer {
	private StringBuffer string = new StringBuffer();
	private long waitingBits = 0l;
	private int nbWaitingBits = 0;
	private int width = 7;
	private boolean lastFlushed = true;
	
	public void addBits(long theBits, int nbBits) {
		///System.err.println("addBits: " + Long.toBinaryString(theBits));
		for(int i = nbBits-1; i>=0; i--) {
			addBit((theBits>>i) & 1);
		}
		lastFlushed = false;
	}
	
	private void addBit(long theBit) {
		waitingBits = (waitingBits<<1) | theBit;
		nbWaitingBits++;
		///System.err.println("    addBit: " + theBit + " -> " + Long.toBinaryString(waitingBits) + "/" + nbWaitingBits);
		if(nbWaitingBits == width) {
			//System.out.println("addChar: " + waitingBits);
			char chr = bitsToChar(waitingBits);
			if(chr<32) string.append(".<").append((int)chr).append(">");
			else string.append(chr);
			waitingBits = 0;
			nbWaitingBits = 0;
		}
	}
	
	private char bitsToChar(long bits) {
		long res = 0;
		for(int i=0; i<width; i++) {
			res = (res<<1) | (bits & 1);
			bits = bits>>1;
		}
		///System.err.println("        bitsToChar -> " + Long.toBinaryString(res));
		return (char)res;
	}
	
	public void flush() {
		waitingBits = 0;
		nbWaitingBits = 0;
		if(!lastFlushed)
			string.append(" ## ");
		lastFlushed = true;
	}
	
	public String toString() {
		return string.toString();
	}
	
	public boolean empty() {
		return string.length() == 0;
	}
	
	public void mark() {
		string.append("#");
	}
}
