package net.nordu.lobber.client.applet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.klomp.snark.MetaInfo;
import org.klomp.snark.Storage;
import org.klomp.snark.StorageListener;

public class LobberClientApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3328873182884573174L;
	private String tracker;
	private LobberUI lobberUI;
	
	
	
	private void createGUI() {
		tracker = getParameter("tracker");
		setContentPane(new LobberUI());
		getContentPane().setVisible(true);
		System.err.println("kaka3");
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
	
	
	public class LobberUI extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7301588955270617709L;
		private JButton uploadButton;
		
		private void jsAlert(String msg) {
			getAppletContext().showStatus(msg);
		}
		
		public LobberUI() {
			
			setLayout(new BorderLayout());
			//setOpaque(true);
			setVisible(true);
			uploadButton = new JButton("Upload");
			add(uploadButton,BorderLayout.CENTER);
			
		
			uploadButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Storage storage = null;
					
					try {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						int rc = fileChooser.showOpenDialog(getContentPane());
						if (rc == JFileChooser.APPROVE_OPTION) {
				            File file = fileChooser.getSelectedFile();
				            storage = new Storage(file, tracker, new StorageListener() {
				    			
				    			public void storageCreateFile(Storage arg0, String arg1, long arg2) {
				    				jsAlert("created "+arg1+" "+arg2);
				    			}
				    			
				    			public void storageChecked(Storage arg0, int arg1, boolean arg2) {
				    				jsAlert("checked "+arg1+" "+arg2);
				    			}
				    			
				    			public void storageAllocated(Storage arg0, long arg1) {
				    				jsAlert("allocated "+arg1);
				    			}
				    			
				    			public void storageAllChecked(Storage arg0) {
				    				jsAlert("all checked");
				    			}
				    		});
				            
				            MetaInfo metaInfo = storage.getMetaInfo();
				            jsAlert(metaInfo.getHexInfoHash());
				        }
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						storage = null;
					}
				}
			});
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
