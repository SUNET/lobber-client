package net.nordu.lobber.client.applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.klomp.snark.CoordinatorListener;
import org.klomp.snark.MessageListener;
import org.klomp.snark.Peer;
import org.klomp.snark.PeerCoordinator;
import org.klomp.snark.ScrapeClient;
import org.klomp.snark.ScrapeInfo;
import org.klomp.snark.SnarkSeeder;
import org.klomp.snark.SnarkSeederShutdown;
import org.klomp.snark.Storage;
import org.klomp.snark.StorageListener;
import org.klomp.snark.ScrapeInfo.ScrapeStats;

public class LobberClientApplet extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3328873182884573174L;
	private String tracker;
	private SnarkSeeder seeder;
	
	
	private void createGUI() {
		tracker = getParameter("tracker");
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
	
	
	public class LobberUI extends JPanel implements ActionListener {
		/**
		 * 
		 */
		
		private static final long serialVersionUID = -7301588955270617709L;
		private JButton uploadButton;
		private JProgressBar progressBar;
		private boolean done = false;
		private JTextField progressLabel;
		
		private static final String UPLOAD_ICON = "/Earth-Upload-icon.png";
		
		private void progress(String msg) {
			System.err.println("Progress: "+msg);
			progressLabel.setText(msg);
		}
		
		public LobberUI() {
			setBackground(Color.WHITE);
			setLayout(new BorderLayout());
			//setOpaque(true);
			setVisible(true);
			uploadButton = new JButton(new ImageIcon(LobberUI.class.getResource(UPLOAD_ICON)));
			uploadButton.setToolTipText("Select a file or directory to upload");
			uploadButton.setBackground(Color.WHITE);
			
	        progressBar = new JProgressBar(0, 100);
	        progressBar.setValue(0);
	        progressBar.setStringPainted(true);

	        JPanel panel = new JPanel();
	        panel.setBackground(Color.WHITE);
	        panel.add(uploadButton);
	        
	        JPanel progressPanel = new JPanel();
	        progressPanel.setLayout(new BorderLayout());
	        progressPanel.setBackground(Color.WHITE);
	        progressLabel = new JTextField(40);
	        progressLabel.setBackground(Color.WHITE);
	        progressLabel.setFont(new Font(Font.MONOSPACED,Font.PLAIN,9));
	        progressLabel.setBorder(BorderFactory.createEmptyBorder());
	        progressLabel.setEditable(false);
	        progressPanel.add(progressBar,BorderLayout.NORTH);
	        progressPanel.add(progressLabel,BorderLayout.SOUTH);
	        
	        panel.add(progressPanel);

	        add(panel, BorderLayout.PAGE_START);
	        
	        setBorder(BorderFactory.createTitledBorder("Lobber BitTorrent Client"));
	        //setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			
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
					uploadButton.setEnabled(false);
					progressBar.setIndeterminate(true);
		            File file = fileChooser.getSelectedFile();
		            StorageListener slistener = new StorageListener() {
		    			
		    			public void storageCreateFile(Storage arg0, String arg1, long arg2) {
		    				System.err.println("created "+arg1+" "+arg2);
		    			}
		    			
		    			public void storageChecked(Storage arg0, int arg1, boolean arg2) {
		    				System.err.println("checked "+arg1+" "+arg2);
		    			}
		    			
		    			public void storageAllocated(Storage arg0, long arg1) {
		    				System.err.println("allocated "+arg1);
		    			}
		    			
		    			public void storageAllChecked(Storage arg0) {
		    				System.err.println("all checked");
		    			}
	
						public void storateGetPiece(Storage storage,int num) {
							System.err.println("get piece "+num);
						}
						
						public void message(Object msg) {
							System.err.println(msg.toString());
						}
						
						public void exception(Throwable t) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							t.printStackTrace(new PrintStream(baos));
							JOptionPane.showMessageDialog(getContentPane(), baos.toString());
						}
		    		};
		    		
		    		CoordinatorListener clistener = new CoordinatorListener() {
						
						public void peerChange(PeerCoordinator coordinator, Peer peer) {
							System.err.println("Peer "+peer.getPeerID()+" has downloaded "+ peer.getUploaded()+" bytes");
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

				   boolean usingLinkLocalAddress = false;
				   String addrs[] = InetUtils.getAddresses(new NonLocalUnicastAddressFilter());
				   if (addrs == null || addrs.length == 0) {
					   addrs = InetUtils.getAddresses(new NonLoopbackUnicastAddressFilter());
					   usingLinkLocalAddress = true;
				   }
				   
				   System.err.println("Addresses: ");
				   for (String addr: addrs) {
					   System.err.println(addr);
				   }
				   System.err.println("using link locals: "+usingLinkLocalAddress);
					   
				   
		           seeder = new SnarkSeeder(file,addrs,usingLinkLocalAddress,-1,tracker,slistener,clistener,mlistener);
		           final SnarkSeederShutdown shutdown = new SnarkSeederShutdown(seeder, null);
			       Runtime.getRuntime().addShutdownHook(shutdown);
		           progress("Setting up network...");
		           seeder.setupNetwork();
		           final long sz = seeder.meta.getTotalLength();
		           progress("Starting monitor...");
		           
		           Timer timer = new Timer(true);
		           TimerTask monitor = new TimerTask() {
		        	    @Override
			        	public void run() {
		        	    	
		        	    	if (seeder.coordinator.getPeers() < 2)
		        	    		progressBar.setIndeterminate(true);
		        	    	
		        	    	long ul = seeder.coordinator.getUploaded();
		        	    	progressBar.setValue((int)(ul/sz));
		        	    	
		        	    	//progress("Stats: u="+seeder.coordinator.getUploaded()+" d="+seeder.coordinator.getDownloaded()+" p="+seeder.coordinator.getPeers());
		        	    	ScrapeStats stats = getStats();
		        	    	if (stats != null) {
		        	    		progress(stats.toString());
		        	    		done = stats.completed > 1;
		        	    	}
		        	    	
		        	    	if (done) {
		        	    		progressBar.setIndeterminate(false);
		        	    		progressBar.setValue(100);
		        	    		progress((stats.downloaded)+" peers completed");
		        	    		shutdown.run();
		        	    		uploadButton.setEnabled(true);
		        	    		this.cancel();
		        	    	}
			        	}
		           };
		           timer.schedule(monitor,1000,1000);
		           
		           progress("Serving "+sz+" bytes...");
		           seeder.collectPieces();
		        }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	
	private ScrapeStats getStats() {
		try {
			ScrapeClient scrape = new ScrapeClient();
	    	String scrapeUrl = tracker;
	    	scrapeUrl = scrapeUrl.replace("announce", "scrape");
    		byte[] info_hash = seeder.meta.getInfoHash();
    		ScrapeInfo info = scrape.scrape(scrapeUrl,info_hash);
    		if (info.getFailureReason() != null)
    			throw new IOException(info.getFailureReason());
    		return info.getScrapeStats(info_hash);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
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
