package nl.thedutchmc.betterplayer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

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
    
    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
