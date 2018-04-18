package com.maoniu.core;

import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;

import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 * 智能生成器主要用于标题的生成
 */
public interface IntelligentGenerator<T, K> {
    List<K> doGenerate(List<T> t, List<ProductAttrData> productAttrData, List<ThesaurusData> thesaurusData);
    List<K> output(List<T> t);
    void sort(List<T> t, List<String> beAddedList, String intermediateTitle);
}
