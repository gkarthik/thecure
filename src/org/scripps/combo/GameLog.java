/**
 * 
 */
package org.scripps.combo;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONObject;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Track data for combo - high scores, players, etc.
 * @author bgood
 *
 */
public class GameLog {
	ObjectMapper mapper;
	ObjectNode json_root;
	Map<String, Integer> pheno_multiplier;
	
	
	public GameLog() {
		super();
		pheno_multiplier = new HashMap<String, Integer>();
		pheno_multiplier.put("mammal", 5);
		pheno_multiplier.put("zoo", 7);
		pheno_multiplier.put("vantveer", 10);
		pheno_multiplier.put("coronal_case_control", 15);
		pheno_multiplier.put("griffith_full_filtered", 20);
		mapper = new ObjectMapper();
		json_root = mapper.createObjectNode();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameLog log = new GameLog();
		GameLog.high_score sb = log.getScoreBoard();
		String json = log.getD3CompatibleJson(sb);
		System.out.println(json);
//		for(String name : sb.getPlayer_max().keySet()){
//			//System.out.println(name+" "+sb.getPlayer_max().get(name)+" "+sb.getPlayer_avg().get(name));
//			System.out.println(name+" "+sb.getPlayer_global_points().get(name));
//		}
//		for(Entry<Long, Integer> date_game : sb.getDate_games().entrySet()){
//			System.out.println(date_game.getKey()+" "+date_game.getValue());
//		}
//		JSONObject r = new JSONObject(sb);
//		System.out.println(r.toString());
//		for(String board : sb.getBoard_max().keySet()){
//			System.out.println(board+" "+sb.getBoard_max().get(board)+" "+sb.getBoard_avg().get(board));
//		}

	}




	public String getD3CompatibleJson(high_score sb){
		String json = "";
		
		//json_root.put("name", top.getLabel());
		//json_root.put("kind", "split_node");
		
		ArrayNode games = mapper.createArrayNode();		
		//for chart
		for(Entry<Long, Integer> date_game : sb.getDate_games().entrySet()){
			//System.out.println(date_game.getKey()+" "+date_game.getValue());
			ObjectNode game = mapper.createObjectNode();
			game.put("timestamp", date_game.getKey());
			game.put("y", date_game.getValue());
			games.add(game);
		}
		json_root.put("chart", games);
		
		ArrayNode scores = mapper.createArrayNode();
		//for scoreboard
		for(Entry<String, Integer> player_score : sb.getPlayer_global_points().entrySet()){
			ObjectNode player = mapper.createObjectNode();
			player.put("username", player_score.getKey());
			player.put("score", player_score.getValue());
			scores.add(player);
		}
		json_root.put("leadersboard", scores);
		try {
			json = mapper.writeValueAsString(json_root);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	public class high_score{
		Map<Long, Integer> date_games;
		Map<String, Integer> player_global_points;
		Map<String, Integer> player_games;
		Map<String, Integer> player_max;
		Map<String, Float> player_avg;
		Map<String, Integer> board_max;
		Map<String, Float> board_avg;
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
		
		public Map<String, Integer> getPlayer_games() {
			return player_games;
		}
		public void setPlayer_games(Map<String, Integer> player_games) {
			this.player_games = player_games;
		}
		public Map<String, Integer> getPlayer_global_points() {
			return player_global_points;
		}
		public void setPlayer_global_points(Map<String, Integer> player_global_points) {
			this.player_global_points = player_global_points;
		}
		public Map<String, Integer> getBoard_max() {
			return board_max;
		}
		public void setBoard_max(Map<String, Integer> board_max) {
			this.board_max = board_max;
		}
		public Map<String, Float> getBoard_avg() {
			return board_avg;
		}
		public void setBoard_avg(Map<String, Float> board_avg) {
			this.board_avg = board_avg;
		}
		public Map<Long, Integer> getDate_games() {
			return date_games;
		}

		public void setDate_games(Map<Long, Integer> date_games) {
			this.date_games = date_games;
		}
	}
	

	
	public high_score getScoreBoard(){
		high_score scores = new high_score();
		List<Hand> hands = getAllWinningHands();
		Map<String, Integer> player_global_points = new HashMap<String, Integer>();
		Map<String, Integer> player_max = new HashMap<String, Integer>();
		Map<String, List<Integer>> player_games = new HashMap<String, List<Integer>>();
		Map<String, Integer> board_max = new HashMap<String, Integer>();
		Map<String, List<Integer>> board_games = new HashMap<String, List<Integer>>();
		Map<Long, Integer> date_games = new TreeMap<Long, Integer>();;
		int games_won = 0;
		for(Hand hand : hands){
			String player = hand.getPlayer_name();
			if(hand.getPhenotype()==null){
				continue;
			}
			//points for hand
			int points = 0;
			int multiplier = 1; int board_performance = 1;
			board_performance = hand.getCv_accuracy();
			//training levels use training scoring to make trees easier to understand
			//if(hand.getPhenotype().equals("mammal")||hand.getPhenotype().equals("zoo")){
			//	board_performance = hand.getTraining_accuracy();
			//}
			multiplier = pheno_multiplier.get(hand.getPhenotype());
			if(board_performance < 1){
				continue;
			}
			games_won++;
			date_games.put(hand.getTimestamp().getTimeInMillis(), games_won);
			
			points = multiplier*board_performance;
			Integer gpoints = player_global_points.get(player);
			if(gpoints==null){
				gpoints = new Integer(0);
			}
			gpoints+=points;
			player_global_points.put(player, gpoints);
			
			//player max
			int score = hand.getScore();
			Integer max = player_max.get(player);
			if(max ==null){
				max = score;
			}else if(score > max){
				max = score;
			}
			player_max.put(player, max);
			//board max
			String board = hand.getPhenotype()+"_"+hand.getBoard_id();
			max = board_max.get(board);
			if(max ==null){
				max = score;
			}else if(score > max){
				max = score;
			}
			board_max.put(board, max);
			//player game scores
			List<Integer> pscores = player_games.get(player);
			if(pscores==null){
				pscores = new ArrayList<Integer>();
			}
			pscores.add(score);
			player_games.put(player, pscores);
			//board scores
			List<Integer> bscores = board_games.get(player);
			if(bscores==null){
				bscores = new ArrayList<Integer>();
			}
			bscores.add(score);
			board_games.put(board, bscores);
			
		}
		
		//set averages
		Map<String, Float> player_avg = new TreeMap<String, Float>();
		Map<String, Float> board_avg = new TreeMap<String, Float>();
		Map<String, Integer> player_games_out = new HashMap<String, Integer>();
		for(String player : player_max.keySet()){
			float avg = 0; int count = 0;
			for(Integer s : player_games.get(player)){
				count++;
				avg+=s;
			}
			avg = avg/count;
			player_avg.put(player, avg);
			player_games_out.put(player, count);
		}
		for(String board : board_max.keySet()){
			float avg = 0; int count = 0;
			for(Integer s : board_games.get(board)){
				count++;
				avg+=s;
			}
			avg = avg/count;
			board_avg.put(board, avg);
		}
		//sort by values
		List<String> keys = MapFun.sortMapByValue(player_max);
		Map<String, Integer> player_max_out = new LinkedHashMap<String, Integer>();
		for(String key : keys){
			player_max_out.put(key, player_max.get(key));
		}
		keys = MapFun.sortMapByValue(player_avg);
		Map<String, Float> player_avg_out = new LinkedHashMap<String, Float>();
		for(String key : keys){
			player_avg_out.put(key, player_avg.get(key));
		}	
		List<String> ks = MapFun.sortMapByValue(board_max);
		Map<String, Integer> board_max_out = new LinkedHashMap<String, Integer>();
		for(String key : ks){
			board_max_out.put(key, board_max.get(key));
		}
		ks = MapFun.sortMapByValue(board_avg);
		Map<String, Float> board_avg_out = new LinkedHashMap<String, Float>();
		for(String key : ks){
			board_avg_out.put(key, board_avg.get(key));
		}
		
		ks = MapFun.sortMapByValue(player_global_points);
		Map<String, Integer> player_global_out = new LinkedHashMap<String, Integer>();
		for(String key : ks){
			player_global_out.put(key, player_global_points.get(key));
		}
		
		//save scoreboard object
		scores.setPlayer_games(player_games_out);
		scores.setBoard_avg(board_avg_out);
		scores.setBoard_max(board_max_out);
		scores.setPlayer_avg(player_avg_out);
		scores.setPlayer_max(player_max_out);
		scores.setPlayer_global_points(player_global_out);
		scores.setDate_games(date_games);
		return scores;
	}
	
	public List<Hand> getAllWinningHands(){
		List<Hand> hands = new ArrayList<Hand>();
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery("select * from hand where win > 0 and player_name != 'anonymous_hero'");
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


	public Map<String, Integer> getPheno_multiplier() {
		return pheno_multiplier;
	}


	public void setPheno_multiplier(Map<String, Integer> pheno_multiplier) {
		this.pheno_multiplier = pheno_multiplier;
	}
	
}
