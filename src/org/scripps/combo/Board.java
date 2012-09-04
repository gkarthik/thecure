package org.scripps.combo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.combo.weka.Weka.card;
import org.scripps.util.JdbcConnection;

public class Board {

	int id;
	String phenotype;
	List<String> entrez_ids;
	List<String> gene_symbols;
	int n_players;
	int n_wins;
	float average_score;
	float max_score;
	float base_score;
	Timestamp updated;
	List<String> attribute_names;
	
	public Board(){
		id = 0;
		phenotype = "";
		entrez_ids = new ArrayList<String>();
		gene_symbols = new ArrayList<String>();
		attribute_names = new ArrayList<String>();
		n_players = 0;
		n_wins = 0;
		average_score = 0;
		max_score = 0;
		base_score = 0;
		updated = new Timestamp(System.currentTimeMillis());
		
	}

	
	public String list2string(List<String> things){
		String list = "";
		for(String thing : things){
			list+=thing+"\t";
		}
		return list;
	}
	
	public List<String> string2list(String del){
		List<String> l = new ArrayList<String>();
		String[] split = del.split("\t");
		for(String s : split){
			l.add(s);
		}
		return l;
	}
	
	public void insert(){
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("insert into board values(null,?,?,?,?,?,?,?,?,?,?)");
			pst.clearParameters();
			pst.setString(1,getPhenotype());
			pst.setString(2,list2string(getEntrez_ids()));
			pst.setString(3, list2string(getGene_symbols()));
			pst.setInt(4, getN_players());
			pst.setInt(5, getN_wins());
			pst.setFloat(6, getAverage_score());
			pst.setFloat(7,getMax_score());
			pst.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
			pst.setString(9, list2string(getAttribute_names()));
			pst.setFloat(10, getBase_score());
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void update(){
		if(getId()<1){
			System.out.println("Can't update without an id");
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("update board set phenotype = ?, entrez_ids = ?, gene_symbols = ?, n_players = ?, n_wins = ?, average_score = ?, max_score = ?, updated = ?, attribute_names = ?, base_score = ? where id = "+getId());
			pst.clearParameters();
			pst.setString(1,getPhenotype());
			pst.setString(2,list2string(getEntrez_ids()));
			pst.setString(3, list2string(getGene_symbols()));
			pst.setInt(4, getN_players());
			pst.setInt(5, getN_wins());
			pst.setFloat(6, getAverage_score());
			pst.setFloat(7,getMax_score());
			pst.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
			pst.setString(9, list2string(getAttribute_names()));
			pst.setFloat(10, getBase_score());
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
	
//	public void updatePlay(int board_id, int cv_score, String player){
//		if(getId()<1){
//			System.out.println("Can't update without an id");
//			return;
//		}
//		JdbcConnection conn = new JdbcConnection();
//		try {
//			PreparedStatement pst = conn.connection.prepareStatement("update board set n_players = ?, n_wins = ?, average_score = ?, max_score = ?, updated = ? where id = "+getId());
//			pst.clearParameters();
//			pst.setString(1,getPhenotype());
//			pst.setString(2,list2string(getEntrez_ids()));
//			pst.setString(3, list2string(getGene_symbols()));
//			pst.setInt(4, getN_players());
//			pst.setInt(5, getN_wins());
//			pst.setFloat(6, getAverage_score());
//			pst.setFloat(7,getMax_score());
//			pst.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
//			pst.setString(9, list2string(getAttribute_names()));
//			pst.setFloat(10, getBase_score());
//			pst.executeUpdate();
//			pst.close();
//			conn.connection.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public List<Board> getBoardsByPhenotype(String phenotype){
		List<Board> boards = new ArrayList<Board>();
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery("select * from board where phenotype = '"+phenotype+"' order by base_score desc");
		try {
			while(rslt.next()){
				Board board = new Board();
				board.setAverage_score(rslt.getFloat("average_score"));
				board.setEntrez_ids(string2list(rslt.getString("entrez_ids")));
				board.setGene_symbols(string2list(rslt.getString("gene_symbols")));
				board.setId(rslt.getInt("id"));
				board.setMax_score(rslt.getFloat("max_score"));
				board.setN_players(rslt.getInt("n_players"));
				board.setN_wins(rslt.getInt("n_wins"));
				board.setPhenotype(phenotype);
				board.setUpdated(rslt.getTimestamp("updated"));
				board.setAttribute_names(string2list(rslt.getString("attribute_names")));
				board.setBase_score(rslt.getFloat("base_score"));
				boards.add(board);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return boards;
	}

	public Board getBoardById(String id){
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery("select * from board where id = '"+id+"'");
		try {
			if(rslt.next()){
				Board board = new Board();
				board.setAverage_score(rslt.getFloat("average_score"));
				board.setEntrez_ids(string2list(rslt.getString("entrez_ids")));
				board.setGene_symbols(string2list(rslt.getString("gene_symbols")));
				board.setId(rslt.getInt("id"));
				board.setMax_score(rslt.getFloat("max_score"));
				board.setN_players(rslt.getInt("n_players"));
				board.setN_wins(rslt.getInt("n_wins"));
				board.setPhenotype(phenotype);
				board.setUpdated(rslt.getTimestamp("updated"));
				board.setAttribute_names(string2list(rslt.getString("attribute_names")));
				board.setBase_score(rslt.getFloat("base_score"));
				return board;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPhenotype() {
		return phenotype;
	}
	public void setPhenotype(String phenotype) {
		this.phenotype = phenotype;
	}
	public List<String> getEntrez_ids() {
		return entrez_ids;
	}
	public void setEntrez_ids(List<String> entrez_ids) {
		this.entrez_ids = entrez_ids;
	}
	public List<String> getGene_symbols() {
		return gene_symbols;
	}
	public void setGene_symbols(List<String> gene_symbols) {
		this.gene_symbols = gene_symbols;
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

	public List<String> getAttribute_names() {
		return attribute_names;
	}

	public void setAttribute_names(List<String> attribute_names) {
		this.attribute_names = attribute_names;
	}


	public float getBase_score() {
		return base_score;
	}


	public void setBase_score(float base_score) {
		this.base_score = base_score;
	}
	
	
}
