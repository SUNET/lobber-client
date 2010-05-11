package net.nordu.lobber.client.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.pietschy.wizard.PanelWizardStep;

public class LobberUIAUPStep extends PanelWizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5328593224616693106L;
	private static final String aupText = "Do no evil!";
	
	public LobberUIAUPStep() {
		super("Usage conditions","Read and accept the usage policy");
		
		setLayout(new BorderLayout());
		JTextField tAup = new JTextField(aupText, 40);
		add(tAup,BorderLayout.NORTH);
		final JCheckBox cAup = new JCheckBox("I accept these terms");
		add(cAup,BorderLayout.SOUTH);
		
		cAup.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				setComplete(cAup.isSelected());
			}
		});
	}
	
}
