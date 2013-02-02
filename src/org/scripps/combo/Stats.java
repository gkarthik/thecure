/**
 * 
 */
package org.scripps.combo;

import java.util.List;

import org.scripps.combo.Boardroom.boardview;
import org.scripps.combo.GameLog.high_score;
import org.scripps.combo.model.Game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
		List<Game> whs = Game.getTheFirstGamePerPlayerPerBoard(only_winning, null, false);

		//filters
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode json_root = mapper.createObjectNode();
		ArrayNode players = mapper.createArrayNode();	
		
		GameLog log = new GameLog();
		high_score results = log.getScoreBoard(whs);
		
	}

}
