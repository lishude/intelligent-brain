package com.maoniu.core;

import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;

import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 */
public interface IntelligentMatcher<T, K>{
    List<K> doMatch(List<T> t, String position, List<ProductAttrData> productAttrData, List<ThesaurusData> thesaurusData, List<String>... coreWords);
    List<K> output(List<T> t);
}
