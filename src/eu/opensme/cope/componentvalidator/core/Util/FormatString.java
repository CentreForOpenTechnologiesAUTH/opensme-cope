package eu.opensme.cope.componentvalidator.core.Util;

public final class FormatString {

	public static String forString(String value) {
		String text = value.replaceAll("\n", "&#xA;");
		text = text.replace("\"", "\\" + "\"");		
		
		if (text.equals("null")){
			return text;
		}		

//		int fromIndex  = text.indexOf("<");
//		int toIndex = text.indexOf(">");
//		if ((fromIndex!= -1) && (toIndex!= -1)){
//			text = text.substring(fromIndex+1, toIndex);
//		}
		
		return "\""+text+ "\"";
	}

	public static String forHashcode(String value) {
		int v = 0;
		try {
			String val[] = value.split("@");
			v = Integer.valueOf(val[1], 16);
		} catch (Exception e) {
		}

		return String.valueOf(v);
	}

	public static String forStringList(String value) {
		String text = value.replaceAll("\n", "&#xA;");
		text = text.replace("\"", "\\" + "\"");

		if (text.equals("null")){
			text = "[" + text + "]";
			return text;
		}

		try {
			String[] textParts = text.split(", ");
			
			for (int l = 0; l < textParts.length; l++) {
				int indexOfLL = textParts[l].indexOf("[");
				int indexOfRL = textParts[l].indexOf("]");
				if ((indexOfLL!= -1) && (indexOfRL!= -1)){
					textParts[l] = "[" +"\""+textParts[l].substring(indexOfLL+1, textParts[l].length()-1) + "\"" + "]";
				}
				else if (indexOfLL!= -1){
					textParts[l] = "[" +"\""+textParts[l].substring(indexOfLL+1, textParts[l].length()) + "\"";
				}
				else if (indexOfRL!= -1){
					textParts[l] = "\"" + textParts[l].substring(0, textParts[l].length()-1) + "\"]";
				}
				else{
					textParts[l] = "\"" + textParts[l].substring(0, textParts[l].length()) + "\"";
				}
			}

			text = textParts[0];
			for (int l = 1; l < textParts.length; l++) {
				text = text + " " + textParts[l];
			}
			
		} catch (Exception e) {
		}

		return text;
	}

	public static String forOtherObject(String value) {
		String text = value.replaceAll("\n", "&#xA;"); 

		return text;
	}

	public static String formatValueToType(String value, String repType) {
		if (repType.equals("java.lang.String")){
			value = FormatString.forString(value);
		}
		else if (repType.equals("hashcode")){
			value = FormatString.forHashcode(value);
		}
		else if (repType.equals("java.lang.String[]")){
			value = FormatString.forStringList(value);
		}
		else{
			value = FormatString.forOtherObject(value);
		}
		
		return value;
	}

}
