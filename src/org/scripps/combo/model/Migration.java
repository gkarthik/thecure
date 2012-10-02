/**
 * 
 */
package org.scripps.combo.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;

/**
 * @author bgood
 *
 */
public class Migration {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//migrateHandsCure1Cure2();
		//migratePlayersCure1Cure2();
		migrateHandFeatureCure1Cure2();
	}

	public static void removeDuplicates(){
		/*
		 rename table game to game1;
		 create table game (id int(10) NOT NULL AUTO_INCREMENT, board_id int, ip varchar(30), player1_id int, player2_id int, updated timestamp, game_started timestamp, game_finished timestamp, p1_score int, p2_score int, win int, created Date, primary key (id), unique key g1 (board_id, player1_id, game_finished));
		 insert ignore into game select * from game1;
		 >Query OK, 3080 rows affected (0.03 sec)
		 >Records: 3753  Duplicates: 673  Warnings: 0

		 rename table game_player_feature to game_player_feature_1;
		 create table game_player_feature (game_id int, player_id int, feature_id int, unique key gpf1 (game_id, player_id, feature_id));
		 insert ignore into game_player_feature select * from game_player_feature_1;
		 >Query OK, 28454 rows affected (0.14 sec)
		 >Records: 32177  Duplicates: 3723  Warnings: 0

		  delete from game_player_feature where game_id < 4428;
		 */
	}

	public static void migrateCardsCure1Cure2(){	
		//insert into cure2.card select id,user_id,board_id,geneid,display_loc,timestamp from cure.card;
	}
	public static void migrateHandToGame(){
		//insert into game (id, board_id, ip, player1_id, updated, game_finished, p1_score, win, created) select hand.id, board_id, hand.ip, player.id, hand.time, hand.time, cv_accuracy,win,hand.time from cure.hand, cure.player where cure.hand.player_name = player.name;
		//	insert into game_player_feature (game_id, player_id, feature_id) select old_hand_feature.hand_id, game.player1_id, old_hand_feature.feature_id from old_hand_feature, game where game.id = old_hand_feature.hand_id;
	}

	public static void migrateBoardsCure1Cure2(){	

		JdbcConnection conn = new JdbcConnection("127.0.0.1","cure","cure","cure");
		String dataset = "dream_breast_cancer"; 
		String q= "select * from board where phenotype = '"+dataset+"'";
		try {
			ResultSet rslt = conn.executeQuery(q);
			while(rslt.next()){
				Board board = new Board();
				board.setAvg_score(rslt.getFloat("average_score"));
				board.setId(rslt.getInt("id"));
				board.setMax_score(rslt.getFloat("max_score"));
				board.setN_players(rslt.getInt("n_players"));
				board.setN_wins(rslt.getInt("n_wins"));
				board.setDataset(dataset);
				board.setRoom("1");
				board.setUpdated(rslt.getTimestamp("updated"));
				board.setBase_score(rslt.getFloat("base_score"));
				java.sql.Date cdate = new java.sql.Date(rslt.getTimestamp("updated").getTime());				
				board.setCreated(cdate);				
				//diff
				List<String> f_ids = MapFun.string2list(rslt.getString("entrez_ids"), "\t");
				List<Feature> fs = new ArrayList<Feature>();
				for(String f_id : f_ids){
					Feature f = Feature.getByUniqueId(f_id);
					if(f!=null){
						fs.add(f);
					}
				}
				board.setFeatures(fs);
				board.insert();
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void migrateHandFeatureCure1Cure2(){	
		JdbcConnection conn = new JdbcConnection("127.0.0.1","cure","cure","cure");
		String dataset = "dream_breast_cancer"; //"mammal"; won't work
		String q= "select player.id as pid, hand.* from hand, player where phenotype = '"+dataset+"' and player.name = hand.player_name";
		try {
			ResultSet rslt = conn.executeQuery(q);
			while(rslt.next()){
				int hand_id = rslt.getInt("id");
				int player_id = rslt.getInt("pid");
				String features = rslt.getString("feature_names");
				List<String> att_list = MapFun.string2list(features,"\\|");
				JdbcConnection conn_current = new JdbcConnection();
				for(String att : att_list){
					String[] a = att.split(":");
					String unique_id = a[0]; String attname = a[1]; String symbol = a[2];
					Attribute atr = Attribute.getByAttNameDataset(attname, dataset);
					Feature f = Feature.getByUniqueId(unique_id);
					if(f!=null&&atr!=null){
						conn_current.executeUpdate("insert into game_player_feature values("+hand_id+", "+player_id+","+f.getId()+")");
					}else{
						System.out.println("No attribute found for "+att);
					}
				}
				//			//insert in current default db
				//hand.insert();
			}
			rslt.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//	public static void migrateHandsCure1Cure2(){	
	//		JdbcConnection conn = new JdbcConnection("127.0.0.1","cure","cure","cure");
	//		String dataset = "dream_breast_cancer"; //"mammal";
	//		String q= "select * from hand where phenotype = '"+dataset+"'";
	//		try {
	//			ResultSet rslt = conn.executeQuery(q);
	//			while(rslt.next()){
	//				Hand hand = new Hand();
	//				hand.setBoard_id(rslt.getInt("board_id"));
	//				hand.setCv_accuracy(rslt.getInt("cv_accuracy"));				
	//				hand.setId(rslt.getInt("id"));
	//				hand.setIp(rslt.getString("ip"));
	//				hand.setScore(rslt.getInt("score"));
	//				hand.setDataset(rslt.getString("phenotype"));
	//				hand.setTraining_accuracy(rslt.getInt("training_accuracy"));
	//				hand.setWin(rslt.getInt("win"));
	//				//diffs
	//				String name = rslt.getString("player_name");
	//				Player p = Player.lookupPlayerByName(name);
	//				hand.setPlayer_id(p.getId());			
	//				
	//				Timestamp time = rslt.getTimestamp("time");
	//				java.sql.Date cdate = new java.sql.Date(time.getTime());
	//				
	//				hand.setCreated(cdate);
	//				hand.setUpdated(time);			
	//				
	//				String atts = rslt.getString("features");
	//				List<String> att_list = MapFun.string2list(atts,",");
	//				JdbcConnection conn_current = new JdbcConnection();
	//				for(String col : att_list){
	//					ResultSet fs = conn_current.executeQuery("select feature_id from attribute where dataset = '"+dataset+"' and col_index = "+col);
	//					if(fs.next()){
	//						int f_id = fs.getInt("feature_id");
	//						conn_current.executeUpdate("insert into hand_feature values("+hand.getId()+", "+f_id+")");
	//					}
	//				}
	////				//insert in current default db
	//				//hand.insert();
	//			}
	//			rslt.close();
	//			conn.connection.close();
	//		} catch (SQLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	}		
	/**
		 mysql> update game set board_id = 201 where board_id = 0;
Query OK, 194 rows affected (0.02 sec)
Rows matched: 194  Changed: 194  Warnings: 0

mysql> update game set board_id = 202 where board_id = 1;
Query OK, 240 rows affected (0.02 sec)
Rows matched: 240  Changed: 240  Warnings: 0

mysql> update game set board_id = 203 where board_id = 2;
Query OK, 282 rows affected (0.02 sec)
Rows matched: 282  Changed: 282  Warnings: 0

mysql> update game set board_id = 204 where board_id = 3;
Query OK, 178 rows affected (0.01 sec)
Rows matched: 178  Changed: 178  Warnings: 0


	 */

	//}

	public static void migratePlayersCure1Cure2(){	
		JdbcConnection conn = new JdbcConnection("127.0.0.1","cure","cure","cure");
		String q= "select * from player";
		try {
			ResultSet r = conn.executeQuery(q);
			while(r.next()){
				Player player = new Player();
				player.setName(r.getString("name"));
				player.setEmail(r.getString("email"));
				player.setPassword(r.getString("password"));
				player.setGames_played(r.getInt("games_played"));
				player.setId(r.getInt("id"));
				player.setTop_score(r.getInt("top_score"));
				player.setBiologist(r.getString("biologist"));
				player.setCancer(r.getString("cancer"));
				player.setDegree(r.getString("degree"));				
				//insert in current default db
				player.insert();
			}
			r.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
