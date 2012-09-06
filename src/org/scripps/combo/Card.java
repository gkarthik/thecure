/**
 * 
 */
package org.scripps.combo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.card;
import org.scripps.util.JdbcConnection;

/**
 * @author bgood
 * create table card (id int(10) NOT NULL AUTO_INCREMENT, username varchar(25), user_id int, phenotype varchar(50), board_id int, att_index int, att_name varchar(50), name varchar(50), geneid varchar(50), power float, display_loc int, timestamp timestamp, primary key (id));
 */
public class Card {
	String db_id;
	String username;
	String user_id;
	String phenotype;
	String board_id;
	int att_index;
	String att_name;
	String name;
	String unique_id;
	float power;
	int display_loc;
	Timestamp timestamp;

	
	public Card(String username, String user_id, String phenotype,
			String board_id, int att_index, String att_name, String name,
			String unique_id, float power, int display_loc, Timestamp timestamp) {
		super();
		this.username = username;
		this.user_id = user_id;
		this.phenotype = phenotype;
		this.board_id = board_id;
		this.att_index = att_index;
		this.att_name = att_name;
		this.name = name;
		this.unique_id = unique_id;
		this.power = power;
		this.display_loc = display_loc;
		this.timestamp = timestamp;
	}

	public Card(card c, String username, String user_id, String phenotype,
			String board_id){
		super();
		this.username = username;
		this.user_id = user_id+"";
		this.phenotype = phenotype;
		this.board_id = board_id+"";
		this.att_index = c.att_index;
		this.att_name = c.att_name;
		this.name = c.name;
		this.unique_id = c.unique_id;
		this.power = c.power;
		this.display_loc = c.display_loc;
	}
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String train_file = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff" ;
		String metadatafile = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/id_map.txt"; 
		Weka weka = new Weka(train_file);
		weka.loadMetadata(new FileInputStream(metadatafile));
		List<card> cards = weka.getGeneid_cards().get("6505");
		for(card c : cards){
			Card tosave = new Card(c, "bgood", "1", "test_pheno", "567");
			tosave.save();
		}
	}

	public void save(){
		//if no player exists make a new one
		JdbcConnection conn = new JdbcConnection();
		String insert = "insert into card values(null,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, username);
			p.setString(2,user_id);
			p.setString(3,phenotype);
			p.setString(4,board_id);
			p.setInt(5, att_index);
			p.setString(6, att_name); //probe name
			p.setString(7, name); //gene name
			p.setString(8, unique_id); //gene id
			p.setFloat(9, power);
			p.setInt(10, display_loc); 
			p.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
			p.executeUpdate();
			
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getUser_id() {
		return user_id;
	}


	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}


	public String getPhenotype() {
		return phenotype;
	}


	public void setPhenotype(String phenotype) {
		this.phenotype = phenotype;
	}


	public String getBoard_id() {
		return board_id;
	}


	public void setBoard_id(String board_id) {
		this.board_id = board_id;
	}


	public int getAtt_index() {
		return att_index;
	}


	public void setAtt_index(int att_index) {
		this.att_index = att_index;
	}


	public String getAtt_name() {
		return att_name;
	}


	public void setAtt_name(String att_name) {
		this.att_name = att_name;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getUnique_id() {
		return unique_id;
	}


	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}


	public float getPower() {
		return power;
	}


	public void setPower(float power) {
		this.power = power;
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

	
	
}
