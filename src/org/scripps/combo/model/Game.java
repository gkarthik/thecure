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
create table game (id int(10) NOT NULL AUTO_INCREMENT, board_id int, ip varchar(30), player1_id int, player2_id int, updated timestamp, game_started timestamp, game_finished timestamp, p1_score int, p2_score int, win int, created Date, primary key (id));create table game_player_feature (game_id int, player_id int, feature_id int);
create table game_card_ux(game_id int, feature_id int, time timestamp, panel varchar(50), board_hover boolean);
create table game_mouse_action(game_id int, x int, y int, time timestamp);

 */
public class Game {

	int id;
	String ip;
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

	public class ux{
		public ux(String feature_id, long t, String panel, boolean board_hover) {
			super();
			this.feature_id = feature_id;
			this.timestamp = new Timestamp(t);
			this.panel = panel;
			this.board_hover = board_hover;
		}
		String feature_id;
		Timestamp timestamp;
		String panel;
		boolean board_hover;
	}

	public ux makeUx(String unique_id, long t, String panel, boolean board_hover){
		return new ux(unique_id, t, panel, board_hover);
	}

	public class mouse{
		public mouse(long t, int x, int y) {
			super();
			this.timestamp = new Timestamp(t);
			this.x = x;
			this.y = y;
		}
		Timestamp timestamp;
		int x;
		int y;
	}

	public mouse makeMouse(long t, int x, int y){
		return new mouse(t, x, y);
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
					"insert into game (id, board_id, player1_id, player2_id, game_started, game_finished, p1_score, p2_score, win, created, ip)" +
					"values (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
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
			pst.setString(11, getIp());

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
						String upfs = "insert into game_player_feature values ("+newid+","+player1_id+","+f.id+")";
						conn.executeUpdate(upfs);
					}
				}
				for(String unique_id : player2_features){
					Feature f = Feature.getByUniqueId(unique_id);
					if(f!=null){
						String upfs = "insert into game_player_feature values ("+newid+","+player2_id+","+f.id+")";
						conn.executeUpdate(upfs);
					}
				}
				//insert ux
				PreparedStatement pst_ux = conn.connection.prepareStatement("insert into game_card_ux values (?,?,?,?,?)");				
				for(ux x : feature_ux){
					Feature f = Feature.getByUniqueId(x.feature_id);
					if(f!=null){
						pst_ux.clearParameters();
						//"+newid+","+x.feature_id+","+x.timestamp+","+x.panel+","+x.board_hover+"
						pst_ux.setInt(1, newid);
						pst_ux.setInt(2, f.getId());
						pst_ux.setTimestamp(3, x.timestamp);
						pst_ux.setString(4, x.panel);
						pst_ux.setBoolean(5, x.board_hover);
						pst_ux.executeUpdate();
					}
				}
				pst_ux.close();
				//insert mouse
				PreparedStatement pst_m = conn.connection.prepareStatement("insert into game_mouse_action values (?,?,?,?)");
				for(mouse m : mouse_actions){
					pst_m.clearParameters();
					//"+newid+","+m.x+","+m.y+","+m.timestamp+"
					pst_m.setInt(1, newid);
					pst_m.setInt(2, m.x);
					pst_m.setInt(3, m.y);
					pst_m.setTimestamp(4, m.timestamp);
					pst_m.executeUpdate();				
				}
				pst_m.close();

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


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public List<String> getPlayer1_features() {
		return player1_features;
	}


	public void setPlayer1_features(List<String> player1_features) {
		this.player1_features = player1_features;
	}


	public List<String> getPlayer2_features() {
		return player2_features;
	}


	public void setPlayer2_features(List<String> player2_features) {
		this.player2_features = player2_features;
	}


	public List<ux> getFeature_ux() {
		return feature_ux;
	}


	public void setFeature_ux(List<ux> feature_ux) {
		this.feature_ux = feature_ux;
	}


	public List<mouse> getMouse_actions() {
		return mouse_actions;
	}


	public void setMouse_actions(List<mouse> mouse_actions) {
		this.mouse_actions = mouse_actions;
	}



}
