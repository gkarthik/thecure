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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.scripps.combo.Board;
import org.scripps.combo.Card;
import org.scripps.combo.GameLog;
import org.scripps.combo.Hand;
import org.scripps.combo.Player;
import org.scripps.combo.weka.Weka.card;
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
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {
		//		List<String> gids = getRankedGenes_1();
		//		try {
		//			testGeneList(gids);
		//		} catch (FileNotFoundException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		//use played cards - as opposed to hands - to rate players
		//List<Card> cards = Card.getAllPlayedCards();
		//System.out.println(cards.size());

		describePlayers();
	}

	public static void describePlayers(){
		List<Player> players = Player.getAllPlayers();
		Map<String, Player> name_player = Player.playerListToMap(players);
		GameLog log = new GameLog();
		List<Hand> wm = Hand.getTheFirstHandPerPlayerPerBoard(false); // Hand.getAllHands(false);
		//remove mammal
		List<Hand> hands = new ArrayList<Hand>();
		for(Hand hand : wm){
			if(hand.getBoard_id()>4){
				hands.add(hand);
			}
		}
		
		Map<String, Float> player_cardsboard = new HashMap<String, Float>();
		for(Player player : players){
			Map<String, Integer> board_counts = Card.getBoardCardCount(player.getId());
			float avg_cards_board = 0;
			for(Entry<String, Integer> board_count : board_counts.entrySet()){
				avg_cards_board+= board_count.getValue();
			}
			avg_cards_board = avg_cards_board/board_counts.size();
			player_cardsboard.put(player.getName(), avg_cards_board);
		}
		
		GameLog.high_score sb = log.getScoreBoard(hands);
		for(String name : sb.getPlayer_avg().keySet()){
			if(name_player.get(name)!=null){
				System.out.println(name+"\t"+
						name_player.get(name).getBiologist()+"\t"+
						name_player.get(name).getCancer()+"\t"+
						name_player.get(name).getDegree()+"\t"+
						sb.getPlayer_max().get(name)+"\t"
						+sb.getPlayer_avg().get(name)+"\t"+
						sb.getPlayer_global_points().get(name)+"\t"+
						sb.getPlayer_games().get(name)+"\t"+
						sb.getPlayer_avg_win().get(name)+"\t"+
						player_cardsboard.get(name)
				);
			}
		}
	}

	/**
	 * run a gene list through weka and see what comes out
	 * @param gids
	 * @throws FileNotFoundException
	 */
	public static void testGeneList(List<String> gids) throws FileNotFoundException {
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff" ;
		String metadatafile = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/id_map.txt"; 
		Weka weka = new Weka(train_file);
		weka.loadMetadata(new FileInputStream(metadatafile), false);
		String features = "";
		int limit = 100; int l = 0;
		for(String geneid : gids){		
			List<card> cards = weka.getGeneid_cards().get(geneid);
			String f = "";
			if(cards!=null){
				for(card c : cards){
					f+=c.getAtt_index()+",";
				}
			}
			System.out.println(geneid+"\t"+weka.getGeneid_cards().get(geneid).get(0).getName()+"\t"+f);
			features+=f;
			l++;
			if(l>=limit){
				break;
			}
		}
		J48 wekamodel = new J48();
		Weka.execution result = weka.pruneAndExecute(features, wekamodel);
		ClassifierEvaluation short_result = new ClassifierEvaluation((int)result.eval.pctCorrect(), result.model.getClassifier().toString());
		System.out.println("cv_accuracy\t"+short_result.getAccuracy()+"\n"+short_result.getModelrep());
	}

	/**
	 * For each board that has been played by more than 5 people, 
	 * Find the genes that have been selected with the highest frequency by all players.
	 * Only examine the first hand per player per board, regardless of whether they won.
	 * @return a list of geneids sorted according to the frequency that they were selected - first is the most frequent
	 */
	public static List<String> getRankedGenes_1(){
		Board b = new Board();
		int min_hands_per_board = 5;
		int min_players_per_board = 5;
		int n_finished_boards = 0;
		List<Board> boards = b.getBoardsByPhenotype("dream_breast_cancer");
		Map<Board, Map<String, Float>> board_gene_freq = new HashMap<Board, Map<String, Float>>();
		Map<String, Integer> gene_views = new HashMap<String, Integer>();
		Map<String, Integer> gene_votes = new HashMap<String, Integer>();
		Map<String, Integer> gene_board = new HashMap<String, Integer>();
		for(Board board : boards){
			Map<String, Float> gene_freq = new HashMap<String, Float>();
			List<Hand> hands = getTheFirstHandPerPlayerByBoard(board.getId());
			float n_hands = hands.size();
			Set<String> players = new HashSet<String>();
			if(n_hands>=min_hands_per_board){
				for(Hand hand : hands){
					players.add(hand.getPlayer_name());
					String[] feature_names = hand.getFeature_names().split("\\|");
					if(feature_names.length>4){
						Set<String> distinct = new HashSet<String>();
						for(String feature : feature_names){
							String gene = feature.split(":")[0];
							if(distinct.add(gene)){//onlu count once if the same gene gets into multiple hands
								Float freq = gene_freq.get(gene);
								if(freq==null){
									freq = new Float(0);
								}
								freq++;
								gene_freq.put(gene, freq);
								Integer votes = gene_votes.get(gene);
								if(votes==null){
									votes = new Integer(0);
								}
								votes++;
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
		ResultSet rslt = conn.executeQuery("select * from hand where board_id = "+board_id+" and player_name != 'anonymous_hero' order by time asc");
		Map<String, Hand> bpw_hand = new HashMap<String, Hand>();
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
				hand.setFeatures(rslt.getString("features"));
				hand.setId(rslt.getInt("id"));
				hand.setIp(rslt.getString("ip"));
				hand.setPlayer_name(rslt.getString("player_name"));
				hand.setScore(rslt.getInt("score"));
				hand.setFeature_names(rslt.getString("feature_names"));
				hand.setGame_type(rslt.getString("game_type"));
				hand.setPhenotype(rslt.getString("phenotype"));
				hand.setTraining_accuracy(rslt.getInt("training_accuracy"));
				hand.setWin(rslt.getInt("win"));
				Calendar t = Calendar.getInstance();
				t.setTime(rslt.getTimestamp("time"));
				hand.setTimestamp(t);

				if(!bpw_hand.containsKey(hand.getBoard_id()+"_"+hand.getPlayer_name())){
					bpw_hand.put(hand.getBoard_id()+"_"+hand.getPlayer_name(), hand);
					//		System.out.println("first "+hand.getId()+"\t"+hand.getPlayer_name()+"\t"+hand.getBoard_id());
				}else{
					//		System.out.println(" next "+hand.getId()+"\t"+hand.getPlayer_name()+"\t"+hand.getBoard_id());
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Hand> hands = new ArrayList<Hand>(bpw_hand.values());
		return hands;
	}	

	public static List<Hand> getHandsByBoard(int board_id){
		List<Hand> hands = new ArrayList<Hand>();
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery("select * from hand where board_id ='"+board_id+"' and player_name != 'anonymous_hero'");
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
				hand.setFeatures(rslt.getString("features"));
				hand.setId(rslt.getInt("id"));
				hand.setIp(rslt.getString("ip"));
				hand.setPlayer_name(rslt.getString("player_name"));
				hand.setScore(rslt.getInt("score"));
				hand.setFeature_names(rslt.getString("feature_names"));
				hand.setGame_type(rslt.getString("game_type"));
				hand.setPhenotype(rslt.getString("phenotype"));
				hand.setTraining_accuracy(rslt.getInt("training_accuracy"));
				hand.setWin(rslt.getInt("win"));
				Calendar t = Calendar.getInstance();
				t.setTime(rslt.getTimestamp("time"));
				hand.setTimestamp(t);
				hands.add(hand);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hands;
	}
}
