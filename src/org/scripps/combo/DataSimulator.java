/**
 * 
 */
package org.scripps.combo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bgood
 *
 */
public class DataSimulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataSimulator ds = new DataSimulator();
		int ncols = 6; int nrows = 1000; float node1_bias = (float)(0.85);
		String datafile = "sim1.txt";
	//	int[][] data = generateData(ncols, nrows, node1_bias);
	//	writeData(data,nrows,ncols,datafile);
		int[][] data = ds.readData(datafile);		
		int p1 = 0; int p2 = 2; int p3 = 3;
		score r = ds.scoreTree(data, nrows, ncols, p1, p2, p3);
		r.printString();
	}

	public class score{	
		int n1_c = 0; int n1_n = 0; float n1_cov = 0;
		int n2_c = 0; int n2_n = 0; float n2_cov = 0;
		int n3_c = 0; int n3_n = 0; float n3_cov = 0;
		int n4_c = 0; int n4_n = 0; float n4_cov = 0;
		double n1_score = 0; double n2_score = 0; double n3_score = 0; double n4_score = 0;
		double total_score = 0;
		
		public void printString(){
			System.out.println(n1_n+":"+n1_c+"\t"+n2_n+":"+n2_c+"\t"+n3_n+":"+n3_c+"\t"+n4_n+":"+n4_c+"\t");
			System.out.println(n1_cov+":"+n1_score+"\t"+n2_cov+":"+n2_score+"\t"+n3_cov+":"+n3_score+"\t"+n4_cov+":"+n4_score+"\n\t"+total_score);
		}

		public int getN1_c() {
			return n1_c;
		}

		public void setN1_c(int n1_c) {
			this.n1_c = n1_c;
		}

		public int getN1_n() {
			return n1_n;
		}

		public void setN1_n(int n1_n) {
			this.n1_n = n1_n;
		}

		public float getN1_cov() {
			return n1_cov;
		}

		public void setN1_cov(float n1_cov) {
			this.n1_cov = n1_cov;
		}

		public int getN2_c() {
			return n2_c;
		}

		public void setN2_c(int n2_c) {
			this.n2_c = n2_c;
		}

		public int getN2_n() {
			return n2_n;
		}

		public void setN2_n(int n2_n) {
			this.n2_n = n2_n;
		}

		public float getN2_cov() {
			return n2_cov;
		}

		public void setN2_cov(float n2_cov) {
			this.n2_cov = n2_cov;
		}

		public int getN3_c() {
			return n3_c;
		}

		public void setN3_c(int n3_c) {
			this.n3_c = n3_c;
		}

		public int getN3_n() {
			return n3_n;
		}

		public void setN3_n(int n3_n) {
			this.n3_n = n3_n;
		}

		public float getN3_cov() {
			return n3_cov;
		}

		public void setN3_cov(float n3_cov) {
			this.n3_cov = n3_cov;
		}

		public int getN4_c() {
			return n4_c;
		}

		public void setN4_c(int n4_c) {
			this.n4_c = n4_c;
		}

		public int getN4_n() {
			return n4_n;
		}

		public void setN4_n(int n4_n) {
			this.n4_n = n4_n;
		}

		public float getN4_cov() {
			return n4_cov;
		}

		public void setN4_cov(float n4_cov) {
			this.n4_cov = n4_cov;
		}

		public double getN1_score() {
			return n1_score;
		}

		public void setN1_score(double n1_score) {
			this.n1_score = n1_score;
		}

		public double getN2_score() {
			return n2_score;
		}

		public void setN2_score(double n2_score) {
			this.n2_score = n2_score;
		}

		public double getN3_score() {
			return n3_score;
		}

		public void setN3_score(double n3_score) {
			this.n3_score = n3_score;
		}

		public double getN4_score() {
			return n4_score;
		}

		public void setN4_score(double n4_score) {
			this.n4_score = n4_score;
		}

		public double getTotal_score() {
			return total_score;
		}

		public void setTotal_score(double total_score) {
			this.total_score = total_score;
		}
		
	}
	
	public score scoreTree(int[][] data, int nrows, int ncols, int p1, int p2, int p3){
		score result = new score();

		for(int y=0;y<nrows; y++){
			//node 1 
			if(data[p1][y]==1&&data[p2][y]==1){
				result.n1_cov++;
				if(data[ncols-1][y]==1){
					result.n1_c++;
				}else{
					result.n1_n++;
				}
			}
			//node 2
			if(data[p1][y]==1&&data[p2][y]==0){
				result.n2_cov++;
				if(data[ncols-1][y]==1){
					result.n2_c++;
				}else{
					result.n2_n++;
				}
			}
			//node 3
			if(data[p1][y]==0&&data[p3][y]==1){
				result.n3_cov++;
				if(data[ncols-1][y]==1){
					result.n3_c++;
				}else{
					result.n3_n++;
				}
			}
			//node 4
			if(data[p1][y]==0&&data[p3][y]==0){
				result.n4_cov++;
				if(data[ncols-1][y]==1){
					result.n4_c++;
				}else{
					result.n4_n++;
				}
			}
		}
		
		result.n1_score = result.n1_cov*(float)Math.max(result.n1_n, result.n1_c)/(0.01+result.n1_n+result.n1_c); //n1_cov*
		result.n2_score = result.n2_cov*(float)Math.max(result.n2_n, result.n2_c)/(0.01+result.n2_n+result.n2_c);
		result.n3_score = result.n3_cov*(float)Math.max(result.n3_n, result.n3_c)/(0.01+result.n3_n+result.n3_c);
		result.n4_score = result.n4_cov*(float)Math.max(result.n4_n, result.n4_c)/(0.01+result.n4_n+result.n4_c);
		
		result.total_score = result.n1_score+result.n2_score+result.n3_score+result.n4_score;
		return result;
	}
	
	public void writeData(int[][] data, int nrows, int ncols, String file){
		try {
			FileWriter f = new FileWriter(file);
			for(int y=0;y<nrows; y++){
				for(int x=0;x<ncols; x++){
					f.write(data[x][y]+",");
				}
				f.write("\n");
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int[][] generateData(int ncols, int nrows, float node1_bias){
		int[][] data = new int[ncols][nrows];
		//initialize to 50/50 class divide with random variables
		for(int y=0;y<nrows; y++){
			for(int x=0;x<ncols-1; x++){
				data[x][y] = flip();
			}
			if(y<nrows/2){
				data[ncols-1][y] = 1;
			}else{
				data[ncols-1][y] = 0;
			}
		}
		//make the first variable correlate with the class 50% of the time
		//		for(int y=0;y<nrows; y++){
		//			if(Math.random()>0.50){
		//				data[0][y] = data[ncols-1][y];
		//			}
		//		}
		//make the intersection of the the first and third variables correlate at least node1_bias%
		for(int y=0;y<nrows; y++){
			if(Math.random()<node1_bias){
				if(data[ncols-1][y]==1){
					data[0][y] = 1;
					data[2][y] = 1;				
				}else{
					if(data[0][y]== 1){
						data[2][y] = 0;	
					}else{
						data[2][y] = 1;	
					}
				}
			}
		}
		return data;
	}
	
	public int[][] readData(String file){
		Map<Integer, Integer[]> i_row = new HashMap<Integer, Integer[]>();
		int ncols = 0;
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String row = f.readLine();
			Integer index = 0;
			while(row!=null){
				String[] r = row.split(",");
				ncols = r.length;
				Integer[] rint = new Integer[r.length];
				int i = 0;
				for(String item : r){
					rint[i] = Integer.parseInt(item);
					i++;
				}
				i_row.put(index, rint);
				index++;
				row = f.readLine();
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[][] data = new int[ncols][i_row.size()];
		for(int r=0; r<i_row.size();r++){
			for(int c=0; c<ncols; c++){
				data[c][r] = i_row.get(r)[c];
			}
		}
		return data;
	}
	
	public static int flip(){
		if(Math.random()>0.5){
			return 1;
		}else{
			return 0;
		}
	}

}
