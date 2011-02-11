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

/*  Note: this file was translated from a Java language implementation
 *  found in the OpenPoc project. The OpenPoc file came with the following
 *  notice:
 *  
 *  
 *  OpenPoc BCHDecoder Class

    This file is part of OpenPoc.

    OpenPoc is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenPoc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenPoc.  If not, see <http://www.gnu.org/licenses/>.
*/



package jpocsag.pocsag;

class Syndrom {
	public int[] t = new int[4];
}

public class BCHDecoder {
	public static class Correction {
		public long initialCW;
		public long cw;
		public int nbCorr;
		public boolean good;
	}
	
	private static int n;
	private static int[] GF;
	private static int[] GF_rev;
	
	private static final byte polynomial = (1<<5) | (1<<2) | 1; // 100101
	private static final int m = 5;
	
	private static void genGF() {
		GF = new int[n];
		GF_rev = new int[n+1];
		GF[0] = 1;
		
	    for(int i=1; i<GF.length; i++) {
	    	GF[i] = GF[i-1] << 1;
	    	if((GF[i] & (1<<m)) != 0) GF[i] = GF[i] ^ polynomial;
	    	///System.out.println(i + " -> "+ GF[i]);
	    	GF_rev[GF[i]] = i;
	    }
	    GF_rev[0] = -1;
	}
	
	private static int[] calcSyndrom(long cw) {
		int[] result = new int[4];
		for(int i=0; i<4; i++) {
			result[i] = 0;
			for(int j=0; j<n; j++)
				if(((cw>>(j+1)) & 1) !=0) result[i] ^= GF[(i+1)*j % n];
			result[i] = GF_rev[result[i]];
		}
		///System.out.println("Syndrom of " + Long.toHexString(cw) + ": " + Arrays.toString(result));
		return result;
	}
	
	static {
		n = (1 << m)  - 1;
		genGF();
	}
	
	public static Correction correct(long cw) {
		int[] S = new int[4];
		int s3;
		int[] C = new int[3];
		int[] loc = new int[3];
		int tmp;
		boolean error;
		int errors = 0;
		
		Correction result = new Correction();
		result.initialCW = cw;
		result.cw = cw;
		result.nbCorr = 0;
		result.good = true;
		
		S = calcSyndrom(cw);
		error = false;

		for(int i=0; i<4; i++)
			if(S[i] != -1) {
				error = true;
				break;
			}
	        
		if(!error) return result;
		
		///System.out.println("     errors !!!");
		
		// are there errors? */
		if(S[0] != -1) {
			s3 = (S[0] * 3) % n;
			
			// is it only one error?
			if(S[2] == s3) { cw ^= 1<<(S[0]+1); errors=1; } ///System.out.println("    1 erreur"); }
			else  {
				// there are (hopefully) two errors
				// solve for the coeffs of C(X) (=error locator polynomial)
				///System.out.println("    >1 erreur :-(");
				tmp = GF[s3];
				if(S[2] != -1) tmp ^= GF[S[2]];
				
				C[0] = 0;
				C[1] = (S[1] - GF_rev[tmp] + n) % n;
				C[2] = (S[0] - GF_rev[tmp] + n) % n;

				// get the roots of C(x) using Chien-Search
				errors = 0;
				for(int i=0; i<n; i++) {
					tmp = 1;
					for(int j=1; j<=2; j++)
						if(C[j] != -1) {
							C[j] = (C[j] + j) % n;
							tmp ^= GF[C[j]];
						}
					
					if(tmp == 0) {
						loc[errors] = (i+1) % n;
						errors++;
					}
				}
				
				if(errors == 2) {
					cw ^= (1<<(loc[0]+1));
					cw ^= (1<<(loc[1]+1));
				}
			}
		}
		
		// calculate syndrom again
		S = calcSyndrom(cw);
		error = false;

		for(int i=0; i<4; i++)
			if(S[i] != -1) {
				error = true;
				break;
			}
		
		if(!error) {
			result.cw = cw & 0xFFFFFFFFl;
			result.nbCorr = errors;
		} else {
			result.good = false;
		}
		return result;
	}
}
