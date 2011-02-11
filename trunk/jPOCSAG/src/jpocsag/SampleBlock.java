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


package jpocsag;

public class SampleBlock {
	private byte[] data;
	private SampleBlock next;
	
	public SampleBlock() {
		data = null;
		next = null;
	}
	
	public void addData(byte[] data) {
		if(this.data == null) {
			this.data = new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
		}
		else {
			if(next == null) next = new SampleBlock();
			next.addData(data);
		}
	}
	
	private int size() {
		if(next == null) return data.length;
		else return next.size() + data.length;
	}
	
	public int[] getWholeData() {
		int[] res = new int[size()];
		putData(res, 0);
		return res;
	}
	
	private void putData(int[] res, int pos) {
		for(int i=0; i<data.length; i++) { res[i+pos] = data[i];  } //System.out.print((i+pos) + ":" + res[i+pos]); }
		if(next != null) next.putData(res, pos+data.length);
	}
}
