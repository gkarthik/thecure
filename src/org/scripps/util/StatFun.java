/**
 * 
 */
package org.scripps.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

//import com.flaptor.hist4j.AdaptiveHistogram;
//import com.flaptor.hist4j.Cell;

/**
 * Statistical functions needed for this project
 //TODO merge with other StatFun classes into a more general utility..
 * @author bgood
 *
 */
public class StatFun {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
//		ArrayList<Cell> table = getHistoTable(getRandomValues(2000));
//		for(Cell cell : table){
//			System.out.println(cell.count+"\t"+cell.minValue+"\t"+cell.maxValue);
//		}
	}

//	public static ArrayList<Cell> getHistoTable(float[] data){
////		ArrayList<Cell> table = null;
////		AdaptiveHistogram h = new AdaptiveHistogram();
////	    int size = data.length;
////	    for (int i = 0; i < size; i++) {
////	        h.addValue (data[i]);
////	    }
////	    table = h.toTable();
////		return table;
//	}
	
	 public static float[] getRandomValues(int tests) throws Exception {
		 Random rnd = new Random(new Date().getTime());   
		 float[] data = new float[tests];
	        for (int i = 0; i < tests; i++) {
	            float val = rnd.nextFloat();
	            val = val * val * val; // skew the data set so that most points fall at the begining of the [0-1] range.
	            data[i] = val;
	        }
			return data;
	    }
}
