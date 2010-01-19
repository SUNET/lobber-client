package org.klomp.snark;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.klomp.snark.bencode.BDecoder;
import org.klomp.snark.bencode.BEValue;
import org.klomp.snark.bencode.InvalidBEncodingException;

public class ScrapeInfo {

	public class ScrapeStats {
		public int completed;
		public int downloaded;
		public int incomplete;
		public String name;
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("ScrapeStats[completed=").append(completed).append(" downloaded=").append(downloaded).append(" incomplete=").append(incomplete).append("]");
			return buf.toString();
		}
	}
	
	private String failure_reason;
	private Map<String, ScrapeStats> info;
	
	public ScrapeStats getScrapeStats(byte[] info_hash) {
		return info.get(new String(info_hash));
	}
	
    public ScrapeInfo (InputStream in)
	    throws IOException
	{
	    this(new BDecoder(in));
	}

	public ScrapeInfo (BDecoder be)
	    throws IOException
	{
	    decodeResponseMap(be.bdecodeMap().getMap());
	}
	
	public ScrapeStats decodeInfoMap(Map infoMap) throws InvalidBEncodingException {
		if (infoMap == null)
			return null;
		
		ScrapeStats stats = new ScrapeStats();
		stats.completed = ((BEValue)infoMap.get("complete")).getInt();
		stats.downloaded = ((BEValue)infoMap.get("downloaded")).getInt();
		stats.incomplete = ((BEValue)infoMap.get("incomplete")).getInt();
		BEValue beName = (BEValue)infoMap.get("name");
		if (beName != null)
			stats.name = beName.getString();
		
		System.err.println(stats);
		
		return stats;
	}
	
	private void decodeResponseMap(Map m)
	    throws IOException
	{
	    BEValue reason = (BEValue)m.get("failure reason");
	    if (reason != null) {
	        this.failure_reason = reason.getString();
	    } else {
	        failure_reason = null;
	        info = new HashMap<String, ScrapeStats>();
	        BEValue beFiles = (BEValue)m.get("files");
	        Map filesMap = beFiles.getMap();
	        for (String beInfoHash : (Set<String>)filesMap.keySet()) {
	        	byte[] info_hash = beInfoHash.getBytes();
	        	System.err.println(new String(info_hash));
	        	BEValue infoMap = (BEValue)filesMap.get(beInfoHash);
	        	info.put(new String(info_hash), decodeInfoMap(infoMap.getMap()));
	        }
	    }
	}

	public String getFailureReason() {
		return failure_reason;
	}
	
}
