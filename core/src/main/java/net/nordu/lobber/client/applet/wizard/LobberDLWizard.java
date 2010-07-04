package net.nordu.lobber.client.applet.wizard;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.Properties;

import javax.swing.JPanel;


import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;

public class LobberDLWizard extends JPanel implements WizardListener {
		
		/**
		 * 
		 */

		private static final long serialVersionUID = -7301588955270617709L;
		private Wizard wizard;
		private LobberUIWizardModel model;

		public LobberDLWizard(Properties p) {
			
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

		public void launch() {
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
		
}
