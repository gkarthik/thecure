/**
 * 
 */
package org.scripps.combo.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.combo.weka.Weka;
import org.scripps.util.JdbcConnection;

/**
 * @author bgood
 * create table card (id int(10) NOT NULL AUTO_INCREMENT, user_id int, board_id int, unique_id varchar(50), display_loc int, timestamp timestamp, primary key (id));
 */
public class Card {
	String id;
	String user_id;
	String board_id;
	String unique_id;
	int display_loc;
	Timestamp timestamp;


	public Card(String user_id, String board_id, String unique_id, int display_loc){
		super();
		this.user_id = user_id+"";
		this.board_id = board_id+"";
		this.unique_id = unique_id;
		this.display_loc = display_loc;
	}
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {

	}

	public void insert(){
		JdbcConnection conn = new JdbcConnection();
		String insert = "insert into card (id, user_id, board_id, unique_id, display_loc) values(null,?,?,?,?)";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, user_id);
			p.setString(2, board_id);
			p.setString(3,unique_id);
			p.setInt(4,display_loc);
			p.executeUpdate();
			p.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public static double getUniqueIdNovelty(List<String> unique_id){
		double nov = 1;
		//select count(*) from card where unique_id = 2261 or unique_id = 1717 or unique_id = 9135;
		JdbcConnection conn = new JdbcConnection();
		String q = "select count(*) as total from card";
		String q2 = "select count(*) as n from card where ";
		for(String uid : unique_id){
			q2 += " unique_id = '"+uid+"' or ";// quotes('') for unique ids of clinical features like 'metabric_clinical_5'
		}
		q2 = q2.substring(0,q2.length()-3);
		double base = 1; double n = 1;
		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				base = rslt.getDouble("total");
			}
			rslt.close();
			rslt = conn.executeQuery(q2);
			if(rslt.next()){//returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
				n = rslt.getDouble("n");
			}
			if(base>0 && n > 0){
				nov = (1 - Math.log(n)/Math.log(base));
			}else if(base == 0 && n == 0){//With this condition, novelty = Infinity error resolved.
				nov = 1; //First time card used.
			}
			
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nov;
	}
	
	public static List<Card> getAllPlayedCards(String user_id, String dataset){
		List<Card> cards = new ArrayList<Card>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from card where user_id = '"+user_id+"'";
		if(dataset!=null){
			q = "select card.* from card, board where user_id = '"+user_id+"' and card.board_id = board.id and board.dataset = '"+dataset+"'";
		}
		
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Card card = new Card(
						rslt.getString("user_id"),
						rslt.getString("board_id"), 
						rslt.getString("unique_id"),
						rslt.getInt("display_loc"));
				card.setTimestamp(rslt.getTimestamp("timestamp"));
				card.setId(rslt.getString("id"));
				cards.add(card);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cards;
	}
	
	public static List<Card> getAllPlayedCards(){
		List<Card> cards = new ArrayList<Card>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from card ";

		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				Card card = new Card(
						rslt.getString("user_id"),
						rslt.getString("board_id"), 
						rslt.getString("unique_id"),
						rslt.getInt("display_loc"));
				card.setTimestamp(rslt.getTimestamp("timestamp"));
				card.setId(rslt.getString("id"));
				cards.add(card);
				cards.add(card);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cards;
	}
	
	public static Map<String, Integer> getBoardCardCount(int user_id, String dataset ){
		Map<String, Integer> board_count = new HashMap<String, Integer>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select board_id, count(*) from card where user_id = '"+user_id+"' group by user_id, board_id";
		if(dataset!=null){
			q = "select board_id, count(card.id) from card, board " +
				"where user_id = '"+user_id+"' " +
				"and card.board_id = board.id " +
				"and board.dataset = '"+dataset+"' " +
				"group by user_id, board_id";
		}
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				String board_id = rslt.getString(1);
				int c = rslt.getInt(2);
				board_count.put(board_id,c);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return board_count;
	}

	public String getUser_id() {
		return user_id;
	}


	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getBoard_id() {
		return board_id;
	}


	public void setBoard_id(String board_id) {
		this.board_id = board_id;
	}

	public String getUnique_id() {
		return unique_id;
	}


	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	public int getDisplay_loc() {
		return display_loc;
	}


	public void setDisplay_loc(int display_loc) {
		this.display_loc = display_loc;
	}


	public Timestamp getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	
	
}
