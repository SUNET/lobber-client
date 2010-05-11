package net.nordu.lobber.client.applet;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Properties;

import javax.swing.JPanel;

import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;

public class LobberClientApp extends JPanel implements WizardListener {
		
		/**
		 * 
		 */

		private static final long serialVersionUID = -7301588955270617709L;
		private Wizard wizard;
		private LobberUIWizardModel model;

		public LobberClientApp(Properties p) {
			
			final boolean needsAUP = false;
			
			setBackground(Color.WHITE);
			
			model = new LobberUIWizardModel();
			if (needsAUP)
				model.add(new LobberUIAUPStep());
			
			model.add(new LobberUISelectFileStep());
			model.add(new LobberUIMetadataStep());
			model.add(new LobberUIConfirmStep());
			model.add(new LobberUIProgessStep());
			
			model.setTracker(p.getProperty("tracker"));
			model.setExpires(p.getProperty("expires"));
			model.setSessionid(p.getProperty("sessionid"));
			model.setApiurl(p.getProperty("apiurl"));
			model.setDescription(p.getProperty("description"));
		}

		protected void launch() {
			wizard = new Wizard(model);
			wizard.setOverviewVisible(true);
			wizard.addWizardListener(this);
			wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
			wizard.showInFrame("Lobber upload wizard");
		}
		
		public void wizardCancelled(WizardEvent event) {
			System.exit(1);
		}

		public void wizardClosed(WizardEvent event) {
			System.exit(0);
		}

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
						LobberClientApp ui = new LobberClientApp(p);
						ui.launch();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		
}
