/**
 * 
 */
package org.scripps.combo;

import java.util.List;

import org.scripps.combo.model.Player;
import org.scripps.util.Mail;

/**
 * @author bgood
 *
 */
public class Spam {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Mail mail = new Mail("/props/EmailCredentials.properties");
		List<Player> players = Player.getAllPlayers();
		String message = "Thank you very much for registering to play The Cure!  " +
		"The first few weeks have been very exciting.  " +
		"We have had many players get involved, have lots of fun and contribute useful data.  " +
		"We have just released a new collection of boards that use a new version of the breast " +
		"cancer dataset.  Please give it a try!   " +
		"We only have a few weeks left to win the SAGE competition!\n\n Thanks very much, " +
		"\n\nhttp://genegames.org/cure/" +
		"\n\nBenjamin Good" +
		"\n\np.s. Check out the much improved new gene annotation interface!";
		String subject = "New boards at The Cure";	
		String from_email = "bgood@scripps.edu";
		String from_name = "Benjamin Good (The Cure Game)";
		String to_name = "";
		String to_email = "ben.mcgee.good@gmail.com";
		int c = 0;
		for(Player player : players){
			to_email = player.getEmail();
			//System.out.println(to_email);
			if(to_email!=null&&to_email.length()>3){
				c++;
				System.out.println(c+"\t"+to_email);
				if(c>13){
					if(to_email.contains("@")){
						try{
							mail.sendMail(message, subject, from_email, from_name, to_name, to_email);
						}catch(Exception e){
							System.err.print(e.getMessage());
						}
					}else{
						System.out.println(c+"\t"+to_email+"\tprobprobprobrob");
					}
				}
			}
		}
	}

}
