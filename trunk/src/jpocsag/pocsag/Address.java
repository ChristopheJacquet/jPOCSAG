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

public class Address {
	private final int address;
	private final int function;
	
	public Address(AddressCodeword cw, int frame) {
		if(frame<0 || frame>7) throw new RuntimeException("Bad frame number: " + frame);
		address = cw.getAddress()<<3 | frame;
		function = cw.getFunction();
	}
	
	public String toString() {
		return address + "/" + function; //String.format("%1$07/%2$", address, function);
	}
	
	public boolean equals(Object o) {
		if(! (o instanceof Address)) return false;
		Address a = (Address) o;
		return address == a.address && function == a.function;
	}
}
