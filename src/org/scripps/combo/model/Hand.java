/**
 * 
 */
package org.scripps.combo.model;

import java.sql.Date;
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
 * create table hand (id int(10) NOT NULL AUTO_INCREMENT, player_id int, board_id int, dataset varchar(50), ip varchar(25), 
   score int, cv_accuracy int, training_accuracy int, win int, 
   created Date, updated timestamp, primary key (id));
   
   create table hand_feature (hand_id int, feature_id int);
 **/
public class Hand {
	int id;
	int player_id;
	String ip;
	List<String> features; 
	int score;
	int cv_accuracy;
	int training_accuracy;
	int board_id;
	String dataset;
	int win;
	Date created;
	Timestamp updated;
	Timestamp game_started;
	Timestamp game_finished;

	
	
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
	public int getBoard_id() {
		return board_id;
	}
	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}
	/**
	 * Save this hand.
	 */
	public void insert(){
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("insert into hand " +
					"(id, player_id, board_id, dataset, ip, score, cv_accuracy, training_accuracy, win, created, updated)" +
					"values(?,?,?,?,?,?,?,?,?,?, ?)");
			pst.clearParameters();
			if(id>0){
				pst.setInt(1,getId());
			}else{
				pst.setString(1, null);
			}
			pst.setInt(2,getPlayer_id());
			pst.setInt(3, getBoard_id());
			pst.setString(4, getDataset());
			pst.setString(5, getIp());
			pst.setInt(6,getScore());
			pst.setInt(7, getCv_accuracy());
			pst.setInt(8, getTraining_accuracy());
			pst.setInt(9, getWin());
			
			if(created==null){
				pst.setDate(10, new Date(System.currentTimeMillis()));
			}else{
				pst.setDate(10, created);
			}
			if(updated==null){
				pst.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
			}else{
				pst.setTimestamp(11, updated);
			}
			
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setFeaturesForHandToUniqueIds(){
		if(id==0){
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		String q = "select feature.unique_id from hand_feature, feature " +
				"where feature.id = hand_feature.feature_id and " +
				"hand_id = "+id;

		ResultSet rslt = conn.executeQuery(q);
		List<String> unique_ids = new ArrayList<String>();
		try {
			while(rslt.next()){
				unique_ids.add(rslt.getString(1));
			}
			setFeatures(unique_ids);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Limit the hand list to the first hand per player per board that was won.
	 * @return
	 */
	public static List<Hand> getTheFirstHandPerPlayerPerBoard(boolean only_winning){
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from hand order by time asc";
		if(only_winning){
			q = "select * from hand where win > 0 order by updated asc";
		}
		ResultSet rslt = conn.executeQuery(q);
		Map<String, Hand> bpw_hand = new HashMap<String, Hand>();
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
			//	hand.setFeatures(rslt.getString("features"));
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
		String q = "select * from hand ";
		if(only_winning){
			q+=" and win > 0 "; 
		}
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Hand hand = new Hand();
				hand.setBoard_id(rslt.getInt("board_id"));
				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));
			//	hand.setFeatures(rslt.getString("features"));
				hand.setId(rslt.getInt("id"));
				hand.setIp(rslt.getString("ip"));
				hand.setPlayer_id(rslt.getInt("player_id"));
				hand.setScore(rslt.getInt("score"));
				hand.setDataset(rslt.getString("dataset"));
				hand.setTraining_accuracy(rslt.getInt("training_accuracy"));
				hand.setWin(rslt.getInt("win"));
				hand.setCreated(rslt.getDate("created"));
				hand.setUpdated(rslt.getTimestamp("updated"));
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
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Timestamp getUpdated() {
		return updated;
	}
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}
	public List<String> getFeatures() {
		return features;
	}
	public void setFeatures(List<String> features) {
		this.features = features;
	}




	
}
