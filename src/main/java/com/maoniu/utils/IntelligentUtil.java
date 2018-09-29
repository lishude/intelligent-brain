package com.maoniu.utils;

import com.maoniu.core.AbstractIntelligent;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ThesaurusData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2018/5/4.
 */
public class IntelligentUtil {

    public static Set<String> omitStemAndWordGroup(KeywordData keywordData, ThesaurusData thesaurusData, Set<String> wordGroupSet){
        String result = keywordData.getName();
        if(StringUtils.isNotEmpty(keywordData.getKeyword())){
            //删除核心词
            result =  result.replaceAll(AbstractIntelligent.BOUNDARY+keywordData.getKeyword()+AbstractIntelligent.BOUNDARY, "");
            //如果这边没有删除掉核心词的话，说明核心词在关键词中被分开了
            if(result.equalsIgnoreCase(keywordData.getName())){
                for(String single : keywordData.getKeyword().split(AbstractIntelligent.SPACE_PLUS)){
                    result =  result.replaceAll(AbstractIntelligent.BOUNDARY+single+AbstractIntelligent.BOUNDARY, "");
                }
            }
        }

        if(StringUtils.isNotEmpty(result)){
            Set<String> commonWordGroup = null;
            //删除通用词
            if(CollectionUtils.isEmpty(thesaurusData.getCommonWords())){
                commonWordGroup = new HashSet<>(AbstractIntelligent.common_words);
            }else{
                commonWordGroup = new HashSet<>(thesaurusData.getCommonWords());
            }
            if(!CollectionUtils.isEmpty(commonWordGroup)){
                for(String cwg : commonWordGroup){
                    result = result.replaceAll(AbstractIntelligent.BOUNDARY+cwg+AbstractIntelligent.BOUNDARY, "");
                }
            }
        }
        if(StringUtils.isNotEmpty(result)){
            //删除介词
            Set<String> prepWords = new HashSet<>(AbstractIntelligent.prep_words);
            if(!CollectionUtils.isEmpty(prepWords)){
                for(String cwg : prepWords){
                    result = result.replaceAll(AbstractIntelligent.BOUNDARY+cwg+AbstractIntelligent.BOUNDARY, "");
                }
            }
        }
/*
        //删除核心词
        result =  keywordData.getOmittedName().replaceAll(AbstractIntelligent.BOUNDARY+keywordData.getKeyword()+AbstractIntelligent.BOUNDARY, "");
        //如果这边没有删除掉核心词的话，说明核心词在关键词中被分开了
        if(result.equalsIgnoreCase(keywordData.getName())){
            for(String single : keywordData.getKeyword().split(AbstractIntelligent.SPACE_PLUS)){
                result =  keywordData.getName().replaceAll(AbstractIntelligent.BOUNDARY+single+AbstractIntelligent.BOUNDARY, "");
            }
        }*/
        //将消除通用词、介词、核心词的关键词赋值
        keywordData.setOmittedName(result.trim());
        //删除词组
        if(!CollectionUtils.isEmpty(wordGroupSet)){
            for(String wordGroup : wordGroupSet){
                result = result.replaceAll(AbstractIntelligent.BOUNDARY+wordGroup+AbstractIntelligent.BOUNDARY, "");//删除词组
            }
        }

        if(StringUtils.isEmpty(result.trim()))
            return new HashSet<String>();

        Set<String> newResp = new HashSet<String>(Arrays.asList(result.trim().split(AbstractIntelligent.SPACE_PLUS)));
        //把词组当做一个词再加入进去
        if(wordGroupSet.size() > 0)
            newResp.addAll(wordGroupSet);
        return newResp;
    }
}
