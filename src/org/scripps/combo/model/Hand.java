/**
 * 
 */
package org.scripps.combo.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.util.JdbcConnection;

/**
 * @author bgood
 * create table board (id int(10) NOT NULL AUTO_INCREMENT, dataset varchar(50) not null, n_players int default 0, n_wins int default 0, avg_score float default 0, max_score float default 0, base_score float default 0, created Date, updated timestamp, primary key (id));
 **/
public class Hand {
	int id;
	int player_id;
	String ip;
	String features; 
	int score;
	int cv_accuracy;
	int training_accuracy;
	int board_id;
	String dataset;
	int win;
	Calendar timestamp;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getCv_accuracy() {
		return cv_accuracy;
	}
	public void setCv_accuracy(int cv_accuracy) {
		this.cv_accuracy = cv_accuracy;
	}
	public String getFeatures() {
		return features;
	}
	public void setFeatures(String features) {
		this.features = features;
	}
	public int getBoard_id() {
		return board_id;
	}
	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}
	/**
	 * Save this hand.
	 */
	public void save(){
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("insert into hand values(null, ?,?,?,?,?,?,?,?,?,?)");
			pst.clearParameters();
			pst.setInt(1,getPlayer_id());
			pst.setString(2,getIp());
			pst.setInt(3, getScore());
			pst.setInt(4, getCv_accuracy());
			pst.setString(5, getFeatures());
			pst.setInt(6, getBoard_id());
			pst.setString(7,getDataset());
			pst.setInt(8, getTraining_accuracy());
			pst.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
			pst.setInt(10, getWin());
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Limit the hand list to the first hand per player per board that was won.
	 * @return
	 */
	public static List<Hand> getTheFirstHandPerPlayerPerBoard(boolean only_winning){
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from hand where player_name != 'anonymous_hero' order by time asc";
		if(only_winning){
			q = "select * from hand where win > 0 and player_name != 'anonymous_hero' order by time asc";
		}
		ResultSet rslt = conn.executeQuery(q);
		Map<String, Hand> bpw_hand = new HashMap<String, Hand>();
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
				hand.setFeatures(rslt.getString("features"));
				hand.setId(rslt.getInt("id"));
				hand.setIp(rslt.getString("ip"));
				hand.setPlayer_id(rslt.getInt("player_id"));
				hand.setScore(rslt.getInt("score"));
				hand.setDataset(rslt.getString("dataset"));
				hand.setTraining_accuracy(rslt.getInt("training_accuracy"));
				hand.setWin(rslt.getInt("win"));
				Calendar t = Calendar.getInstance();
				t.setTime(rslt.getTimestamp("time"));
				hand.setTimestamp(t);
				
				if(!bpw_hand.containsKey(hand.getBoard_id()+"_"+hand.getPlayer_id())){
					bpw_hand.put(hand.getBoard_id()+"_"+hand.getPlayer_id(), hand);
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
	
	/**
	 * get everything - includes multiple hands per board per player caused by refreshes..
	 * @return
	 */
	public static List<Hand> getAllHands(boolean only_winning){
		List<Hand> hands = new ArrayList<Hand>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from hand where player_name != 'anonymous_hero' ";
		if(only_winning){
			q+=" and win > 0 "; 
		}
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
				hand.setFeatures(rslt.getString("features"));
				hand.setId(rslt.getInt("id"));
				hand.setIp(rslt.getString("ip"));
				hand.setPlayer_id(rslt.getInt("player_id"));
				hand.setScore(rslt.getInt("score"));
				hand.setDataset(rslt.getString("dataset"));
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
	public int getTraining_accuracy() {
		return training_accuracy;
	}
	public void setTraining_accuracy(int training_accuracy) {
		this.training_accuracy = training_accuracy;
	}
	public int getWin() {
		return win;
	}
	public void setWin(int win) {
		this.win = win;
	}
	public Calendar getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	public String getDataset() {
		return dataset;
	}
	public void setDataset(String dataset) {
		this.dataset = dataset;
	}
	public int getPlayer_id() {
		return player_id;
	}
	public void setPlayer_id(int player_id) {
		this.player_id = player_id;
	}




	
}
