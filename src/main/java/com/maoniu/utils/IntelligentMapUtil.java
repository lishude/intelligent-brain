package com.maoniu.utils;

import com.google.common.collect.Multimap;
import com.maoniu.enums.IntelligentOrderSort;

import java.util.*;

/**
 * Created by Windows on 2016/7/24.
 */
public class IntelligentMapUtil {

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map, final IntelligentOrderSort sort)
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                if(sort == IntelligentOrderSort.ASC){
                    return (o1.getValue()).compareTo( o2.getValue() );
                }else if(sort == IntelligentOrderSort.DESC){
                    return -(o1.getValue()).compareTo( o2.getValue() );
                }
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static   <K, V extends Multimap> Map<K, V>
    sortByMultiSize(Map<K, V> map, final IntelligentOrderSort sort)
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                if(sort == IntelligentOrderSort.ASC){
                    return o1.getValue().size() - o2.getValue().size();
                }else if(sort == IntelligentOrderSort.DESC){
                    return -(o1.getValue().size() - o2.getValue().size());
                }
                return o1.getValue().size() - o2.getValue().size();
            }
        } );
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static  Map<String, Set<String>>
    sortBySetSize(Map<String, Set<String>> map)
    {
        List<Map.Entry<String, Set<String>>> list =
                new LinkedList<Map.Entry<String, Set<String>>>( map.entrySet() );
        Collections.sort(list, new Comparator<Map.Entry<String, Set<String>>>() {
            @Override
            public int compare(Map.Entry<String, Set<String>> o1, Map.Entry<String, Set<String>> o2) {
                return -(o1.getValue().size() - o2.getValue().size());
            }
        });

        Map<String, Set<String>> result = new LinkedHashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static String getRandomKey(Map<String, Integer> map){
        List<String> list = new ArrayList<String>(map.keySet());
        Collections.shuffle(list);
        return list.get(0);
    }

    public static Map<String, Integer> copy(Map<String, Integer> e){
        Map<String, Integer> map = new HashMap<String, Integer>();
        for(String key : e.keySet()){
            map.put(key, e.get(key));
        }
        return map;
    }

}


