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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.scripps.combo.GameLog;
import org.scripps.combo.TimeCounter;
import org.scripps.combo.GameLog.high_score;
import org.scripps.util.JdbcConnection;

/**
 * @author bgood
create table player (id int(10) NOT NULL AUTO_INCREMENT, name varchar(50), password varchar(50), ip varchar(25), top_score int, games_played int, email varchar(100), created date, updated timestamp, degree varchar(12), cancer varchar(5), biologist varchar(5), primary key (id), unique key a1 (name, password)); 
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
	float avg_cards_per_hand;
	Calendar created;
	//The string key corresponds to a game/phenotype like 'dream_breast_cancer'
	//the Map links board_ids to the player's score on that board
	Map<String, Map<Integer,Integer>> dataset_board_scores; 
	//	List<Integer> barney_levels;	

	public Player() {
		dataset_board_scores = new HashMap<String, Map<Integer,Integer>>();
	}

	public static void main(String[] args){
		describePlayers(true, "dream_breast_cancer_2");
	}


	public class RegMonth {
		public Map<String, Integer> degree_count;
		public Map<String, Integer> cancer_knowledge_count;
		public Map<String, Integer> biologist_count;
		public Calendar month;
		public RegMonth() {
			degree_count = new TreeMap<String, Integer>();
			cancer_knowledge_count = new TreeMap<String, Integer>();
			biologist_count = new TreeMap<String, Integer>();
			degree_count.put("phd", 0);degree_count.put("masters", 0);degree_count.put("bachelors", 0);degree_count.put("none", 0);degree_count.put("ns", 0);degree_count.put("other", 0);degree_count.put("md", 0);
			cancer_knowledge_count.put("yes", 0); cancer_knowledge_count.put("no", 0); cancer_knowledge_count.put("ns", 0);
			biologist_count.put("yes", 0); biologist_count.put("no", 0);biologist_count.put("ns", 0);
		}

	}

	public Map<String, RegMonth> getNewPlayersPerMonth(){
		Map<String, RegMonth> month_reg = new TreeMap<String, RegMonth>();
		JdbcConnection conn = new JdbcConnection();
		String q = "select YEAR(created), MONTH(created), degree, count(*) as c from player group by YEAR(created), MONTH(created), degree;";
		ResultSet rslt = conn.executeQuery(q);
		SimpleDateFormat f = new SimpleDateFormat(); f.applyPattern("yyyy.MM");
		try {
			while(rslt.next()){
				int year = rslt.getInt(1);
				int month = rslt.getInt(2);
				String degree = rslt.getString(3);
				int count = rslt.getInt(4);
				Calendar c = Calendar.getInstance();
				c.set(Calendar.YEAR, year);
				c.set(Calendar.MONTH, month-1);
				String key = f.format(c.getTime());
				RegMonth rm = month_reg.get(key);
				if(rm==null){
					rm = new RegMonth();
				}
				rm.month = c;
				rm.degree_count.put(degree, count);
				month_reg.put(key, rm);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		q = "select YEAR(created), MONTH(created), cancer, count(*) as c from player group by YEAR(created), MONTH(created), cancer;";
		conn = new JdbcConnection();
		rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				int year = rslt.getInt(1);
				int month = rslt.getInt(2);
				String cancer = rslt.getString(3);
				int count = rslt.getInt(4);
				Calendar c = Calendar.getInstance();
				c.set(Calendar.YEAR, year);
				c.set(Calendar.MONTH, month-1);
				String key = f.format(c.getTime());
				RegMonth rm = month_reg.get(key);
				if(rm==null){
					rm = new RegMonth();
				}
				rm.month = c;
				rm.cancer_knowledge_count.put(cancer, count);
				month_reg.put(key, rm);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		q = "select YEAR(created), MONTH(created), biologist, count(*) as c from player group by YEAR(created), MONTH(created), biologist;";
		conn = new JdbcConnection();
		rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				int year = rslt.getInt(1);
				int month = rslt.getInt(2);
				String biologist = rslt.getString(3);
				int count = rslt.getInt(4);
				Calendar c = Calendar.getInstance();
				c.set(Calendar.YEAR, year);
				c.set(Calendar.MONTH, month-1);
				String key = f.format(c.getTime());
				RegMonth rm = month_reg.get(key);
				if(rm==null){
					rm = new RegMonth();
				}
				rm.month = c;
				rm.biologist_count.put(biologist, count);
				month_reg.put(key, rm);
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return month_reg;
	}

	public class PlayerSet {
		public Map<String, Integer> degree_count;
		public Map<String, Integer> cancer_count;
		public Map<String, Integer> biologist_count;
	}
	public PlayerSet getGlobalPlayerCounts(){
		PlayerSet ps = new PlayerSet();
		String q = "select degree, count(*) as c from player group by degree";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(q);
		try {
			ps.degree_count = new TreeMap<String, Integer>();
			while(rslt.next()){
				String degree = rslt.getString("degree");
				int c = rslt.getInt("c");
				ps.degree_count.put(degree, c);
			} 
			rslt.close();
			q = "select cancer, count(*) as c from player group by cancer";
			ps.cancer_count = new TreeMap<String, Integer>();
			rslt = conn.executeQuery(q);
			while(rslt.next()){
				String can = rslt.getString("cancer");
				int c = rslt.getInt("c");
				ps.cancer_count.put(can, c);
			} 
			rslt.close();
			q = "select biologist, count(*) as c from player group by biologist";
			ps.biologist_count = new TreeMap<String, Integer>();
			rslt = conn.executeQuery(q);
			while(rslt.next()){
				String can = rslt.getString("biologist");
				int c = rslt.getInt("c");
				ps.biologist_count.put(can, c);
			} 
			rslt.close();
			conn.connection.close();
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ps;
	}

	public static void describePlayers(boolean all_hands, String dataset){
		List<Player> players = Player.getAllPlayers();
		Map<Integer, Player> name_player = Player.playerListToIdMap(players);
		GameLog log = new GameLog();
		List<Game> wm = null;
		if(all_hands){
			wm = Game.getAllGames(false); // //
		}else{//just get the first hand per player per board
			wm = Game.getTheFirstGamePerPlayerPerBoard(false, null, false, 1);
		}
		//remove mammal
		List<Game> mammalhands = new ArrayList<Game>();
		List<Game> hands = new ArrayList<Game>();
		for(Game hand : wm){
			if(hand.getBoard_id()>200&&hand.getBoard_id()<205){
				mammalhands.add(hand);
			}else{
				hands.add(hand);
			}
		}

		Map<String, Float> player_cardsboard = new HashMap<String, Float>();
		for(Player player : players){
			Map<String, Integer> board_counts = Card.getBoardCardCount(player.getId());
			float avg_cards_board = 0;
			for(Entry<String, Integer> board_count : board_counts.entrySet()){
				avg_cards_board+= board_count.getValue();
			}
			avg_cards_board = avg_cards_board/board_counts.size();
			player_cardsboard.put(player.getName(), avg_cards_board);
		}

		System.out.println("name	created	biologist	cancer	degree	max_plus	avg	points	games	win_perct	n_cards_hand	avg_t_per_board	avg_t_card	total_time");
		GameLog.high_score sb = log.getScoreBoard(hands, dataset);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		for(Player player : players){
			String name = player.getName();
			Calendar c = player.getCreated();
			String cdate = formatter.format(c.getTime());
			int id = player.getId();
			if(name_player.get(id)!=null){
				TimeCounter tc = new TimeCounter(""+id);
				System.out.println(name+"\t"+cdate+"\t"+
						player.getBiologist()+"\t"+
						player.getCancer()+"\t"+
						player.getDegree()+"\t"+
						sb.getPlayer_max().get(name)+"\t"
						+sb.getPlayer_avg().get(name)+"\t"+
						sb.getPlayer_global_points().get(name)+"\t"+
						sb.getPlayer_games().get(name)+"\t"+
						sb.getPlayer_avg_win().get(name)+"\t"+
						player_cardsboard.get(name)+"\t"+
						tc.getAvg_time_per_board()+"\t"+
						tc.getAvg_time_per_card()+"\t"+
						tc.getTotal_time()
				);
			}
		}
	}

	public static boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(.\\d+)?");
	}

	public static Player lookupPlayerByName(String name){
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
			p.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player;
	}

	public static Player lookupPlayerById(int id){
		Player player = null;
		JdbcConnection conn = new JdbcConnection();
		String insert = "select * from player where id = ?";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setInt(1, id);
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
			p.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player;
	}

	public static Map<Integer, Player> playerListToIdMap(List<Player> players){
		Map<Integer, Player> name_player = new HashMap<Integer, Player>();
		for(Player p : players){
			name_player.put(p.getId(), p);
		}
		return name_player;
	}
	public static List<Player> getAllPlayers(){
		List<Player> players = new ArrayList<Player>();
		JdbcConnection conn = new JdbcConnection();
		String q= "select * from player";
		try {

			ResultSet r = conn.executeQuery(q);
			while(r.next()){
				Player player = new Player();
				Calendar c = Calendar.getInstance();
				c.setTime(r.getDate("created"));
				player.setCreated(c);
				player.setName(r.getString("name"));
				player.setPassword(r.getString("password"));
				player.setGames_played(r.getInt("games_played"));
				player.setId(r.getInt("id"));
				player.setTop_score(r.getInt("top_score"));
				player.setBiologist(r.getString("biologist"));
				player.setCancer(r.getString("cancer"));
				player.setDegree(r.getString("degree"));	
				player.setEmail(r.getString("email"));
				players.add(player);

			}
			r.close();
			conn.connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return players;
	}

	public static Map<Integer, Player> mapPlayersByDbId(List<Player> players){
		Map<Integer, Player> id_player = new HashMap<Integer, Player>();
		for(Player player : players){
			id_player.put(player.getId(), player);
		}
		return id_player;
	}

	public static Map<String, Player> mapPlayersByName(List<Player> players){
		Map<String, Player> name_player = new HashMap<String, Player>();
		for(Player player : players){
			name_player.put(player.getName(), player);
		}
		return name_player;
	}

	public static List<Player> lookupByEmail(String email){
		List<Player> players = new ArrayList<Player>();
		JdbcConnection conn = new JdbcConnection();
		String insert = "select * from player where email = ?";
		try {
			PreparedStatement p = conn.connection.prepareStatement(insert);
			p.setString(1, email);
			ResultSet r = p.executeQuery();
			while(r.next()){
				Player player = new Player();
				player.setName(r.getString("name"));
				player.setPassword(r.getString("password"));
				player.setGames_played(r.getInt("games_played"));
				player.setId(r.getInt("id"));
				player.setTop_score(r.getInt("top_score"));
				players.add(player);
			}
			r.close();
			conn.connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return players;
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
				if(!r.getString("name").equals(name)){ //this is here because mysql isn't set up to check for case and its screwing up the scoreboard
					return null; 
				}
				player = new Player();
				player.setName(r.getString("name"));
				player.setPassword(r.getString("password"));
				player.setGames_played(r.getInt("games_played"));
				player.setId(r.getInt("id"));
				player.setTop_score(r.getInt("top_score"));

				player.setBoardScoresWithDb();
			}
			r.close();
			conn.connection.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player;
	}

	public static Player create(String name, String ip, String password, String email, String degree, String cancer, String biologist){
		//first check if username taken
		Player player =  lookupPlayerByName(name);
		if(player!=null){
			return null;
		}
		//if no player exists make a new one
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement p = null;		
		String insert = "insert into player (id,name, ip, password, email, created, degree, cancer, biologist) " +
		"values(null,?,?,?,?,?,?,?,?)";
		try {
			p = conn.connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);					
			p.setString(1, name);
			p.setString(2,ip);
			p.setString(3,password);
			p.setString(4,email);
			p.setDate(5, new Date(System.currentTimeMillis()));
			p.setString(6, degree);
			p.setString(7,cancer);
			p.setString(8, biologist);

			int affectedRows = p.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating player failed, no rows affected.");
			}
			generatedKeys = p.getGeneratedKeys();
			if (generatedKeys.next()) {
				newid = generatedKeys.getInt(1);
				player = new Player();
				player.setBiologist(biologist);
				player.setCancer(cancer);
				player.setDegree(degree);
				player.setEmail(email);
				player.setId(newid);
				player.setName(name);
			} else {
				throw new SQLException("Creating player failed, no generated key obtained.");
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (p != null) try { p.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}		
		return player;
	}

	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement p = null;

		String insert = "insert into player (id,name, ip, password, email, created, degree, cancer, biologist) " +
		"values(?,?,?,?,?,?,?,?,?)";
		try {
			p = conn.connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
			if(id>0){
				p.setInt(1, id);
			}else{
				p.setString(1, null);
			}					
			p.setString(2, name);
			p.setString(3,ip);
			p.setString(4,password);
			p.setString(5,email);
			p.setDate(6, new Date(System.currentTimeMillis()));
			p.setString(7, degree);
			p.setString(8,cancer);
			p.setString(9, biologist);

			int affectedRows = p.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating player failed, no rows affected.");
			}
			generatedKeys = p.getGeneratedKeys();
			if (generatedKeys.next()) {
				newid = generatedKeys.getInt(1);
				//use to link up the feature to the attribute ids

			} else {
				throw new SQLException("Creating player failed, no generated key obtained.");
			}
			conn.connection.close();
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (p != null) try { p.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}

	public void setBoardScoresWithDb(){
		//anonymous players don't get to track their scores..
		if(name.equals("anonymous_hero")){
			return;
		}
		String gethands = "select dataset, board_id, p1_score, win from game,board where game.player1_id = ? and game.board_id = board.id";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			p.setInt(1, id);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				String dataset = hands.getString("dataset");
				int board_id = hands.getInt("board_id");
				int score = hands.getInt("p1_score");
				int win = hands.getInt("win");

				if(win==1){
					Map<Integer,Integer> tile_scores = dataset_board_scores.get(dataset);
					if(tile_scores==null){
						tile_scores = new HashMap<Integer,Integer>();
					}
					tile_scores.put(board_id, score);
					dataset_board_scores.put(dataset, tile_scores);
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




	public float getAvg_cards_per_hand() {
		return avg_cards_per_hand;
	}


	public void setAvg_cards_per_hand(float avg_cards_per_hand) {
		this.avg_cards_per_hand = avg_cards_per_hand;
	}


	public Map<String, Map<Integer, Integer>> getDataset_board_scores() {
		return dataset_board_scores;
	}


	public void setDataset_board_scores(
			Map<String, Map<Integer, Integer>> dataset_board_scores) {
		this.dataset_board_scores = dataset_board_scores;
	}

	public Calendar getCreated() {
		return created;
	}

	public void setCreated(Calendar created) {
		this.created = created;
	}







}
