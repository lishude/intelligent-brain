package com.maoniu.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/11.
 */
public class MaoNiuGenerator extends AbstractIntelligent implements IntelligentGenerator{

    public MaoNiuGenerator(Map classify_keyword_map, Map classify_common_words_map, List prep_words) {
        super(classify_keyword_map, classify_common_words_map, prep_words);
    }

    @Override
    public Object convert(Object o) {
        return null;
    }
}
