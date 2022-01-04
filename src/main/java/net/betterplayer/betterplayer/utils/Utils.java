package net.betterplayer.betterplayer.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.TextChannel;

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
    
    /**
     * Extract parameters from a URL into a HashMap
     * @param url The URL to get the parameters from
     * @return Returns a HashMap&ltString parameterName, String parameterValue&gt
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if(idx + 1 > pairs.length - 1) {
                continue;
            }
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
    
    /**
     * Convert miliseconds to a timestamp of format (hh:)mm:ss
     * @param milis
     * @return Returns the timestamp as a String
     */
    public static String milisToTimeStamp(long milis) {
        int seconds = (int) (milis / 1000) % 60 ;
        int minutes = (int) ((milis / (1000 * 60)) % 60);
        int hours   = (int) ((milis / (1000 * 60 * 60)) % 24);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    	} else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Verify if a String is a positive number, less than Integer.MAX_VALUE
     * @param input The input to verify
     * @param senderChannel The TextChannel to send output to
     * @return Returns true if the provided String is a valid positive Integer
     */
    public static boolean verifyPositiveInteger(String input, TextChannel senderChannel) {
		if(input.matches("-?\\d+")) {
			
			BigInteger bigInt = new BigInteger(input);
			if(bigInt.compareTo(BigInteger.valueOf((long) Integer.MAX_VALUE)) > 0) {
				senderChannel.sendMessage("That number is too big! Nice try :)").queue();
				return false;
			}
			
			if(Integer.valueOf(input) <= 0) {
				senderChannel.sendMessage("Only numbers higher than 0 are allowed!").queue();
				return false;
			}
		} else {
			senderChannel.sendMessage("You must provide a valid number!").queue();
			return false;
		}
		
		return true;
    }

    public static byte[] toPrimitive(Byte[] object) {
        byte[] b = new byte[object.length];
        for(int i = 0; i < object.length; i++) {
            b[i] = object[i];
        }

        return b;
    }
}
