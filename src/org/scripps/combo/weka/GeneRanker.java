/**
 * 
 */
package org.scripps.combo.weka;

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
import org.scripps.combo.Hand;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;

/**
 * Use the data collected from game play to rank the genes
 * @author bgood
 *
 */
public class GeneRanker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Board b = new Board();
		List<Board> boards = b.getBoardsByPhenotype("dream_breast_cancer");
		Map<Board, Map<String, Float>> board_gene_freq = new HashMap<Board, Map<String, Float>>();
		for(Board board : boards){
			Map<String, Float> gene_freq = new HashMap<String, Float>();
			List<Hand> hands = getHandsByBoard(board.getId());
			float n_hands = 0;
			for(Hand hand : hands){
				n_hands++;
				String[] feature_names = hand.getFeature_names().split("\\|");
				if(feature_names.length>4){
					for(String feature : feature_names){
						String gene = feature.split(":")[0];
						Float freq = gene_freq.get(gene);
						if(freq==null){
							freq = new Float(0);
						}
						freq++;
						gene_freq.put(gene, freq);
					}
				}
			}
			//convert counts to fractions
//			for(String gene : gene_freq.keySet()){
//				if(gene_freq.get(gene)!=null){
//					gene_freq.put(gene, gene_freq.get(gene)/n_hands);
//				}
//			}
			board_gene_freq.put(board, gene_freq);
		}
		
		
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
			global_gene_freq.put(gene, global_gene_freq.get(gene)/global_gene_nboards.get(gene));
		}
		List<String> ranked = MapFun.sortMapByValue(global_gene_freq); 
		for(String gene : ranked){
			if(global_gene_nboards.get(gene)>1){
				System.out.println(gene+"\t"+global_gene_freq.get(gene)+"\t"+global_gene_nboards.get(gene));
			}
		}
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
