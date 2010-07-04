package net.nordu.lobber.client.applet.wizard;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import net.nordu.lobber.client.applet.SpringUtilities;

import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

import com.toedter.calendar.JDateChooser;

public class LobberUIMetadataStep extends PanelWizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 82550024799062463L;
	private LobberUIWizardModel model;
	private JTextArea tDesc;
	private JDateChooser dateChooser;
	private JCheckBox tRO;
	private SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd");
	
	public void init(WizardModel model) {
		this.model = (LobberUIWizardModel)model;
	}
	
	public LobberUIMetadataStep()  {
		super("Upload metadata", "Provide additional information for your upload");
		setComplete(true);
		setLayout(new SpringLayout());
		
		add(new JLabel("Description"));
		tDesc = new JTextArea(4,30);
		add(tDesc);
		
		final Date now = new Date();
		add(new JLabel("Expiration"));
		dateChooser = new JDateChooser(now,"yyyy-MM-dd");
		add(dateChooser);
		
		add(new JLabel("Allow public access?"));
		tRO = new JCheckBox();
		add(tRO);
		
		SpringUtilities.makeCompactGrid(this, 2, 2, 6, 6, 6, 6);
	}
	
	@Override
	public void applyState() throws InvalidStateException {
		model.setDescription(tDesc.getText());
		model.setExpires(iso.format(dateChooser.getDate()));
		model.setPublicAccess(tRO.isSelected());
		
		if (model.getDescription() != null && model.getDescription().length() > 0)
			setComplete(true);
	}
	
}
