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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Message {
	private TextDecodeBuffer text;
	private Set<Address> addresses;
	private List<Batch> batches;
	private final Date date;
	
	private static final SimpleDateFormat dateFormat =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.ENGLISH);
	
	public Message() {
		text = new TextDecodeBuffer();
		addresses = new HashSet<Address>();
		batches = new ArrayList<Batch>();
		date = new Date();
	}
	
	/**
	 * 
	 * @param b
	 * @return {@code true} if the batch could be added to the current message,
	 * {@code false} if a new message needs to be created.
	 */
	public boolean addBatch(Batch b) {
		for(int i=1; i<17; i++) {
			Codeword cw = b.getCodeword(i);
			if(cw instanceof AddressCodeword) {
				if(!text.empty()) return false;
				addresses.add(new Address((AddressCodeword)cw, (i-1)/2));
				text.flush();
			} else if(cw instanceof MessageCodeword) {
				cw.text(text);
			} else if(cw instanceof IdleCodeword && !text.empty()) {
				//return true;
			}
		}
		batches.add(b);
		return true;
	}
	
	public String toString() {
		return text.toString();
	}
	
	public String text() {
		return text.toString();
	}
	
	public Element xml(Document doc) {
		Element xmlMsg = doc.createElement("message");
		Element xmlDate = doc.createElement("date");
		xmlDate.setTextContent(dateFormat.format(date));
		Element xmlText = doc.createElement("text");
		xmlText.setTextContent(text.toString());
		Element xmlAddresses = doc.createElement("addresses");
		for(Address a : addresses) {
			Element xmlAddr = doc.createElement("address");
			xmlAddr.setTextContent(a.toString());
			xmlAddresses.appendChild(xmlAddr);
		}
		Element xmlBatches = doc.createElement("batches");
		for(Batch b : batches) {
			xmlBatches.appendChild(b.xml(doc));
		}
		
		xmlMsg.appendChild(xmlDate);
		xmlMsg.appendChild(xmlAddresses);
		xmlMsg.appendChild(xmlText);
		xmlMsg.appendChild(xmlBatches);
		
		return xmlMsg;
	}
	
	public Date getDate() {
		return date;
	}
	
	public Set<Address> getAddresses() {
		return addresses;
	}
	
	public boolean empty() {
		return text.empty();
	}

	public String toHTML() {
		StringBuffer buf = new StringBuffer("<html>");
		for(Batch b : batches)
			b.toHTML(buf);
		buf.append("</html>");
		return buf.toString();
	}
}
