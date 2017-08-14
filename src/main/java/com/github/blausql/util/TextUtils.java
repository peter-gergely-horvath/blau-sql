package com.github.blausql.util;

public class TextUtils {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	
	public static String breakLine(String theString, int maxLineLen) {
		StringBuilder multilineStringBuilder = new StringBuilder();
		
		for(int i=0; i < theString.length(); i+=maxLineLen) {
			multilineStringBuilder.append(
					theString.substring(i, Math.min(i + maxLineLen, theString.length())));
			
			multilineStringBuilder.append(LINE_SEPARATOR);
		}
		return multilineStringBuilder.toString();
	}
}
