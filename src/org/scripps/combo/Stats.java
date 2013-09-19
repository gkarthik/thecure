/**
 * 
 */
package org.scripps.combo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.scripps.StatUtil;
import org.scripps.SimulationP;
import org.scripps.combo.GameLog.high_score;
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.combo.model.Player.PlayerSet;
import org.scripps.combo.weka.ClassifierEvaluation;
import org.scripps.combo.weka.GeneRanker;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.GeneRanker.gene_rank;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;
import org.scripps.util.StatFun;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

/**
 * @author bgood
 *
 */
public class Stats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String output_dir = "/Users/bgood/workspace/aacure/database/stats/";
		generateAllStats(output_dir);
	}

	/**
	 * Track the generation of results data to be used for presentation and publication
	 * 
	 * @param output_dir
	 */
	public static void generateAllStats(String output_dir){
		String outfile = "";
		//////////////////////////////////////////
		/// Time and quantity
		////////////////////////////////////////
		//games
		////////////////////////////////////////
//		System.out.println("Starting game analysis...");
//		outfile = output_dir+"games_day.txt";
//		String day_or_month = "day";
//		outputGamesPerTime(day_or_month, outfile);
//		outfile = output_dir+"games_month.txt";
//		day_or_month = "month";
//		outputGamesPerTime(day_or_month, outfile);
//		///////////////////////////////////////
//		//players
//		//////////////////////////////////////
//		System.out.println("Starting players analysis...");
//		outfile = output_dir+"players_month";
//		outputNewPlayersPerTime(outfile);
//		outfile = output_dir+"global_player_info.txt";
//		outputGlobalPlayerInfo(outfile);
//		outfile = output_dir+"player_game_counts.txt";
//		outputPlayerGames(outfile);
//		outfile = output_dir+"players/players_all_games.txt";
//		boolean only_first_per_board = false;
//		Player.describePlayers(only_first_per_board, outfile, null);
//		List<String> datasets = getDatasets();				
//		for(String dataset : datasets){
//			outfile = output_dir+"players/players_"+dataset+".txt";
//			Player.describePlayers(only_first_per_board, outfile, dataset);
//		}
//		only_first_per_board = true;
//		outfile = output_dir+"players/players_all_first_games.txt";
//		Player.describePlayers(only_first_per_board, outfile, null);
//		for(String dataset : datasets){
//			outfile = output_dir+"players/players_first_"+dataset+".txt";
//			Player.describePlayers(only_first_per_board, outfile, dataset);
//		}
//		outfile = output_dir+"players/player_agreeability_dream_griffith_breast_cancer_1.txt";
//		boolean first_hand_only = true;
//		String dataset = "griffith_breast_cancer_1";
//		outputPlayerAgreeability(outfile, first_hand_only, dataset);
//		String inforchart = outfile;
//		buildAgreeabilityCharts(inforchart, output_dir); 
//		///////////////////////////////////////
//		//Boards (and genes)
//		//////////////////////////////////////		
//		System.out.println("Starting board analysis...");
//		outfile = output_dir+"board_consensus.txt";
//		boolean random = false;
//		first_hand_only = true;
//		outputBoardConsensus(outfile, output_dir+"generankings/OneYear/Sgene", first_hand_only, random);
//		random = true;
//		outfile = output_dir+"board_consensus_1st_hand_random.txt";
//		outputBoardConsensus(outfile, output_dir+"generankings/BarneyBoardConsensus", first_hand_only, random);
//		///////////////////////////////////////
//		//genes
//		//////////////////////////////////////
//		System.out.println("Starting gene-centric analysis...");
//		outputFrequencyBasedGeneRankings(output_dir+"generankings/OneYear/");
//		outputIntersectionOfDiffRankingMethods(output_dir+"generankings/OneYear/");
//		String backgroundgenefile = output_dir+"generankings/background_genes.txt";
//		String gamegenefiledir = output_dir+"generankings/OneYear/";
//		String againstgenefiledir = output_dir+"PublicPredictorGeneSets/GeneSigDB/";
//		outfile = output_dir+"PublicPredictorGeneSets/CompareToCure.txt";
		int maxdepth = 404;
//		outputGeneSetComparisons(backgroundgenefile, gamegenefiledir, againstgenefiledir, maxdepth, outfile);
		///////////////////////////////////////
		//classifiers
		//////////////////////////////////////	
		maxdepth = 404;
		outfile = output_dir+"generankings/classifier_eval_d404.txt";
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
		String dataset = "griffith_breast_cancer_full_train";
		testGeneSetsInClassifiers(output_dir+"generankings/genesets_for_classifiers/",maxdepth, train_file, dataset, outfile);
	}

	//TODO fix situation where dataset being tested does not contain the genes in the test set
	// e.g. Alert! entrez_id 114569 did not map to any attributes in dataset: griffith_breast_cancer_full_train !!
	// Alert! entrez_id 222166 did not map to any attributes in dataset: griffith_breast_cancer_full_train !!
	
	public static void testGeneSetsInClassifiers(String genesetdir, int maxdepth, String train_file, String dataset, String fileout){
		boolean all_probes = true;
		Classifier model = null;
		try {
			Weka weka = new Weka();
			System.out.println("loading... "+train_file);
			weka.buildWeka(new FileInputStream(train_file), null, dataset);
			System.out.println("Weka initialized ");
			FileWriter out = new FileWriter(fileout);			
			float cv = 0;
			File d = new File(genesetdir);
			if(d.isDirectory()){
				for(String testgenefile : d.list()){
					if(testgenefile.startsWith(".")){
						continue;
					}else{
						int colindex = 0; String delimiter = "\t";
						Set<Integer> genes = readEntrezIdsFromFile(genesetdir+testgenefile, colindex, maxdepth, delimiter);
						List<Integer> entrez_ids = new ArrayList<Integer>(genes);
						int c = 0;
						String cmodel = "";
						for(int cm=0; cm<3; cm++){
							if(c==0){
								model = new J48();
								cmodel = "J48";
							}else if(c==1){
								model = new SMO();
								cmodel = "SVM";
							}else if(c==2){
								model = new RandomForest();
								cmodel = "RF";
							}
							Weka.execution result = weka.pruneAndExecuteWithEntrezIds(entrez_ids, model, dataset, all_probes);
							ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
							cv = short_result.getAccuracy();
							String row = testgenefile+"\t"+cmodel+"_cv\t"+cv;
							System.out.println(row);
							out.write(testgenefile+"\t"+cmodel+"_cv\t"+cv+"\n");
							c++;
						}
					}
				}	
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
/**
 * get the genes that always show up.. limited to the top 200 of ranked lists
 * @param gamegenesetdir
 */
	public static void outputIntersectionOfDiffRankingMethods(String gamegenesetdir){
		File d = new File(gamegenesetdir);
		Set<Integer> intersect = new HashSet<Integer>();
		if(d.isDirectory()){
			int i=0;
			for(String testgenefile : d.list()){
				if(testgenefile.startsWith(".")||testgenefile.startsWith("intersect")){
					continue;
				}else{
					if(i==0){
						intersect = readEntrezIdsFromFile(gamegenesetdir+testgenefile, 0, 200, "\t");
					}else{
						intersect.retainAll(readEntrezIdsFromFile(gamegenesetdir+testgenefile, 0, 200, "\t"));
					}
				}				
				i++;
			}			
			try {
				FileWriter w = new FileWriter(gamegenesetdir+"intersection.txt");
				for(Integer gene : intersect){
					w.write(gene+"\n");
				}
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	/**
	 * Iterates through the gene sets produced by this analysis and compares them to several references gene sets fore predicting breast cancer prognosis.
	 * @param backgroundgenefile
	 * @param gamegenesetdir
	 * @param comparetodir
	 * @param depth
	 * @param outfile
	 */
	public static void outputGeneSetComparisons(String backgroundgenefile, String gamegenesetdir, String comparetodir, int maxdepth, String outfile){
		File d = new File(gamegenesetdir);
		if(d.isDirectory()){
			FileWriter w;
			try {
				w = new FileWriter(outfile);
				w.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(String testgenefile : d.list()){
				if(testgenefile.startsWith(".")){
					continue;
				}
				Map<String, TwoByTwo> p = computeOverlapMetrics(backgroundgenefile, gamegenesetdir+testgenefile, comparetodir, maxdepth);
				try {
					w = new FileWriter(outfile, true);
					w.write("\n"+testgenefile+"\n");
					System.out.println("\n"+testgenefile);
					for(String key : p.keySet()){
						System.out.println(key+"\t"+p.get(key));
						TwoByTwo t = p.get(key);
						w.write(key+"\t"+t.fisherP+"\t"+t.a+"\t"+t.b+"\t"+t.c+"\t"+t.d+"\n");
					}
					w.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	static class TwoByTwo {
		int a, b, c, d;
		double fisherP;
	}
	/**
	 * Does the set comparisons to produce a 2 * 2 table
	 * Responds with a map of the files used to compare against and the p value from a Fisher's exact test on the table
	 * @param backgroundgenefile
	 * @param testgenefile
	 * @param againstgenefiledir
	 * @param depth
	 * @return
	 */
	public static Map<String, TwoByTwo> computeOverlapMetrics(String backgroundgenefile, String testgenefile, String againstgenefiledir, int depth){
		Map<String, TwoByTwo> p_map = new HashMap<String, TwoByTwo>();
		Set<Integer> background = readEntrezIdsFromFile(backgroundgenefile, 0, 0, "\t");
		//		System.out.println("Background contains: "+background.size());
		Set<Integer> testgenes = readEntrezIdsFromFile(testgenefile, 0, depth, "\t");
		//		System.out.println("test set contains: "+testgenes.size());

		File d = new File(againstgenefiledir);
		if(d.isDirectory()){
			for(String againstgenefile : d.list()){
				if(againstgenefile.startsWith(".")){
					continue;
				}
				Set<Integer> against = readEntrezIdsFromFile(againstgenefiledir+againstgenefile, 0, 0, "\t");
				//			System.out.println("compareto contains: "+against.size());
				//load in gene sets

				//remove any genes not in background
				against.retainAll(background);
				//				System.out.println("After removing non-background, compareto contains: "+against.size());
				//this should not be necessary, but just to b sure
				testgenes.retainAll(background);
				//				System.out.println("After removing non-background, test set contains: "+testgenes.size());

				//build 2 by 2 table
				//a tp = n genes in both sets
				Set<Integer> tp_a = new HashSet<Integer>(against);
				tp_a.retainAll(testgenes);
				//b fp = n genes in the testset and not in the against set
				Set<Integer> fp_b = new HashSet<Integer>(testgenes);
				fp_b.removeAll(against);
				//c fn = n genes in against set and not in the test set
				Set<Integer> fn_c = new HashSet<Integer>(against);
				fn_c.removeAll(testgenes);
				//d tn = n genes not in the test set and not in the against set
				Set<Integer> tn_d = new HashSet<Integer>(background);
				tn_d.removeAll(against);  tn_d.removeAll(testgenes);
				//test with fisherexact
				double p = StatUtil.fishersExact2tailed(tp_a.size(), fp_b.size(), fn_c.size(), tn_d.size());
				System.out.println("\n"+tp_a.size()+"\t"+fp_b.size()+"\n"+fn_c.size()+"\t"+tn_d.size());
				System.out.println(p);
				TwoByTwo t = new TwoByTwo();
				t.a = tp_a.size(); t.b = fp_b.size(); t.c = fn_c.size(); t.d = tn_d.size();
				t.fisherP = p;
				p_map.put(againstgenefile, t);
			}
		}
		return p_map;
	}
	/**
	 * Considers all the games on all the boards from gene-centric view.
	 * Results are sorted with lowest p values on top.
	 * @param outfileroot
	 */
	public static void outputFrequencyBasedGeneRankings(String outfileroot){
		GeneRanker gr = new GeneRanker();
		String dataset = null;
		String outfile = outfileroot+"all_players.txt";
		boolean only_winning = false;
		boolean only_cancer_people = false;
		boolean only_bio_people = false;
		boolean only_phd = false;
		for(int i=0; i< 4; i++){
			if(i==0){
				System.out.println("Running all.. ");
			}
			else if(i==1){
				only_cancer_people = true;
				outfile = outfileroot+"only_cancer_f.txt";
				System.out.println("Running with cancer knowledge.. ");
			}else if(i==2){
				only_cancer_people = false;
				only_phd = true;
				outfile = outfileroot+"only_phd_f.txt";
				System.out.println("Running with phd.. ");
			}else if(i==3){
				only_cancer_people = true;
				only_phd = true;
				outfile = outfileroot+"only_cancer_and_phd_f.txt";
				System.out.println("Running with cancer knowledge and phd.. ");
			}
			Map<String, gene_rank> bg_rank = gr.getRankedGenes(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
			List<gene_rank> ranked = new ArrayList<gene_rank>(bg_rank.values());
			ranked = gr.setFrequencyByViews(ranked);
			ranked = gr.setEstimatedPvalue(ranked);
			ranked = gr.sortByFrequency(ranked);
			ranked = gr.sortByP(ranked);
			try {
				FileWriter f = new FileWriter(outfile);
				f.write("entrez_id\tlocal_id\tsymbol\tviews\tvotes\tfrequency\tSimP\n");
				int count_p_sig = 0;
				for(gene_rank r : ranked){
					if(r.views > 4){
						if(r.p<0.01){
							count_p_sig++;
						}
						f.write(r.entrez+"\t"+r.f_id+"\t"+r.symbol+"\t"+r.views+"\t"+r.votes+"\t"+r.frequency+"\t"+r.p+"\n");
					}
				}
				System.out.println("N sig genes = "+count_p_sig);
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Summarizes the results on a per-board basis across all players.
	 * Also outputs 2 gene lists (one at p < 0.05 and one < 0.01)
	 * The genes in the list are members of boards that contained significantly different distributions of responses than random (based on a simulation).
	 * Only the genes with a within-board frequency of selection >= to the gene with the lowest p value in the board are kept.
	 * Typically, a gene with a very high frequency of selection will also have a low p value.  The strategy taken here is meant to cover 
	 * cases where a board may contain multiple good genes hence making their individual frequencies lower.  So, a gene that has the 
	 * second highest frequency may actually have a lower p value (based on its rank) than the gene with the higher frequency. In that case, both 
	 * of the genes are kept.
	 * @param outfile
	 * @param genefile
	 * @param first_hand_only
	 * @param random
	 */
	public static void outputBoardConsensus(String outfile, String genefile, boolean first_hand_only, boolean random){
		//set up tables to get P vlaues from simulated data
		int n_per_board = 25;
		int min_players = 1;
		int max_players = 26;
		int n_cards = 5;
		int n_runs = 1000; int n_times = 100;
		System.out.println("P sim initializing");
		SimulationP simp = new SimulationP();
		simp.initCureSim(n_per_board, min_players, max_players, n_cards, n_runs, n_times);
		System.out.println("P sim initialized");
		boolean drop_mammal = true; boolean setfeatures = false;
		List<Board> boards = Board.getAllBoards(drop_mammal, setfeatures);

		try {
			FileWriter out = new FileWriter(outfile);
			GeneRanker gr = new GeneRanker();
			out.write("board_id\tmin_freq\tavg_freq\tmax_freq\tn_cards_counted\tplayer_count\tboard_base_score\tdataset\troom\tcreated\tchisquared\tchi_p\tg\tg_p\tmin_simP\t");
			for(int i=1; i<=25; i++){
				out.write("G"+i+"\t");
			}
			out.write("\n");
			int i = 0;
			Set<String> p05_genes = new HashSet<String>();
			Set<String> p01_genes = new HashSet<String>();
			for(Board board : boards){
				i++;
				Map<String, GeneRanker.gene_rank> ranks = gr.getBoardConsensus(board.getId(), 0, first_hand_only, random);
				DescriptiveStatistics freqs = new DescriptiveStatistics();
				DescriptiveStatistics votes = new DescriptiveStatistics();
				float player_count = 0;
				long[] counts = new long[ranks.size()];//should always be 25 but anyway..
				int b = 0;	
				String histo = "";
				List<GeneRanker.gene_rank> ranked = new ArrayList<GeneRanker.gene_rank>(ranks.values());
				Collections.sort(ranked);
				Collections.reverse(ranked);
				double lowest_p = 1;
				Set<String> p_genes_tmp = new HashSet<String>();
				for(GeneRanker.gene_rank rank : ranked){
					//grab simP value at this rank.
					double p = simp.getP((int)rank.players, (double)rank.frequency, b);
					p_genes_tmp.add(rank.entrez+"\t"+rank.symbol+"\t");
					if(lowest_p > p){
						lowest_p = p;
						if(lowest_p<0.01){
							p01_genes.addAll(p_genes_tmp);
						}
						if(lowest_p<0.05){
							p05_genes.addAll(p_genes_tmp);
						}
					}
					counts[b] = (long)rank.votes;
					histo+=rank.symbol+"_"+rank.entrez+":"+rank.votes+";"+rank.frequency+" p"+p+"\t";
					freqs.addValue(rank.frequency);
					votes.addValue(rank.votes);
					player_count = rank.players; //inelegant to keep setting, but same for all players..
					b++;
				}
				double[] r_p = StatUtil.chiSquaredTestForUniformDistribution(counts);
				double[] g_p = StatUtil.gTestForUniformDistribution(counts);
				String row = board.getId()+"\t"+freqs.getMin()+"\t"+freqs.getMean()+"\t"+freqs.getMax()+"\t"+votes.getSum()+"\t"+player_count;
				row+= "\t"+board.getBase_score()+"\t"+board.getDataset()+"\t"+board.getRoom()+"\t"+board.getUpdated();
				row+= "\t"+r_p[0]+"\t"+r_p[1]+"\t"+g_p[0]+"\t"+g_p[1]+"\t"+lowest_p+"\t"+histo;

				out.write(row+"\n");
				System.out.println(i+"\t"+row);
			}
			out.close();

			//write the significant gene sets
			FileWriter gene05 = new FileWriter(genefile+"p05.txt");
			for(String gene : p05_genes){
				gene05.write(gene+"\n");
			}
			gene05.close();

			FileWriter gene01 = new FileWriter(genefile+"p01.txt");
			for(String gene : p01_genes){
				gene01.write(gene+"\n");
			}
			gene01.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void buildAgreeabilityCharts(String input_file, String output_dir){

		try {
			int n = 0;
			DescriptiveStatistics phd = new DescriptiveStatistics();
			DescriptiveStatistics nophd = new DescriptiveStatistics();
			DescriptiveStatistics cancerk = new DescriptiveStatistics(); DescriptiveStatistics nocancerk = new DescriptiveStatistics();
			DescriptiveStatistics biok = new DescriptiveStatistics(); DescriptiveStatistics nobiok = new DescriptiveStatistics();
			BufferedReader f = new BufferedReader(new FileReader(input_file));
			f.readLine();//skip header
			String line = f.readLine();
			while(line!=null){
				n++;
				String[] data = line.trim().split("\t");
				if(data!=null&&data.length==8){
					if(!data[5].equals("0")){
						double count = Double.parseDouble(data[5]);
						double mean = Double.parseDouble(data[6]);
						double median = Double.parseDouble(data[7]);
						if(data[2].equals("yes")){
							biok.addValue(median);
						}else{
							nobiok.addValue(median);
						}

						if(data[3].equals("yes")){
							cancerk.addValue(median);
						}else{
							nocancerk.addValue(median);
						}
						if(data[4].equals("phd")){
							phd.addValue(median);
						}else{
							nophd.addValue(median);
						}
					}
				}
				line = f.readLine();
			}
			//sanity check
			MannWhitneyUTest mw = new MannWhitneyUTest();
			double know_cancer_diff_agree_w = mw.mannWhitneyUTest(nocancerk.getSortedValues(), cancerk.getSortedValues());
			double phd_diff_agree_w = mw.mannWhitneyUTest(nophd.getSortedValues(), phd.getSortedValues());
			double bio_diff_agree_w = mw.mannWhitneyUTest(nobiok.getSortedValues(), biok.getSortedValues());
			System.out.println(
					"MW for cancer knowledge and agreeability:"+know_cancer_diff_agree_w+" " +
					" mean know: "+cancerk.getMean()+" "+cancerk.getN()+" "+
					" mean don't know: "+nocancerk.getMean()+" "+nocancerk.getN());
			System.out.println(
					"w for phd and agreeability:"+phd_diff_agree_w +
					" mean phd: "+phd.getMean()+" "+phd.getN()+" "+
					" mean without phd: "+nophd.getMean()+" "+nophd.getN());
			System.out.println(
					"w for bioK and agreeability:"+bio_diff_agree_w +
					" mean with bio k: "+biok.getMean()+" "+biok.getN()+" "+
					" mean without bio K: "+nobiok.getMean()+" "+nobiok.getN());			
			//histos
			//			Map<String,double[]> datas = new HashMap<String, double[]>();
			//			datas.put("PhD", phd.getValues());
			//			datas.put("No PhD", nophd.getValues());
			//			StatUtil.plotHistograms(datas, 10, "Agreeability Estimates", "Bin Mean (count)", "Frequency", output_dir+"players/PhD_agreeability", true);
			//			datas = new HashMap<String, double[]>();
			//			datas.put("Cancer Knowledge", phd.getValues());
			//			datas.put("No Cancer Knowledge", nophd.getValues());
			//			StatUtil.plotHistograms(datas, 10, "Agreeability Estimates", "Bin Mean (count)", "Frequency", output_dir+"players/CancerK_agreeability", true);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	/**
	 * Quantify how similar the player's gene selections are to the community consensus
	 * @param outfile
	 */
	public static void outputPlayerAgreeability(String outfile, boolean first_hand_only, String dataset){
		List<Player> players = Player.getAllPlayers();

		try {
			FileWriter out = new FileWriter(outfile);
			out.write("player.getId()\tplayer.getName()\tplayer.getBiologist()\tplayer.getCancer()\tplayer.getDegree()\tpc.getN()\tpc.getMean()\tpc.getPercentile(50)\n");
			int i = 0;
			DescriptiveStatistics with_cancer_knowledge = new DescriptiveStatistics();
			DescriptiveStatistics without_cancer_knowledge = new DescriptiveStatistics();
			DescriptiveStatistics with_phd = new DescriptiveStatistics();
			DescriptiveStatistics without_phd = new DescriptiveStatistics();
			for(Player player : players){
				i++;
				DescriptiveStatistics pc = Player.measurePCscore(player.getId(), first_hand_only, dataset);
				out.write(player.getId()+"\t"+player.getName()+"\t"+player.getBiologist()+"\t"+player.getCancer()+"\t"+player.getDegree()+"\t");
				out.write(pc.getN()+"\t"+pc.getMean()+"\t"+pc.getPercentile(50)+"\n");
				System.out.println(i);
				if(pc.getN()>0){
					if(player.getCancer().equals("yes")){
						with_cancer_knowledge.addValue(pc.getPercentile(50));
					}else{
						without_cancer_knowledge.addValue(pc.getPercentile(50));
					}
					if(player.getDegree().equals("phd")){
						with_phd.addValue(pc.getPercentile(50));
					}else{
						without_phd.addValue(pc.getPercentile(50));
					}
				}
			}
			//tests
			double know_cancer_diff_agree_t = TestUtils.tTest(without_cancer_knowledge, with_cancer_knowledge);
			double phd_diff_agree_t = TestUtils.tTest(without_phd, with_phd);

			MannWhitneyUTest mw = new MannWhitneyUTest();
			double know_cancer_diff_agree_w = mw.mannWhitneyUTest(without_cancer_knowledge.getSortedValues(), with_cancer_knowledge.getSortedValues());
			double phd_diff_agree_w = mw.mannWhitneyUTest(without_phd.getSortedValues(), with_phd.getSortedValues());
			System.out.println("t for cancer knowledge and agreeability:"+know_cancer_diff_agree_t+" " +
					"MW for cancer knowledge and agreeability:"+know_cancer_diff_agree_w+" " +
					" mean know: "+with_cancer_knowledge.getMean()+" "+with_cancer_knowledge.getN()+" "+
					" mean don't know: "+without_cancer_knowledge.getMean()+" "+without_cancer_knowledge.getN());
			System.out.println("t for phd and agreeability:"+phd_diff_agree_t +" "+
					"w for phd and agreeability:"+phd_diff_agree_w +
					" mean phd: "+with_phd.getMean()+" "+with_phd.getN()+" "+
					" mean without phd: "+without_phd.getMean()+" "+without_phd.getN());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static List<String> getDatasets(){
		List<String> datasets = new ArrayList<String>();
		String q = "select distinct dataset from board";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				String d = rslt.getString("dataset");
				datasets.add(d);
			} 
			rslt.close();
			conn.connection.close();	
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datasets;
	}

	public static void outputPlayerGames(String outfile){
		boolean only_winning = false;
		String dataset = null;//gets all of them
		List<Game> hands = Game.getAllGames(only_winning, dataset);
		GameLog log = new GameLog();
		GameLog.high_score sb = log.getScoreBoard(hands, dataset);

		try {
			FileWriter out = new FileWriter(outfile);
			int i = 0;
			for(Entry<String, Integer> p_games : sb.player_games.entrySet()){
				i++;
				if(i<10){
					System.out.println(p_games.getKey()+"\t"+p_games.getValue()+"\t");
				}
				out.write(p_games.getKey()+"\t"+p_games.getValue()+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputGlobalPlayerInfo(String outfile){
		try {
			FileWriter out = new FileWriter(outfile);
			Player p = new Player();
			PlayerSet ps = p.getGlobalPlayerCounts();
			Map<String, Float> degree_pct = MapFun.convertCountsToPercentages(ps.degree_count);
			for(Entry<String, Integer> degree_c : ps.degree_count.entrySet()){
				System.out.println(degree_c.getKey()+"\t"+degree_c.getValue()+"\t"+degree_pct.get(degree_c.getKey()));
				out.write(degree_c.getKey()+"\t"+degree_c.getValue()+"\n");
			}
			Map<String, Float> bio_pct = MapFun.convertCountsToPercentages(ps.biologist_count);
			System.out.println("\nBiologist?");
			for(Entry<String, Integer> bio_c : ps.biologist_count.entrySet()){
				System.out.println(bio_c.getKey()+"\t"+bio_c.getValue()+"\t"+bio_pct.get(bio_c.getKey()));
				out.write("\nBiologist?\n"+bio_c.getKey()+"\t"+bio_c.getValue()+"\n");
			}
			Map<String, Float> cancer_pct = MapFun.convertCountsToPercentages(ps.cancer_count);
			System.out.println("\nKnow about cancer?");
			for(Entry<String, Integer> cancer_c : ps.cancer_count.entrySet()){
				System.out.println(cancer_c.getKey()+"\t"+cancer_c.getValue()+"\t"+cancer_pct.get(cancer_c.getKey()));
				out.write("\nKnow about cancer?\n"+cancer_c.getKey()+"\t"+cancer_c.getValue()+"\n");
			}
			//
			System.out.println("\nTarget Audience");
			for(Entry<String, Integer> target_c : ps.target_audience_count.entrySet()){
				System.out.println(target_c.getKey()+"\t"+target_c.getValue());
				out.write("\nTarget\n"+target_c.getKey()+"\t"+target_c.getValue()+"\n");
			}			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputNewPlayersPerTime(String outfile){
		Player p = new Player();
		Map<String, Player.RegMonth> day_count = p.getNewPlayersPerMonth();
		SimpleDateFormat f = new SimpleDateFormat();
		try {
			//by degree
			FileWriter out = new FileWriter(outfile+"_degree.txt");
			System.out.println("Month\tPhD\tMD\tMSc\tBA\tnone\tDid not state\tOther");
			out.write("Month\tPhD\tMD\tMSc\tBA\tnone\tDid not state\tOther\n");
			for(Player.RegMonth month : day_count.values()){
				String output = f.format(month.month.getTime())+"\t"+month.degree_count.get("phd")+"\t"+month.degree_count.get("md")+"\t"+month.degree_count.get("masters")+"\t"+month.degree_count.get("bachelors")+"\t"+month.degree_count.get("none")+"\t"+month.degree_count.get("ns")+"\t"+month.degree_count.get("other");
				System.out.println(output);
				out.write(output+"\n");
			}
			out.close();
			//by cancer knowledge
			out = new FileWriter(outfile+"_cancerknowledge.txt");
			System.out.println("Month\tKnow about cancer\tDo not know about cancer\tDid not state");
			out.write("\nMonth\tKnow about cancer\tDo not know about cancer\tDid not state");
			for(Player.RegMonth month : day_count.values()){
				String output = f.format(month.month.getTime())+"\t"+month.cancer_knowledge_count.get("yes")+"\t"+month.cancer_knowledge_count.get("no")+"\t"+month.cancer_knowledge_count.get("ns");
				System.out.println(output);
				out.write(output+"\n");
			}
			out.close();
			//by biologist
			out = new FileWriter(outfile+"_biologist.txt");
			System.out.println("Month\tBiologist\tNot a biologist\tDid not state");
			out.write("\nMonth\tBiologist\tNot a biologist\tDid not state\n");
			for(Player.RegMonth month : day_count.values()){
				String output = f.format(month.month.getTime())+"\t"+month.biologist_count.get("yes")+"\t"+month.biologist_count.get("no")+"\t"+month.biologist_count.get("ns");
				System.out.println(output);
				out.write(output+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputGamesPerTime(String day_or_month, String outfile){
		boolean only_winning = false;
		Map<Calendar, Integer> day_count = Game.getGamesPerTime(only_winning, day_or_month);
		SimpleDateFormat f = new SimpleDateFormat();
		try {
			FileWriter out = new FileWriter(outfile);
			System.out.println(day_or_month+"\tGames");
			out.write(day_or_month+"\tGames\n");
			for(Entry<Calendar, Integer> dc : day_count.entrySet()){
				System.out.println(f.format(dc.getKey().getTime())+"\t"+dc.getValue());
				out.write(f.format(dc.getKey().getTime())+"\t"+dc.getValue()+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printDatasetScoreboard(String dataset, boolean only_winning){
		List<Game> whs = Game.getTheFirstGamePerPlayerPerBoard(only_winning, dataset, false, 0);
		//get a scoreboard
		GameLog log = new GameLog();
		high_score sb = log.getScoreBoard(whs, dataset);
		int r = 0;
		for(String name : sb.getPlayer_global_points().keySet()){
			r++;
			String displayName = name;
			if(name == null || name.length() == 0) {
				displayName = "anon";
			}
			if(name.length() > 14) {
				displayName = name.substring(0, 13);
			}
			System.out.println("<li>");    
			System.out.println("<span class=\"rank\">"+r+"</span>");
			System.out.println("<span class=\"player\">"+displayName+"</span>");
			System.out.println("<span class=\"max\">"+sb.getPlayer_max().get(name)+"</span>");
			System.out.println("<span class=\"avg\">"+sb.getPlayer_avg().get(name)+"</span>");
			System.out.println("<span class=\"games\">"+sb.getPlayer_games().get(name)+"</span>");
			System.out.println("<span class=\"points\">"+sb.getPlayer_global_points().get(name)+"</span>");
			System.out.println("</li>");
		}
	}

	public static Set<Integer> readEntrezIdsFromFile(String file, int colindex, int limit, String delimiter){
		System.out.println("parsing file "+file);
		Set<Integer> item = new HashSet<Integer>();
		try {
			int n = 0;
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null&&(n<limit||limit==0)){				
				if(!line.startsWith("#")&&line.length()>0&&!line.startsWith("Gene")&&!line.startsWith("\"Gene Id")&&!line.startsWith("entrez")){
					String[] s = line.split(delimiter);
					if(s!=null&&s.length>colindex){
						String thing = s[colindex].trim();
						//check for "" grouped csv.. 
						if(thing.startsWith("\"")&&thing.endsWith("\"")){
							System.out.println("sorry, can't do the \"\"s with this csv.  try tab delimited..");
							return null;
						}else{
							try{
								int id = Integer.parseInt(thing);
								item.add(id);
								n++;
							}catch(Exception e){
								System.out.println("Tried to add "+thing+" whish is clearly not an entrez id from "+file);
								return null;
							}
						}
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}

	/**
	 * Simulate game play to estimate p values for n views / n selections of a gene
	 * @param n_views
	 * @param n_votes
	 * @return
	 */
	public static double getSimPerGeneP(int n_views, int n_votes){
		double p = 1;
		int times = 1;
		int runs = 10000;
		int n_sim_votes = 0;
		int board_size = 25; int hand_size = 5;
		Set<Integer> bboard = new HashSet<Integer>(board_size);
		List<Integer> board = null;
		for(int i=0; i<board_size; i++){
			bboard.add(i);
		}
		double sumP = 0;
		for(int t=0; t<times; t++){
			n_sim_votes = 0;
			for(int r=0; r<runs; r++){
				int sim_votes = 0;
				// one game = one view
				for(int o=0; o<n_views; o++){
					board = new ArrayList<Integer>(bboard);
					Collections.shuffle(board);
					//in each round two cards are selected
					//player 1 (human) always goes first
					for(int g=0; g<hand_size*2; g++){
						int test = board.get(g); 
						if(test==0){ //0 is the target gene
							if(g%2==0){
								sim_votes++;
							}else{
								break; //barney got the card
							}
						}
						board.remove(g); //take the selected card off the board
					}
				}
				if(sim_votes>n_votes){
					n_sim_votes++;
				}
			}
			p = (double)n_sim_votes/runs;
			sumP+=p;
		}
		return sumP/times;
	}
}
