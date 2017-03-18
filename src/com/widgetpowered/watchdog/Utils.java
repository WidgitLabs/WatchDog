package com.widgetpowered.watchdog;

public class Utils {
	public String parseReason(String[] args) {
		StringBuffer reason = new StringBuffer();
		
		for (int i = 2; i < args.length; i++) {
			reason.append(args[i]);
			
			if (i < args.length) {
				reason.append(" ");
			}
		}
		
		return reason.toString();
	}
}