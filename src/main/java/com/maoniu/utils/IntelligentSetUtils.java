package com.maoniu.utils;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created with IDEA
 * User: lsd
 * Company: ND
 * Date: 2016/11/10
 * Time: 14:53
 * DESC:
 */
public class IntelligentSetUtils {

    public  static Set threeIntersection(Set set1, Set set2, Set set3){
       return intersection(set1, set2, set3);
    }

    public  static Set twoIntersection(Set set1, Set set2){
        return  intersection(set1, set2);
    }

    public static Set intersection(Set set1, Set set2, Set... sets){
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(set1);
        set.retainAll(set2);
        if(null != sets && sets.length > 0){
            for(Set set0 : sets){
                set.retainAll(set0);
            }
        }
        return set;

    }


    /**
     * 以set2未比较基础，输出set1在set2中不存在数据
     * @param set1
     * @param set2
     * @return
     */
    public static Set twoDifference(Set set1, Set set2){
        Sets.SetView<String> setView = Sets.difference(set1, set2);
        return setView;
    }

    /**
     * 以set2未比较基础，输出set1在set2中不存在数据
     * @param set1
     * @param set2
     * @return
     */
    public static Set twoUnion(Set set1, Set set2){
        Sets.SetView setView = Sets.union(set1, set2);
        return setView;
    }

    public static void main(String[] args) {
        Set<String> set1 = new HashSet<String>();
        Set<String> set2 = new HashSet<String>();
        Set<String> set3 = new HashSet<String>();
        set1.add("1");
        set1.add("3");
        set2.add("1");
        set2.add("2");
        set2.add("3");
   //     set2.add("3");
        set3.add("1");
        set3.add("4");
        Set<String> commons = threeIntersection(set1, set2, set3);
        Set<String> intersection = twoIntersection(set1, set2);
        Set<String> difference =  twoDifference(set1, set2);
        Set<String> union =  twoUnion(set1, set2);
        System.out.println(111);
    }
}
