package net.nordu.lobber.client.applet.wizard;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import net.nordu.lobber.client.applet.AppletCoordinatorListener;
import net.nordu.lobber.client.applet.AppletStorageListener;
import net.nordu.lobber.client.applet.InetUtils;
import net.nordu.lobber.client.applet.NonLocalUnicastAddressFilter;
import net.nordu.lobber.client.applet.NonLoopbackUnicastAddressFilter;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.klomp.snark.CoordinatorListener;
import org.klomp.snark.MessageListener;
import org.klomp.snark.ScrapeClient;
import org.klomp.snark.ScrapeInfo;
import org.klomp.snark.ScrapeInfo.ScrapeStats;
import org.klomp.snark.SnarkSeeder;
import org.klomp.snark.SnarkSeederShutdown;
import org.klomp.snark.StorageListener;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

public class LobberUIProgessStep extends PanelWizardStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 965698268116933447L;
	private JProgressBar progressBar;
	private JTextField progressLabel;
	private LobberUIWizardModel model;
	private SnarkSeeder seeder;
	private boolean done = false;
	private SnarkSeederShutdown shutdown;
	private TimerTask monitor;
	
	private static final int MAX_TRIES = 10;
	
	public LobberUIProgessStep() {
		
		setLayout(new BorderLayout());
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		add(progressBar,BorderLayout.NORTH);
		progressLabel = new JTextField(40);
		progressLabel.setFont(new Font(Font.MONOSPACED,Font.PLAIN,9));
		progressLabel.setBorder(BorderFactory.createEmptyBorder());
		progressLabel.setEditable(false);
		add(progressLabel,BorderLayout.SOUTH);
	}
	
	@Override
	public void prepare() {
		setBusy(true);
		startSeeding();
	}
	
	public void init(WizardModel model) {
		this.model = (LobberUIWizardModel)model;
	}
	
	private void startSeeding() {
		try {
			StorageListener slistener = new AppletStorageListener();
			CoordinatorListener clistener = new AppletCoordinatorListener();
			MessageListener mlistener = new MessageListener() {
				
				public void message(Object msg) {
					progressLabel.setText(msg.toString());
				}
				
				public void exception(Throwable t) {
					t.printStackTrace();
				}
			};

			createTorrent(model.getFile(), model.getName(), slistener, clistener, mlistener);
			if (model.getApiurl() != null) {
				Map<String,String> parameters = new HashMap<String, String>();
				parameters.put("expires",model.getExpires());
				parameters.put("description",model.getDescription());
				registerTorrent(model.getApiurl(),model.getSessionid(),parameters);
			}

			startTorrent();
		} catch (Exception ex) {
			ex.printStackTrace();
			progressBar.setIndeterminate(true);
			progressLabel.setText(ex.getMessage());
		}
	}

	private void startTorrent() throws IOException {
		progressLabel.setText("Serving "+seeder.meta.getTotalLength()+" bytes...");
		seeder.collectPieces();
	}

	private ScrapeStats getStats() {
		try {
			ScrapeClient scrape = new ScrapeClient();
			String scrapeUrl = model.getTracker();
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
	
	private void createTorrent(File file, String name, StorageListener slistener,CoordinatorListener clistener, MessageListener mlistener)
		throws SocketException, IOException 
	{
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


		seeder = new SnarkSeeder(file,name,addrs,usingLinkLocalAddress,-1,model.getTracker(),slistener,clistener,mlistener);
		shutdown = new SnarkSeederShutdown(seeder, null);
		Runtime.getRuntime().addShutdownHook(shutdown);
		progressLabel.setText("Setting up network...");
		seeder.setupNetwork();

		startMonitor();

		saveTorrentToTmpf();
	}

	private void startMonitor() {
		progressLabel.setText("Starting monitor...");
		final long sz = seeder.meta.getTotalLength();
		Timer timer = new Timer(true);
		monitor = new TimerTask() {
			@Override
			public void run() {

				if (seeder.coordinator == null)
					return;
				
				if (seeder.coordinator.getPeers() > 0)
					progressBar.setIndeterminate(false);

				long ul = seeder.coordinator.getUploaded();
				progressBar.setValue((int)(ul/sz));

				//progress("Stats: u="+seeder.coordinator.getUploaded()+" d="+seeder.coordinator.getDownloaded()+" p="+seeder.coordinator.getPeers());
				ScrapeStats stats = getStats();
				if (stats != null) {
					done = stats.completed > 1;
				}

				if (done) {
					progressBar.setIndeterminate(false);
					progressBar.setValue(100);
					progressLabel.setText((stats.completed - 1) + " peer(s) is complete");
					shutdown.run();
					setComplete(true);
					setBusy(false);
					this.cancel();
				}
			}
		};
		timer.schedule(monitor,1000,1000);
	}
	
	@Override
	public void abortBusy() {
		super.abortBusy();
		progressBar.setIndeterminate(false);
		if (monitor != null)
			monitor.cancel();
		if (shutdown != null)
			shutdown.run();
		setComplete(true);
		setBusy(false);
	}

	private void saveTorrentToTmpf() {
		try {
			File tmpFile = File.createTempFile("lobber", ".torrent");
			FileOutputStream fout = new FileOutputStream(tmpFile);
			fout.write(seeder.meta.getTorrentData());
			fout.flush();
			fout.close();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
	
	private boolean okStatus(int status) {
		return status == 200 || status == 302;
	}

	private void registerTorrent(String apiUrl, String sessionid, Map<String,String> parameters) 
		throws IOException, HttpException, IllegalAccessException, InvocationTargetException, NoSuchMethodException 
	{
		int status = 0;
		int ntries = 0;
		while (!okStatus(status) && ntries++ < MAX_TRIES) {
			HttpClient http = new HttpClient();
			PostMethod post = new PostMethod(apiUrl);
			post.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
	        post.setRequestHeader("Cookie", "sessionid="+sessionid);

	        
			Part[] parts = new Part[parameters.size()+1];
			int i = 0;
			for (Map.Entry<String, String> parameter : parameters.entrySet()) {
				parts[i++] = new StringPart(parameter.getKey(),BeanUtils.getProperty(model, parameter.getKey()));
			}
			parts[i] = new FilePart("file",new PartSource() {

					byte[] torrentData = seeder.meta.getTorrentData();
					ByteArrayInputStream in = new ByteArrayInputStream(torrentData);

					public InputStream createInputStream()
					throws IOException {
						return in;
					}

					public String getFileName() {
						return seeder.meta.getName()+".torrent";
					}

					public long getLength() {
						return torrentData.length;
					}
			},"application/x-bittorrent","utf-8");
			post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
			status = http.executeMethod(post);
			System.err.println(post.getStatusCode());
			System.err.println(post.getStatusText());
			
			if (false) {
				File tmpf = new File("/tmp/foo.html");
				FileOutputStream fout = new FileOutputStream(tmpf);
				InputStream in = post.getResponseBodyAsStream();
				byte[] buf = new byte[1024];
				while (in.read(buf) > 0) {
					fout.write(buf);
				}
				fout.close();
			}
					
			if (!okStatus(status)) {
				progressLabel.setText("Got "+status+" "+post.getStatusText()+" from index. Retrying in 1s ...");
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException ign) {}
			}
		}
		if (okStatus(status)) {
			progressLabel.setText("Registered torrent with tracker...");
		} else {
			throw new IOException("Unable to register torrent with tracker!");
		}
	}
}

