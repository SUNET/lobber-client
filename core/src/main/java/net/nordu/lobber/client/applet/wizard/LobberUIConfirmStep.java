package net.nordu.lobber.client.applet.wizard;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

public class LobberUIConfirmStep extends PanelWizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LobberUIWizardModel model;
	
	public void init(WizardModel model) {
		this.model = (LobberUIWizardModel)model;
	}
	
	public LobberUIConfirmStep()  {
		super("Confirm", "Confirm and start upload");
		setComplete(true);

		setLayout(new BorderLayout());
		JLabel confirmLabel = new JLabel("Click on \"Next\" to start upload...");
		add(confirmLabel,BorderLayout.CENTER);
	}
	
	@Override
	public void applyState() throws InvalidStateException {
	}
	
}
