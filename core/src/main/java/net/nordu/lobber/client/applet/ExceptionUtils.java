package net.nordu.lobber.client.applet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionUtils {

	public static String stackTrace(Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		t.printStackTrace(new PrintStream(baos));
		return baos.toString();
	}
	
}
