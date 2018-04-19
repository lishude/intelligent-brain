package com.maoniu.core;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;
import com.maoniu.enums.IntelligentOrderSort;
import com.maoniu.utils.IntelligentMapUtil;
import com.maoniu.utils.IntelligentSetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/4/10.
 */
public class MaoNiuMatcher extends AbstractPreprocessorIntelligent implements IntelligentMatcher<KeywordData,KeywordData>{
    private Map<String,Integer> modelToCountMap = new HashMap<>();//型号 ==》 已发布产品数量
    private ListMultimap<String, ProductAttrData> classifyMultimap = ArrayListMultimap.create();//品类对应的产品属性数据
    private Map<String, KeywordData> idToKeywordMap = new HashMap<>();

    public MaoNiuMatcher(Map classify_keyword_map, Map classify_common_words_map, List prep_words) {
        super(classify_keyword_map, classify_common_words_map, prep_words);
    }

    @Override
    public List<KeywordData> doMatch(List<KeywordData> input, String position, List<ProductAttrData> productAttrData, List<ThesaurusData> thesaurusData, List<String>... coreWords) {
        prepare(input, productAttrData, thesaurusData);
        //将产品属性表进行 品类分类
        productAttrData.stream().forEach(product ->{
            modelToCountMap.put(product.getModel(), product.getReleasedCount());
            if(StringUtils.isNotEmpty(product.getClassify())){
                classifyMultimap.put(product.getClassify(), product);
            }
        });

        Map<Integer, Integer> positionMap = doWithPosition(position);
        //同一品类的数据归在一起
        List<List<KeywordData>> keywordCategoryList = getKeywordCategoryList(input, positionMap);
        //同一品类的数据去除自定义型号后归在一起
        List<List<KeywordData>> excludeCustomModelList = new ArrayList<>();
        //核心词 -》关键词列表
        ListMultimap<String, KeywordData> coreAndBestCategoryMultimap = ArrayListMultimap.create();
        //最佳类目+核心词归类
        List<List<KeywordData>> coreAndBestCategoryList = new ArrayList<>();
        //自定义型号数据
        List<KeywordData> customModelList = new ArrayList<>();
        keywordCategoryList.stream().forEach(out -> {
            List<KeywordData> nonCustomModelList = out.stream().filter(in -> {
                idToKeywordMap.put(in.getId(), in);
               if(CollectionUtils.isEmpty(in.getCustomModels())){
                   return true;
               }else{
                   customModelList.add(in);
                   return false;
               }
            }).collect(Collectors.toList());
            excludeCustomModelList.add(nonCustomModelList);
        });
        //外层表示同一品类
        //内层表示同一品类同一核心词+最佳类目
        excludeCustomModelList.stream().forEach(out -> {
            coreAndBestCategoryMultimap.clear();
            coreAndBestCategoryList.clear();
            out.stream().forEach(in -> {
                coreAndBestCategoryMultimap.put(in.getKeyword()+in.getBestCategory(), in);
            });
            coreAndBestCategoryMultimap.keySet().stream().forEach(key -> {
                coreAndBestCategoryList.add(coreAndBestCategoryMultimap.get(key));
            });
            sortByKeywordDataLengthDesc(coreAndBestCategoryList);
            //进一步数据处理（交差集匹配，这边假设关键词一定属于某个品类和核心词）
            doMatchDiffAndIntersection(coreAndBestCategoryList, thesaurusData);
        });

        //归类 非空形容词和空形容词
        List<KeywordData> emptyAdjList = new ArrayList<>();
        List<KeywordData> notEmptyAdjList = input.stream().filter(in -> {
            if(in.isEmptyAdj()){
                emptyAdjList.add(in);
                return false;
            }else{
                return true;
            }
        }).collect(Collectors.toList());
        //这里不要考虑uuid情况，因为这边只考虑匹配到什么型号就行了，不用关心型号是否连续的
        doWithEmptyAdjList(emptyAdjList);
        doWithCustomModelList(customModelList);
        doWithNotEmptyAdjList(notEmptyAdjList);
        return input;
    }

    private void doWithCustomModelList(List<KeywordData> customModelList) {
        ListMultimap<String, KeywordData> classifyDataMultiMap = ArrayListMultimap.create();
        int index = 0;
        for(KeywordData data : customModelList){
            idToKeywordMap.put(data.getId(), data);
            classifyDataMultiMap.put(""+index, data);
            index++;
        }
        doAssignModel(classifyDataMultiMap);
    }

    /**
     * 1.如果交差集都包含则视为完全匹配
     * 2.如果只完全包含交集+部分差集（取差集包含最多的）
     * 3.如果只包含差集，则将差集包含关系的内容，随机匹配一个型号（保证所有的关键词都能匹配到一个型号）
     * @param notEmptyAdjList
     */
    private void doWithNotEmptyAdjList(List<KeywordData> notEmptyAdjList) {
        //先过滤只有差集的数据
        List<KeywordData> excludeOnlyDiffList = new ArrayList<>();
        ListMultimap<String, KeywordData> onlyDiffMultimap = ArrayListMultimap.create();
        List<KeywordData> excludeDiffNotEmptyAdjList = notEmptyAdjList.stream().filter(in -> {
            if(in.getIntersectionSet().size() <= 0 && in.getDiffSet().size() > 0){
                onlyDiffMultimap.put(in.getClassify(), in);
                return true;
            }
            excludeOnlyDiffList.add(in);
            return false;
        }).collect(Collectors.toList());
        //匹配只有差集的型号
        doMatchModelOnlyDiffMultimap(onlyDiffMultimap);
        doMatchModelNotEmptyList(excludeDiffNotEmptyAdjList);
        ListMultimap<String, KeywordData> classifyDataMultiMap = ArrayListMultimap.create();
        excludeDiffNotEmptyAdjList.stream().forEach(in -> {
            classifyDataMultiMap.put(in.getClassify(), in);
        });
        doAssignModel(classifyDataMultiMap);
    }

    private void doAssignModel(ListMultimap<String, KeywordData> classifyDataMultiMap) {
        classifyDataMultiMap.keySet().stream().forEach(key -> {
            List<KeywordData> datas = classifyDataMultiMap.get(key);
            Set<String> totalValidSet = new HashSet<>();
            SetMultimap<String, String> innerRespList =  TreeMultimap.create();
            datas.stream().forEach(data -> {
                if(data.getMatchModelSet().size() > 0){
                    data.getMatchModelSet().stream().forEach(model -> {
                        innerRespList.put(model.trim(), data.getId());
                        totalValidSet.add(data.getId());
                    });
                }
            });
            Set<String> union = new LinkedHashSet<>();//创建并集
            Map<String, Integer> filterMap = innerRespList.keySet().stream().filter(model -> {
                if(modelToCountMap.keySet().contains(model)){
                    return true;
                }
                return false;
            }).collect(Collectors.toMap(p -> p, p -> modelToCountMap.get(p)));
            if(filterMap.size() > 0){
                while(totalValidSet.size() != union.size()){
                    //排序过滤后的map，用于保证每个型号抽取的概率是平等的
                    Map<String,Integer> sortMap = IntelligentMapUtil.sortByValue(filterMap, IntelligentOrderSort.ASC);
                    String model = sortMap.entrySet().iterator().next().getKey();
                    Set<String> sortValue = innerRespList.get(model);//获取某个型号的所有关键词集合
                    Set<String> differenceSet = IntelligentSetUtils.twoDifference(sortValue, union);
                    if(differenceSet.size() <=0 ){//则说明该型号的数据都已经匹配完了
                        filterMap.put(model,Integer.MAX_VALUE);
                        continue;
                    }
                    ImmutableSet<String> differenceImmutableSet = getImmutableSet(idToKeywordMap, differenceSet);
                    if(differenceImmutableSet.size() == 1){
                        KeywordData keywordData = idToKeywordMap.get(new ArrayList<String>(differenceImmutableSet).get(0));
                        keywordData.setModel(model);
                        union = IntelligentSetUtils.twoUnion(union, differenceImmutableSet);
                        addModelCount(model, filterMap);
                    }else if(differenceImmutableSet.size() == 2){
                        realAssignModel(new ArrayList<String>(differenceImmutableSet), model, filterMap);
                        union =  IntelligentSetUtils.twoUnion(union, differenceImmutableSet);
                    }else if(differenceImmutableSet.size() > 2){
                        List<String> subList = new ArrayList<String>(differenceImmutableSet).subList(0, 3);
                        realAssignModel(subList, model, filterMap);
                        union =  IntelligentSetUtils.twoUnion(union, new HashSet(subList));
                    }
                }
            }

        });

    }

    /**
     * 真正的赋值操作
     * @param intersectionSet
     * @param model
     */
    private void realAssignModel(List<String> intersectionSet, String model,  Map<String, Integer> map) {
        for(String key : intersectionSet){
            KeywordData keywordData  = idToKeywordMap.get(key);
            keywordData.setModel(model);
        }
        addModelCount(model, map);
    }

    /**
     * 转换为有排序好的set
     * @param idToKeywordRespMap
     * @param sortValue
     * @return
     */
    private ImmutableSet<String> getImmutableSet(final Map<String, KeywordData> idToKeywordRespMap, Set<String> sortValue) {
        ImmutableSet<KeywordData> immutableSet = FluentIterable.from(sortValue).transform(new Function<String, KeywordData>() {
            @Override
            public KeywordData apply(String input) {
                return idToKeywordRespMap.get(input);
            }
        }).toSet();

        List<KeywordData> immutableList = new ArrayList<>(immutableSet);
        List<KeywordData> resultList = new ArrayList<>();

        Collections.sort(immutableList, new Comparator<KeywordData>() {
            @Override
            public int compare(KeywordData o1, KeywordData o2) {
                return o1.getSort() - o2.getSort();
            }
        });

        return FluentIterable.from(immutableList).transform(new Function<KeywordData, String>() {
            @Override
            public String apply(KeywordData input) {
                return input.getId();
            }
        }).toSet();
    }

    private void doMatchModelNotEmptyList(List<KeywordData> notEmptyAdjList) {
        notEmptyAdjList.stream().forEach(in -> {
            Set<String> bestMatchModels = new HashSet<>();
            ListMultimap<Integer, String> keyDescMultimap = MultimapBuilder.treeKeys((x1, x2) -> {
                return-(Integer.valueOf(String.valueOf(x1)).intValue() - Integer.valueOf(String.valueOf(x2)).intValue());
            }).arrayListValues().build();
            Set<String> goodMatchModels = new HashSet<>();
            List<ProductAttrData> datas = classifyMultimap.get(in.getClassify());
            datas.stream().forEach(data ->{
                if(!CollectionUtils.isEmpty(data.getCompositeSet())){
                    //交集和差集匹配
                    if(!CollectionUtils.isEmpty(in.getIntersectionAndDiffSet())){
                        Set<String> bestMatchDiffSet = IntelligentSetUtils.twoDifference(in.getIntersectionAndDiffSet(), data.getCompositeSet());
                        if(bestMatchDiffSet.size() == 0){
                            bestMatchModels.add(data.getModel());
                        }else{//这边先比较是否交集都能完全匹配，如果完全匹配在匹配差集数量
                            Set<String> goodMatchDiffSet = IntelligentSetUtils.twoDifference(in.getIntersectionSet(), data.getCompositeSet());
                            if(goodMatchDiffSet.size() == 0){
                                goodMatchModels.add(data.getModel());//先把交集放到good中，防止better没有值
                                Set<String> betterMatchIntersectionSet = IntelligentSetUtils.twoIntersection(in.getDiffSet(), data.getCompositeSet());
                                keyDescMultimap.put(betterMatchIntersectionSet.size(), data.getModel());
                            }
                        }
                    }else{//这边只剩下交集情况，因为只有差集的已经排除
                        //交集匹配
                        Set<String> goodMatchDiffSet = IntelligentSetUtils.twoDifference(in.getIntersectionSet(), data.getCompositeSet());
                        if(goodMatchDiffSet.size() == 0){
                            goodMatchModels.add(data.getModel());
                        }
                    }
                }
            });
            if(bestMatchModels.size() > 0){
                in.setMatchModelSet(bestMatchModels);
            }else if(keyDescMultimap.size() > 0){
                Set<String> betterMatchModels = new HashSet<>(keyDescMultimap.asMap().entrySet().iterator().next().getValue());
                in.setMatchModelSet(bestMatchModels);
            }else{
                in.setMatchModelSet(goodMatchModels);
            }

        });
    }

    private void doMatchModelOnlyDiffMultimap(ListMultimap<String, KeywordData> onlyDiffMultimap) {
        onlyDiffMultimap.keySet().stream().forEach(key -> {
            Set<String> existSet = new HashSet<>();
            List<KeywordData> onlyDiffList = onlyDiffMultimap.get(key);
            Collections.sort(onlyDiffList, (k1, k2) -> {
                int length1 = k1.getOmittedName().split(SPACE_PLUS).length;
                int length2 = k1.getOmittedName().split(SPACE_PLUS).length;
                return -(length1 - length2);
            });
            for(KeywordData out : onlyDiffList){
                if(existSet.contains(out.getId()))
                    continue;
                List<KeywordData> togetherList = new ArrayList<>();
                existSet.add(out.getId());
                togetherList.add(out);
                String outOmittedName = out.getOmittedName();
                for(KeywordData in : onlyDiffList){
                    if(out.getId().equals(in.getId()))
                        continue;
                    if(existSet.contains(in.getId()))
                        break;
                    String inOmittedName = in.getOmittedName();
                    Set<String> outSet = new HashSet<>(Arrays.asList(outOmittedName.split(SPACE_PLUS)));
                    Set<String> inSet = new HashSet<>(Arrays.asList(inOmittedName.split(SPACE_PLUS)));
                    if(IntelligentSetUtils.twoDifference(outSet, inSet).size() == 0){
                        existSet.add(in.getId());
                        togetherList.add(in);
                        outOmittedName = inOmittedName;
                    }
                }
                Map<String, Integer> map = getModelAndCountMapByClassify(togetherList.stream().findAny().get().getClassify());
                if(map.size() > 0){
                    String randomModel = IntelligentMapUtil.getRandomKey(map);
                    togetherList.stream().forEach(together -> {
                        together.setModel(randomModel);
                        addModelCount(randomModel, null);
                    });
                }
            }
        });

    }

    private void doWithEmptyAdjList(List<KeywordData> emptyAdjList) {
        emptyAdjList.stream().forEach(in -> {
            Map<String, Integer> map = getModelAndCountMapByClassify(in.getClassify());
            String randomModel = IntelligentMapUtil.getRandomKey(map);
            in.setModel(randomModel);
            addModelCount(randomModel, null);
        });

    }

    /**
     * 根据匹配获取型号和已发布产品数映射
     * @param classify
     * @return
     */
    private Map<String, Integer> getModelAndCountMapByClassify(String classify) {
        List<ProductAttrData> list = classifyMultimap.get(classify);
        return list.stream().collect(Collectors.toMap(ProductAttrData::getModel, ProductAttrData::getReleasedCount, (oldValue, newValue) -> oldValue));
    }

    private void addModelCount(String model, Map<String, Integer> map) {
        int newCount = modelToCountMap.get(model).intValue() + 1;
        modelToCountMap.put(model, newCount);
        if(null != map){
            map.put(model, newCount);
        }
    }

    private void doMatchDiffAndIntersection(List<List<KeywordData>> coreAndBestCategoryList, List<ThesaurusData> thesaurusData){
        coreAndBestCategoryList.stream().forEach(out -> {
            out.stream().forEach(in -> {
                if(StringUtils.isNotEmpty(in.getKeyword())){
                    Optional<ThesaurusData> thesaurusDataOptional = thesaurusData.stream().filter(thesaurus -> {
                        if(thesaurus.getClassify().equalsIgnoreCase(in.getClassify())){
                            return true;
                        }
                        return false;
                    }).findFirst();
                    if(thesaurusDataOptional.isPresent()){
                        ThesaurusData data = thesaurusDataOptional.get();
                        //查询词组
                        Set<String> wordGroupSet = findWordGroup(in, data);
                        //先将词组去除，然后在加进来，这样就不会有问题, 这边要考虑通用词
                        Set<String> adjectives = omitStemAndWordGroup(in, thesaurusDataOptional.get(), wordGroupSet);
                        if(!CollectionUtils.isEmpty(wordGroupSet) || !CollectionUtils.isEmpty(adjectives)){
                            Set<String> intersectionAndDiffSet = new HashSet<>();
                            //交集
                            Set<String> intersectionSet = IntelligentSetUtils.twoIntersection(adjectives, data.getCharacteristicWords());
                            //将词组加入到交集中
                            if(wordGroupSet.size() > 0){
                                intersectionSet.addAll(wordGroupSet);
                            }
                            //差集
                            Set<String> diffSet = IntelligentSetUtils.twoDifference(adjectives, data.getCharacteristicWords());
                            if(intersectionSet.size() >0){
                                in.setIntersectionSet(intersectionSet);
                            }
                            if(diffSet.size() > 0){
                                in.setDiffSet(diffSet);
                            }
                            if(intersectionAndDiffSet.size() > 0 && diffSet.size() > 0){
                                intersectionAndDiffSet.addAll(intersectionSet);
                                intersectionAndDiffSet.addAll(diffSet);
                                in.setIntersectionAndDiffSet(intersectionAndDiffSet);
                            }
                            if(intersectionSet.size() <= 0 && diffSet.size() <= 0){
                                in.setEmptyAdj(true);
                            }
                        }else if(!CollectionUtils.isEmpty(wordGroupSet)){
                            in.setIntersectionSet(wordGroupSet);
                        }else{
                            in.setEmptyAdj(true);
                        }
                    }
                }

            });

        });
    }

    /**
     * 将智能整理后数据进行处理
     * @param position
     * @return
     */
    private Map<Integer, Integer> doWithPosition(String position) {
        String[] posArr = position.split("#");
        Map<Integer,Integer> map = new LinkedHashMap<Integer, Integer>();
        for(String pos : posArr){
            if(StringUtils.isEmpty(pos)){
                continue;
            }
            String[] innerArr = pos.split(",");
            Integer start = Integer.valueOf(innerArr[0].trim());
            Integer end = Integer.valueOf(innerArr[1].trim());
            map.put(start, end);
        }
        return map;
    }

    private List<List<KeywordData>> getKeywordCategoryList(List<KeywordData> input, Map<Integer, Integer> map) {
        //用于获取唯一存在的值
        Map<Integer, Integer> missMap = new TreeMap<>();
        List<List<KeywordData>> lists = new ArrayList<>();
        //将同一归类的关键词放到一个List
        int currentKey = 0;
        for(Integer key : map.keySet()) {
            if(key != currentKey){
                missMap.put(currentKey, key);
            }
            lists.add(input.subList(key, map.get(key)));
            currentKey = map.get(key);
        }
        if(currentKey != input.size()){
            missMap.put(currentKey, input.size());
        //    missMap.put(input.size(), input.size() + 1);
        }
        for(Integer missKey : missMap.keySet()){
            lists.add(input.subList(missKey, missMap.get(missKey)));
        }
        return lists;
    }

    public static void main(String[] args) {
        Map<String, List<String>> map = new HashMap<>();
        MaoNiuMatcher matcher = new MaoNiuMatcher(map, null, null);
        List<KeywordData> input = new ArrayList<>();
        for(int i= 1; i <= 40; i++){
            KeywordData keywordData = new KeywordData();
            keywordData.setId(""+i);
            input.add(keywordData);
        }
        Map<Integer, Integer> map1 = new TreeMap<>();
        map1.put(0, 5);
        map1.put(10, 15);
        map1.put(15, 20);
        map1.put(25, 30);
        map1.put(30, 35);
        matcher.getKeywordCategoryList(input, map1);
    }

    private void sortByKeywordDataLengthDesc(List<List<KeywordData>> lists) {
        Collections.sort(lists, new Comparator<List<KeywordData>>() {
            @Override
            public int compare(List<KeywordData> o1, List<KeywordData> o2) {
                return -(o1.size() - o2.size());
            }
        });
    }

    private Set<String> findWordGroup(KeywordData keywordData, ThesaurusData thesaurusData) {
        Set<String> wordGroupSet = new HashSet<>();
        if(!CollectionUtils.isEmpty(thesaurusData.getWordGroups())){
            thesaurusData.getWordGroups().stream().forEach(wordGroup -> {
                String copy = wordGroup;
                wordGroup = getReplacePointStr(wordGroup);
                if(keywordData.getName().contains(wordGroup)){
                    String name = getReplacePointStr(keywordData.getName());
                    Pattern p = Pattern.compile(BOUNDARY+wordGroup+BOUNDARY);
                    Matcher m = p.matcher(name);
                    if(m.find()){
                        wordGroupSet.add(copy.trim());
                    }
                }
            });
        }
        return wordGroupSet;
    }

    private String getReplacePointStr(String input) {
        if(input.contains(".")){
            input = input.replaceAll("\\.", "maoniu");
        }
        return input;
    }

    private Set<String> omitStemAndWordGroup(KeywordData keywordData, ThesaurusData thesaurusData, Set<String> wordGroupSet){
        String result = keywordData.getName();
        if(StringUtils.isNotEmpty(result)){
            //删除通用词
            Set<String> commonWordGroup = new HashSet<>(thesaurusData.getCommonWords());
            if(commonWordGroup.size() <= 0){
                commonWordGroup.addAll(common_words);
            }
            if(!CollectionUtils.isEmpty(commonWordGroup)){
                for(String cwg : commonWordGroup){
                    result = result.replaceAll(BOUNDARY+cwg+BOUNDARY, "");
                }
            }
        }
        if(StringUtils.isNotEmpty(result)){
            //删除介词
            Set<String> prepWords = new HashSet<>(prep_words);
            if(!CollectionUtils.isEmpty(prepWords)){
                for(String cwg : prepWords){
                    result = result.replaceAll(BOUNDARY+cwg+BOUNDARY, "");
                }
            }
        }
        //将消除通用词和介词的关键词赋值
        keywordData.setOmittedName(result);
        //删除核心词
         result =  keywordData.getName().replaceAll(BOUNDARY+keywordData.getKeyword()+BOUNDARY, "");
         //如果这边没有删除掉核心词的话，说明核心词在关键词中被分开了
         if(result.equalsIgnoreCase(keywordData.getName())){
             for(String single : keywordData.getKeyword().split(SPACE_PLUS)){
                 result =  keywordData.getName().replaceAll(BOUNDARY+single+BOUNDARY, "");
             }
         }
        //删除词组
        if(!CollectionUtils.isEmpty(wordGroupSet)){
            for(String wordGroup : wordGroupSet){
                result = result.replaceAll(BOUNDARY+wordGroup+BOUNDARY, "");//删除词组
            }
        }

        if(StringUtils.isEmpty(result.trim()))
            return new HashSet<String>();

        Set<String> newResp = new HashSet<String>(Arrays.asList(result.trim().split(SPACE_PLUS)));
        //把词组当做一个词再加入进去
        if(wordGroupSet.size() > 0)
            newResp.addAll(wordGroupSet);
        return newResp;
    }

    @Override
    public List<KeywordData> output(List<KeywordData> t) {
        return t;
    }

}
