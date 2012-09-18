/**
 * 
 */
package org.scripps.combo;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * @author bgood
 *
 */
public class TimeCounter {

	float avg_time_per_card = 0;
	float avg_time_per_board = 0;
	float total_time = 0;
	float max_per_card = 0;
	static int max_timeout = 300000; //5 minute max
	
	public TimeCounter(String user_id){
		setForUser(user_id);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimeCounter tc = new TimeCounter("54");
		System.out.println("total time\t"+tc.total_time+"\tavg_time_card\t"+tc.avg_time_per_card+"\tavg_time_board\t"+tc.avg_time_per_board);
	}

	/**
	 * Calculate the time this user has spent playing the cure
	 * @param user_id
	 */
	public void setForUser(String user_id){
		List<Card> cards = Card.getAllPlayedCards(user_id);
		Calendar t_prev = Calendar.getInstance();
		int i = 0; long delta = 0; int c = 0;
		String prev_board = cards.get(0).getBoard_id();
		int n_boards = 1;
		for(Card card : cards){
			String board_id = card.getBoard_id();
			Calendar t = Calendar.getInstance();
			t.setTime(card.getTimestamp());
			
			//if we are on the same board and not on the same card count the delta
			if(board_id.equals(prev_board)&&c>0){
				delta = (t.getTimeInMillis() - t_prev.getTimeInMillis())/1000;
				i++;
				if(delta!=0&&delta<max_timeout){ //sometimes multiple cards get stored associated with the same gene and one click - ignore
					//System.out.println(i+"\t"+card.getBoard_id()+"\t"+delta);
					total_time += delta;
					if(max_per_card<delta){
						max_per_card = delta;
					}
				}
			}else{ // new board
				i = 0;
				prev_board = board_id;
				n_boards++;
			}			
			t_prev = t;			
			c++;
		}
		avg_time_per_card = total_time/cards.size();
		avg_time_per_board = total_time/n_boards;
	}

	public float getAvg_time_per_card() {
		return avg_time_per_card;
	}

	public void setAvg_time_per_card(float avg_time_per_card) {
		this.avg_time_per_card = avg_time_per_card;
	}

	public float getAvg_time_per_board() {
		return avg_time_per_board;
	}

	public void setAvg_time_per_board(float avg_time_per_board) {
		this.avg_time_per_board = avg_time_per_board;
	}

	public float getTotal_time() {
		return total_time;
	}

	public void setTotal_time(float total_time) {
		this.total_time = total_time;
	}

	public float getMax_per_card() {
		return max_per_card;
	}

	public void setMax_per_card(float max_per_card) {
		this.max_per_card = max_per_card;
	}
	
}
