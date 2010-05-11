package net.nordu.lobber.client.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

public class LobberUISelectFileStep extends PanelWizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9005315970633674854L;
	private LobberUIWizardModel model;
	
	private final JTextField tFile;
	private File file;
	private final JTextField tName;
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public LobberUISelectFileStep() {
		super("Select data","Select a file or directory to upload");
		
		JPanel rootPanel = new JPanel();
		
		rootPanel.setLayout(new SpringLayout());
		JLabel lFile = new JLabel("File:",JLabel.TRAILING);
		rootPanel.add(lFile);
		
		JPanel fileSelectorPanel = new JPanel();
		fileSelectorPanel.setLayout(new BorderLayout());
		tFile = new JTextField(30);
		tFile.setEditable(false);
		fileSelectorPanel.add(tFile,BorderLayout.WEST);
		JButton bFile = new JButton("...");
		fileSelectorPanel.add(bFile,BorderLayout.EAST);
		
		rootPanel.add(fileSelectorPanel);
		
		JLabel lName = new JLabel("Name:",JLabel.TRAILING);
		rootPanel.add(lName);
		tName = new JTextField(30);
		rootPanel.add(tName);
		
		bFile.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int rc = fileChooser.showOpenDialog(getView());
				if (rc == JFileChooser.APPROVE_OPTION) {
					setFile(fileChooser.getSelectedFile());
					tFile.setText(file.getAbsolutePath());
					tName.setText(file.getName());
					setComplete(true);
					model.setLastVisible(true);
				}
			}
		});
		
		
		SpringUtilities.makeCompactGrid(rootPanel, 2, 2, 6, 6, 6, 6);
		
		setLayout(new BorderLayout());
		add(rootPanel,BorderLayout.NORTH);
		
	}

	public void applyState() throws InvalidStateException {
		model.setName(tName.getText());
		model.setFile(file);
	}

	public void init(WizardModel model) {
		this.model = (LobberUIWizardModel)model;
	}

	public void prepare() {
		
	}

}
