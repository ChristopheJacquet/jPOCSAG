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

import java.awt.Color;

public interface CaptureInfoLogger {
	public enum State {
		NOK(Color.RED), OK(Color.GREEN), WITH_EC(Color.YELLOW);
		
		public final Color color;
		
		private State(Color color) {
			this.color = color;
		}
	};
	
	public void reset();
	public void setLevelState(int highLevel, int lowLevel, State s);
	public void setThresholds(int high, int low);
	public void notSynced();
}
