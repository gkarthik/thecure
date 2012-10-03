/**
 * 
 */
package org.scripps.combo.weka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import org.scripps.combo.TimeCounter;
import org.scripps.combo.Boardroom.boardview;
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Card;
import org.scripps.combo.model.Feature;
import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.Weka.metaExecution;
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
	Weka weka;
	public void initWeka(String trainfile, String dataset) throws FileNotFoundException, Exception{
		weka = new Weka();
		weka.buildWeka(new FileInputStream(trainfile), null, dataset);
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {		
		Map<String, gene_rank> gid_ranked = null;
		String dataset = "dream_breast_cancer";
		GeneRanker ranker = new GeneRanker();
		boolean only_winning = false;
		boolean only_cancer_people = true;
		boolean only_bio_people = false;
		boolean only_phd = false;
		gid_ranked = ranker.getRankedGenes(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		List<gene_rank> gr = new ArrayList<gene_rank>(gid_ranked.values());
		Collections.sort(gr);
		Collections.reverse(gr);

		//		for(gene_rank r : gr){
		//			System.out.println(r.f_id+"\t"+r.entrez+"\t"+r.symbol+"\t"+r.views+"\t"+r.votes+"\t"+r.votes/r.views);
		//		}

		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes_v1.arff";
		ranker.initWeka(train_file, dataset);
		boolean printout = false;
		int runs = 50;
		System.out.println("N_genes\tcv_score");
		for(int run=5; run<=runs; run+=5){
			Classifier model = new RandomForest();
			String[] options = new String[4];
			options[0] = "-I"; options[1] = "500";
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
			float cv = ranker.testGeneList(test_fs, dataset, printout, model);
			System.out.println(run+"\t"+cv);
		}
		
		//ranker.testHGF(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);
		
		//	generateRandomBaseline(n_genes, 1000, dataset, train_file);
		//	Player.describePlayers(true);
	}

	public gene_rank makeGene_rank(){
		gene_rank r = new gene_rank();
		r.frequency = 0;
		r.views = 0;
		r.votes = 0;
		return r;
	}

	public class gene_rank implements Comparable{

		String f_id;
		String entrez;
		String symbol;
		float frequency;
		float views;
		float votes;
		List<Integer> board_id;
		@Override
		public int compareTo(Object arg0) {
			gene_rank compareto = (gene_rank)arg0;
			Float fcomp = compareto.votes/compareto.views;
			Float f = this.votes/this.views;
			return f.compareTo(fcomp);
		}
	}


	public Map<String, gene_rank> getRankedGenes(String dataset, boolean only_winning, boolean only_cancer_people, boolean only_bio_people, boolean only_phd){

		List<Game> hands = getFilteredGameList(dataset, only_winning, only_cancer_people, only_bio_people, only_phd);

		//this will be the output
		Map<String, gene_rank> gene_ranked = new HashMap<String, gene_rank>();

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
			//todo cache this to make it faster
			Board board = Board.getBoardById(""+hand.getBoard_id(), false);
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

	public List<Game> getFilteredGameList(String dataset, boolean only_winning, boolean only_cancer_people, boolean only_bio_people, boolean only_phd){

		//get the hands		
		List<Game> hands = new ArrayList<Game>();
		List<Game> handsall = Game.getTheFirstGamePerPlayerPerBoard(only_winning, dataset);

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
