/**
 * 
 */
package org.scripps.combo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.util.JdbcConnection;

/**
 * @author bgood
 *create table player (id int(10) NOT NULL AUTO_INCREMENT, 
 *name varchar(50), password varchar(50), top_score int, games_played int) primary key (id), unique key a1 (name, password); 
 */
public class Player {
	int id;
	String name;
	String ip;
	String password;
	int top_score;
	int games_played;
	String email;
	String degree;
	String cancer;
	String biologist;

	//The string key corresponds to a game/phenotype like 'dream_breast_cancer'
	//the Map links board_ids to the player's score on that board
	Map<String, Map<Integer,Integer>> phenotype_board_scores; 
	List<Integer> barney_levels;	
	
	
	public Player() {
		phenotype_board_scores = new HashMap<String, Map<Integer,Integer>>();
	}

	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(.\\d+)?");
	}
	
	public static Player lookupPlayer(String name){
		Player player = null;
		JdbcConnection conn = new JdbcConnection();
		String insert = "select * from player where name = ?";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, name);
			ResultSet r = p.executeQuery();
			if(r.next()){
				player = new Player();
				player.setName(r.getString("name"));
				player.setGames_played(r.getInt("games_played"));
				player.setId(r.getInt("id"));
				player.setTop_score(r.getInt("top_score"));
				//for boardroom
				player.setBoardScoresWithDb();
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player;
	}
	
	public Player lookupByUserPassword(){
		Player player = null;
		JdbcConnection conn = new JdbcConnection();
		String insert = "select * from player where name = ? and password = ?";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, name);
			p.setString(2,password);
			ResultSet r = p.executeQuery();
			if(r.next()){
				player = new Player();
				player.setName(r.getString("name"));
				player.setPassword(r.getString("password"));
				player.setGames_played(r.getInt("games_played"));
				player.setId(r.getInt("id"));
				player.setTop_score(r.getInt("top_score"));
				//storing as a comma delimited string in db for now.
				String levels_ = r.getString("barney_levels");
				List<Integer> barney_levels = new ArrayList<Integer>();
				if(levels_!=null){
					String[] levels = levels_.split(",");
					for(String level : levels){
						if(isNumeric(level)){
							barney_levels.add(Integer.parseInt(level));
						}
					}
				}
				//need to get rid of barney level idea..
				//player.setBarney_levels(barney_levels);
				player.setBoardScoresWithDb();
			}
			conn.connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player;
	}
	
	public static Player create(String name, String ip, String password, String email, String degree, String cancer, String biologist){
		//first check if username taken
		Player player =  lookupPlayer(name);
		if(player!=null){
			return null;
		}
		//if no player exists make a new one
		JdbcConnection conn = new JdbcConnection();
		String insert = "insert into player values(null,?,?,?,0,0,?,'',?,?,?,?,?)";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, name);
			p.setString(2,ip);
			p.setString(3,password);
			p.setString(4,email);
			p.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			p.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			p.setString(7, degree);
			p.setString(8,cancer);
			p.setString(9, biologist);
			p.executeUpdate();
			
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player = lookupPlayer(name);
		return player;
	}
	
	public void updateBarneyLevelsInDatabase(){
		//if no player exists make a new one
		JdbcConnection conn = new JdbcConnection();
		String insert = "update player set barney_levels = ?, updated = ? where name = ?";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, barneyLevels2string());
			p.setString(2, name);
			p.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			p.executeUpdate();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String barneyLevels2string(){
		String levels = "";
		if(barney_levels!=null&&barney_levels.size()>0){
			for(Integer i : barney_levels){
				levels+=i.toString()+",";
			}
			levels = levels.substring(0,levels.lastIndexOf(","));
		}
		return levels;
	}
	
	public void setBoardScoresWithDb(){
		//anonymous players don't get to track their scores..
		if(name.equals("anonymous_hero")){
			return;
		}
		String gethands = "select phenotype, game_type, board_id, score, training_accuracy, cv_accuracy, win from hand where player_name = ?";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			p.setString(1, name);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				String phenotype = hands.getString("phenotype");
				String game_type = hands.getString("game_type");
				int board_id = hands.getInt("board_id");
				int score = hands.getInt("score");
				int training = hands.getInt("training_accuracy");
				int cv = hands.getInt("cv_accuracy");
				int win = hands.getInt("win");
				
				if(win>0){
					Map<Integer,Integer> tile_scores = phenotype_board_scores.get(phenotype);
					if(tile_scores==null){
						tile_scores = new HashMap<Integer,Integer>();
					}
					if(training<0){
						training = cv;
					}
					tile_scores.put(board_id, training);
					phenotype_board_scores.put(phenotype, tile_scores);
				}
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getTop_score() {
		return top_score;
	}
	public void setTop_score(int top_score) {
		this.top_score = top_score;
	}
	public int getGames_played() {
		return games_played;
	}
	public void setGames_played(int games_played) {
		this.games_played = games_played;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Integer> getBarney_levels() {
		return barney_levels;
	}

	public void setBarney_levels(List<Integer> barney_levels) {
		this.barney_levels = barney_levels;
	}


	public String getDegree() {
		return degree;
	}


	public void setDegree(String degree) {
		this.degree = degree;
	}


	public String getCancer() {
		return cancer;
	}


	public void setCancer(String cancer) {
		this.cancer = cancer;
	}


	public String getBiologist() {
		return biologist;
	}


	public void setBiologist(String biologist) {
		this.biologist = biologist;
	}


	public Map<String, Map<Integer, Integer>> getPhenotype_board_scores() {
		return phenotype_board_scores;
	}


	public void setPhenotype_board_scores(
			Map<String, Map<Integer, Integer>> phenotype_board_scores) {
		this.phenotype_board_scores = phenotype_board_scores;
	}





	
	
}
