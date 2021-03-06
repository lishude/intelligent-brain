package com.maoniu.core;

import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/12.
 */
public interface Preprocessor<T> {
    void prepare(List<T> t, List<ProductAttrData> productAttrData, List<ThesaurusData> thesaurusData, Map<String, String> map);
}
