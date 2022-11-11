package com.ab.quiz.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.ab.quiz.pojo.CustomerTicket;

public class CCUtils {
	
	public static final String ISSUE_DATE_KEY = "PLAYED_DATE";
    public static final String ISSUE_GAMEID_KEY = "GAME_ID";
	
    public static String encodeCCExtraValues(HashMap<String,String> extraValues) {

        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> mapEntry : extraValues.entrySet()) {
            String key = mapEntry.getKey();
            String value = mapEntry.getValue();
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(value);
            stringBuilder.append(";");
        }
        return stringBuilder.toString();
    }

    public static HashMap<String,String> decodeCCExtraValues(String extraValues) {
        extraValues = extraValues.trim();
        StringTokenizer firstTokenizer = new StringTokenizer(extraValues, ";");
        HashMap<String, String> extraValuesMap = new HashMap<>();
        while (firstTokenizer.hasMoreTokens()) {
            StringTokenizer secondTokenizer = new StringTokenizer(firstTokenizer.nextToken(), "=");
            String key = secondTokenizer.nextToken();
            String value = secondTokenizer.nextToken();
            extraValuesMap.put(key, value);
       }
       return extraValuesMap;
    }

    public static CustomerTicket createdCCTicket(int ccTktType, long uid,
                                       String extraDetails) {

        CustomerTicket customerTicket = new CustomerTicket();
        customerTicket.setUserId(uid);
        customerTicket.setRequestType(ccTktType);
        customerTicket.setExtraDetails(extraDetails);
        
        return customerTicket;

    }
}
