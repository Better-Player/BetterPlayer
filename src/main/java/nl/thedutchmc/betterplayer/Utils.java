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
	
    public static String fixArtistName(String input) {
    	//YouTube does it in the format of 'ArtistName - Topic'
    	//We only care for everything before the hyphen
    	input = input.split("-")[0];
    	
    	//Remove leading and trailing spaces    	
    	input = input.trim();
    	
    	//Remove VEVO and fix spacing (based on capitalization, e.g TaylorSwiftVEVO -> Taylor Swift)                	
    	if(input.contains("VEVO")) {
    		input = input.replace("VEVO", "");
    		input = Character.toUpperCase(input.charAt(0)) + input.substring(1).replaceAll("(?<!_)(?=[A-Z])", " ");
    	}
    	
    	return input;
    }
}
