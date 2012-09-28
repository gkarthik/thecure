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
		//List<Player> players = Player.getAllPlayers();
		String message = "testing 123";
		String subject = "the subject";	
		String from_email = "bgood@scripps.edu";
		String from_name = "Benjamin Good (The Cure Game)";
		String to_name = "ben";
		String to_email = "ben.mcgee.good@gmail.com";
		mail.sendMail(message, subject, from_email, from_name, to_name, to_email);

	}

}
