/**
 * 
 */
package org.scripps.combo;

/**
 * @author bgood
 *create table game (id int(10) NOT NULL AUTO_INCREMENT, player_id varchar(50), 
 *ip varchar(25), score int, features varchar(100), primary key (id));

 */
public class Game {
	int id;
	int player_id;
	String ip;
	int score;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPlayer_id() {
		return player_id;
	}
	public void setPlayer_id(int player_id) {
		this.player_id = player_id;
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
	
	
	
}
