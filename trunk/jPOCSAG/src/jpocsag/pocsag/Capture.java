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
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class Capture {
	private static class Sync {
		//public final int pos;
		public final boolean correctionNeeded;
		
		public Sync(boolean correctionNeeded) {
			//this.pos = pos;
			this.correctionNeeded = correctionNeeded;
		}
	}
	
	
	private final int[] samples;
	private int min, max, minD, maxD;
	private final float frameRate;
	private int[] samplingTicks = null;
	private int[] derivative = null;
	private int[] bits = null;
	private final static long SYNC_WORD = 0x7CD215D8l;
	private int syncPos = Integer.MAX_VALUE;
	private final Map<Integer, Codeword> cwPositions = new HashMap<Integer, Codeword>();
	private final CaptureInfoLogger pnlInfo;
	
	public Capture(AudioInputStream ais, CaptureInfoLogger pnlInfo) throws IOException {
		int frameLength = (int) ais.getFrameLength();
		AudioFormat format = ais.getFormat();
		int frameSize = format.getFrameSize();
		byte[] smpl = new byte[frameSize];
		//boolean bigEndian = format.isBigEndian();
		this.pnlInfo = pnlInfo;
		
		samples = new int[frameLength];
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		frameRate = format.getFrameRate();
		
		for(int i=0; i<frameLength; i++) {
			ais.read(smpl, 0, frameSize);
			//samples[i] = 0;
			// little endian
			//for(int j=frameSize-1; j>=0; j--) {
			//	samples[i] = ((int)samples[i] << 8) + (0xFF & (int)smpl[j]);
			//}
			samples[i] = (0xFF & (int)smpl[0]) + ((int)smpl[1]) * 256;
			if(samples[i] < min) min = samples[i];
			if(samples[i] > max) max = samples[i];
			//if(i<10) System.out.println(Arrays.toString(smpl));
		}
	}
	
	public Capture(int[] samples, float framerate, CaptureInfoLogger pnlInfo) {
		this.pnlInfo = pnlInfo;
		
		this.samples = samples;
		this.frameRate = framerate;
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		
		for(int i=0; i<samples.length; i++) {
			if(samples[i] < min) min = samples[i];
			if(samples[i] > max) max = samples[i];
		}
		System.out.println("len=" + samples.length + ", min=" + min + ", max=" + max);
	}
	
	private static double correlation(int[] sa, int[] sb, int j) {
		double autocorr = 0;
		for(int i=j; i<sa.length; i++) {
			autocorr += (double)sa[i] * (double)sb[i-j];
		}
		return autocorr;
	}
	
	public double autocorrelation(int j) {
		return correlation(samples, samples, j);
	}
	
	public boolean within(double a, double b, double prec) {
		return Math.abs((a-b)/a) < .1;
	}
	
	public double getPeriodInSamples() {
		// decreasing correlations
		double corr = autocorrelation(1);
		double prevCorr;
		int j = 1;
		
		do {
			j++;
			prevCorr = corr;
			corr = autocorrelation(j);
		} while(corr < prevCorr && j<500);
		
		do {
			j++;
			prevCorr = corr;
			corr = autocorrelation(j);
		} while(corr > prevCorr && j<500);
		
		if(j<500) return j-1; else return -1;
	}
	
	public double periodInSamplesToFrequency(double period) {
		return frameRate / period;
	}
	
	public List<Message> calculateSamplingTicks(double period) {
		int[] syntheticSquare = new int[samples.length];
		
		for(int i=0; i<samples.length; i++) {
			if(i % period < period/4 || i % period > 3*period/4) syntheticSquare[i] = max;
			else syntheticSquare[i] = min;
		}
		
		double corrMax = Double.MIN_VALUE;
		int indexMax = -1;
		
		for(int i=0; i<period; i++) {
			//double corr = correlation(syntheticSquare, samples, i);
			double corr = correlation(samples, syntheticSquare, i);
			//System.out.println("corr @ " + i + ": "  +corr);
			if(corr > corrMax) {
				corrMax = corr;
				indexMax = i;
			}
		}
		
		double pos = indexMax;
		double halfPeriod = period/2;
		samplingTicks = new int[(int)(samples.length / halfPeriod)];
		for(int i=0; i<samplingTicks.length; i++) {
			if(pos < samples.length) samplingTicks[i] = (int)pos;
			pos += halfPeriod;
		}
		
		derivative = new int[samplingTicks.length];
		derivative[0] = 0;
		derivative[1] = 0;
		minD = Integer.MAX_VALUE;
		maxD = Integer.MIN_VALUE;
		for(int i=2; i<derivative.length; i++) {
			//System.out.println("i=" + i);
			//System.out.println("samplingTicks[i]=" + samplingTicks[i]);
			//System.out.println("samplingTicks[i-1]=" + samplingTicks[i-1]);
			//System.out.println("samples[samplingTicks[i]]=" + samples[samplingTicks[i]]);
			//System.out.println("samples[samplingTicks[i-1]]=" + samples[samplingTicks[i-1]]);
			/// WARNING: here it depends if the sound card invert signals... Mine inverts...
			derivative[i] = -( samples[samplingTicks[i]] - samples[samplingTicks[i-1]] );
			if(derivative[i] > maxD) maxD = derivative[i];
			if(derivative[i] < minD) minD = derivative[i];
		}
		
		/// "MARGIN PATTERN"
		int high1=-1, low1=-1;
		int high2=-1, low2=-1;
		boolean foundSync = false;
		pnlInfo.reset();
		for(int high=10; high<=90; high++) {
			//System.out.print("H=" + high + ": ");
			for(int low=10; low<=90; low++) {
				Sync sync = findSync((int)(maxD*high/100.), (int)(minD*low/100.));
				if(sync != null) {
					//System.out.println("Sync: " + sync);
					//System.out.print(low/10);
					if(!sync.correctionNeeded) {
						pnlInfo.setLevelState(high, low, CaptureInfoLogger.State.OK);
						high2 = high; low2 = low; 
						if(high1 == -1) { high1 = high2; low1 = low2; }
						foundSync = true;
					} else {
						pnlInfo.setLevelState(high, low, CaptureInfoLogger.State.WITH_EC);
					}
				}
				//else System.out.print(" ");
			}
			//System.out.println();
		}
		
		if(!foundSync) {
			///System.out.println("Cannot find sync.");
			pnlInfo.notSynced();
			return null;
		}
		
		// determine thresholds at the center of the eye pattern
		
		pnlInfo.setThresholds((high1+high2)/2, (low1+low2)/2);
		///System.out.println("Thresholds: H=" + (high1+high2)/2. +
		///		", L=" + (low1+low2)/2.);
		double highThresh = maxD/100. * (high1+high2)/2.;
		double lowThresh = minD/100 * (low1+low2)/2.;
		
		/// END EYE PATTERN		
		
		int state = 0;
		long latest32bits = 0;
		bits = new int[derivative.length];
		for(int i=0; i<derivative.length; i++) {
			if(derivative[i]>highThresh) state=0;
			if(derivative[i]<lowThresh) state=1;
			bits[i] = state;
			latest32bits = latest32bits<<1 | bits[i];
			if((latest32bits & 0xFFFFFFFFl) == SYNC_WORD) {
				int alt = alterningBeforeInBits(bits, i-32);
				System.out.print("Synced with " + alt + ": ");
				//if(alt>576) syncPos = i-31;
				if(syncPos == Integer.MAX_VALUE) syncPos = i-31;
			}
			//System.out.println(Long.toString(latest32bits & 0xFFFFFFFFl, 16));
		}
		
		List<Message> messages = new LinkedList<Message>();
		Message msg = new Message();
		messages.add(msg);
		for(int i = syncPos; i<bits.length-544; i+=544) {
			Batch b = new Batch(bits, i);
			if(!b.isSynced()) continue;
			if(!msg.addBatch(b)) {
				msg = new Message();
				messages.add(msg);
				msg.addBatch(b);
			}
			//buf.flush();
			System.out.println(b);
			for(int j=0; j<17; j++) {
				cwPositions.put(i+32*j, b.getCodeword(j));
			}
		}
		
		//System.out.println("Text: " + msg.text());
		return messages;
		
	}
	
	
	private int alterningBeforeInBits(int[] bits, int pos) {
		int prec = bits[pos];
		int i = pos-1;
		while(i>=0 && prec == 1-bits[i]) {
			prec = bits[i];
			i--;
		}
		return pos-i;
	}
	
	private Sync findSync(int highT, int lowT) {
		int state = 0;
		long latest32bits = 0;
		int[] bits = new int[derivative.length];
		for(int i=0; i<derivative.length; i++) {
			if(derivative[i]>highT) state=0;
			if(derivative[i]<lowT) state=1;
			bits[i] = state;
			latest32bits = latest32bits<<1 | bits[i];
			if((latest32bits & 0xFFFFFFFFl) == SYNC_WORD) {
				int alt = alterningBeforeInBits(bits, i-32);
				System.out.print("Synched with " + alt + ": ");
				if(alt>300) return new Sync(false); //new Sync(i-31, false);  // 576
				//if(syncPos == Integer.MAX_VALUE) syncPos = i-31;
			}

			/*BCHDecoder.Correction corr = BCHDecoder.correct(latest32bits & 0xFFFFFFFFl);
			if(corr.good && corr.cw == POCSAG.SYNC_CODEWORD) {
				int alt = alterningBeforeInBits(bits, i-32);
				if(alt>40) return new Sync(i-31, true); 
			}*/
			//System.out.println(Long.toString(latest32bits & 0xFFFFFFFFl, 16));
		}
		return null;
	}
	
	public void print(int pos, int num) {
		for(int i=pos; i<pos+num; i++) {
			System.out.print(samples[i] + "\t");
			if(i % 10 == 9) System.out.println();
		}
	}
	
	
	public Dimension getDimension() {
		return new Dimension(samples.length, 190);
	}

	
	public void paint(Graphics g) {			
		g.setColor(Color.BLACK);
		//g.drawLine(0, 350, samples.length, 350);

		float scale = Math.min(45f/max, 45f/-min);
		float scaleD = Math.min(Math.abs(30f/maxD), Math.abs(30f/minD));
		int tick = 0;
		for(int i=1; i<samples.length; i++) {
			//g.drawLine(i, 150, i, 150-(int)(samples[i]*scale));
			g.setColor(Color.BLUE);
			int y1 = 50-(int)(samples[i-1]*scale);
			int y2 = 50-(int)(samples[i]*scale);
			g.drawLine(i-1, y1, i, y2);
			if(samplingTicks != null && tick < samplingTicks.length && samplingTicks[tick] == i) {
				g.setColor(Color.WHITE);
				g.drawLine(i, y2, i, 170);

				g.setColor(Color.RED);
				g.drawLine(i, y2-3, i, y2+3);
				g.fillOval(i-2, y2-2, 5, 5);

				g.drawLine(i, 135, i, 135-(int)(derivative[tick]*scaleD));
				g.fillOval(i-1, 134-(int)(derivative[tick]*scaleD), 3, 3);

				if(bits != null) {
					if(tick>=syncPos) g.setColor(Color.MAGENTA);
					g.drawString("" + bits[tick], i-3, 180);
				}

				tick++;
			}
		}

		tick = 0;
		for(int i=1; i<samples.length; i++) {
			if(samplingTicks != null && tick < samplingTicks.length && samplingTicks[tick] == i) {
				Codeword cw = cwPositions.get(tick);
				if(cw != null) {
					g.setColor(Color.RED);
					g.drawString(cw.toString(), i-3, 193);
				}


				tick++;
			}
		}

	}

}
