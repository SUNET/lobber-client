package org.klomp.snark;

public interface MessageListener {

	public abstract void message(Object msg);
	public abstract void exception(Throwable t);
	
}
