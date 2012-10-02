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
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;

import weka.classifiers.trees.J48;

/**
 * Use the data collected from game play to rank the genes
 * @author bgood
 *
 */
public class GeneRanker {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int n_genes = 10;		
		Map<String, gene_rank> gid_ranked = null;
		String dataset = "dream_breast_cancer";
		GeneRanker ranker = new GeneRanker();
		boolean only_winning = false;
		boolean only_cancer_people = false;
		boolean only_bio_people = true;
		gid_ranked = ranker.getRankedGenes2(only_winning, only_cancer_people, only_bio_people, dataset);
		
		List<gene_rank> gr = new ArrayList<gene_rank>(gid_ranked.values());
		Collections.sort(gr);
		Collections.reverse(gr);
		
//		for(gene_rank r : gr){
//			System.out.println(r.f_id+"\t"+r.entrez+"\t"+r.symbol+"\t"+r.views+"\t"+r.votes+"\t"+r.votes/r.views);
//		}
		
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes_v1.arff";
		
		List<String> test_fs = new ArrayList<String>();
		for(int i=0; i< n_genes; i++){
			test_fs.add(gr.get(i).entrez);
			System.out.println(gr.get(i).entrez);
		}
		
		testGeneList(test_fs, n_genes, dataset, train_file);
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


	public Map<String, gene_rank> getRankedGenes2(boolean only_winning, boolean only_cancer_people, boolean only_bio_people, String dataset){

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
		}
		if(only_bio_people){
			//filter hands by player attributes	
			for(Game hand : handsall){
				Player theplayer = name_player.get(hand.getPlayer1_id());
				if(theplayer!=null&&theplayer.getBiologist().equals("yes")){ //player_cardsboard.get(theplayer.getName())<13
					hands.add(hand);
				}
			}				
		}
		if(!only_cancer_people&&!only_bio_people){
			hands = handsall;
		}
		
		
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



	/**
	 * For each board that has been played by more than 5 people, 
	 * Find the genes that have been selected with the highest frequency by all players.
	 * Only examine the first hand per player per board, regardless of whether they won.
	 * @return a list of geneids sorted according to the frequency that they were selected - first is the most frequent
	 */
	public Map<String, gene_rank> getRankedGenes(boolean weight_card_order, boolean only_cancer_people, int min_players_per_board, String dataset){
		//		//set up player filter
		List<Player> playerss = Player.getAllPlayers();
		Map<Integer, Player> name_player = Player.playerListToIdMap(playerss);
		//		Map<String, Float> player_cardsboward = new HashMap<String, Float>();

		int n_finished_boards = 0;
		List<Board> boards = Board.getBoardsByDatasetRoom(dataset,"1");
		Map<Board, Map<String, Float>> board_gene_freq = new HashMap<Board, Map<String, Float>>();
		Map<String, Integer> gene_views = new HashMap<String, Integer>();
		Map<String, Integer> gene_votes = new HashMap<String, Integer>();
		Map<String, Integer> gene_board = new HashMap<String, Integer>();
		for(Board board : boards){
			Map<String, Float> gene_freq = new HashMap<String, Float>();
			List<Game> hands = new ArrayList<Game>();
			List<Game> handsall = Game.getTheFirstGamePerPlayerByBoard(board.getId());
			if(only_cancer_people){
				//filter hands by player attributes	
				for(Game hand : handsall){
					Player theplayer = name_player.get(hand.getPlayer1_id());
					if(theplayer!=null&&theplayer.getCancer().equals("yes")){ //player_cardsboard.get(theplayer.getName())<13
						hands.add(hand);
					}
				}				
			}

			float n_hands = hands.size();
			Set<Integer> players = new HashSet<Integer>();
			if(n_hands>=min_players_per_board){
				//for all the hands that we've kept for this board
				for(Game hand : hands){
					players.add(hand.getPlayer1_id());
					List<String> features = hand.getPlayer1_features();
					if(features.size()==5){
						Set<String> distinct = new HashSet<String>();
						float order = 5;
						for(String gene : features){
							if(distinct.add(gene)){//onlu count once if the same gene gets into multiple hands
								Float freq = gene_freq.get(gene);
								if(freq==null){
									freq = new Float(0);
								}
								freq++;
								if(weight_card_order){
									gene_freq.put(gene, order);
								}else{
									gene_freq.put(gene, freq);
								}
								Integer votes = gene_votes.get(gene);
								if(votes==null){
									votes = new Integer(0);
								}
								votes++;
								order--;
								gene_votes.put(gene, votes);
								gene_views.put(gene, (int)n_hands);
								gene_board.put(gene, board.getId());
							}
						}
					}else{
						System.out.println("? wrong n features/hand "+hand.getId());
					}
				}

				//convert counts to fractions
				for(String gene : gene_freq.keySet()){
					if(gene_freq.get(gene)!=null){
						gene_freq.put(gene, gene_freq.get(gene)/n_hands);
					}
				}
				if(players.size()>=min_players_per_board){
					board_gene_freq.put(board, gene_freq);
					n_finished_boards++;
				}
			}
		}

		//deal with genes appearing on multiple boards
		Map<String, Float> global_gene_freq = new HashMap<String, Float>();
		Map<String, Float> global_gene_nboards = new HashMap<String, Float>();
		for(Entry<Board, Map<String, Float>> board_map : board_gene_freq.entrySet()){
			Map<String, Float> gene_scores = board_map.getValue();
			for(Entry<String, Float> gene_score : gene_scores.entrySet()){
				Float gscore = global_gene_freq.get(gene_score.getKey());
				if(gscore==null){
					gscore = new Float(0);
				}
				gscore+=gene_score.getValue();
				global_gene_freq.put(gene_score.getKey(), gscore);
				Float n = global_gene_nboards.get(gene_score.getKey());
				if(n==null){
					n = new Float(0);
				}
				n++;
				global_gene_nboards.put(gene_score.getKey(),n);
			}			
		}
		//convert to fractions over multiple boards
		for(String gene : global_gene_freq.keySet()){
			if(global_gene_freq.get(gene)/global_gene_nboards.get(gene)>1){
				System.out.println(gene+" "+global_gene_freq.get(gene)+" "+global_gene_nboards.get(gene));
				System.out.println("..");
			}
			global_gene_freq.put(gene, global_gene_freq.get(gene)/global_gene_nboards.get(gene));
		}
		List<String> ranked = MapFun.sortMapByValue(global_gene_freq); 
		Map<String, gene_rank> gene_frequency = new HashMap<String, gene_rank>();
		for(String id : ranked){
			gene_rank rank = makeGene_rank();
			rank.frequency = global_gene_freq.get(id);
			rank.views = gene_views.get(id);
			rank.votes = gene_votes.get(id);
			Feature fg = Feature.getByUniqueId(id);
			rank.symbol = fg.getShort_name();
			gene_frequency.put(id, rank);
		}
		//		int r = 0;
		//		for(String gene : ranked){
		//			r++;
		//			System.out.println(r+"\t"+gene+"\t"+global_gene_freq.get(gene)+"\t"+global_gene_nboards.get(gene)+"\t"+gene_votes.get(gene)+"\t"+gene_views.get(gene)+"\t"+gene_board.get(gene));
		//			//			if(global_gene_nboards.get(gene)>1){
		//			//				System.out.println(gene+"\t"+global_gene_freq.get(gene)+"\t"+global_gene_nboards.get(gene));
		//			//			}
		//		}
		//		System.out.println("");
		//		System.out.println("N finished boards "+n_finished_boards);
		return gene_frequency;
	}

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

	/**
	 * run a feature id list through weka and see what comes out
	 * these ids are local to our database
	 * @param gids
	 * @throws Exception 
	 */
	public static void testGeneList(List<String> unique_ids, int n_genes, String dataset, String trainfile) throws Exception {
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(trainfile), null, dataset);
		Weka.execution result = weka.pruneAndExecuteWithUniqueIds(unique_ids, null, dataset);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());
	}

}
