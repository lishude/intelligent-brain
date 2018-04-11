package com.maoniu.core;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/10.
 */
public abstract class AbstractIntelligent<T, S> implements Convertor{
    public final String SPACE_PLUS = "\\s+";
    public final String BOUNDARY = "\\b";
    protected Map<String, List<String>> classify_keyword_map;
    protected Map<String, List<String>> classify_common_words_map;
    protected Map<String, List<String>> keyword_common_words_map;
    protected List<String> common_words;
    protected List<String> prep_words;
    protected List<String> common_words_with_space;
    protected List<String> prep_words_with_prefix_suffix;
    protected boolean common_words_map_flag = true;

    public AbstractIntelligent(@NotNull Map<String, List<String>> classify_keyword_map, @Nullable Map<String, List<String>> classify_common_words_map, @Nullable List<String> prep_words){
        this.classify_keyword_map = classify_keyword_map;
        this.classify_common_words_map = classify_common_words_map;
        this.prep_words = prep_words;
        init();
    }

    @Override
    public void init(){
        //如果common_words_map为空，则通用词对所有的数据都是公用的
        if(null == classify_common_words_map || classify_common_words_map.size() <= 0){
            common_words = Arrays.asList("custom", "wholesale", "china");
            common_words_map_flag = false;
        }
        if(CollectionUtils.isEmpty(prep_words)){
            prep_words = Arrays.asList("for", "with", "in", "of", "on", "to", "and");
        }
        common_words_with_space = new ArrayList<>();
        prep_words_with_prefix_suffix = new ArrayList<>();

        for(String commonWord : common_words){
            commonWord = BOUNDARY + commonWord + BOUNDARY;
            common_words_with_space.add(commonWord);
        }
        for(String prepWord : prep_words){
            prepWord = "(((\\w+\\s+)+)" + prepWord + "((\\s+\\w+)+))";
            prep_words_with_prefix_suffix.add(prepWord);
        }
    }


}
