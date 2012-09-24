/**
 * 
 */
package org.scripps.combo.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.scripps.util.JdbcConnection;

/**
 * @author bgood
create table game (id int(10) NOT NULL AUTO_INCREMENT, board_id int, player1_id int, player2_id int, game_started timestamp, game_finished timestamp, p1_score int, p2_score int, win int, created Date, updated timestamp, primary key (id));
create table game_player_feature (game_id int, player_id int, feature_id int);
create table game_card_ux(game_id int, feature_id int, time timestamp, panel varchar(50), board_hover boolean);
create table game_mouse_action(game_id int, x int, y int, time timestamp);

 */
public class Game {

	int id;
	int player1_id;
	int player2_id;
	Timestamp game_started;
	Timestamp game_finished;
	int p1_score;
	int p2_score;
	int win; //0 = tie, 1 = win, 2 = loss
	int board_id;
	Date created;
	Timestamp updated;
	List<String> player1_features; //unique ids for the cards in the hand
	List<String> player2_features; 
	List<ux> feature_ux;
	List<mouse> mouse_actions;
	
	class ux{
		String feature_id;
		Timestamp timestamp;
		String panel;
		boolean board_hover;
	}
	
	class mouse{
		Timestamp timestamp;
		int x;
		int y;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	/**
	 * Insert a game record 
	 * @throws SQLException 
	 */
	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into game (id, board_id, player1_id, player2_id, game_started, game_finished, p1_score, p2_score, win, created)" +
					"values (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			if(id>0){
				pst.setInt(1,getId());
			}else{
				pst.setString(1, null);
			}
			pst.setInt(2,getBoard_id());
			pst.setInt(3, getPlayer1_id());
			pst.setInt(4, getPlayer2_id());
			pst.setTimestamp(5, getGame_started());
			pst.setTimestamp(6, getGame_finished());
			pst.setInt(7, getP1_score());
			pst.setInt(8, getP2_score());
			pst.setInt(9, getWin());
			pst.setDate(10, new Date(System.currentTimeMillis()));
			
			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Inserting game failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				setId(generatedKeys.getInt(1));
				newid = generatedKeys.getInt(1);
				//insert the features
				for(String unique_id : player1_features){
					Feature f = Feature.getByUniqueId(unique_id);
					if(f!=null){
						conn.executeUpdate("insert into game_player_feature values ("+newid+","+player1_id+","+f.id);
					}
				}
				for(String unique_id : player2_features){
					Feature f = Feature.getByUniqueId(unique_id);
					if(f!=null){
						conn.executeUpdate("insert into game_player_feature values ("+newid+","+player2_id+","+f.id);
					}
				}
				//insert ux
				for(ux x : feature_ux){
					conn.executeUpdate("insert into game_card_ux values ("+newid+","+x.feature_id+","+x.timestamp+","+x.panel+","+x.board_hover);
				}
				//insert mouse
				for(mouse m : mouse_actions){
					conn.executeUpdate("insert into game_mouse_action values ("+newid+","+m.x+","+m.y+","+m.timestamp);				
				}
				
			} else {
				throw new SQLException("Creating board failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPlayer1_id() {
		return player1_id;
	}

	public void setPlayer1_id(int player1_id) {
		this.player1_id = player1_id;
	}

	public int getPlayer2_id() {
		return player2_id;
	}

	public void setPlayer2_id(int player2_id) {
		this.player2_id = player2_id;
	}

	public Timestamp getGame_started() {
		return game_started;
	}

	public void setGame_started(Timestamp game_started) {
		this.game_started = game_started;
	}

	public Timestamp getGame_finished() {
		return game_finished;
	}

	public void setGame_finished(Timestamp game_finished) {
		this.game_finished = game_finished;
	}

	public int getP1_score() {
		return p1_score;
	}

	public void setP1_score(int p1_score) {
		this.p1_score = p1_score;
	}

	public int getP2_score() {
		return p2_score;
	}

	public void setP2_score(int p2_score) {
		this.p2_score = p2_score;
	}

	public int getWin() {
		return win;
	}

	public void setWin(int win) {
		this.win = win;
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


	public int getBoard_id() {
		return board_id;
	}


	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}

	
	
}
