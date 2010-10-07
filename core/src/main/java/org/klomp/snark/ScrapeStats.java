package org.klomp.snark;

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
	
	public ScrapeStats()  {}
}