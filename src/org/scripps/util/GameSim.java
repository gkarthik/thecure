package org.scripps.util;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Calculate P values based on simulations
 * @author bgood
 *
 */
public class GameSim {
	int n_per_board; 
	int n_cards;
	int min_players;
	int max_players;
	int n_runs;
	int n_times;
	public Map<Integer, double[][][]> playercount_runfreq;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
		int n_per_board = 25;
		int min_players = 9;
		int max_players = 11;
		int n_cards = 5;
		int n_runs = 1000; int n_times = 100;
		GameSim sim = new GameSim();
		long seed = 1;
		sim.initCureSim(n_per_board, min_players, max_players, n_cards, n_runs, n_times, seed);

		double count = 0;
		int rank = 0;
		int nplayers = 10;
		double[][][] run_freqs = sim.playercount_runfreq.get(nplayers);

		System.out.println("Done building matrix");

		System.out.println("rank\ttest\tp");
		for(rank=0; rank<3; rank++){
			for(double test=0.1; test<0.9; test+=0.1){
				double p = sim.getP(nplayers, test, rank);
				System.out.println(rank+"\t"+test+"\t"+p);
			}
		}
	}

	public double getP(int nplayers, double test, int rank){
		if(nplayers<min_players||nplayers>max_players){
			return 10;
		}
		double p = -10;
		double avg_p = 0;
		double[][][] run_freqs = playercount_runfreq.get(nplayers);
		double count=0;
		for(int ran=0; ran<n_times; ran++){
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for(int r=0; r<n_runs; r++){
//				try{
				stats.addValue(run_freqs[ran][r][rank]);
//				}catch(Exception e){
//					System.out.println(ran+" "+r+" "+rank);
//				}
			}	
			for(int i=0;i<stats.getN();i++){
				if(stats.getElement(i)>test){
					count++;
				}
			}
			p = count/(double)n_runs;
			//System.out.println(test+" one p is "+p);
			avg_p+=p;
			count=0;
		}
		p = avg_p/n_times;
		return p;
	}


	/**
	 * Make a table of random values for each num players between min and max
	 * This can be used to generate p values for real data given the same parameters
	 * @param n_per_board
	 * @param min_players
	 * @param max_players
	 * @param n_cards
	 * @param n_runs
	 * @return
	 */
	public void initCureSim(int _n_per_board, int _min_players, int _max_players, int _n_cards, int _n_runs, int _n_times, long seed){
		//init params
		n_per_board = _n_per_board; 
		n_cards = _n_cards;
		min_players = _min_players;
		max_players = _max_players;
		n_runs = _n_runs;
		n_times = _n_times;
		Random random = new Random(seed);
		playercount_runfreq = new HashMap<Integer, double[][][]>();
		//initialize
		List<String> genes = new ArrayList<String>(n_per_board);
		Map<String, Double> gene_freqs = new HashMap<String, Double>();
		for(int i=0; i< n_per_board; i++){
			String g = "g"+i;
			genes.add(g);
			gene_freqs.put(g, new Double(0));
		}

		//across n_players per board
		for(int n_players=min_players; n_players<max_players; n_players++){
			double[][][] run_freqs = new double[n_times][n_runs][n_per_board];
			//for each player level do this n_times 
			for(int ran=0; ran<n_times; ran++){
				//across n_runs
				for(int r=0;r< n_runs; r++){
					//across n players (1 game per player)
					for(int p=0; p<n_players; p++){
						//grab a new collection of n_per_board
						List<String> board = new ArrayList<String>(genes);
						//make a random hand of cards
						Collections.shuffle(board, random);
						for(int i=0; i<n_cards; i++){
							String g = board.get(i);
							//increment count for gene
							gene_freqs.put(g, gene_freqs.get(g)+1);
							//without replacement!
							board.remove(i);
						}
					}
					//counts to freqs
					for(Entry<String,Double> gene_freq : gene_freqs.entrySet()){
						gene_freqs.put(gene_freq.getKey(), gene_freq.getValue()/n_players);
					}
					//sort by count
					List<String> keys = MapFun.sortMapByValue(gene_freqs);
					Collections.reverse(keys); 
					int k =0;
					for(String key : keys){
						run_freqs[ran][r][k] = gene_freqs.get(key);
						k++;
					}
				}
			}
			playercount_runfreq.put(n_players, run_freqs);
		}
	}
	//make P table
	//	for(int rank=0; rank<n_per_board; rank++){
	//		DescriptiveStatistics stats = new DescriptiveStatistics();
	//		for(int r=0; r<n_runs; r++){
	//			stats.addValue(run_freqs[r][rank]);
	//		}
	//		//how many of the n_runs produce a value at this rank greater than test?
	//		for(double test=0; test <1; test+=0.01){
	//			double count = 0;
	//			for(int i=0;i<stats.getN();i++){
	//				if(stats.getElement(i)>test){
	//					count++;
	//				}
	//			}
	//			System.out.println("n_players "+n_players+" rank "+rank+" P for "+test+" = "+count/n_runs);
	//		}
	//	}		

	//for output
	//		Map<String,double[]> datas = new HashMap<String, double[]>();
	//		for(int rank=0; rank<n_per_board; rank++){
	//			DescriptiveStatistics stats = new DescriptiveStatistics();
	//			for(int r=0; r<n_runs; r++){
	//				stats.addValue(run_freqs[r][rank]);
	//			}
	//			if(rank==0||rank==1||rank==3||rank==12||rank==24){
	//				datas.put("Rank "+rank, stats.getValues());
	//			}
	//			
	//			System.out.println("rank "+rank+" mean="+stats.getMean()+" stndev="+stats.getStandardDeviation());
	//		}
	//		
	//		int bin_count = 0;
	//		String title = "Histo1";
	//		String xAxisTitle = "Bin Means";
	//		String yAxisTitle = "Counts";
	//		String file_out = null;
	//		StatUtil.plotHistograms(datas, bin_count, title, xAxisTitle, yAxisTitle, file_out, true);
}

