/**
 * 
 */
package org.scripps.combo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.scripps.util.JdbcConnection;

/**
 * Track data for combo - high scores, players, etc.
 * @author bgood
 *
 */
public class GameLog {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	class high_score{
		Map<String, Integer> player_max;
		Map<String, Float> player_avg;
		Map<Integer, Integer> board_max;
		Map<Integer, Float> board_avg;
		public Map<String, Integer> getPlayer_max() {
			return player_max;
		}
		public void setPlayer_max(Map<String, Integer> player_max) {
			this.player_max = player_max;
		}
		public Map<String, Float> getPlayer_avg() {
			return player_avg;
		}
		public void setPlayer_avg(Map<String, Float> player_avg) {
			this.player_avg = player_avg;
		}
		public Map<Integer, Integer> getBoard_max() {
			return board_max;
		}
		public void setBoard_max(Map<Integer, Integer> board_max) {
			this.board_max = board_max;
		}
		public Map<Integer, Float> getBoard_avg() {
			return board_avg;
		}
		public void setBoard_avg(Map<Integer, Float> board_avg) {
			this.board_avg = board_avg;
		}
		
	}
	
	public high_score getScoreBoard(){
		high_score scores = new high_score();
		List<Hand> hands = getAllHands();
		Map<String, Integer> player_max = new TreeMap<String, Integer>();
		Map<String, Float> player_avg = new TreeMap<String, Float>();
		Map<Integer, Integer> board_max = new TreeMap<Integer, Integer>();
		Map<Integer, Float> board_avg = new TreeMap<Integer, Float>();
		
		//todo set up the tables
		
		return scores;
	}
	
	public List<Hand> getAllHands(){
		List<Hand> hands = new ArrayList<Hand>();
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery("select * from hand");
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
				hands.add(hand);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hands;
	}
	
}
