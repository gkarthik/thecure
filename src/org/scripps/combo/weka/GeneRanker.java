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
import org.scripps.combo.model.Hand;
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
		List<String> gids = null;
		boolean weight_card_order = false; boolean only_cancer_people = true;
		gids = getRankedGenes(weight_card_order, only_cancer_people);
		testGeneList(gids, n_genes);

	//	generateRandomBaseline(n_genes, 1000);
	//	Player.describePlayers(true);
	}


	public static void generateRandomBaseline(int n_genes, int n_sampled) throws Exception{
		//get results vector ready
		DescriptiveStatistics cvs = new DescriptiveStatistics();
		
		//get weka ready
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff";
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, "dream_breast_cancer");
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
			execution base = weka.pruneAndExecuteWithFeatureIds(geneids, null);
			double cv = base.eval.pctCorrect();
			cvs.addValue(cv);
			System.out.println(i+"\t"+cv);
		}

		System.out.println(cvs.toString());
	}

	/**
	 * run a gene list through weka and see what comes out
	 * @param gids
	 * @throws Exception 
	 */
	public static void testGeneList(List<String> gids, int n_genes) throws Exception {
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff";
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, "dream_breast_cancer");
		Weka.execution result = weka.pruneAndExecuteWithFeatureIds(gids, null);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());
	}

	/**
	 * For each board that has been played by more than 5 people, 
	 * Find the genes that have been selected with the highest frequency by all players.
	 * Only examine the first hand per player per board, regardless of whether they won.
	 * @return a list of geneids sorted according to the frequency that they were selected - first is the most frequent
	 */
	public static List<String> getRankedGenes(boolean weight_card_order, boolean only_cancer_people){
//		//set up player filter
		List<Player> playerss = Player.getAllPlayers();
		Map<String, Player> name_player = Player.playerListToMap(playerss);
		Map<String, Float> player_cardsboard = new HashMap<String, Float>();
		
		int min_hands_per_board = 5;
		int min_players_per_board = 5;
		int n_finished_boards = 0;
		List<Board> boards = Board.getBoardsByDatasetRoom("dream_breast_cancer","1");
		Map<Board, Map<String, Float>> board_gene_freq = new HashMap<Board, Map<String, Float>>();
		Map<String, Integer> gene_views = new HashMap<String, Integer>();
		Map<String, Integer> gene_votes = new HashMap<String, Integer>();
		Map<String, Integer> gene_board = new HashMap<String, Integer>();
		for(Board board : boards){
			Map<String, Float> gene_freq = new HashMap<String, Float>();
			List<Hand> hands = new ArrayList<Hand>();
			List<Hand> handsall = getTheFirstHandPerPlayerByBoard(board.getId());
			if(only_cancer_people){
				//filter hands by player attributes	
				for(Hand hand : handsall){
					Player theplayer = name_player.get(hand.getPlayer_id());
					if(theplayer!=null&&theplayer.getCancer().equals("yes")&&
							player_cardsboard.get(theplayer.getName())<13){
						hands.add(hand);
					}
				}				
			}
						
			float n_hands = hands.size();
			Set<Integer> players = new HashSet<Integer>();
			if(n_hands>=min_hands_per_board){
				
				for(Hand hand : hands){
					players.add(hand.getPlayer_id());
					List<String> features = hand.getFeatures();
					if(features.size()>4){
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
		return ranked;
	}

	public static List<Hand> getTheFirstHandPerPlayerByBoard(int board_id){
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery("select * from hand where board_id = "+board_id+" order by updated asc");
		Map<String, Hand> bpw_hand = new HashMap<String, Hand>();
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
				hand.setId(rslt.getInt("id"));
				hand.setIp(rslt.getString("ip"));
				hand.setPlayer_id(rslt.getInt("player_id"));
				hand.setScore(rslt.getInt("score"));
				hand.setDataset(rslt.getString("dataset"));
				hand.setTraining_accuracy(rslt.getInt("training_accuracy"));
				hand.setWin(rslt.getInt("win"));
				hand.setCreated(rslt.getDate("created"));
				hand.setUpdated(rslt.getTimestamp("updated"));
						
				if(!bpw_hand.containsKey(hand.getBoard_id()+"_"+hand.getPlayer_id())){
					hand.setFeaturesForHandToUniqueIds();
					bpw_hand.put(hand.getBoard_id()+"_"+hand.getPlayer_id(), hand);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Hand> hands = new ArrayList<Hand>(bpw_hand.values());
		return hands;
	}	

}
