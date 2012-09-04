/**
 * 
 */
package org.scripps.combo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import org.scripps.util.JdbcConnection;

/**
 * @author bgood
 *create table hand (id int(10) NOT NULL AUTO_INCREMENT, player_name varchar(50), ip varchar(25), score int, cv_accuracy int, features varchar(100), board_id int, primary key (id));
 */
public class Hand {
	int id;
	String player_name;
	String ip;
	String features; 
	int score;
	int cv_accuracy;
	int training_accuracy;
	String feature_names;
	int board_id;
	String phenotype;
	String game_type;
	int win;
	Calendar timestamp;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPlayer_name() {
		return player_name;
	}
	public void setPlayer_name(String player_name) {
		this.player_name = player_name;
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
			PreparedStatement pst = conn.connection.prepareStatement("insert into hand values(null, ?,?,?,?,?,?,?,?,?,?,?,?)");
			pst.clearParameters();
			pst.setString(1,getPlayer_name());
			pst.setString(2,getIp());
			pst.setInt(3, getScore());
			pst.setInt(4, getCv_accuracy());
			pst.setString(5, getFeatures());
			pst.setInt(6, getBoard_id());
			pst.setString(7,getPhenotype());
			pst.setString(8, getFeature_names());
			pst.setInt(9, getTraining_accuracy());
			pst.setString(10, getGame_type());
			pst.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
			pst.setInt(12, getWin());
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getTraining_accuracy() {
		return training_accuracy;
	}
	public void setTraining_accuracy(int training_accuracy) {
		this.training_accuracy = training_accuracy;
	}
	public String getFeature_names() {
		return feature_names;
	}
	public void setFeature_names(String feature_names) {
		this.feature_names = feature_names;
	}
	public String getPhenotype() {
		return phenotype;
	}
	public void setPhenotype(String phenotype) {
		this.phenotype = phenotype;
	}
	public String getGame_type() {
		return game_type;
	}
	public void setGame_type(String game_type) {
		this.game_type = game_type;
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




	
}
