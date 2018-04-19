package com.maoniu.debug;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.kevin.utils.GeneralExcelPoi;
import com.maoniu.debug.generate.GenerateKeywordData;
import com.maoniu.debug.match.MatchKeywordData;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;
import org.springframework.beans.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/4/18.
 */
public class DebugCommon {

    public static String getPosition(String prefix) throws IOException {
        return Files.toString(new File(prefix+"position.txt"), Charset.forName("UTF-8"));
    }

    public static List<ThesaurusData> getThesaurusData(String prefix) throws InvocationTargetException, IllegalAccessException, IOException {
        GeneralExcelPoi<DebugThesaurusData> debugThesaurusDataGeneralExcelPoi = new GeneralExcelPoi<>();
        List<DebugThesaurusData> thesaurusDataList = debugThesaurusDataGeneralExcelPoi.parseExcel(new File(prefix + "thesaurusData.xlsx"), DebugThesaurusData.class);
        List<ThesaurusData> thesaurusData = convertToThesaurusData(thesaurusDataList);
        return thesaurusData;
    }

    public static List<ProductAttrData> getProductAttrData(String prefix) throws InvocationTargetException, IllegalAccessException, IOException {
        GeneralExcelPoi<DebugProductAttrData> debugProductAttrDataGeneralExcelPoi = new GeneralExcelPoi<>();
        List<DebugProductAttrData> productAttrDataList = debugProductAttrDataGeneralExcelPoi.parseExcel(new File(prefix + "productAttr.xlsx"), DebugProductAttrData.class);
        List<ProductAttrData> productAttrData = convertToProductAttr(productAttrDataList);
        return productAttrData;
    }


    private static List<ThesaurusData> convertToThesaurusData(List<DebugThesaurusData> o) throws InvocationTargetException, IllegalAccessException {
        List<ThesaurusData> e = new ArrayList<ThesaurusData>();
        for(DebugThesaurusData origin : o){
            ThesaurusData target = new ThesaurusData();
            BeanUtils.copyProperties(origin, target, "characteristicWords","commonWords","synonymWords","wordGroups");
            if(StringUtils.isNotEmpty(origin.getCharacteristicWords())){
                target.setCharacteristicWords(convertToSet(origin.getCharacteristicWords()));
            }
            if(StringUtils.isNotEmpty(origin.getCommonWords())){
                target.setCommonWords(new ArrayList<>(convertToSet(origin.getCommonWords())));
            }
            if(StringUtils.isNotEmpty(origin.getWordGroups())){
                target.setWordGroups(new ArrayList<>(convertToSet(origin.getWordGroups())));
            }
            if(StringUtils.isNotEmpty(origin.getSynonymWords())){
                List<List<String>> lists = new ArrayList<>();
                List<String> list = Splitter.on("#").splitToList(origin.getSynonymWords());
                list.forEach(s -> {
                    List<String> list0 = Splitter.on(",").splitToList(origin.getSynonymWords());
                    lists.add(list0);
                });
                target.setSynonymWords(lists);
            }
            e.add(target);
        }
        return e;
    }

    private static List<ProductAttrData> convertToProductAttr(List<DebugProductAttrData> o) throws InvocationTargetException, IllegalAccessException {
        List<ProductAttrData> e = new ArrayList<ProductAttrData>();
        for(DebugProductAttrData origin : o){
            ProductAttrData target = new ProductAttrData();
            BeanUtils.copyProperties(origin, target,"compositeSet","featureOnes","featureTwos","featureThrees");
            if(StringUtils.isNotEmpty(origin.getFeatureOnes())){
                target.setFeatureOnes(convertToSet(origin.getFeatureOnes()));
            }
            if(StringUtils.isNotEmpty(origin.getFeatureTwos())){
                target.setFeatureTwos(convertToSet(origin.getFeatureTwos()));
            }
            if(StringUtils.isNotEmpty(origin.getFeatureThrees())){
                target.setFeatureThrees(convertToSet(origin.getFeatureThrees()));
            }
            if(StringUtils.isNotEmpty(origin.getCompositeSet())){
                target.setCompositeSet(convertToSet(origin.getCompositeSet()));
            }
            e.add(target);
        }
        return e;
    }

    public static List<KeywordData> matchKeywordDataConvertToKeyword(List<MatchKeywordData> o) throws InvocationTargetException, IllegalAccessException {
        List<KeywordData> e = new ArrayList<KeywordData>();
        for(MatchKeywordData origin : o){
            KeywordData target = new KeywordData();
            BeanUtils.copyProperties(origin, target, "customModels");
            if(StringUtils.isNotEmpty(origin.getCustomModels())){
                target.setCustomModels(convertToSet(origin.getCustomModels()));
            }
            e.add(target);
        }
        return e;
    }

    public static List<KeywordData> generateKeywordDataConvertToKeyword(List<GenerateKeywordData> o) throws InvocationTargetException, IllegalAccessException {
        List<KeywordData> e = new ArrayList<KeywordData>();
        for(GenerateKeywordData origin : o){
            KeywordData target = new KeywordData();
            BeanUtils.copyProperties(origin, target,"customModels", "intersectionSet","diffSet");
            if(StringUtils.isNotEmpty(origin.getCustomModels())){
                target.setCustomModels(convertToSet(origin.getCustomModels()));
            }
            if(StringUtils.isNotEmpty(origin.getIntersectionSet())){
                target.setIntersectionSet(convertToSet(origin.getIntersectionSet()));
            }
            if(StringUtils.isNotEmpty(origin.getDiffSet())){
                target.setDiffSet(convertToSet(origin.getDiffSet()));
            }
            e.add(target);
        }
        return e;
    }

    private static Set<String> convertToSet(String string){
        Set<String> result = new HashSet<>();
        if(StringUtils.isNotEmpty(string)){
            String[] strArr = string.replaceAll("，",",").split(",");
            for(String str : strArr){
                result.add(str.trim());
            }
        }
        return result;
    }
}
