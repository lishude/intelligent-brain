package com.maoniu.core;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;
import com.maoniu.utils.IntelligentSetUtils;
import com.maoniu.utils.IntelligentUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/4/11.
 */
public class MaoNiuGenerator extends AbstractPreprocessorIntelligent implements IntelligentGenerator<KeywordData, KeywordData>{
    private Map<String, List<List<String>>> synonymMap = new HashMap<>();
    private Map<String, ProductAttrData> classifyModelMap = new HashMap<>();
    private Map<String, List<String>> classifyWordGroupMap = new HashMap<>();
    private final int TOTAL_KEYWORD_NUM = 10;

    public MaoNiuGenerator(Map classify_keyword_map, Map classify_common_words_map, List prep_words) {
        super(classify_keyword_map, classify_common_words_map, prep_words);
    }


    @Override
    public List<KeywordData> doGenerate(List<KeywordData> input, List<ProductAttrData> productAttrData, List<ThesaurusData> thesaurusData) {
        prepare(input, productAttrData, thesaurusData, null);
        //这里假设更新型号或者模板生成标题，或者刷新标题他们的是按照品类分类好的
        classifyModelMap = productAttrData.stream().filter(p -> StringUtils.isNotEmpty(p.getModel()) && StringUtils.isNotEmpty(p.getClassify())).collect(Collectors.toMap(p -> p.getClassify() + p.getModel(), p -> p));
        classifyWordGroupMap = thesaurusData.stream().filter(p -> !CollectionUtils.isEmpty(p.getWordGroups())).collect(Collectors.toMap(p -> p.getClassify(), p -> p.getWordGroups()));
        synonymMap = thesaurusData.stream().filter(p -> !CollectionUtils.isEmpty(p.getSynonymWords())).collect(Collectors.toMap(p -> p.getClassify(), p -> p.getSynonymWords()));
        List<KeywordData> hasPrepList = new ArrayList<>();
        String copyTitle = "";
        //按照介词，然后热度顺序
        Collections.sort(input, (k1, k2) -> {
            int prep1 = getContainPrep(k1);
            int prep2 = getContainPrep(k2);
            if(prep1 - prep2 > 0){
                return -1;
            }else if(prep1 - prep2 < 0){
                return 1;
            }else{
                return -(k1.getHeat().intValue() - k2.getHeat().intValue());
            }
        });
        String title = "";
        if(input.size() <= 1){
            title = input.stream().findAny().orElseGet(() -> new KeywordData()).getName();
        }else{
            //这边先把包含介词给提取出来（如果关键词包含介词，则该关键词不参与后面的添加形容词，并且在该关键词前面加逗号）
            List<KeywordData> notPrepList = input.stream().filter(kd -> {
                if(kd.isHasPrep()){
                    hasPrepList.add(kd);
                    return false;
                }
                return true;
            }).collect(Collectors.toList());
            if(null != notPrepList && notPrepList.size() > 0){
                doWithSynonym(notPrepList);
                List<String> list1 = new ArrayList<String>(Arrays.asList(notPrepList.get(0).getName().split(SPACE_PLUS)));
                Map<String, String> map = new HashMap<String, String>();
                for(String s : list1){
                    map.put(s, s);
                }

                int size = input.size();
                for(KeywordData keywordData : input.subList(1, size)){
                    List<String> list2 = new ArrayList<String>(Arrays.asList(keywordData.getName().split(SPACE_PLUS)));
                    generateListResult(list1, map, list2);
                }
                title = doAssembleTitle(list1);
                copyTitle = title;
                title = toUpperCaseFirstOne(title);
            }
            List<String> nameList = hasPrepList.stream().map(kd -> toUpperCaseFirstOne(kd.getName())).collect(Collectors.toList());
            if(StringUtils.isNotEmpty(title)){
                nameList.add(0, title);
            }
            title = String.join(",", nameList);
        }
        List<String> intersectionAndDiffs = new ArrayList<>();
        input.stream().forEach(kd -> {
            if(kd.getIntersectionSet().size() > 0){
                intersectionAndDiffs.addAll(kd.getIntersectionSet());
            }else if(kd.getDiffSet().size() > 0){
                intersectionAndDiffs.addAll(kd.getDiffSet());
            }else{
                if(StringUtils.isNotEmpty(kd.getKeyword())){
                    Optional<ThesaurusData> thesaurusDataOptional = thesaurusData.stream().filter(thesaurus -> {
                        if(thesaurus.getClassify().equalsIgnoreCase(kd.getClassify())){
                            return true;
                        }
                        return false;
                    }).findFirst();
                    if(thesaurusDataOptional.isPresent()){
                        Set<String> adjs = IntelligentUtil.omitStemAndWordGroup(kd, thesaurusDataOptional.get(), new HashSet<>());
                        intersectionAndDiffs.addAll(adjs);
                    }

                }
            }

        });
        KeywordData firstElement = input.stream().findFirst().get();
        //这边总数= 10 - prepNum
        int prepKeywordNum = hasPrepList.stream().mapToInt(kd -> kd.getName().split(SPACE_PLUS).length).sum();
        int excludePrepKeywordNum = copyTitle.split(SPACE_PLUS).length;
        int currentKeywordNum = prepKeywordNum + excludePrepKeywordNum;
        ProductAttrData matchedProductAttrData = classifyModelMap.get(firstElement.getClassify()+firstElement.getLowerCaseModel());
        List<String> finalAddRandomStr = new ArrayList<>();
        if(currentKeywordNum < TOTAL_KEYWORD_NUM && Objects.nonNull(matchedProductAttrData)){
            Set<String> featureOneSet = matchedProductAttrData.getFeatureOnes();
            Set<String> featureTwoSet = matchedProductAttrData.getFeatureTwos();
            Set<String> filterDuplicationFeatureOneSet = IntelligentSetUtils.twoDifference(featureOneSet, new HashSet(intersectionAndDiffs));
            Set<String> filterDuplicationFeatureTwoSet = IntelligentSetUtils.twoDifference(featureTwoSet, new HashSet(intersectionAndDiffs));
            Set<String> filterDuplicationFeatureTwoInFeatureOneSet = IntelligentSetUtils.twoDifference(filterDuplicationFeatureTwoSet, filterDuplicationFeatureOneSet);

            List<String> randomAddSynonymFeatureOneSet = randomAddSynonymToSet(filterDuplicationFeatureOneSet, firstElement.getClassify());
            List<String> randomAddSynonymFeatureTwoSet = randomAddSynonymToSet(filterDuplicationFeatureTwoInFeatureOneSet, firstElement.getClassify());

            List<String> featureOneRandom = new ArrayList<>();
            List<String> featureTwoRandom = new ArrayList<>();
            if(currentKeywordNum <= 6){
                featureOneRandom = getRandomList(randomAddSynonymFeatureOneSet, 2);
                featureTwoRandom = getRandomList(randomAddSynonymFeatureTwoSet, 2);
            }else if(currentKeywordNum == 7){
                featureOneRandom = getRandomList(randomAddSynonymFeatureOneSet, 2);
                featureTwoRandom = getRandomList(randomAddSynonymFeatureTwoSet, 1);
            }else if(currentKeywordNum == 8){
                featureOneRandom = getRandomList(randomAddSynonymFeatureOneSet, 1);
                featureTwoRandom = getRandomList(randomAddSynonymFeatureTwoSet, 1);
            }else if(currentKeywordNum == 9){
                featureOneRandom = getRandomList(randomAddSynonymFeatureOneSet, 1);
            }
            finalAddRandomStr.addAll(featureOneRandom);
            finalAddRandomStr.addAll(featureTwoRandom);
        }
        sort(input, finalAddRandomStr, title);
        return input;
    }

    @Override
    public void sort(List<KeywordData> t, List<String> beAddedList, String intermediateTitle) {
        if(beAddedList.isEmpty()){
            t.forEach(kd -> kd.setTitle(toUpperCaseFirstOne(String.join(" ", intermediateTitle))));
        }else{
            //这边只是单纯的把添加的词加到中间体标题前面
            String beAddedStr = toUpperCaseFirstOne(String.join(" ", beAddedList));
            String finalTitle = String.join(" ", beAddedStr, intermediateTitle);
            t.forEach(kd -> kd.setTitle(finalTitle));
        }

    }

    private List<String> getRandomList(List<String> list, int size){
        if(size > list.size())
            return list;
        //分为当个单词和单词组
        List<String> singleList = new ArrayList<>();
        List<String> doubleUpList = list.stream().filter(str -> {
            if(str.split(SPACE_PLUS).length > 1){
                return true;
            }
            singleList.add(str);
            return false;
        }).collect(Collectors.toList());
        //如果只需要添加一个，则只需要从单词列表中随机返回就行
        if(size == 1){
            if(singleList.isEmpty())
                return singleList;
            Collections.shuffle(singleList);
            return singleList.subList(0, size);
        }else{
            Collections.shuffle(list);//这边对原数据进行随机，保证抽中单词和单词组的概率是一样的
            if(list.get(0).split(SPACE_PLUS).length > 1){//如果直接抽中单词组直接返回
                return list.subList(0, 1);
            }else{
                if(singleList.size() > 1){//这边要保证要足够的单词列表可以添加
                    Collections.shuffle(singleList);
                    return singleList.subList(0, size);
                }else{
                    if(doubleUpList.size() > 0){
                        Collections.shuffle(doubleUpList);
                        return doubleUpList.subList(0, 1);
                    }else{
                        return singleList;
                    }
                }
            }
        }
    }


    private List<String> randomAddSynonymToSet(Set<String> set, String classify){
        List<String> list =  new ArrayList<>(set);
        List<List<String>> synonymList = synonymMap.get(classify);
        if(CollectionUtils.isEmpty(synonymList))
            return new ArrayList<String>(set);
        Map<String, List<String>> map = new HashMap<>();
        for(List<String> synonym : synonymList){
            for(String str : list){
                if(synonym.contains(str)){
                    map.put(str, synonym);
                }
            }
        }

        for (Iterator<String> i = list.iterator(); i.hasNext();) {
            String item = i.next();
            if(null != map.get(item)){
                i.remove();
            }
        }
        for(List<String> value : map.values()){
            Collections.shuffle(value);
            list.add(value.stream().findAny().get());
        }
        return list;
    }

    private  String toUpperCaseFirstOne(String s)
    {
        if(StringUtils.isNotEmpty(s)){
            String[] arr = s.split(SPACE_PLUS);
            List<String> list = new ArrayList<String>();
            for(String str : arr){
                if(str.length() == 1){
                    str = str.toUpperCase();
                }else if(str.length() > 1){
                    str = (new StringBuilder()).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1).toLowerCase()).toString();
                }
                list.add(str);
            }
            return Joiner.on(" ").appendTo(new StringBuilder(), list).toString();
        }
        return "";
    }

    private void generateListResult(List<String> base, Map<String, String> map, List<String> append) {
        List<String> sub = new ArrayList<String>();
        int index = -1;
        for(String s : append){
            if(null == map.get(s) && index == -1){
                sub.add(s);
            }else{
                int temp = index;
                if(sub.size() > 0){// 假设 base = a b c， 如果append = e f c，则执行这些操作
                    Collections.reverse(sub);
                    for(String subString : sub){
                        base.add(0, subString);
                        map.put(subString, subString);
                    }
                    sub.clear();
                }
                index = base.indexOf(s);
                if(index == -1){
                    base.add(temp + 1, s);
                    map.put(s, s);
                    index = temp + 1;
                }
                if(index < temp){
                    index = temp;
                }
            }
        }
        if(sub.size() > 0){
            for(String str : sub){
                map.put(str, str);
                base.add(str);
            }
        }
    }

    private String doAssembleTitle(List<String> result){
        StringBuilder sb1 = new StringBuilder();
        return Joiner.on(" ").appendTo(sb1, result).toString();
    }

    /**
     * 将同义词用单斜杠分隔
     * @param subList
     */
    private void doWithSynonym(List<KeywordData> subList) {
        Multimap<String, String> map = ArrayListMultimap.create();
        for(KeywordData k : subList){
            String keyword = k.getName();
            if(StringUtils.isNotEmpty(k.getClassify())){
                List<List<String>> synonymLists = synonymMap.get(k.getClassify());
                if(null != synonymLists && synonymLists.size() > 0){
                    for(List<String> synonymList : synonymLists){
                        findSynonym(synonymList, keyword, map);
                    }
                }
            }
        }
        replaceSynonymWithSlash(subList, map);
    }

    /**
     * 用斜杠替换同义词、
     * @param subList
     * @param map
     */
    private void replaceSynonymWithSlash(List<KeywordData> subList, Multimap<String, String> map) {
        String separator = "maoniu_slash";//用于同一个关键词中有同义词情况
        for(String key : map.keySet()){
            Collection<String> values = map.get(key);
            Set<String> onlySet = new HashSet<String>(values);
            if(onlySet.size() > 1){
                String result = Joiner.on(separator).join(onlySet);
                for(KeywordData k : subList){
                    String keyword = k.getName();
                    Map<String, String> replaceMap = new HashMap<String, String>();
                    for(String value : onlySet){
                        if(keyword.contains(value)){
                            Matcher m = getMatcher(keyword, value);
                            while (m.find()){
                                replaceMap.put(value,result);
                            }
                        }
                    }
                    if(!replaceMap.isEmpty()){
                        for(String replace : replaceMap.keySet()){
                            Pattern p = Pattern.compile(BOUNDARY+replace+BOUNDARY);
                            Matcher m = p.matcher(keyword);
                            while (m.find()){
                                keyword = keyword.replaceAll(BOUNDARY+replace+BOUNDARY, replaceMap.get(replace));
                            }
                        }
                        keyword = keyword.replaceAll(separator, "/");
                        keyword = omitDuplication(keyword);
                        k.setKeyword(keyword);
                    }
                }
            }
        }
    }

    private Matcher getMatcher(String keyword, String value) {
        Pattern p = Pattern.compile(BOUNDARY+value+BOUNDARY);
        return p.matcher(keyword);
    }

    private String omitDuplication(String keyword){
        if(StringUtils.isEmpty(keyword))
            return keyword;
        String[] keywords = keyword.split(SPACE_PLUS);
        List<String> list = new ArrayList<String>();
        for(String k : keywords){
            if(!list.contains(k))
                list.add(k);
        }
        return Joiner.on(" ").join(list);
    }

    /**
     * 消除同义词
     * @param subList
     */
    private void omitSynonym(List<KeywordData> subList) {
        Map<String,Integer> resultMap = new HashMap<String, Integer>();
        for(KeywordData k : subList){
            String keyword = k.getName();
            if(StringUtils.isNotEmpty(k.getClassify())){
                List<List<String>> synonymLists = synonymMap.get(k.getClassify());
                if(null != synonymLists && synonymLists.size() > 0){
                    int i = 0;
                    for(List<String> synonymList : synonymLists){
                        Map<String, Integer> map = new HashMap<String, Integer>();
                        findSynonym(synonymList, keyword, map, i);
                        if(map.size() > 0){
                            if(resultMap.size() <= 0){
                                resultMap.putAll(map);
                            }else{
                                for(String key : map.keySet()){
                                    Integer value = map.get(key);
                                    if(resultMap.containsValue(value)){
                                        for(String outKey : resultMap.keySet()){
                                            if(value.intValue() == resultMap.get(outKey).intValue()){
                                                keyword = keyword.replaceAll(BOUNDARY+key+BOUNDARY, outKey);
                                                k.setKeyword(keyword);
                                                break;
                                            }
                                        }
                                    }else{
                                        resultMap.put(key, map.get(key));
                                    }

                                }
                            }
                        }
                        i++;
                    }
                }

            }

        }
    }

    private void findSynonym(List<String> synonymList,String keyword, Map<String, Integer> map, int code) {
        if(null != synonymList && synonymList.size() > 0){
            for(String str : synonymList){
                if(keyword.contains(str)){
                    Pattern p = Pattern.compile(BOUNDARY+str+BOUNDARY);
                    Matcher m = p.matcher(keyword);
                    if(m.find()){
                        map.put(str, code);
                    }
                }
            }
        }
    }

    private void findSynonym(List<String> synonymList,String keyword, Multimap<String, String> map) {
        if(null != synonymList && synonymList.size() > 0){
            for(String str : synonymList){
                if(keyword.contains(str)){
                    Pattern p = Pattern.compile(BOUNDARY+str+BOUNDARY);
                    Matcher m = p.matcher(keyword);
                    if(m.find()){
                        map.put(Joiner.on("/").join(synonymList), str);
                    }
                }
            }
        }
    }

    private int getContainPrep(KeywordData data) {
        int prep1 = 0;
        String[] keywordArray = data.getName().toLowerCase().trim().split(SPACE_PLUS);
        for(String prep : keywordArray){
            if(prep_words.contains(prep)){
                if(prep1 == 0){
                    data.setHasPrep(true);
                    prep1 = 1;
                }
            }
        }
        return prep1;
    }

    @Override
    public List<KeywordData> output(List<KeywordData> t) {
        return t;
    }


}
