package org.klomp.snark;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.klomp.snark.bencode.BDecoder;
import org.klomp.snark.bencode.BEValue;
import org.klomp.snark.bencode.InvalidBEncodingException;

public class ScrapeInfo {
	
	private String failure_reason;
	private Map<String, ScrapeStats> info;
	
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		for (Map.Entry<String, ScrapeStats> e: info.entrySet()) {
			buf.append(e.getKey()).append(" -> ").append(e.getValue().toString());
		}
		
		return buf.toString();
	}
	
	public ScrapeStats getScrapeStats(byte[] info_hash) {
		System.err.println("ScrapeInfo: "+info);
		System.err.println("Looking for "+MetaInfo.hexencode(info_hash));
		return info.get(MetaInfo.hexencode(info_hash));
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
	        	System.err.println("beInfoHash: "+beInfoHash);
	        	System.err.println(MetaInfo.hexencode(beInfoHash.getBytes()));
	        	BEValue infoMap = (BEValue)filesMap.get(beInfoHash);
	        	//info.put(MetaInfo.hexencode(info_hash), decodeInfoMap(infoMap.getMap()));
	        	info.put(beInfoHash, decodeInfoMap(infoMap.getMap()));
	        }
	    }
	}

	public String getFailureReason() {
		return failure_reason;
	}
	
}
