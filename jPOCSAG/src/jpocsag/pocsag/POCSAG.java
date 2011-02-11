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

public class POCSAG {
	public final static long SYNC_CODEWORD = 0x7CD215D8l;
	public final static long IDLE_CODEWORD = 0x7A89C197l;
	public final static long WORD_MASK = 0xFFFFFFFFl;
	
	public static long readWord(int[] bits, int pos) {
		if(bits.length - pos < 32) return -1;
		long word = 0;
		for(int i=0; i<32; i++)
			word = (word<<1) & WORD_MASK | bits[pos+i];
		return word;
	}
}
