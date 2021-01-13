package nl.thedutchmc.betterplayer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

	public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
	}
	
}
