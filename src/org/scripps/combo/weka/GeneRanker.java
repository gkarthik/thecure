/**
 * 
 */
package org.scripps.combo.weka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.paukov.combinatorics.CombinatoricsVector;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.combination.simple.SimpleCombinationGenerator;
import org.scripps.combo.Boardroom;
import org.scripps.combo.GameLog;
import org.scripps.combo.Stats;
import org.scripps.combo.TimeCounter;
import org.scripps.combo.Boardroom.boardview;
import org.scripps.combo.model.Attribute;
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Card;
import org.scripps.combo.model.Feature;
import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.Weka.metaExecution;
import org.scripps.combo.weka.viz.JsonTree;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

/**
 * Use the data collected from game play to rank the genes
 * @author bgood
 *
 */
public class GeneRanker {
	public Weka weka;
	public void initWeka(String trainfile, String dataset) throws FileNotFoundException, Exception{
		weka = new Weka();
		weka.buildWeka(new FileInputStream(trainfile), null, dataset);
	}


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {		


		/*
		 * workflow for merging

		String dataset = "dream_breast_cancer";
		String outfile = "/Users/bgood/workspace/acure/database/ranking_merged_r1r2_only_cancer.txt";
		boolean only_winning = false;
		boolean only_cancer_people = true;
		boolean only_bio_people = false;
		boolean only_phd = false;
		GeneRanker ranker = new GeneRanker();
		Map<String, gene_rank> gid_ranked_1 = null;
		gid_ranked_1 = ranker.getRankedGenes(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		dataset = "dream_breast_cancer_2";
		Map<String, gene_rank> gid_ranked_2 = null;
		gid_ranked_2 = ranker.getRankedGenes(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		List<gene_rank> gr = ranker.mergeGeneRankings(gid_ranked_1, gid_ranked_2);
		ranker.writeFile(outfile, gr);
		 */

		/*		String dataset = "griffith_breast_cancer_1";
		String outfile = "/Users/bgood/workspace/aacure/database/griffith_1_cancer_only.txt";
		boolean only_winning = false;
		boolean only_cancer_people = true;
		boolean only_bio_people = false;
		boolean only_phd = false;
		GeneRanker ranker = new GeneRanker();
		List<gene_rank> gr = ranker.exportGeneRankings(dataset, outfile, only_winning, only_cancer_people, only_bio_people, only_phd);
		gr = ranker.sortByReliefAndFrequency(gr);
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_1.arff";
		ranker.initWeka(train_file, dataset);

		//build and export model (e.g. decision tree) with top n genes according to ranking above.
		J48 model = new J48();
		model.setUnpruned(false); 
		model.setMinNumObj(15);
		List<String> test_fs = new ArrayList<String>();
		int n = 25;
		for(int i=0; i< n; i++){
			Feature f = ranker.weka.features.get(gr.get(i).entrez);
			if(f!=null){
				test_fs.add(gr.get(i).entrez);
			}else{
				System.out.println("missing "+f.getShort_name());
			}
		}		
		Weka.execution result = ranker.weka.pruneAndExecuteWithUniqueIds(test_fs, model, dataset);

		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());

		JsonTree jsonexporter = new JsonTree();
		String json = jsonexporter.getJsonTreeStringFromGraph(model, ranker.weka);
		System.out.println(json);

		ranker.weka.setEval_method("training_set");
		result = ranker.weka.pruneAndExecuteWithUniqueIds(test_fs, model, dataset);
		System.out.println("training_set_accuracy\t"+result.eval.pctCorrect());
		 */		
		/**
		 * workflow for iterating through model building params	and testing	 
		int runs = 125;
		System.out.println("N_genes\tcv_score");
		for(int run=5; run<=runs; run=run*5){
			Classifier model = new RandomForest();
			String[] options = new String[4];
			options[0] = "-I"; options[1] = "100";
			options[2] = "-K"; options[3] = "5";
			try {
				model.setOptions(options);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List<String> test_fs = new ArrayList<String>();
			for(int i=0; i< run; i++){
				//don't use genes not in next dataset
				Feature f = ranker.weka.features.get(gr.get(i).entrez);
				if(f!=null){
					test_fs.add(gr.get(i).entrez);
				}
			}		
			float cv = ranker.testGeneList(test_fs, dataset, false, model);
			System.out.println(test_fs.size()+"\t"+cv);
		}
		 */

		GeneRanker gr = new GeneRanker();
		boolean first_hand_only = false; boolean barney = false;
		Map<String, gene_rank> bg_rank = gr.getBoardConsensus(1001, 0, first_hand_only, barney);
		List<gene_rank> ranked = gr.sortByFrequency(new ArrayList<gene_rank>(bg_rank.values()));
		Collections.reverse(ranked);
		for(gene_rank r : ranked){
			System.out.println(r.entrez+"\t"+r.f_id+"\t"+r.symbol+"\t"+r.votes+"\t"+r.frequency+"\t"+r.players);
		}
	}

	public List<gene_rank> mergeGeneRankings(Map<String, gene_rank> r1, Map<String, gene_rank> r2){
		List<gene_rank> merged = new ArrayList<gene_rank>();
		Set<String> genes = new HashSet<String>(r1.keySet());
		genes.addAll(r2.keySet());
		for(String gene : genes){
			gene_rank rank1 = r1.get(gene);
			gene_rank rank2 = r2.get(gene);
			gene_rank merg = mergeRanks(rank1, rank2);
			merged.add(merg);
		}
		return merged;
	}

	public gene_rank mergeRanks(gene_rank r1, gene_rank r2){
		gene_rank merged  = new gene_rank();
		if(r1!=null){
			merged.entrez = r1.entrez;
			merged.f_id = r1.f_id;
			merged.symbol = r1.symbol;
			merged.views = r1.views;
			merged.votes = r1.votes;
		}else if(r2!=null){
			merged.entrez = r2.entrez;
			merged.symbol = r2.symbol;
			merged.f_id = r2.f_id;
			merged.views = 0;
			merged.votes = 0;
		}else{
			return null;
		}
		if(r2!=null){
			merged.views+=r2.views;
			merged.votes+=r2.votes;
		}
		return merged;
	}

	/**
	 * Get the gene information for a given dataset
	 * @param dataset
	 * @param outfile
	 * @param only_winning
	 * @param only_cancer_people
	 * @param only_bio_people
	 * @param only_phd
	 * @return
	 */
	public List<gene_rank> exportGeneRankings(String dataset, String outfile, boolean only_winning, boolean only_cancer_people, boolean only_bio_people, boolean only_phd){
		Map<String, gene_rank> gid_ranked = null;
		gid_ranked = getRankedGenes(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		gid_ranked = addReliefToGeneRanking(gid_ranked);
		gid_ranked = scaleRelief(gid_ranked);
		List<gene_rank> gr = new ArrayList<gene_rank>(gid_ranked.values());
		writeFile(outfile, gr);
		return gr;
	}

	/**
	 * Select a set of played hands based on player filters (always limited to first hand per player per board)
	 * Calculate the frequency with which each gene is selected
	 * Sort based on frequency and reliefF measure for the attribute
	 * Use top N genes to train/test classifiers
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public void testRankings() throws FileNotFoundException, Exception{
		Map<String, gene_rank> gid_ranked = null;
		String dataset = "dream_breast_cancer";
		boolean only_winning = false;
		boolean only_cancer_people = false;
		boolean only_bio_people = true;
		boolean only_phd = false;
		gid_ranked = getRankedGenes(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		gid_ranked = addReliefToGeneRanking(gid_ranked);
		gid_ranked = scaleRelief(gid_ranked);
		List<gene_rank> gr = new ArrayList<gene_rank>(gid_ranked.values());
		gr = sortByReliefAndFrequency(gr);

		String outlist = "/Users/bgood/workspace/acure/database/ranking_r1_biologists.txt";
		writeFile(outlist, gr);
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes_v1.arff";
		initWeka(train_file, dataset);
		boolean printout = false;
		int runs = 125;
		System.out.println("N_genes\tcv_score");
		for(int run=5; run<=runs; run=run*5){
			Classifier model = new RandomForest();
			String[] options = new String[4];
			options[0] = "-I"; options[1] = "100";
			options[2] = "-K"; options[3] = "5";
			try {
				model.setOptions(options);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List<String> test_fs = new ArrayList<String>();
			for(int i=0; i< run; i++){
				test_fs.add(gr.get(i).entrez);
			}		
			float cv = testGeneList(test_fs, dataset, printout, model);
			System.out.println(run+"\t"+cv);
		}

	}

	public List<gene_rank> sortByRelief(List<gene_rank> ranked){
		//sort in descending order of the addition of relief and selection frequency
		Comparator<gene_rank> MergeOrder =  new Comparator<gene_rank>() {
			public int compare(gene_rank compare, gene_rank compareto) {
				Float r2 = compareto.relief; 
				Float r1 = compare.relief;
				return r2.compareTo(r1);
			}
		};
		Collections.sort(ranked, MergeOrder);
		return ranked;
	}

	public List<gene_rank> sortByReliefAndFrequency(List<gene_rank> ranked){
		//sort in descending order of the addition of relief and selection frequency
		Comparator<gene_rank> MergeOrder =  new Comparator<gene_rank>() {
			public int compare(gene_rank compare, gene_rank compareto) {
				Float r2 = compareto.relief; 
				Float r1 = compare.relief;
				Float f2 = compareto.votes/compareto.views;
				Float f1 = compare.votes/compare.views;
				Float r1_merge = r1+f1;
				Float r2_merge = r2+f2;
				return r2_merge.compareTo(r1_merge);
			}
		};
		Collections.sort(ranked, MergeOrder);
		return ranked;
	}

	public List<gene_rank> setFrequencyByViews(List<gene_rank> ranked){
		for(gene_rank gene : ranked){
			if(gene.views==0){
				gene.frequency = 0;
			}else{
				gene.frequency = gene.votes/gene.views;
			}
		}
		return ranked;
	}
	
	public List<gene_rank> setEstimatedPvalue(List<gene_rank> ranked){
		for(gene_rank gene : ranked){
			if(gene.views==0){
				gene.p = 1;
			}else{
				gene.p = Stats.getSimPerGeneP((int)gene.views,(int)gene.votes);
			}
		}
		return ranked;
	}
	
	public List<gene_rank> sortByFrequency(List<gene_rank> ranked){
		//sort in descending order of the addition of selection frequency
		Comparator<gene_rank> MergeOrder =  new Comparator<gene_rank>() {
			public int compare(gene_rank compare, gene_rank compareto) {
				Float r2 = compareto.frequency;
				Float r1 = compare.frequency;
				return r2.compareTo(r1);
			}
		};
		Collections.sort(ranked, MergeOrder);
		return ranked;
	}
	
	public List<gene_rank> sortByP(List<gene_rank> ranked){
		//sort in descending order of significance
		Comparator<gene_rank> MergeOrder =  new Comparator<gene_rank>() {
			public int compare(gene_rank compare, gene_rank compareto) {
				Double r2 = compareto.p;
				Double r1 = compare.p;
				return r1.compareTo(r2);
			}
		};
		Collections.sort(ranked, MergeOrder);
		return ranked;
	}

	/**
	 * Divide all the relief values by the max to get a -1 to 1 scale.
	 * @param gid_ranked
	 * @return
	 */
	public Map<String, gene_rank> scaleRelief(Map<String, gene_rank> gid_ranked) {
		float max = -1;
		for(gene_rank gr : gid_ranked.values()){
			if(max<gr.relief){
				max = gr.relief;
			}
		}
		for(String fid : gid_ranked.keySet()){
			gene_rank gr = gid_ranked.get(fid);
			gr.relief = gr.relief/max;
			gid_ranked.put(fid, gr);
		}
		return gid_ranked;
	}


	public void writeFile(String filename, List<gene_rank> grs){
		try {
			FileWriter f = new FileWriter(filename);
			f.write("f_id\tentrez\tsymbol\tviews\tvote\tvotes/views\trelief\trelief_plus_f\n");
			for(gene_rank r : grs){
				float merge = ((r.votes/r.views) + r.relief);
				f.write(r.f_id+"\t"+r.entrez+"\t"+r.symbol+"\t"+r.views+"\t"+r.votes+"\t"+r.votes/r.views+"\t"+r.relief+"\t"+merge+"\n");
			}
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public gene_rank makeGene_rank(){
		gene_rank r = new gene_rank();
		r.frequency = 0;
		r.views = 0;
		r.votes = 0;
		r.players = 0;
		return r;
	}

	public class gene_rank implements Comparable<gene_rank>{

		public String f_id;
		public String entrez;
		public String symbol;
		public float frequency;
		public float views;
		public float votes;
		public float relief;
		public float players;
		public double p;
		List<Integer> board_id;
		public float mean_selection_order; // within each 5 card game, is it selected first (5), second (4), ...or 0 //average across all games considered  
		@Override
		public int compareTo(gene_rank arg0) {
			gene_rank compareto = (gene_rank)arg0;
			if(views==0&&compareto.views==0){
				Float freq = this.frequency;
				Float cfreq = compareto.frequency;
				return freq.compareTo(cfreq);
			}
			Float fcomp = compareto.votes/compareto.views;
			Float f = this.votes/this.views;
			if(f==fcomp){
				Float views = this.views;
				Float cviews = compareto.views;
				return cviews.compareTo(views);
			}else{
				return fcomp.compareTo(f);
			}
		}
	}

	public Map<String, gene_rank> addReliefToGeneRanking(Map<String, gene_rank> gene_ranked){
		//add relief
		for(String fid : gene_ranked.keySet()){
			Feature f = Feature.getByDbId(Integer.parseInt(fid));
			if(f!=null){
				f.getMappedAttributesFromDb();
				if(f.getDataset_attributes()!=null){
					float maxRelief = -1;
					for(Attribute att : f.getDataset_attributes()){
						if(att.getReliefF()>maxRelief){
							maxRelief = att.getReliefF();
						}
					}
					gene_rank gr = gene_ranked.get(fid);
					gr.relief = maxRelief;
					gene_ranked.put(fid, gr);
				}
			}
		}

		return gene_ranked;
	}

	/**
	 * Calculate frequency with which players selected each gene on a given board
	 * frequency(gene) = number of players to select that gene / number of players to play board
	 * @param board_id
	 * @return
	 */
	public Map<String, gene_rank> getBoardConsensus(int board_id, int player_id, boolean first_hand_only, boolean barney){
		List<Game> games = Game.getGamesForBoard(board_id, first_hand_only, player_id);
		//this will be the output
		Map<String, gene_rank> gene_ranked = new HashMap<String, gene_rank>();
		//int games_played = games.size();
		//catch what each player to play the board selected
		Map<Integer, Set<String>> player_genes = new HashMap<Integer, Set<String>>();
		for(Game hand : games){
			List<String> features = hand.getPlayer1_features();
			if(barney){
				features = hand.getPlayer2_features();
			}
			int p_id = hand.getPlayer1_id();
			Set<String> genes = player_genes.get(p_id);
			if(genes==null){
				genes = new HashSet<String>();
			}
			if(features!=null){
				genes.addAll(features);
				player_genes.put(p_id, genes);
			}
		}	
		//System.out.println(player_genes.keySet().size()+" players");
		//go through each gene on the board and count occurrences
		for(Entry<Integer, Set<String>> p_genes : player_genes.entrySet()){
			Set<String> genes = p_genes.getValue();
			for(String gene : genes){
				gene_rank gr = gene_ranked.get(gene);
				if(gr == null){
					gr = makeGene_rank();
				}
				//add votes for each of the genes
				gr.votes++;
				gene_ranked.put(gene, gr);
			}
		}	
		//set the frequencies
		int players_who_played = player_genes.size();
		Board board = Board.getBoardById(""+board_id, false);
		for(Feature f : board.getFeatures()){
			String f_id = ""+f.getId();
			gene_rank gr = gene_ranked.get(f_id);
			if(gr == null){
				gr = makeGene_rank();
			}
			gr.f_id = f_id;
			gr.entrez = f.getUnique_id();
			gr.symbol = f.getShort_name();
			//set the frequencies
			gr.frequency = gr.votes/players_who_played;
			gr.players = players_who_played;
			gene_ranked.put(f_id, gr);
		}

		return gene_ranked;
	}


	/**
	 * Get data for ranking genes based on selection frequency
	 * Results returned unsorted
	 * Returns map with the feature id in our database as the key and a gene_rank object as the value
	 * @param dataset (if null, will get them all)
	 * @param only_winning
	 * @param only_cancer_people
	 * @param only_bio_people
	 * @param only_phd
	 * @return
	 */
	public Map<String, gene_rank> getRankedGenes(String dataset, boolean only_winning, boolean only_cancer_people, boolean only_bio_people, boolean only_phd){

		List<Game> hands = new ArrayList<Game>();
		//get rid of the mammal hands
		for(Game hand : getFilteredGameList(dataset, only_winning, only_cancer_people, only_bio_people, only_phd)){
			if(hand.getBoard_id()<201||hand.getBoard_id()>204){
				hands.add(hand);
			}
		}
		
		//this will be the output
		Map<String, gene_rank> gene_ranked = new HashMap<String, gene_rank>();

		//get the boards ready
		boolean drop_mammal = true; boolean setfeatures = true;
		List<Board> boards = Board.getAllBoards(drop_mammal, setfeatures);
		Map<Integer, Board> id_board = new HashMap<Integer, Board>();
		for(Board board : boards){
			id_board.put(board.getId(), board);
		}
		
		//count the votes
		int c = 0;
		for(Game hand : hands){
			List<String> features = hand.getPlayer1_features();
			Set<String> distinct = new HashSet<String>();
			for(String feature : features){
				if(distinct.add(feature)){//only count once if the same gene gets into multiple hands
					gene_rank gr = gene_ranked.get(feature);
					if(gr == null){
						gr = makeGene_rank();
					}
					//add votes for each of the genes
					gr.votes++;
					gene_ranked.put(feature, gr);
				}
			}
			//increase view count for all the features in the board for this hand
			Board board = id_board.get(hand.getBoard_id());
			for(Feature f : board.getFeatures()){
				String f_id = ""+f.getId();
				gene_rank gr = gene_ranked.get(f_id);
				if(gr == null){
					gr = makeGene_rank();
				}
				gr.entrez = f.getUnique_id();
				gr.symbol = f.getShort_name();
				//add views
				gr.views++;
				gr.f_id = f_id;
				gene_ranked.put(f_id, gr);
			}
			c++;			
			//System.out.println(c);
		}

		return gene_ranked;
	}

	/**
	 * Filter games based on the dataset they came from and the characteristics of who played them
	 * @param dataset
	 * @param only_winning
	 * @param only_cancer_people
	 * @param only_bio_people
	 * @param only_phd
	 * @return
	 */
	public List<Game> getFilteredGameList(String dataset, boolean only_winning, boolean only_cancer_people, boolean only_bio_people, boolean only_phd){

		//get the hands		
		List<Game> hands = new ArrayList<Game>();
		List<Game> handsall = Game.getTheFirstGamePerPlayerPerBoard(only_winning, dataset, false, 0);

		//		//set up player filter
		List<Player> playerss = Player.getAllPlayers();
		Map<Integer, Player> name_player = Player.playerListToIdMap(playerss);

		if(only_cancer_people){
			//filter hands by player attributes	
			for(Game hand : handsall){
				Player theplayer = name_player.get(hand.getPlayer1_id());
				if(theplayer!=null&&theplayer.getCancer().equals("yes")){ //player_cardsboard.get(theplayer.getName())<13
					hands.add(hand);
				}
			}
			handsall = hands;
		}
		if(only_bio_people){
			hands = new ArrayList<Game>();
			for(Game hand : handsall){
				Player theplayer = name_player.get(hand.getPlayer1_id());
				if(theplayer!=null&&theplayer.getBiologist().equals("yes")){ //player_cardsboard.get(theplayer.getName())<13
					hands.add(hand);
				}
			}
			handsall = hands;
		}
		if(only_phd){
			hands = new ArrayList<Game>();
			for(Game hand : handsall){
				Player theplayer = name_player.get(hand.getPlayer1_id());
				if(theplayer!=null&&(theplayer.getDegree().equals("phd")||theplayer.getDegree().equals("md"))){ //player_cardsboard.get(theplayer.getName())<13
					hands.add(hand);
				}
			}
			handsall = hands;
		}

		if(!only_cancer_people&&!only_bio_people&&!only_phd){
			hands = handsall;
		}
		return hands;
	}

	/**
	 * Build a voting classifier, each completed hand is used to make a decision tree and is given one vote
	 * classifications made using majority rule.  Simple version of bagging that is similar but less
	 * sophisticated than a random forest as it does not involve boostrap resampling of the training data
	 * @param dataset
	 * @param only_winning
	 * @param only_cancer_people
	 * @param only_bio_people
	 * @param only_phd
	 * @return
	 */
	public float testHGF(String dataset, boolean only_winning, boolean only_cancer_people, boolean only_bio_people, boolean only_phd){
		float cv = 0;
		List<Game> hands = getFilteredGameList(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		List<List<String>> id_sets = new ArrayList<List<String>>();
		for(Game hand : hands){
			List<String> entrez = new ArrayList<String>();
			for(String fid : hand.getPlayer1_features()){
				Feature f = Feature.getByDbId(Integer.parseInt(fid));
				if(f!=null){
					entrez.add(f.getUnique_id());
				}
			}
			id_sets.add(entrez);
		}
		System.out.println("Testing HGF on "+id_sets.size()+" hands");
		metaExecution result = weka.executeNonRandomForestOnUniqueIds(id_sets);
		System.out.println("avg_pct_correct "+result.avg_percent_correct);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), "");
		boolean printoutput = true;
		if(printoutput){
			System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());
		}
		cv = short_result.getAccuracy();
		return cv;
	}

	/**
	 * run a feature id list through weka and see what comes out
	 * these ids are local to our database
	 * @param gids
	 * @throws Exception 
	 */
	public float testGeneList(List<String> unique_ids, String dataset, boolean printoutput) throws Exception {
		float cv = 0;
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, null, dataset);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		if(printoutput){
			System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());
		}
		cv = short_result.getAccuracy();
		return cv;
	}

	public float testGeneList(List<String> unique_ids, String dataset, boolean printoutput, Classifier model) throws Exception {
		float cv = 0;
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, model, dataset);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		if(printoutput){
			System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());
		}
		cv = short_result.getAccuracy();
		return cv;
	}

	
	
	/**
	 * Get an id what random sampling would look like
	 * @param n_genes
	 * @param n_sampled
	 * @param dataset
	 * @param train_file
	 * @throws Exception
	 */
	public static void generateRandomBaseline(int n_genes, int n_sampled, String dataset, String train_file) throws Exception{
		//get results vector ready
		DescriptiveStatistics cvs = new DescriptiveStatistics();
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);
		List<String> all_genes = new ArrayList<String>(weka.getFeatures().keySet());

		//      go through n_sampled random combos
		for(int i = 0; i< n_sampled; i++) {
			Collections.shuffle(all_genes);
			List<String> group = new ArrayList<String>(n_sampled);
			int n = 0;
			for(String g : all_genes){
				group.add(g);
				if(n>=n_genes){
					break;
				}
				n++;
			}
			List<String> geneids = new ArrayList<String>();
			String genes = "";
			for(String gh : group){
				geneids.add(gh);
				genes+=gh+",";
			}
			//test group
			execution base = weka.pruneAndExecuteWithUniqueIds(geneids, null, dataset);
			double cv = base.eval.pctCorrect();
			cvs.addValue(cv);
			System.out.println(i+"\t"+cv);
		}

		System.out.println(cvs.toString());
	}
}
