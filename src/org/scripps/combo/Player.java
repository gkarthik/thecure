/**
 * 
 */
package org.scripps.combo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	//levels unlocked
	//position in the list corresponds to the level index
	//0 unlocked, 1 = 1 star, 2 = 2 stars...
	List<Integer> barney_levels;

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
				player.setBarney_levels(barney_levels);
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
				player.setBarney_levels(barney_levels);
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player;
	}
	
	public static Player create(String name, String ip, String password, String email){
		//first check if username taken
		Player player =  lookupPlayer(name);
		if(player!=null){
			return null;
		}
		//if no player exists make a new one
		JdbcConnection conn = new JdbcConnection();
		String insert = "insert into player values(null,?,?,?,0,0,?)";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, name);
			p.setString(2,ip);
			p.setString(3,password);
			p.setString(4,email);
			p.executeUpdate();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player = lookupPlayer(name);
		return player;
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
	
	
}
