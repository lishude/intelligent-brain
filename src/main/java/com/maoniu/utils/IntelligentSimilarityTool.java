package com.maoniu.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IDEA
 * User: lsd
 * Company: ND
 * Date: 2016/6/12
 * Time: 13:45
 * DESC:
 */
public class IntelligentSimilarityTool {

    private static List wordLetterPairs(String str) {
        List<String> allPairs = new ArrayList<String>();
        String[] words = str.split("\\s+");
        for (int w=0; w < words.length; w++) {
            allPairs.add(words[w]);
        }
        return allPairs;
    }

    public static Double compareStrings(String str1, String str2, List<String>... lists) {
        List<String> same = new ArrayList<String>();
        List<String> pairs1 = wordLetterPairs(str1.toUpperCase());
        List<String> pairs2 = wordLetterPairs(str2.toUpperCase());
        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i=0; i<pairs1.size(); i++) {
            Object pair1 = pairs1.get(i);
            for(int j = 0; j < pairs2.size(); j++) {
                Object pair2 = pairs2.get(j);
                if (pair1.equals(pair2)) {
                    same.add(String.valueOf(pair1).toLowerCase());
                    intersection++;
                    pairs2.remove(j);
                    break;
                }
            }
        }
        DecimalFormat df = new DecimalFormat("#.###");
        if(same.size() > 0){
            if(null != lists && lists.length > 0){
                lists[0].addAll(same);
            }
        }
        return Double.valueOf(df.format((2.0*intersection)/union));
    }


    public static void main(String[] args) {
        String  str1 = "test bar wholesale stool";
        String str2 = "bar wholesale stool";
        System.out.println(compareStrings(str1, str2));

    }




}
