package net.nordu.lobber.client.applet;

import java.awt.EventQueue;
import java.util.Properties;

import net.nordu.lobber.client.applet.wizard.LobberDLWizard;

public class LobberClientApp {

	
	
	public static void main(final String[] args) {
		final Properties p = new Properties();
		
		for (String arg : args) {
			int pos = arg.indexOf('=');
			if (pos > 0) {
				p.put(arg.substring(0,pos), arg.substring(pos+1));
			}
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LobberDLWizard ui = new LobberDLWizard(p);
					ui.launch();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	
}
