/**
 * 
 */
package org.scripps.combo;

import java.util.List;

import org.scripps.combo.GameLog.high_score;
import org.scripps.combo.model.Game;

/**
 * @author bgood
 *
 */
public class Stats {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean only_winning = true;
		String dataset = "dream_breast_cancer_2";
		List<Game> whs = Game.getTheFirstGamePerPlayerPerBoard(only_winning, dataset, false);

		//filters

		//		ObjectMapper mapper = new ObjectMapper();
		//		ObjectNode json_root = mapper.createObjectNode();
		//		ArrayNode players = mapper.createArrayNode();	

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
