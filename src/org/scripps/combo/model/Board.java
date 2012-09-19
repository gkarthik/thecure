package org.scripps.combo.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.combo.weka.Weka.card;
import org.scripps.util.JdbcConnection;

/**
 * A pre-computed set of features (e.g. genes) for building board games.
 * create table board (id int(10) NOT NULL AUTO_INCREMENT, dataset varchar(50), n_players int, n_wins int, avg_score float, max_score float, base_score float, created Date, updated timestamp, primary key (id));
 * @author bgood
 *
 */
public class Board {

	int id;
	String dataset;
	List<String> feature_ids; // >> Feature
	int n_players;
	int n_wins;
	float average_score;
	float max_score;
	float base_score;
	Date created;
	Timestamp updated;
		
	public static void main(String args[]){
		Board b = new Board();
		b.setDataset("test");
		b.setBase_score(22);
		b.setCreated(new Date(System.currentTimeMillis()));
		ArrayList<String> ids = new ArrayList<String>();
		ids.add("1"); ids.add("2");
		b.setFeature_ids(ids);
		try {
			b.insert();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Insert a new board.
	 * @throws SQLException 
	 */
	public boolean insert() throws SQLException{
		if(getFeature_ids()==null){
			return false;
		}
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into board (id, dataset, base_score, created) values(null,?,?,?)",
					 Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			pst.setString(1,getDataset());
			pst.setFloat(2,getBase_score());
			pst.setDate(3, getCreated());
			pst.executeUpdate();
			
			int affectedRows = pst.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Creating board failed, no rows affected.");
	        }
	        generatedKeys = pst.getGeneratedKeys();
	        if (generatedKeys.next()) {
	            setId(generatedKeys.getInt(1));
	            //now link up the feature ids
	            
	        } else {
	            throw new SQLException("Creating board failed, no generated key obtained.");
	        }
	    } finally {
	        if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
	        if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
	        if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
	    }
	    return true;
	}
	
	public void updateMaxScore(){
		if(getId()<1){
			System.out.println("Can't update without an id");
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("update board set max_score = ? where id = "+getId());
			pst.clearParameters();
			pst.setFloat(1,getMax_score());			
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Integer> getBoardScoresfromDb(int board_id){
		List<Integer> board_scores = new ArrayList<Integer>();
		String gethands = "select phenotype, game_type, board_id, score, training_accuracy, cv_accuracy, win from hand where board_id = '"+board_id+"' and player_name != 'anonymous_hero'";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				int training = hands.getInt("training_accuracy");
				int cv = hands.getInt("cv_accuracy");
				int win = hands.getInt("win");				
				if(win>0){
					if(training<0){
						training = cv;
					}
					board_scores.add(training);
				}
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return board_scores;
	}

	public Map<String,List<Integer>> getPlayerBoardScoresForWins(int board_id){
		Map<String,List<Integer>> player_board_scores = new HashMap<String,List<Integer>>();
		String gethands = "select player_name, phenotype, game_type, board_id, score, training_accuracy, cv_accuracy, win from hand where win > 0" +
				" and board_id = '"+board_id+"' and player_name != 'anonymous_hero' ";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				int training = hands.getInt("training_accuracy");
				int cv = hands.getInt("cv_accuracy");
				String player_name = hands.getString("player_name");
				if(training<0){
					training = cv;
				}
				List<Integer> board_scores = player_board_scores.get(player_name);
				if(board_scores==null){
					board_scores = new ArrayList<Integer>();
				}
				board_scores.add(training);
				player_board_scores.put(player_name, board_scores);
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player_board_scores;
	}	
	
//	
//	public List<Board> getBoardsByDataset(String dataset){
//		List<Board> boards = new ArrayList<Board>();
//		JdbcConnection conn = new JdbcConnection();
//		ResultSet rslt = conn.executeQuery("select * from board where phenotype = '"+dataset+"' order by base_score desc");
//		try {
//			while(rslt.next()){
//				Board board = new Board();
//				board.setAverage_score(rslt.getFloat("average_score"));
//				board.setEntrez_ids(string2list(rslt.getString("entrez_ids")));
//				board.setGene_symbols(string2list(rslt.getString("gene_symbols")));
//				board.setId(rslt.getInt("id"));
//				board.setMax_score(rslt.getFloat("max_score"));
//				board.setN_players(rslt.getInt("n_players"));
//				board.setN_wins(rslt.getInt("n_wins"));
//				board.setPhenotype(phenotype);
//				board.setUpdated(rslt.getTimestamp("updated"));
//				board.setAttribute_names(string2list(rslt.getString("attribute_names")));
//				board.setBase_score(rslt.getFloat("base_score"));
//				boards.add(board);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return boards;
//	}
//
//	public Board getBoardById(String id){
//		JdbcConnection conn = new JdbcConnection();
//		ResultSet rslt = conn.executeQuery("select * from board where id = '"+id+"'");
//		try {
//			if(rslt.next()){
//				Board board = new Board();
//				board.setAverage_score(rslt.getFloat("average_score"));
//				board.setEntrez_ids(string2list(rslt.getString("entrez_ids")));
//				board.setGene_symbols(string2list(rslt.getString("gene_symbols")));
//				board.setId(rslt.getInt("id"));
//				board.setMax_score(rslt.getFloat("max_score"));
//				board.setN_players(rslt.getInt("n_players"));
//				board.setN_wins(rslt.getInt("n_wins"));
//				board.setPhenotype(dataset);
//				board.setUpdated(rslt.getTimestamp("updated"));
//				board.setAttribute_names(string2list(rslt.getString("attribute_names")));
//				board.setBase_score(rslt.getFloat("base_score"));
//				return board;
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getN_players() {
		return n_players;
	}
	public void setN_players(int n_players) {
		this.n_players = n_players;
	}
	public int getN_wins() {
		return n_wins;
	}
	public void setN_wins(int n_wins) {
		this.n_wins = n_wins;
	}
	public float getAverage_score() {
		return average_score;
	}
	public void setAverage_score(float average_score) {
		this.average_score = average_score;
	}
	public float getMax_score() {
		return max_score;
	}
	public void setMax_score(float max_score) {
		this.max_score = max_score;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public float getBase_score() {
		return base_score;
	}


	public void setBase_score(float base_score) {
		this.base_score = base_score;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}


	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public List<String> getFeature_ids() {
		return feature_ids;
	}

	public void setFeature_ids(List<String> feature_ids) {
		this.feature_ids = feature_ids;
	}
	
	
}
