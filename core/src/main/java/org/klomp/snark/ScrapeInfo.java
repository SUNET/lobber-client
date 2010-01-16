package org.klomp.snark;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.klomp.snark.bencode.BDecoder;
import org.klomp.snark.bencode.BEValue;
import org.klomp.snark.bencode.InvalidBEncodingException;

public class ScrapeInfo {

	public class ScrapeStats {
		public int completed;
		public int downloaded;
		public int incomplete;
		public String name;
	}
	
	private String failure_reason;
	private Map<byte[], ScrapeStats> info;
	
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
		ScrapeStats stats = new ScrapeStats();
		stats.completed = ((BEValue)infoMap.get("completed")).getInt();
		stats.downloaded = ((BEValue)infoMap.get("downloaded")).getInt();
		stats.incomplete = ((BEValue)infoMap.get("incomplete")).getInt();
		BEValue beName = (BEValue)infoMap.get("name");
		if (beName != null)
			stats.name = beName.getString();
		
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
	        info = new HashMap<byte[], ScrapeStats>();
	        
	        BEValue beFiles = (BEValue)m.get("files");
	        Map filesMap = beFiles.getMap();
	        for (BEValue beInfoHash : (List<BEValue>)filesMap.keySet()) {
	        	byte[] info_hash = beInfoHash.getBytes();
	        	BEValue infoMap = (BEValue)filesMap.get(beInfoHash);
	        	info.put(info_hash, decodeInfoMap(infoMap.getMap()));
	        }
	    }
	}
	
}
