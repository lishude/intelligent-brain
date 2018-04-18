package com.maoniu;

import com.maoniu.core.MaoNiuGenerator;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/4/12.
 */
public class MaoNiuGeneratorTest {

    public static void main(String[] args) {

        MaoNiuGenerator maoNiuGenerator = new MaoNiuGenerator(new HashMap(), null, null);
        List<KeywordData> input = new ArrayList<>();
        KeywordData keywordData = new KeywordData();
        keywordData.setName("compression sock");
        keywordData.setModel("a");
        keywordData.setClassify("1");
        keywordData.setHeat(0);
        KeywordData keywordData1 = new KeywordData();
        keywordData1.setName("cute sock cheap");
        keywordData1.setModel("a");
        keywordData1.setClassify("1");
        keywordData1.setHeat(0);
        KeywordData keywordData2 = new KeywordData();
        keywordData2.setName("china sock manufacture");
        keywordData2.setModel("a");
        keywordData2.setClassify("1");
        keywordData2.setHeat(0);
        KeywordData keywordData3 = new KeywordData();
        keywordData3.setName("for woman sock");
        keywordData3.setModel("a");
        keywordData3.setClassify("1");
        keywordData3.setHeat(0);
        input.add(keywordData);
        input.add(keywordData1);
        input.add(keywordData2);
        input.add(keywordData3);

        List<ProductAttrData> productAttrData = new ArrayList<>();
        ProductAttrData pad = new ProductAttrData();
        pad.setModel("a");
        pad.setClassify("1");
        pad.setFeatureOnes(new HashSet<>(Arrays.asList("featureOne1","featureOne2", "word group", "word group1")));
        pad.setFeatureTwos(new HashSet<>(Arrays.asList("featureTwo1","featureTwo2","featureTwo3","featureTwo4")));
        productAttrData.add(pad);


        List<ThesaurusData > thesaurusData = new ArrayList<>();
        ThesaurusData td = new ThesaurusData();
        td.setClassify("1");
        List<List<String>> lists = new ArrayList<>();
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("featureOne1", "featureOne11", "featureOne111"));
        List<String> list1 = new ArrayList<>();
        list1.addAll(Arrays.asList("featureOne2", "featureOne21", "featureOne211"));
        lists.add(list);
        lists.add(list1);
        td.setSynonymWords(lists);
        thesaurusData.add(td);
        maoNiuGenerator.doGenerate(input, productAttrData, thesaurusData);

        List<Integer> set = new ArrayList<>();
        set.add(1);
        set.add(2);
        set.add(3);

        Random rand = new Random();
        List<Integer> wordList = rand.
                ints(2, 0, set.size()).
                mapToObj(i -> set.get(i)).
                collect(Collectors.toList());


    }
}
