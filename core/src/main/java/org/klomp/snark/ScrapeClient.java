package org.klomp.snark;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScrapeClient {

	protected static final Logger log = Logger.getLogger("org.klomp.snark.ScrapeClient");
	
    public ScrapeInfo scrape (String scrape, byte[] infoHash)
            throws IOException
        {
            String s = scrape + "?info_hash=" + TrackerClient.urlencode(infoHash);
            URL u = new URL(s);
            log.log(Level.FINE, "Sending TrackerClient request: " + u);
            
            URLConnection c = u.openConnection();
            c.connect();
            InputStream in = c.getInputStream();

            if (c instanceof HttpURLConnection) {
                // Check whether the page exists
                int code = ((HttpURLConnection)c).getResponseCode();
                if (code == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new IOException("Tracker doesn't handle given info_hash");
                } else if (code / 100 != 2) {
                    throw new IOException("Loading '" + s + "' gave error code "
                        + code + ", it probably doesn't exist");
                }
            }

            ScrapeInfo info = new ScrapeInfo(in);
            log.log(Level.FINE, "TrackerClient response: " + info);
            
            String failure = info.getFailureReason();
            if (failure != null) {
                throw new IOException(failure);
            }
            
            return info;
        }
	
}
