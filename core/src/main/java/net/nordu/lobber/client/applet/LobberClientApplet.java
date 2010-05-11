package net.nordu.lobber.client.applet;
import java.awt.Color;
import java.io.PrintStream;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;

public class LobberClientApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3328873182884573174L;
	private void createGUI() {
		setContentPane(new LobberUI());
		getContentPane().setVisible(true);
	}

	@Override
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					createGUI();
				}
			});
		} catch (Throwable e) { 
			e.printStackTrace(new PrintStream(System.err));
			System.err.println("createGUI didn't successfully complete");
		}

	}


	public class LobberUI extends JPanel implements WizardListener {
		
		/**
		 * 
		 */

		private static final long serialVersionUID = -7301588955270617709L;
		private Wizard wizard;


		public LobberUI() {
			
			final boolean needsAUP = false;
			
			setBackground(Color.WHITE);
			
			LobberUIWizardModel model = new LobberUIWizardModel();
			if (needsAUP)
				model.add(new LobberUIAUPStep());
			
			model.add(new LobberUISelectFileStep());
			model.add(new LobberUIMetadataStep());
			model.add(new LobberUIConfirmStep());
			model.add(new LobberUIProgessStep());
			
			model.setTracker(getParameter("tracker"));
			model.setExpires(getParameter("expires"));
			model.setSessionid(getParameter("sessionid"));
			model.setApiurl(getParameter("apiurl"));
			model.setDescription(getParameter("description"));
			
			wizard = new Wizard(model);
			wizard.setOverviewVisible(true);
			wizard.addWizardListener(this);
			wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
			wizard.showInFrame("Lobber upload wizard");
		}

		public void wizardCancelled(WizardEvent event) {
			stop();
		}

		public void wizardClosed(WizardEvent event) {
			stop();
		}

	}

	public String getAppletInfo() {
		return "Title: Lobber Client\n"
		+ "Author: Leif Johansson\n"
		+ "A simple bittorrent client for lobber.";
	}

	public String[][] getParameterInfo() {
		String[][] info = {
				{"tracker", "string", "the tracker url"}
		};
		return info;
	}

}
