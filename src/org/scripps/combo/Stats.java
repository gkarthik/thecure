/**
 * 
 */
package org.scripps.combo;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.scripps.combo.GameLog.high_score;
import org.scripps.combo.model.Board;
import org.scripps.combo.model.Game;
import org.scripps.combo.model.Player;
import org.scripps.combo.model.Player.PlayerSet;
import org.scripps.combo.weka.GeneRanker;
import org.scripps.util.JdbcConnection;
import org.scripps.util.MapFun;

/**
 * @author bgood
 *
 */
public class Stats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String output_dir = "/Users/bgood/workspace/aacure/database/stats/";
		generateAllStats(output_dir);
	}

	/**
	 * Track the generation of results data to be used for presentation and publication
	 * 
	 * @param output_dir
	 */
	public static void generateAllStats(String output_dir){
		String outfile = "";
		//////////////////////////////////////////
		/// Time and quantity
		////////////////////////////////////////
		//games
		//		outfile = output_dir+"games_day.txt";
		//		String day_or_month = "day";
		//		outputGamesPerTime(day_or_month, outfile);
		//		outfile = output_dir+"games_month.txt";
		//		day_or_month = "month";
		//		outputGamesPerTime(day_or_month, outfile);
		//players
		//		outfile = output_dir+"players_month";
		//		outputNewPlayersPerTime(outfile);
		//		outfile = output_dir+"global_player_info.txt";
		//		outputGlobalPlayerInfo(outfile);
		//		outfile = output_dir+"player_game_counts.txt";
		//		outputPlayerGames(outfile);
		//				outfile = output_dir+"players/players_all_games.txt";
		//				boolean only_first_per_board = false;
		//				Player.describePlayers(only_first_per_board, outfile, null);
		//				List<String> datasets = getDatasets();				
		//				for(String dataset : datasets){
		//					outfile = output_dir+"players/players_"+dataset+".txt";
		//					Player.describePlayers(only_first_per_board, outfile, dataset);
		//				}
		//				only_first_per_board = true;
		//				outfile = output_dir+"players/players_all_first_games.txt";
		//				Player.describePlayers(only_first_per_board, outfile, null);
		//				for(String dataset : datasets){
		//					outfile = output_dir+"players/players_first_"+dataset+".txt";
		//					Player.describePlayers(only_first_per_board, outfile, dataset);
		//				}
			outfile = output_dir+"players/player_agreeability.txt";
			boolean first_hand_only = true;
			outputPlayerAgreeability(outfile, first_hand_only);
//		outfile = output_dir+"board_consensus.txt";
//		boolean first_hand_only = true;
//		outputBoardConsensus(outfile, first_hand_only);
//		outfile = output_dir+"board_consensus_all_hands.txt";
//		first_hand_only = false;
//		outputBoardConsensus(outfile, first_hand_only);
	}


	public static void outputBoardConsensus(String outfile, boolean first_hand_only){

		boolean drop_mammal = true;
		List<Board> boards = Board.getAllBoards(drop_mammal);
		try {
			FileWriter out = new FileWriter(outfile);
			GeneRanker gr = new GeneRanker();
			out.write("board_id\tmin_freq\tavg_freq\tmax_freq\tn_cards_counted\tplayer_count\tboard_base_score\tdataset\troom\tcreated\n");
			int i = 0;
			for(Board board : boards){
				i++;
				Map<String, GeneRanker.gene_rank> ranks = gr.getBoardConsensus(board.getId(), 0, first_hand_only);
				DescriptiveStatistics freqs = new DescriptiveStatistics();
				DescriptiveStatistics votes = new DescriptiveStatistics();
				float player_count = 0;
				for(GeneRanker.gene_rank rank : ranks.values()){
					freqs.addValue(rank.frequency);
					votes.addValue(rank.votes);
					player_count = rank.players; //inelegant to keep setting, but same for all players..
				}
				String row = board.getId()+"\t"+freqs.getMin()+"\t"+freqs.getMean()+"\t"+freqs.getMax()+"\t"+votes.getSum()+"\t"+player_count;
					   row+= "\t"+board.getBase_score()+"\t"+board.getDataset()+"\t"+board.getRoom()+"\t"+board.getUpdated();
					   row = row.replaceAll("NaN", "0");
				out.write(row+"\n");
				System.out.println(i+"\t"+row);
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Quantify how similar the player's gene selections are to the community consensus
	 * @param outfile
	 */
	public static void outputPlayerAgreeability(String outfile, boolean first_hand_only){
		List<Player> players = Player.getAllPlayers();
		
		try {
			FileWriter out = new FileWriter(outfile);
			out.write("player.getId()\tplayer.getName()\tplayer.getBiologist()\tplayer.getCancer()\tplayer.getDegree()\tpc.getN()\tpc.getMean()\tpc.getPercentile(50)\n");
			int i = 0;
			DescriptiveStatistics with_cancer_knowledge = new DescriptiveStatistics();
			DescriptiveStatistics without_cancer_knowledge = new DescriptiveStatistics();
			DescriptiveStatistics with_phd = new DescriptiveStatistics();
			DescriptiveStatistics without_phd = new DescriptiveStatistics();
			for(Player player : players){
				i++;
				DescriptiveStatistics pc = Player.measurePCscore(player.getId(), first_hand_only);
				out.write(player.getId()+"\t"+player.getName()+"\t"+player.getBiologist()+"\t"+player.getCancer()+"\t"+player.getDegree()+"\t");
				out.write(pc.getN()+"\t"+pc.getMean()+"\t"+pc.getPercentile(50)+"\n");
				System.out.println(i);
				if(pc.getN()>0){
					if(player.getCancer().equals("yes")){
						with_cancer_knowledge.addValue(pc.getPercentile(50));
					}else{
						without_cancer_knowledge.addValue(pc.getPercentile(50));
					}
					if(player.getDegree().equals("phd")){
						with_phd.addValue(pc.getPercentile(50));
					}else{
						without_phd.addValue(pc.getPercentile(50));
					}
				}
			}
			double know_cancer_diff_agree_t = TestUtils.tTest(without_cancer_knowledge, with_cancer_knowledge);
			double phd_diff_agree_t = TestUtils.tTest(without_phd, with_phd);
			
			MannWhitneyUTest mw = new MannWhitneyUTest();
			double know_cancer_diff_agree_w = mw.mannWhitneyUTest(without_cancer_knowledge.getSortedValues(), with_cancer_knowledge.getSortedValues());
			double phd_diff_agree_w = mw.mannWhitneyUTest(without_phd.getSortedValues(), with_phd.getSortedValues());
			System.out.println("t for cancer knowledge and agreeability:"+know_cancer_diff_agree_t+" " +
					"MW for cancer knowledge and agreeability:"+know_cancer_diff_agree_w+" " +
					" mean know: "+with_cancer_knowledge.getMean()+" "+with_cancer_knowledge.getN()+" "+
					" mean don't know: "+without_cancer_knowledge.getMean()+" "+without_cancer_knowledge.getN());
			System.out.println("t for phd and agreeability:"+phd_diff_agree_t +" "+
					"w for phd and agreeability:"+phd_diff_agree_w +
					" mean phd: "+with_phd.getMean()+" "+with_phd.getN()+" "+
					" mean without phd: "+without_phd.getMean()+" "+without_phd.getN());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static List<String> getDatasets(){
		List<String> datasets = new ArrayList<String>();
		String q = "select distinct dataset from board";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(q);
		try {
			while(rslt.next()){
				String d = rslt.getString("dataset");
				datasets.add(d);
			} 
			rslt.close();
			conn.connection.close();	
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datasets;
	}

	public static void outputPlayerGames(String outfile){
		boolean only_winning = false;
		String dataset = null;//gets all of them
		List<Game> hands = Game.getAllGames(only_winning, dataset);
		GameLog log = new GameLog();
		GameLog.high_score sb = log.getScoreBoard(hands, dataset);

		try {
			FileWriter out = new FileWriter(outfile);
			int i = 0;
			for(Entry<String, Integer> p_games : sb.player_games.entrySet()){
				i++;
				if(i<10){
					System.out.println(p_games.getKey()+"\t"+p_games.getValue()+"\t");
				}
				out.write(p_games.getKey()+"\t"+p_games.getValue()+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputGlobalPlayerInfo(String outfile){
		try {
			FileWriter out = new FileWriter(outfile);
			Player p = new Player();
			PlayerSet ps = p.getGlobalPlayerCounts();
			Map<String, Float> degree_pct = MapFun.convertCountsToPercentages(ps.degree_count);
			for(Entry<String, Integer> degree_c : ps.degree_count.entrySet()){
				System.out.println(degree_c.getKey()+"\t"+degree_c.getValue()+"\t"+degree_pct.get(degree_c.getKey()));
				out.write(degree_c.getKey()+"\t"+degree_c.getValue()+"\n");
			}
			Map<String, Float> bio_pct = MapFun.convertCountsToPercentages(ps.biologist_count);
			System.out.println("\nBiologist?");
			for(Entry<String, Integer> bio_c : ps.biologist_count.entrySet()){
				System.out.println(bio_c.getKey()+"\t"+bio_c.getValue()+"\t"+bio_pct.get(bio_c.getKey()));
				out.write("\nBiologist?\n"+bio_c.getKey()+"\t"+bio_c.getValue()+"\n");
			}
			Map<String, Float> cancer_pct = MapFun.convertCountsToPercentages(ps.cancer_count);
			System.out.println("\nKnow about cancer?");
			for(Entry<String, Integer> cancer_c : ps.cancer_count.entrySet()){
				System.out.println(cancer_c.getKey()+"\t"+cancer_c.getValue()+"\t"+cancer_pct.get(cancer_c.getKey()));
				out.write("\nKnow about cancer?\n"+cancer_c.getKey()+"\t"+cancer_c.getValue()+"\n");
			}
			//
			System.out.println("\nTarget Audience");
			for(Entry<String, Integer> target_c : ps.target_audience_count.entrySet()){
				System.out.println(target_c.getKey()+"\t"+target_c.getValue());
				out.write("\nTarget\n"+target_c.getKey()+"\t"+target_c.getValue()+"\n");
			}			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputNewPlayersPerTime(String outfile){
		Player p = new Player();
		Map<String, Player.RegMonth> day_count = p.getNewPlayersPerMonth();
		SimpleDateFormat f = new SimpleDateFormat();
		try {
			//by degree
			FileWriter out = new FileWriter(outfile+"_degree.txt");
			System.out.println("Month\tPhD\tMD\tMSc\tBA\tnone\tDid not state\tOther");
			out.write("Month\tPhD\tMD\tMSc\tBA\tnone\tDid not state\tOther\n");
			for(Player.RegMonth month : day_count.values()){
				String output = f.format(month.month.getTime())+"\t"+month.degree_count.get("phd")+"\t"+month.degree_count.get("md")+"\t"+month.degree_count.get("masters")+"\t"+month.degree_count.get("bachelors")+"\t"+month.degree_count.get("none")+"\t"+month.degree_count.get("ns")+"\t"+month.degree_count.get("other");
				System.out.println(output);
				out.write(output+"\n");
			}
			out.close();
			//by cancer knowledge
			out = new FileWriter(outfile+"_cancerknowledge.txt");
			System.out.println("Month\tKnow about cancer\tDo not know about cancer\tDid not state");
			out.write("\nMonth\tKnow about cancer\tDo not know about cancer\tDid not state");
			for(Player.RegMonth month : day_count.values()){
				String output = f.format(month.month.getTime())+"\t"+month.cancer_knowledge_count.get("yes")+"\t"+month.cancer_knowledge_count.get("no")+"\t"+month.cancer_knowledge_count.get("ns");
				System.out.println(output);
				out.write(output+"\n");
			}
			out.close();
			//by biologist
			out = new FileWriter(outfile+"_biologist.txt");
			System.out.println("Month\tBiologist\tNot a biologist\tDid not state");
			out.write("\nMonth\tBiologist\tNot a biologist\tDid not state\n");
			for(Player.RegMonth month : day_count.values()){
				String output = f.format(month.month.getTime())+"\t"+month.biologist_count.get("yes")+"\t"+month.biologist_count.get("no")+"\t"+month.biologist_count.get("ns");
				System.out.println(output);
				out.write(output+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void outputGamesPerTime(String day_or_month, String outfile){
		boolean only_winning = false;
		Map<Calendar, Integer> day_count = Game.getGamesPerTime(only_winning, day_or_month);
		SimpleDateFormat f = new SimpleDateFormat();
		try {
			FileWriter out = new FileWriter(outfile);
			System.out.println(day_or_month+"\tGames");
			out.write(day_or_month+"\tGames\n");
			for(Entry<Calendar, Integer> dc : day_count.entrySet()){
				System.out.println(f.format(dc.getKey().getTime())+"\t"+dc.getValue());
				out.write(f.format(dc.getKey().getTime())+"\t"+dc.getValue()+"\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printDatasetScoreboard(String dataset, boolean only_winning){
		List<Game> whs = Game.getTheFirstGamePerPlayerPerBoard(only_winning, dataset, false, 0);
		//get a scoreboard
		GameLog log = new GameLog();
		high_score sb = log.getScoreBoard(whs, dataset);
		int r = 0;
		for(String name : sb.getPlayer_global_points().keySet()){
			r++;
			String displayName = name;
			if(name == null || name.length() == 0) {
				displayName = "anon";
			}
			if(name.length() > 14) {
				displayName = name.substring(0, 13);
			}
			System.out.println("<li>");    
			System.out.println("<span class=\"rank\">"+r+"</span>");
			System.out.println("<span class=\"player\">"+displayName+"</span>");
			System.out.println("<span class=\"max\">"+sb.getPlayer_max().get(name)+"</span>");
			System.out.println("<span class=\"avg\">"+sb.getPlayer_avg().get(name)+"</span>");
			System.out.println("<span class=\"games\">"+sb.getPlayer_games().get(name)+"</span>");
			System.out.println("<span class=\"points\">"+sb.getPlayer_global_points().get(name)+"</span>");
			System.out.println("</li>");
		}
	}

}
