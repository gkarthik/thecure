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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.scripps.StatUtil;
import org.scripps.combo.GameLog.high_score;
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Feature;
import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.combo.model.Player.PlayerSet;
import org.scripps.combo.weka.ClassifierEvaluation;
import org.scripps.combo.weka.GeneRanker;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.GeneRanker.gene_rank;
import org.scripps.util.GameSim;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;
import org.scripps.util.SetComparison;
import org.scripps.util.StatFun;

import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.OneR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Utils;

/**
 * @author bgood
 *
 */
public class Stats {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String output_dir = "/Users/bgood/workspace/aacure/database/stats/";
		generateAllStats(output_dir);
	}

	/**
	 * Track the generation of results data to be used for presentation and publication
	 * 
	 * @param output_dir
	 * @throws IOException 
	 */
	public static void generateAllStats(String output_dir) throws IOException{
		String outfile = "";
		////////////////////////////////////////
		//games
		////////////////////////////////////////
		/// Time and quantity
		//		System.out.println("Starting game analysis...");
		//		outfile = output_dir+"games_day.txt";
		//		String day_or_month = "day";
		//		outputGamesPerTime(day_or_month, outfile);
		//		outfile = output_dir+"games_month.txt";
		//		day_or_month = "month";
		//		outputGamesPerTime(day_or_month, outfile);
		////////////////////////////////////////
		/// Bias detection

		//		outfile = output_dir+"generankings/rulebased/breast_cancer_power.txt";	
		//String rulegenerated = output_dir+"generankings/rulebased/breast_cancer_in_description.txt";
		//String rulegenerated = output_dir+"generankings/rulebased/cancer_in_description.txt";
		//		String rulegenerated = output_dir+"generankings/rulebased/cancer_or_tumor.txt";
		//String rulegenerated = output_dir+"generankings/rulebased/cancer_or_tumor_or_oma.txt";
		//		long seed = 2;
		//		testForGeneSelectionRules(outfile, rulegenerated, seed);

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
		//		outputIntersectionOfDiffRankingMethods(output_dir+"generankings/test/");
		//				String backgroundgenefile = output_dir+"generankings/background_genes.txt";
		//				String gamegenefiledir = output_dir+"genesets_for_classifiers/";//"generankings/test/";
		//				//String againstgenefiledir = output_dir+"generankings/rulebased/";
		//				outfile = output_dir+"SetComparisons/AllvsAll.txt";
		//				int maxdepth = 10000;
		//				boolean asmatrix = true;
		//				outputGeneSetComparisons(backgroundgenefile, gamegenefiledir, gamegenefiledir, maxdepth, outfile, asmatrix);
		///////////////////////////////////////
		//classifiers
		//////////////////////////////////////	
		//random
		//		outfile = output_dir+"generankings/cv_rand_griffith_full.txt";

		//outfile = output_dir+"generankings/classifier_eval_rule_based.txt";
		//outfile = output_dir+"generankings/classifier_eval_sanford_griffith.txt";		
		//		String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
		//		String test_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/full_test.arff";		
		//		String dataset = "griffith_breast_cancer_full_train";
		//				outfile = output_dir+"generankings/cv_rand_griffith_full_10cvper.txt";
		//int roundscv = 1; long seed = 5;
		//int nruns = 100; int ngenes = 100;
		//buildPvalTableForRandomGeneSets(indices_keep, backgroundgenefile, train_file, dataset, outfile, seed, nruns, ngenes, roundscv);	
		//testGeneSetsInClassifiers(output_dir+"generankings/genesets_for_classifiers/", train_file, test_file, dataset, outfile);	
		//testGeneSetsInClassifiers(output_dir+"generankings/rulebased/", train_file, test_file, dataset, outfile);	
		//testGeneSetsInClassifiers(output_dir+"generankings/test/", train_file, test_file, dataset, outfile);	

		//metabric
		//				String indices_keep = "";// "0,1,2,3,4,5,6,7,8,9,10,11,"; //the metabric clinical features
		//				outfile = output_dir+"generankings/classifier_eval_metabric_no_clinical_overall_survival.txt";
		//				String train_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/processed/Metabric_expression_no_sampleid.arff";		
		//				String test_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/processed/Oslo_expression_no_sampleid.arff";		
		//				String dataset = "metabric_expression_all";

		//				outfile = output_dir+"generankings/classifier_eval_metabric_no_clinical_ds_survival.txt";
		//				String train_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Metabric_clinical_expression_DSS_sample_filtered.arff";		
		//				String test_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Oslo_clinical_expression_OS_sample_filt.arff";	
		//				String dataset = "metabric_with_clinical";
		//				testGeneSetsInClassifiers(indices_keep, output_dir+"generankings/genesets_for_classifiers/", train_file, test_file, dataset, outfile, roundscv);		
		//				outfile = output_dir+"generankings/cv_rand_metabric_no_clinical_overall_survival.txt";
		//				long seed = 1;
		//				int nruns = 100; int ngenes = 100;

		//				outfile = output_dir+"generankings/cv_rand_metabric_with_clinical_ds_survival.txt";
		//				buildPvalTableForRandomGeneSets(indices_keep, backgroundgenefile, train_file, dataset, outfile, seed, nruns, ngenes, roundscv);		
		/**
				int roundscv = 1; long seed = 13;
				String indices_keep = ""; //"0,1,2,3,4,5,6,7,8,9,10,11,";
				String genesetdir = output_dir+"genesets_for_classifiers/";
				int nsimforp = 101;
				String train_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Metabric_clinical_expression_DSS_sample_filtered.arff";		
				String test_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Oslo_clinical_expression_OS_sample_filt.arff";	
				String dataset = "metabric_with_clinical";
//				String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
//				String test_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/full_test.arff";		
//				String dataset = "griffith_breast_cancer_full_train";

				Classifier model = new NaiveBayes();
				String fileout =  output_dir+"WrapperResults/MetaBricOslo/metabric_no_clinical_nb_cv1p100_13.txt";
				int skip_in_rand = 12;//don't pick metabric clinical features
				wrapperGeneSetEvaluation(skip_in_rand, model, indices_keep, genesetdir, train_file, test_file, dataset, fileout, 
						roundscv, nsimforp, seed);
				model = new SMO();
				fileout =  output_dir+"WrapperResults/MetaBricOslo/metabric_no_clinical_smo_cv1p100_13.txt";
				wrapperGeneSetEvaluation(skip_in_rand, model, indices_keep, genesetdir, train_file, test_file, dataset, fileout, 
						roundscv, nsimforp, seed);
				model= new RandomForest();
				((RandomForest) model).setSeed((int)seed);
				((RandomForest) model).setNumTrees(101);
				fileout =  output_dir+"WrapperResults/MetaBricOslo/metabric_no_clinical_rf101t_cv1p100_13.txt";
				wrapperGeneSetEvaluation(skip_in_rand, model, indices_keep, genesetdir, train_file, test_file, dataset, fileout, 
						roundscv, nsimforp, seed);
				model = new J48();
				fileout =  output_dir+"WrapperResults/MetaBricOslo/metabric_no_clinical_j48_cv1p100_13.txt";
				wrapperGeneSetEvaluation(skip_in_rand, model, indices_keep, genesetdir, train_file, test_file, dataset, fileout, 
						roundscv, nsimforp, seed);
		 **/					

		//measureGenePredictivePower();
	}


	public static void measureGenePredictivePower(){
		Set<Integer> genes = readEntrezIdsFromFile("/Users/bgood/workspace/aacure/database/stats/generankings/background_genes.txt",0,0,"\t");
		String out = "/Users/bgood/workspace/aacure/database/stats/singlegene/all_stump.txt";
		int skip_first = 0;
		String indices_keep = "";
		String dataset = "griffith_breast_cancer_full_train";
		String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
		String test_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/full_test.arff";	
		Classifier model = new DecisionStump();
		int ncv = 1; int nsimforp = 1; long seed = 1;
		Weka weka = new Weka();		
		System.out.println("loading... "+train_file);
		boolean setFeatures = false;
		try {
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
			System.out.println("Weka initialized ");
			String testname = "test123";
			System.out.println();
			FileWriter f = new FileWriter(out);
			for(Integer gene_id : genes){
				testname = gene_id+"\t"+Feature.getByUniqueId(gene_id+"").getShort_name();
				Set<Integer> testgenes = new HashSet<Integer>();
				testgenes.add(gene_id);
				classifierTestResult result = computeClassifierEvaluation(skip_first, model, indices_keep, dataset, ncv, nsimforp, seed, 
						testgenes, weka, testname);
				//System.out.println(model.toString());
				System.out.println(testname+"\t"+result.train_acc+"\t"+result.cv_acc+"\t"+result.test_acc);
				f.write("all\tstump\t"+testname+"\t"+result.train_acc+"\t"+result.cv_acc+"\t"+result.test_acc+"\n");
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Given a set of genes (derived from a particular rule applied externally), measure the number of games where
	 * genes were preferentially selected based on that set.  For example, a set might be "genes with cancer in their descriptions"
	 * This measures the probability that a player would select the number of these genes that they did by chance.  
	 * @throws IOException 
	 * 
	 */

	public static void testForGeneSelectionRules(String specialsetoutfile, String specialsetfile, long seed) throws IOException{
		//load the genes that are the result of whatever rule we are examining
		Set<Integer> special_all = readEntrezIdsFromFile(specialsetfile, 0,0,"\t");
		int board_size = 25; int hand_size = 5; int gamesim = 10000; boolean first_play = true;
		double sig_p = 0.05;
		Map<Integer, Map<Integer, Double>> size_ptable = getDistributionsForCardSelectionType(board_size, hand_size, seed, gamesim);
		boolean drop_mammal = true; boolean setfeatures = true;
		List<Board> boards = Board.getAllBoards(drop_mammal, setfeatures);
		int N_biased = 0; //games where people picked genes with cancer n their text more often than expected by random at given p threshold
		int N_unknown = 0;
		int N_no_specials = 0;
		Set<String> specialset = new HashSet<String>();
		Map<Integer, Integer> player_scount = new HashMap<Integer, Integer>();
		for(Board board: boards){
			List<Feature> genes = board.getFeatures();
			//get the "special" set
			List<String> special = new ArrayList<String>();
			for(Feature gene : genes){
				if(special_all.contains(Integer.parseInt(gene.getUnique_id()))){
					specialset.add(gene.getUnique_id());
					special.add(gene.getId()+"");
				}
				//				if((gene.getShort_name()!=null&&gene.getShort_name().contains(search))
				//						||(gene.getLong_name()!=null&&gene.getLong_name().contains(search))
				//						||(gene.getDescription()!=null && gene.getDescription().contains(search))){
				//					special.add(gene.getId()+""); //note in db feature id (not entrez) feature id space
				//					specialset.add(gene.getUnique_id());
				//				}
			}
			int specials = special.size();
			if(specials == 0){
				N_no_specials++;
			}
			Map<Integer, Double> ptable = size_ptable.get(specials);
			//check to see how many times per game, players selected from this set
			List<Game> games = Game.getGamesForBoard(board.getId(), first_play, 0);
			for(Game game : games){
				List<String> picked = game.getPlayer1_features(); //again in db feature id space
				picked.retainAll(special);
				int picked_special = picked.size();
				double p = ptable.get(picked_special);
				if(p<=sig_p){
					N_biased++;
					int pid = game.getPlayer1_id();
					Integer c = player_scount.get(pid);
					if(c==null){
						c= new Integer(0);
					}
					c++;
					player_scount.put(pid, c);
				}else{
					N_unknown++;
				}
			}
		}
		System.out.println("N_biased:\t"+N_biased+"\tN_unknown\t"+N_unknown+"\tN_boards_no_specials\t"+N_no_specials+"\tN_boards\t"+boards.size());

		List<Integer> keys = MapFun.sortMapByValue(player_scount);
		for(Integer key : keys){
			System.out.println(key+"\t"+player_scount.get(key));
		}
		// breast cancer N_biased:	12	N_unknown	4302	N_boards_no_specials	288	N_boards	400
		// cancer N_biased:	76	N_unknown	4238	N_boards_no_specials	93	N_boards	400
		// cancer or tumor: N_biased:	169	N_unknown	4145	N_boards_no_specials	14	N_boards	400
		//top players of biased games for cancer or tumor
		/*
		 * 980	15 kiefer89
59	10 kiefer89 ataly
54	9 ryanM
566	8 Aee
728	6 Savvak
204	5 oneoff64
48	5 gene
572	5 upfeffer
362	4 Bio_prof
1079	4 EXcaliboor
599	4 jreeve
349	4 poyarkov
		 */

		// cancer or tumor or oma:  N_biased:	120	N_unknown	4194	N_boards_no_specials	0	N_boards	400

	}


	/**
	 * Using this we can estimate the chances of observing a particular value in a cross-validation experiment given a random selection of genes.
	 * @param backgroundgenefile
	 * @param train_file
	 * @param dataset
	 * @param fileout
	 */
	public static void buildPvalTableForRandomGeneSets(String indices_keep, String backgroundgenefile, String train_file, String dataset, String fileout, long seed, int runs, int ngenes, int ncv){
		Random ran = new Random(seed);
		boolean all_probes = true;
		Classifier model = null;
		Map<Integer, Integer> cv_count = new TreeMap<Integer, Integer>();
		try {
			FileWriter w = new FileWriter(fileout);
			w.write("r\tnper\tgenes.size()\tagainst.size()\ta\tb\tc\td\tfisherP\tcv_accuracy\tgenes\n");
			Weka weka = new Weka();
			System.out.println("loading... "+train_file);
			weka.buildWeka(new FileInputStream(train_file), null, dataset, false);
			System.out.println("Weka initialized ");		
			Set<Integer> backgroundset = readEntrezIdsFromFile(backgroundgenefile, 0, 0, "\t");
			List<Integer> backgroundgenes = new ArrayList<Integer>(backgroundset);
			//use to benchmark likelihood of matching up with a particular set
			Set<Integer> against = new HashSet<Integer>(ngenes);
			for(int g=0; g<ngenes; g++){
				against.add(backgroundgenes.get(g));
			}
			for(int r=0; r< runs; r++){
				Set<Integer> missing = new HashSet<Integer>();
				Set<Integer> _genes = new HashSet<Integer>();
				Set<Integer> genes = new HashSet<Integer>();
				String genecell = "";
				Collections.shuffle(backgroundgenes, ran);
				String indices = indices_keep;
				//select the genes
				for(int g=0; g<ngenes; g++){
					Integer entrezid = backgroundgenes.get(g);
					_genes.add(entrezid);
				}
				//look them up
				boolean present = false;
				Map<Integer, List<org.scripps.combo.model.Attribute>> gene_atts = org.scripps.combo.model.Attribute.getByFeatureUniqueIds(_genes, dataset);
				for(Integer gene : _genes){
					present = false;
					List<org.scripps.combo.model.Attribute> atts = gene_atts.get(gene);
					if(atts!=null&&atts.size()>0){
						for(org.scripps.combo.model.Attribute a : atts){
							if(a.getDataset().equals(dataset)){
								indices+=a.getCol_index()+",";
								if(!all_probes){
									break;
								}
								present = true;
							}
						}
					}
					if(!present){
						missing.add(gene);
					}else{
						genes.add(gene);
					}
				}
				//keep track of which genes were selected
				for(Integer gene : genes){
					Feature f = Feature.getByUniqueId(gene+"");
					genecell+=(gene+":"+f.getShort_name()+",");
				}
				//test them
				int c = 0;
				int accuracy = 0;
				String cmodel = "RF";
				model = new RandomForest();
				weka.setEval_method("cross_validation");
				Weka.execution result = weka.pruneAndExecute(indices, model, ncv);
				accuracy = Math.round((float)result.eval.pctCorrect());
				Integer cvcount = cv_count.get(accuracy);
				if(cvcount==null){
					cvcount = new Integer(0);
				}
				cvcount++;
				cv_count.put(accuracy, cvcount);
				SetComparison setcompare = new SetComparison(genes, against, backgroundset);
				String row = r+"\t"+ngenes+"\t"+genes.size()+"\t"+against.size()+"\t"+setcompare.getString()+"\t"+accuracy;
				w.write(row+"\t"+genecell+"\n");
				System.out.println(row);
			}
			w.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//write out the empirical p for the gene sets
		for(Entry<Integer,Integer> cvcount : cv_count.entrySet()){
			int cv = cvcount.getKey();
			int n_greater = 0;
			for(Entry<Integer,Integer> cvcountin : cv_count.entrySet()){
				int cvin = cvcountin.getKey();
				if(cvin>=cv){
					n_greater+=cvcountin.getValue();
				}
			}
			System.out.println(cv+"\t"+cvcount.getValue()+"\t"+(float)cvcount.getValue()/(float)runs+"\t"+n_greater+"\t"+n_greater/(float)runs);
		}
	}

	public static class classifierTestResult {
		//classifier features
		int n_atts = 0;

		//observed
		double oobe = 0; 
		double train_acc = 0; double train_auc = 0; double train_f = 0; 
		double cv_acc = 0; double cv_auc = 0; double cv_f = 0; 
		double test_acc = 0; double test_auc = 0; double test_f = 0;
		//chance of random observation
		double oobe_p = 0; 
		double train_acc_p = 0; double train_auc_p = 0; double train_f_p = 0; 
		double cv_acc_p = 0; double cv_auc_p = 0; double cv_f_p = 0; 
		double test_acc_p = 0; double test_auc_p = 0; double test_f_p = 0;
		
		public String toString(){
			String r = oobe+"\t"+oobe_p+"\t" +
			train_acc+"\t"+train_acc_p+"\t"+train_auc_p+"\t"+train_f+"\t"+train_f_p+"\t"+
			cv_acc+"\t"+cv_acc_p+"\t"+cv_auc+"\t"+cv_auc_p+"\t"+cv_f+"\t"+cv_f_p+"\t"+
			test_acc+"\t"+test_acc_p+"\t"+test_auc+"\t"+test_auc_p+"\t"+test_f+"\t"+test_f_p;
			return r;
		}

		public static String names(){
			String n = "oobe\toobe_p\t" +
			"train_acc\ttrain_acc_p\ttrain_auc_p\ttrain_f\ttrain_f_p\t"+
			"cv_acc\tcv_acc_p\tcv_auc\tcv_auc_p\tcv_f\tcv_f_p\t"+
			"test_acc\ttest_acc_p\ttest_auc\ttest_auc_p\ttest_f\ttest_f_p";
			return n;
		}

	}

	/**
	 * Given a weka instance with training/testing data already assigned and set of entrez gene ids,
	 * evaluate the given genes using the model specified.
	 * Optionally execute a simulation where random attributes are selected
	 * @param skip_first
	 * @param model
	 * @param indices_keep
	 * @param dataset
	 * @param ncv
	 * @param nsimforp
	 * @param seed
	 * @param genes
	 * @param weka
	 * @param testname
	 * @return
	 */
	public static classifierTestResult computeClassifierEvaluation(int skip_first, Classifier model, String indices_keep, String dataset, int ncv, int nsimforp, long seed, 
			Set<Integer> genes, Weka weka, String testname){
		classifierTestResult score_out = new classifierTestResult();
		Set<Integer> missing = new HashSet<Integer>();
		Random random = new Random(seed);
		boolean all_probes = true;
		String indices = indices_keep;
		int n_atts = 0;
		for(Integer entrezid : genes){
			boolean present = false;
			List<org.scripps.combo.model.Attribute> atts = org.scripps.combo.model.Attribute.getByFeatureUniqueId(entrezid+"", dataset);
			if(atts!=null&&atts.size()>0){
				for(org.scripps.combo.model.Attribute a : atts){
					if(a.getDataset().equals(dataset)){
						indices+=a.getCol_index()+",";
						n_atts++;
						if(!all_probes){
							break;
						}
						present = true;
					}
				}
			}
			if(!present){
				missing.add(entrezid);
			}
		}
		if(n_atts==0){
			return score_out;
		}
		genes.removeAll(missing);
		String genecell = "";
		for(Integer gene : genes){
			Feature f = Feature.getByUniqueId(gene+"");
			genecell+=(gene+":"+f.getShort_name()+",");
		}
		String missingcell = "";
		for(Integer gene : missing){
			Feature f = Feature.getByUniqueId(gene+"");
			if(f!=null){
				missingcell+=(f.getShort_name()+",");
			}else{
				missingcell+=(gene+",");
			}
		}
		//loop here to embed a simulation of random var selection
		//with n_atts from above
		List<classifierTestResult> scores = new ArrayList<classifierTestResult>();
		classifierTestResult test_score = new classifierTestResult();
		int numFeaturesPerTree = 0;
		for(int sim=0; sim<nsimforp; sim++){
			classifierTestResult score = new classifierTestResult();
			String test_indices = indices_keep;
			if(sim<nsimforp-1){ //on the last one we run the real genes 
				for(int s=0; s<n_atts; s++){
					int ranindex = (int) Math.rint(random.nextDouble()*(weka.getTrain().numAttributes()-1));
					//hack to skip the clinical features of the metabric data
					// pick from th remaining thousands of attributes.
					while(ranindex<skip_first){
						ranindex = (int) Math.rint(random.nextDouble()*(weka.getTrain().numAttributes()-1));
					}

					test_indices+= ranindex+",";
				}
			}else{
				test_indices = indices;
			}
			numFeaturesPerTree = (int) Utils.log2(n_atts)+1;
			//train_accuracy, train_auc, cv_acc, cv_auc, test_acc, test_auc = 0;
			for(int e=0; e<3; e++){
				if(e==0){ // cross_validation, test_set,
					weka.setEval_method("training_set");
				}else if(e==1){
					weka.setEval_method("cross_validation");
				}else if(e==2){
					weka.setEval_method("test_set");
				}
				Weka.execution result = weka.pruneAndExecute(test_indices, model, ncv);

				if(e==0){ // train, cross_validation, test_set,
					score.train_acc = result.eval.pctCorrect();
					score.train_auc = result.eval.areaUnderROC(0);
					score.train_f = result.eval.fMeasure(0);
					if(model.getClass()==RandomForest.class){
						score.oobe = ((RandomForest) model).measureOutOfBagError();
					}
				}else if(e==1){
					score.cv_acc = result.eval.pctCorrect();
					score.cv_auc = result.eval.areaUnderROC(0);
					score.cv_f = result.eval.fMeasure(0);
				}else if(e==2){
					score.test_acc = result.eval.pctCorrect();
					score.test_auc = result.eval.areaUnderROC(0);
					score.test_f = result.eval.fMeasure(0);
				}
			}
			//save for sim test or run test..
			if(sim==nsimforp-1){
				//do the test, this one is real
				for(classifierTestResult simscore : scores){
					if(simscore.cv_acc>=score.cv_acc){
						score.cv_acc_p++;
					}
					if(simscore.cv_auc>=score.cv_auc){
						score.cv_auc_p++;
					}
					if(simscore.cv_f>=score.cv_f){
						score.cv_f_p++;
					}
					if(simscore.train_acc>=score.train_acc){
						score.train_acc_p++;
					}
					if(simscore.train_auc>=score.train_auc){
						score.train_auc_p++;
					}
					if(simscore.train_f>=score.train_f){
						score.train_f_p++;
					}
					if(simscore.test_acc>=score.test_acc){
						score.test_acc_p++;
					}
					if(simscore.test_auc>=score.test_auc){
						score.test_auc_p++;
					}
					if(simscore.test_f>=score.test_f){
						score.test_f_p++;
					}
					if(simscore.oobe<=score.oobe){
						score.oobe_p++;
					}
				}
				double ssize = (double)scores.size();
				score.cv_acc_p = score.cv_acc_p/ssize;
				score.cv_auc_p = score.cv_auc_p/ssize;
				score.cv_f_p = score.cv_f_p/ssize;
				score.train_acc_p = score.train_acc_p/ssize;
				score.train_auc_p = score.train_auc_p/ssize;
				score.train_f_p = score.train_f_p/ssize;
				score.test_acc_p = score.test_acc_p/ssize;
				score.test_auc_p = score.test_auc_p/ssize;
				score.test_f_p = score.test_f_p/ssize;
				score.oobe_p = score.oobe_p/ssize;
				score.n_atts = n_atts;
				score_out = score;
			}else{
				scores.add(score);
		//		String simrow = seed+"\t"+model.getClass().getSimpleName()+"\t"+sim+" sim_"+testname+"\t"+n_atts+"\t"+numFeaturesPerTree+"\t"+score.toString()+"\t"+genes.size()+"\t"+test_indices;
		//		System.out.println(simrow);
			}
		}
	//	String row = seed+"\t"+model.getClass().getSimpleName()+"\t"+testname+"\t"+n_atts+"\t"+numFeaturesPerTree+"\t"+test_score.toString()+"\t"+genes.size()+"\t"+genecell+"\t"+missing.size()+"\t"+missingcell;
	//	System.out.println(row);

		return score_out;
	}

	/**
	 * For each of a set of files where each contains a set of entre gene ids
	 * 	map the entrez ids to attributes in the dataset specified in the train_file
	 *  build and evaluate (train, cv, test) the specified classified model given that data
	 *  Write the results..
	 * @param skip_first
	 * @param model
	 * @param indices_keep
	 * @param genesetdir
	 * @param train_file
	 * @param test_file
	 * @param dataset
	 * @param fileout
	 * @param ncv
	 * @param nsimforp
	 * @param seed
	 */
	public static void wrapperGeneSetEvaluation(int skip_first, Classifier model, String indices_keep, String genesetdir, String train_file, String test_file, String dataset, String fileout, int ncv, int nsimforp, long seed){
		try {
			Weka weka = new Weka();
			System.out.println("loading... "+train_file);
			boolean setFeatures = false;
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
			System.out.println("Weka initialized ");
			FileWriter out = new FileWriter(fileout);	
			out.write("seed\tmodel\ttestgenefile\tn_atts\t"+classifierTestResult.names()+
			"\tn_genes\n");
			File d = new File(genesetdir);
			if(d.isDirectory()){
				for(String testgenefile : d.list()){
					if(testgenefile.startsWith(".")){
						continue;
					}else{ 
						int colindex = 0; String delimiter = "\t";
						Set<Integer> genes = readEntrezIdsFromFile(genesetdir+testgenefile, colindex, 0, delimiter);

						classifierTestResult score = computeClassifierEvaluation(skip_first, model, indices_keep, dataset, ncv, nsimforp, seed, genes, weka, testgenefile);

						String row = seed+"\t"+model.getClass().getSimpleName()+"\t"+testgenefile+"\t"+score.n_atts+"\t"+score.toString()+"\t"+genes.size();
						System.out.println(row);
						out.write(row+"\n");
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

	public static void wrapperGeneSetEvaluation(int skip_first, Classifier model, String indices_keep, String genesetdir, String train_file, String test_file, String dataset, String fileout, int ncv, int nsimforp, long seed){
		Random random = new Random(seed);
		boolean all_probes = true;
		try {
			Weka weka = new Weka();
			System.out.println("loading... "+train_file);
			boolean setFeatures = false;
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
			System.out.println("Weka initialized ");
			FileWriter out = new FileWriter(fileout);	
			out.write("seed\tmodel\ttestgenefile\tn_atts\tn_features_per_tree\t"+classifierTestResult.names()+
			"\tn_genes\tgenecell\tmissing.size()\tmissingcell\n");
			File d = new File(genesetdir);
			if(d.isDirectory()){
				for(String testgenefile : d.list()){
					if(testgenefile.startsWith(".")){
						continue;
					}else{ 
						int colindex = 0; String delimiter = "\t";
						Set<Integer> genes = readEntrezIdsFromFile(genesetdir+testgenefile, colindex, 0, delimiter);
						Set<Integer> missing = new HashSet<Integer>();

						String indices = indices_keep;
						int n_atts = 0;
						for(Integer entrezid : genes){
							boolean present = false;
							List<org.scripps.combo.model.Attribute> atts = org.scripps.combo.model.Attribute.getByFeatureUniqueId(entrezid+"", dataset);
							if(atts!=null&&atts.size()>0){
								for(org.scripps.combo.model.Attribute a : atts){
									if(a.getDataset().equals(dataset)){
										indices+=a.getCol_index()+",";
										n_atts++;
										if(!all_probes){
											break;
										}
										present = true;
									}
								}
							}
							if(!present){
								missing.add(entrezid);
							}
						}
						genes.removeAll(missing);
						String genecell = "";
						for(Integer gene : genes){
							Feature f = Feature.getByUniqueId(gene+"");
							genecell+=(gene+":"+f.getShort_name()+",");
						}
						String missingcell = "";
						for(Integer gene : missing){
							Feature f = Feature.getByUniqueId(gene+"");
							if(f!=null){
								missingcell+=(f.getShort_name()+",");
							}else{
								missingcell+=(gene+",");
							}
						}
						//loop here to embed a simulation of random var selection
						//with n_atts from above
						List<classifierTestResult> scores = new ArrayList<classifierTestResult>();
						classifierTestResult test_score = new classifierTestResult();
						int numFeaturesPerTree = 0;
						for(int sim=0; sim<nsimforp; sim++){
							classifierTestResult score = new classifierTestResult();
							String test_indices = indices_keep;
							if(sim<nsimforp-1){ //on the last one we run the real genes 
								for(int s=0; s<n_atts; s++){
									int ranindex = (int) Math.rint(random.nextDouble()*(weka.getTrain().numAttributes()-1));
									//hack to skip the clinical features of the metabric data
									// pick from th remaining thousands of attributes.
									while(ranindex<skip_first){
										ranindex = (int) Math.rint(random.nextDouble()*(weka.getTrain().numAttributes()-1));
									}

									test_indices+= ranindex+",";
								}
							}else{
								test_indices = indices;
							}
							numFeaturesPerTree = (int) Utils.log2(n_atts)+1;
							//train_accuracy, train_auc, cv_acc, cv_auc, test_acc, test_auc = 0;
							for(int e=0; e<3; e++){
								if(e==0){ // cross_validation, test_set,
									weka.setEval_method("training_set");
								}else if(e==1){
									weka.setEval_method("cross_validation");
								}else if(e==2){
									weka.setEval_method("test_set");
								}
								Weka.execution result = weka.pruneAndExecute(test_indices, model, ncv);

								if(e==0){ // train, cross_validation, test_set,
									score.train_acc = result.eval.pctCorrect();
									score.train_auc = result.eval.areaUnderROC(0);
									score.train_f = result.eval.fMeasure(0);
									if(model.getClass()==RandomForest.class){
										score.oobe = ((RandomForest) model).measureOutOfBagError();
									}
								}else if(e==1){
									score.cv_acc = result.eval.pctCorrect();
									score.cv_auc = result.eval.areaUnderROC(0);
									score.cv_f = result.eval.fMeasure(0);
								}else if(e==2){
									score.test_acc = result.eval.pctCorrect();
									score.test_auc = result.eval.areaUnderROC(0);
									score.test_f = result.eval.fMeasure(0);
								}
							}
							//save for sim test or run test..
							if(sim==nsimforp-1){
								//do the test, this one is real
								for(classifierTestResult simscore : scores){
									if(simscore.cv_acc>=score.cv_acc){
										score.cv_acc_p++;
									}
									if(simscore.cv_auc>=score.cv_auc){
										score.cv_auc_p++;
									}
									if(simscore.cv_f>=score.cv_f){
										score.cv_f_p++;
									}
									if(simscore.train_acc>=score.train_acc){
										score.train_acc_p++;
									}
									if(simscore.train_auc>=score.train_auc){
										score.train_auc_p++;
									}
									if(simscore.train_f>=score.train_f){
										score.train_f_p++;
									}
									if(simscore.test_acc>=score.test_acc){
										score.test_acc_p++;
									}
									if(simscore.test_auc>=score.test_auc){
										score.test_auc_p++;
									}
									if(simscore.test_f>=score.test_f){
										score.test_f_p++;
									}
									if(simscore.oobe<=score.oobe){
										score.oobe_p++;
									}
								}
								double ssize = (double)scores.size();
								score.cv_acc_p = score.cv_acc_p/ssize;
								score.cv_auc_p = score.cv_auc_p/ssize;
								score.cv_f_p = score.cv_f_p/ssize;
								score.train_acc_p = score.train_acc_p/ssize;
								score.train_auc_p = score.train_auc_p/ssize;
								score.train_f_p = score.train_f_p/ssize;
								score.test_acc_p = score.test_acc_p/ssize;
								score.test_auc_p = score.test_auc_p/ssize;
								score.test_f_p = score.test_f_p/ssize;
								score.oobe_p = score.oobe_p/ssize;
								test_score = score;
							}else{
								scores.add(score);
								String simrow = seed+"\t"+model.getClass().getSimpleName()+"\t"+sim+" sim_"+testgenefile+"\t"+n_atts+"\t"+numFeaturesPerTree+"\t"+score.toString()+"\t"+genes.size()+"\t"+test_indices;
								out.write(simrow+"\n");
								System.out.println(simrow);
							}
						}
						String row = seed+"\t"+model.getClass().getSimpleName()+"\t"+testgenefile+"\t"+n_atts+"\t"+numFeaturesPerTree+"\t"+test_score.toString()+"\t"+genes.size()+"\t"+genecell+"\t"+missing.size()+"\t"+missingcell;
						System.out.println(row);
						out.write(row+"\n");
					}
				}
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	 */
	/**
	 * Given a collection of gene sets, evaluate them using several different classifiers on particular dataset using training set, cross-validation, and test set
	 * @param genesetdir
	 * @param train_file
	 * @param test_file
	 * @param dataset
	 * @param fileout
	 */
	public static void testGeneSetsInClassifiers(String indices_keep, String genesetdir, String train_file, String test_file, String dataset, String fileout, int ncv){
		boolean all_probes = true;
		Classifier model = null;
		try {
			Weka weka = new Weka();
			System.out.println("loading... "+train_file);
			boolean setFeatures = false;
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
			System.out.println("Weka initialized ");
			FileWriter out = new FileWriter(fileout);	
			out.write("testgenefile\tmaxdepth\tcmodel\teval_method\taccuracy\tgenes.size()\tgenecell\tmissing.size()\tmissingcell\n");
			//iterate through different set sizes
			int maxdepth = 0;
			for(int s=0; s<1; s++){
				if(s==0){
					maxdepth = 404;
				}else if(s==1){
					maxdepth = 100;
				}else if(s==2){
					maxdepth = 25;
				}
				float accuracy = 0;
				File d = new File(genesetdir);
				if(d.isDirectory()){
					for(String testgenefile : d.list()){
						if(testgenefile.startsWith(".")){
							continue;
						}else{
							int colindex = 0; String delimiter = "\t";
							Set<Integer> genes = readEntrezIdsFromFile(genesetdir+testgenefile, colindex, maxdepth, delimiter);
							Set<Integer> missing = new HashSet<Integer>();
							String indices = indices_keep;
							for(Integer entrezid : genes){
								boolean present = false;
								List<org.scripps.combo.model.Attribute> atts = org.scripps.combo.model.Attribute.getByFeatureUniqueId(entrezid+"", dataset);
								if(atts!=null&&atts.size()>0){
									for(org.scripps.combo.model.Attribute a : atts){
										if(a.getDataset().equals(dataset)){
											indices+=a.getCol_index()+",";
											if(!all_probes){
												break;
											}
											present = true;
										}
									}
								}
								if(!present){
									missing.add(entrezid);
								}
							}
							genes.removeAll(missing);
							String genecell = "";
							for(Integer gene : genes){
								Feature f = Feature.getByUniqueId(gene+"");
								genecell+=(gene+":"+f.getShort_name()+",");
							}
							String missingcell = "";
							for(Integer gene : missing){
								Feature f = Feature.getByUniqueId(gene+"");
								if(f!=null){
									missingcell+=(f.getShort_name()+",");
								}else{
									missingcell+=(gene+",");
								}
							}
							int c = 0;
							String cmodel = "";
							for(int cm=2; cm<3; cm++){
								if(c==0){
									model = new RandomForest();
									cmodel = "RF";
								}else if(c==1){
									model = new SMO();
									cmodel = "SVM";
								}else if(c==2){
									model = new J48();
									cmodel = "J48";
								}
								for(int e=0; e<3; e++){
									if(e==0){ // cross_validation, test_set,
										weka.setEval_method("training_set");
									}else if(e==1){
										weka.setEval_method("cross_validation");
									}else if(e==2){
										weka.setEval_method("test_set");
									}
									Weka.execution result = weka.pruneAndExecute(indices, model, ncv);
									ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
									accuracy = short_result.getAccuracy();
									String row = testgenefile+"\t"+maxdepth+"\t"+cmodel+"\t"+weka.getEval_method()+"\t"+accuracy+"\t"+genes.size()+"\t"+genecell+"\t"+missing.size()+"\t"+missingcell;
									System.out.println(row);
									out.write(row+"\n");
								}
								c++;
							}
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
	public static void outputGeneSetComparisons(String backgroundgenefile, String gamegenesetdir, String comparetodir, int maxdepth, String outfile, boolean asmatrix){
		File d = new File(gamegenesetdir);
		String distinct_genes = "";
		Set<Integer> distinct_cure = new HashSet<Integer>();
		Set<Integer> not_cure = new HashSet<Integer>();
		if(d.isDirectory()){
			List<String> testfiles = new ArrayList<String>();
			for(String tfile : d.list()){
				if(tfile.startsWith(".")){
					continue;
				}
				testfiles.add(tfile);
			}
			FileWriter w;
			try {
				w = new FileWriter(outfile);				
				if(asmatrix){//write the row header
					w.write("gene_set\tset_size\t");
					for(String testgenefile : testfiles){
						w.write(testgenefile+"\t");
					}
					w.write("\n");
				}
				w.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(String testgenefile : testfiles){
				Map<String, SetComparison> p = computeOverlapMetrics(backgroundgenefile, gamegenesetdir+testgenefile, comparetodir, maxdepth);
				//capture any distinct elements of the currenet set
				Set<Integer> distinct = new HashSet<Integer>();
				boolean first = true;
				for(String key : testfiles){
					if(!testgenefile.equals(key)){
						SetComparison compare = p.get(key);
						if(first){
							distinct.addAll(compare.set1notset2);
							first = false;
							if(testgenefile.contains("cure")){
								distinct_cure.addAll(compare.set1);
							}else{
								not_cure.addAll(compare.set1);
							}
						}else{
							distinct.retainAll(compare.set1notset2);
						}
					}
				}

				distinct_genes += testgenefile+"\t"+distinct.size()+"\t"+distinct+"\n";

				//output full comparison
				if(asmatrix){
					try {
						w = new FileWriter(outfile, true);
						//get the size of the test for this row 
						SetComparison tbt = p.values().iterator().next();
						int row_set_size = tbt.a+tbt.b; //note same for all entries
						//write the descriptor column entry
						w.write(testgenefile+"\t"+row_set_size+"\t");
						//write a row
						for(String key : testfiles){
							SetComparison t = p.get(key);
							w.write(t.getMatrixCell()+"\t");
						}
						w.write("\n");
						w.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					try {
						w = new FileWriter(outfile, true);
						w.write("\n"+testgenefile+"\n");
						System.out.println("\n"+testgenefile);
						for(String key : p.keySet()){
							System.out.println(key+"\t"+p.get(key));
							SetComparison t = p.get(key);
							w.write(key+"\t"+t.getString()+"\n");
						}
						w.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				w = new FileWriter(outfile, true);
				w.write("\nDistinct Genes\n");
				System.out.println("\n"+distinct_genes);
				w.write(distinct_genes);

				distinct_cure.removeAll(not_cure);
				w.write("\nCure Only "+distinct_cure.size()+"\n"+distinct_cure);
				System.out.println("Cure_only_gene");
				for(Integer gene : distinct_cure){
					System.out.println(gene);
				}
				w.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	public static Map<String, SetComparison> computeOverlapMetrics(String backgroundgenefile, String testgenefile, String againstgenefiledir, int depth){
		Map<String, SetComparison> p_map = new HashMap<String, SetComparison>();
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
				Set<Integer> against = readEntrezIdsFromFile(againstgenefiledir+againstgenefile, 0, depth, "\t");
				SetComparison t = new SetComparison(testgenes, against, background);
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
	public static void outputFrequencyBasedGeneRankings(String outfileroot, long seed){
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
			ranked = gr.setEstimatedPvalue(ranked, seed);
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
	 * 
	 * TODO Return a map by entrez_id of all the genes considered
	 * @param outfile
	 * @param genefile
	 * @param first_hand_only
	 * @param random
	 */
	public static Map<Integer, gene_rank> outputBoardConsensus(String outfile, String genefile, boolean first_hand_only, boolean random, long seed){
		Map<Integer, gene_rank> gene_weights = new HashMap<Integer, gene_rank>();

		//set up tables to get P values from simulated data
		int n_per_board = 25;
		int min_players = 1;
		int max_players = 26;
		int n_cards = 5;
		int n_runs = 1000; int n_times = 100;
		System.out.println("P sim initializing");
		GameSim simp = new GameSim();
		simp.initCureSim(n_per_board, min_players, max_players, n_cards, n_runs, n_times, seed);
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
					//save all the genes

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
		return gene_weights;
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
		List<Game> whs = Game.getTheFirstGamePerPlayerPerBoard(only_winning, dataset, false, null, true);
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
		//System.out.println("parsing file "+file);
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
	public static double getSimPerGeneP(int n_views, int n_votes, long seed){
		Random ran = new Random(seed);
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
					Collections.shuffle(board, ran);
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


	/**
	 * Fill in the chances of drawing a particular type of card N times in a game given the total number of that type of card on the board	
	 * @param board_size
	 * @param hand_size
	 * @param seed
	 * @param games
	 * @return
	 */
	public static Map<Integer, Map<Integer, Double>> getDistributionsForCardSelectionType(int board_size, int hand_size, long seed, int games){
		Map<Integer, Map<Integer, Double>> size_ptable = new TreeMap<Integer, Map<Integer, Double>>();
		for(int special_set_size=0; special_set_size<board_size; special_set_size++){
			Map<Integer, Double> size_dist = getDistributionForCardSelectionType(board_size, special_set_size, hand_size, seed, games);
			size_ptable.put(special_set_size, size_dist);
		}

		//		for(int special_set_size=0; special_set_size<board_size; special_set_size++){
		//			Map<Integer, Double> n_p = size_ptable.get(special_set_size);
		//			for(Entry<Integer, Double> p : n_p.entrySet()){
		//				System.out.println(special_set_size+"\t"+p.getKey()+"\t"+p.getValue());
		//			}
		//		}

		return size_ptable;
	}

	/**
	 * Assuming we have a board containing a "special" set of genes.  e.g. a genes that have "cancer" in their names.
	 * Estimate the chances of picking a certain number of these genes at random.
	 * If the special set size = board size, chances of drawing a 5 special cards = 1.  
	 * If special set size = 0, chances are 0.
	 * @param board_size
	 * @param special_set_size
	 * @param hand_size
	 * @param seed
	 * @return
	 */
	public static Map<Integer, Double> getDistributionForCardSelectionType(int board_size, int special_set_size, int hand_size, long seed, int games){
		Map<Integer, Integer> n_count = new HashMap<Integer, Integer>();
		for(int c=0; c<=hand_size; c++){
			n_count.put(c, 0);
		}
		Random r = new Random(seed);
		List<Integer> boardbase = new ArrayList<Integer>(board_size);
		for(int i=0; i<special_set_size; i++){
			boardbase.add(i);
		}
		for(int i=special_set_size; i<board_size; i++){
			boardbase.add(-i);
		}
		for(int game=0; game<games; game++){
			List<Integer> board = new ArrayList<Integer>(boardbase);
			Collections.shuffle(board, r);
			//in each round two cards are selected
			//player 1 (human) always goes first
			//this loop completes one game
			int biased_selections = 0; int other_selections = 0;
			for(int g=0; g<hand_size*2; g++){
				int test = board.get(g); 
				if(g%2==0){ //if g is odd then barney got the card
					if(test>=0){ //then its a member of the biased set				
						biased_selections++;
					}else{
						other_selections++;
					}
				}
				board.remove(g); //take the selected card off the board
			}
			Integer c = n_count.get(biased_selections);
			c++;
			//for this game we observed 'biased_selections' out of 5 cards.  
			n_count.put(biased_selections, c);
		}
		Map<Integer, Double> n_p = MapFun.convertCountsToEmpiricalPtable(n_count);
		return n_p;
	}

}
