package com.nyxus.racoon.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
	
	
	public static String getGMTDate(String pattern){
		if (pattern==null){
			 pattern="EEE, MMM d, yyyy hh:mm:ss a z";

		}
		
		 Date currentTime = new Date();

		final SimpleDateFormat sdf =
		        new SimpleDateFormat(pattern);

		// Give it to me in GMT time.
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return sdf.format(currentTime);
		
		
		
	}
	


}
