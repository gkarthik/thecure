/**
 * 
 */
package org.scripps.combo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Collect and represent the information needed to generate the integrated 'boardroom' view.  Show player/community state for each board.
 * @author bgood
 *
 */
public class Boardroom {
	List<boardview> boardviews;
	ObjectMapper mapper;
	ObjectNode json_root;
	
	Boardroom(){
		boardviews = new ArrayList<boardview>();
		mapper = new ObjectMapper();
		json_root = mapper.createObjectNode();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Boardroom b = new Boardroom();
		b.buildBoardView("bgood", "dream_breast_cancer");
		String j = b.renderjsonBoardViews();
		System.out.println(j);
	}
	
	public String renderjsonBoardViews(){
		String json = "";
		if(boardviews==null||boardviews.size()==0){
			return json;
		}
		ArrayNode views = mapper.createArrayNode();	
		for(boardview view : getBoardviews()){
			ObjectNode v = mapper.createObjectNode();
			v.put("position", view.getPosition());
			v.put("attempts", view.getAttempts());
			v.put("enabled", view.isEnabled());
			v.put("trophy", view.isTrophy());
			v.put("board_id", view.getBoard().getId());
			v.put("base_score", view.getBoard().getBase_score());
			v.put("max_score", view.getMax_score());
			v.put("avg_win_score", view.getAvg_win_score());
			v.put("player_score", view.getPlayer_score());
			views.add(v);
		}
		json_root.put("boards", views);
		try {
			json = mapper.writeValueAsString(json_root);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	
	/**
	 * Produce the data to generate a player-specific view of the board collection
	 * @param player_name
	 * @param phenotype
	 */
	public void buildBoardView(String player_name, String phenotype){
		Player player = Player.lookupPlayer(player_name);
		if (player == null) {
			return;
		} 
		Board control = new Board();
		List<Board> boards = control.getBoardsByPhenotype(phenotype); //"dream_breast_cancer"
		Map<Integer,Integer> player_board_scores = player.getPhenotype_board_scores().get(phenotype);
		
		int position = 0;
		for(Board board : boards){
			boardview view = new boardview(board, position);
			int b_id = board.getId();
			int base_score = (int)board.getBase_score();
			//player
			boolean player_won_level = false;
			Integer player_score = null;
			if(player_board_scores!=null){
				player_score = player_board_scores.get(b_id);									
				if(player_score!=null){
					player_won_level = true;
				}
			}
			//community
			boolean anyone_won_level = false;
			int max_score = 0;
			boolean beat_base = false;
			List<Integer> all_scores = control.getBoardScoresfromDb(b_id);
			float avg_score = 0;
			int attempts = 0;
			if(all_scores!=null&&all_scores.size()>0){
				attempts = all_scores.size();
				anyone_won_level = true;
				for(Integer s : all_scores){
					if(s > max_score){
						max_score = s;
					}
					avg_score+=s;
				}
				avg_score = avg_score/all_scores.size();
			}
			if(max_score > base_score){
				beat_base = true;
			}
			
			if((attempts > 9)||(player_won_level)){
				view.setEnabled(false);
			}
			view.setAttempts(attempts);
			if(player_won_level){
				view.setTrophy(true);
				view.setPlayer_score(player_score);
			}
			view.setAvg_win_score(avg_score);
			view.setMax_score(max_score);
			this.getBoardviews().add(view);
			position++;
		}
	}
	
	
	public class boardview {
		Board board;
		int position;
		int attempts; //barney defeats
		boolean enabled; //if player has not already defeated barney for this board and 
		boolean trophy;
		int player_score;
		float avg_win_score; // all wins
		int max_score;
				
		public boardview(Board board, int position) {
			super();
			this.board = board;
			this.position = position;
			this.enabled = true;
			this.trophy = false;
		}
		
		public int getPosition() {
			return position;
		}
		public void setPosition(int position) {
			this.position = position;
		}
		public int getAttempts() {
			return attempts;
		}
		public void setAttempts(int attempts) {
			this.attempts = attempts;
		}
		public boolean isEnabled() {
			return enabled;
		}
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		public boolean isTrophy() {
			return trophy;
		}
		public void setTrophy(boolean trophy) {
			this.trophy = trophy;
		}
		public Board getBoard() {
			return board;
		}
		public void setBoard(Board board) {
			this.board = board;
		}

		public int getPlayer_score() {
			return player_score;
		}

		public void setPlayer_score(int player_score) {
			this.player_score = player_score;
		}

		public float getAvg_win_score() {
			return avg_win_score;
		}

		public void setAvg_win_score(float avg_score) {
			this.avg_win_score = avg_score;
		}

		public int getMax_score() {
			return max_score;
		}

		public void setMax_score(int max_score) {
			this.max_score = max_score;
		}
		
	}


	public List<boardview> getBoardviews() {
		return boardviews;
	}
	public void setBoardviews(List<boardview> boardviews) {
		this.boardviews = boardviews;
	}

}
