package net.nordu.lobber.client.applet;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.klomp.snark.CoordinatorListener;
import org.klomp.snark.MessageListener;
import org.klomp.snark.Peer;
import org.klomp.snark.PeerCoordinator;
import org.klomp.snark.PeerMonitorTask;
import org.klomp.snark.ScrapeClient;
import org.klomp.snark.ScrapeInfo;
import org.klomp.snark.SnarkSeeder;
import org.klomp.snark.Storage;
import org.klomp.snark.StorageListener;
import org.klomp.snark.ScrapeInfo.ScrapeStats;

public class LobberClientApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3328873182884573174L;
	private String tracker;
	private LobberUI lobberUI;
	private SnarkSeeder seeder;
	
	
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
	
	
	public class LobberUI extends JPanel implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7301588955270617709L;
		private JButton uploadButton;
		private JProgressBar progressBar;
		private JTextArea taskOutput;
		
		private void status(String msg) {
			getAppletContext().showStatus(msg);
		}
		
		private void progress(String msg) {
			taskOutput.append(msg+"\n");
		}
		
		public LobberUI() {
			
			setLayout(new BorderLayout());
			//setOpaque(true);
			setVisible(true);
			uploadButton = new JButton("Upload");

	        progressBar = new JProgressBar(0, 100);
	        progressBar.setValue(0);
	        progressBar.setStringPainted(true);

	        taskOutput = new JTextArea(5, 20);
	        taskOutput.setMargin(new Insets(5,5,5,5));
	        taskOutput.setEditable(false);

	        JPanel panel = new JPanel();
	        panel.add(uploadButton);
	        panel.add(progressBar);

	        add(panel, BorderLayout.PAGE_START);
	        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
	        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			
			uploadButton.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent event) {
			if (event.getSource().equals(uploadButton))
				startSeeding();
		}

		private void startSeeding() {
			try {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int rc = fileChooser.showOpenDialog(getContentPane());
				if (rc == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            StorageListener slistener = new StorageListener() {
		    			
		    			public void storageCreateFile(Storage arg0, String arg1, long arg2) {
		    				progress("created "+arg1+" "+arg2);
		    			}
		    			
		    			public void storageChecked(Storage arg0, int arg1, boolean arg2) {
		    				progress("checked "+arg1+" "+arg2);
		    			}
		    			
		    			public void storageAllocated(Storage arg0, long arg1) {
		    				progress("allocated "+arg1);
		    			}
		    			
		    			public void storageAllChecked(Storage arg0) {
		    				progress("all checked");
		    			}
	
						public void storateGetPiece(Storage storage,int num) {
							progress("get piece "+num);
						}
						
						public void message(Object msg) {
							progress(msg.toString());
						}
						
						public void exception(Throwable t) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							t.printStackTrace(new PrintStream(baos));
							JOptionPane.showMessageDialog(getContentPane(), baos.toString());
						}
		    		};
		    		
		    		CoordinatorListener clistener = new CoordinatorListener() {
						
						public void peerChange(PeerCoordinator coordinator, Peer peer) {
							progress("Peer "+peer.getPeerID()+" has downloaded "+ peer.getUploaded()+" bytes");
						}
						
					};
		           
					MessageListener mlistener = new MessageListener() {
						
						public void message(Object msg) {
							progress(msg.toString());
						}
						
						public void exception(Throwable t) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							t.printStackTrace(new PrintStream(baos));
							JOptionPane.showMessageDialog(getContentPane(), baos.toString());
						}
					};
				
				   String addrs[] = InetUtils.getInterfaces();
		           seeder = new SnarkSeeder(file,addrs,-1,tracker,slistener,clistener,mlistener);
		           progress("Serving data...");
		           seeder.setupNetwork();
		           
		           Timer timer = new Timer(true);
		           TimerTask monitor = new TimerTask() {
		        	    @Override
			        	public void run() {
		        	    	ScrapeClient scrape = new ScrapeClient();
		        	    	String scrapeUrl = tracker;
		        	    	scrapeUrl = scrapeUrl.replace("announce", "scrape");
		        	    	progress("Stats: u="+seeder.coordinator.getUploaded()+" d="+seeder.coordinator.getDownloaded()+" p="+seeder.coordinator.getPeers());
		        	    	try {
		        	    		byte[] info_hash = seeder.meta.getInfoHash();
		        	    		ScrapeInfo info = scrape.scrape(scrapeUrl,info_hash);
		        	    		if (info.getFailureReason() != null)
		        	    			throw new IOException(info.getFailureReason());
		        	    		ScrapeStats stats = info.getScrapeStats(info_hash);
		        	    		System.err.println("infohash: "+new String(info_hash));
		        	    		if (stats != null)
		        	    			progress(stats.toString());
		        	    	} catch (IOException ex) {
		        	    		ex.printStackTrace();
		        	    	}
			        	}
		           };
		           timer.schedule(monitor,2000,2000);
		           
		           progress("Collecting pieces...");
		           seeder.collectPieces();
		        }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
