package com.maoniu.core;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.maoniu.entity.MaoniuPendingData;
import com.maoniu.enums.OrderSort;
import com.maoniu.utils.MapUtil;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2018/4/9.
 */
public class MaoNiuCollator extends AbstractIntelligent implements IntelligentCollator<MaoniuPendingData, Object>{
    private final String SLASH = "SLASH";


    public MaoNiuCollator(@NotNull Map<String, List<String>> classify_keyword_map, @Nullable Map<String, List<String>> classify_common_words_map, @Nullable List<String> prep_words){
        super(classify_keyword_map, classify_common_words_map, prep_words);
    }


    @Override
    public List<MaoniuPendingData> preposition(List<MaoniuPendingData> input) {
        //先对数据进行排序
        Collections.sort(input, new MaoNiuComparator());
        //如果品类有定义通用词
        if(null != classify_common_words_map && classify_common_words_map.size() > 0){
            Set<String> keySet = classify_common_words_map.keySet();
            //品类 -》 核心词，品类 -》通用词 转化为 核心词 -》通用词
            keySet.stream().forEach(k -> {
                List<String> keywordList = (List<String>)classify_keyword_map.get(k);
                Collections.sort(keywordList, (o1, o2) -> {
                    return -(o1.split(SPACE_PLUS).length - o2.split(SPACE_PLUS).length);
                });
                if(null == keyword_common_words_map){
                    keyword_common_words_map = new HashMap<>();
                }
                keywordList.stream().forEach(k1 -> {
                    keyword_common_words_map.put(k1, classify_common_words_map.get(k));
                });
            });
            //对待处理的数据进行核心词赋值
            input.stream().forEach(in -> {
                List<String> keywords = (List<String>)classify_keyword_map.get(in);
                String keyword = getMatchKeyword(keywords, in.getOrigin());
                in.setKeyword(keyword);
            });

        }
        //消除通用词和介词
        omitCommonAndPrepWords(input);
        //消除相同数据（去除通用词和介词后词数相同但是顺序不一样比如 a b c 和 a c b/ b c a）
        return omitSamePendingData(input);
    }



    @Override
    public List<MaoniuPendingData> postposition(List<MaoniuPendingData> input) {
        return null;
    }

    @Override
    public void cleanup(@NotNull List<MaoniuPendingData> input) {
        Assert.notEmpty(input);
        //前置处理
        preposition(input);
        //进行核心词归类，并进行核心词数量多少进行排序（这边需要排除不包含核心词的数据）
        Map<String, ListMultimap<Integer, MaoniuPendingData>> pendingMap = classify(input);


    }

    private Map<String, ListMultimap<Integer, MaoniuPendingData>> classify(List<MaoniuPendingData> input) {
        Map<String, ListMultimap<Integer, MaoniuPendingData>> lists = new HashMap<>();
        for(MaoniuPendingData data : input){
            if(StringUtils.isEmpty(data.getKeyword()))
                continue;
            ListMultimap<Integer, MaoniuPendingData> elementList = lists.get(data.getKeyword());
            if(null == elementList){
                elementList = ArrayListMultimap.create();
                lists.put(data.getKeyword(), elementList);
                elementList.put(data.getTarget().split(SPACE_PLUS).length, data);
            }else{
                elementList.put(data.getTarget().split(SPACE_PLUS).length, data);
            }
        }
        return MapUtil.sortByMultiSize(lists, OrderSort.ASC);

    }



    private String getMatchKeyword(List<String> keywords, String input){
        int last = 0;
        String lastMatch = null;
        for(String keyword : keywords){
            Pattern p = Pattern.compile(BOUNDARY + keyword + BOUNDARY);
            Matcher m = p.matcher(input);
            if(m.find()){
                int end = m.end();
                if(end > last){
                    last = end;
                    lastMatch = keyword;
                }
            }
        }
        return lastMatch;
    }
    private void omitCommonAndPrepWords(List<MaoniuPendingData> input){
        input.stream().forEach(in -> {
            in.setTarget(omitCommonWord(omitPrepWord(in.getOrigin())));
        });
    }


    private String omitPrepWord(String origin){
        //预处理
        origin = " " + origin.replaceAll("/", SLASH);
        for(Object  prepWord : prep_words_with_prefix_suffix){
            origin = origin.replaceAll((String)prepWord, "$4 $2");
        }
        origin.replaceAll(SLASH, "/");
        return origin.trim();
    }

    private String omitCommonWord(String keyword){
        for(Object commonWord : common_words_with_space){
            keyword = keyword.replaceAll((String)commonWord, "").replaceAll(SPACE_PLUS, " ").replaceAll(SLASH, "/");
        }
        return keyword.trim();
    }

    private List<MaoniuPendingData> omitSamePendingData(List<MaoniuPendingData> input) {
        List<String> beforeForeachList = new ArrayList<>();
        Map<String, MaoniuPendingData> beforeSortMap = new HashMap<>();
        input.stream().forEach(in -> {
            String[] targetArr = in.getTarget().split(SPACE_PLUS);
            Arrays.sort(targetArr);
            String sortedTarget = Joiner.on(" ").join(targetArr).toLowerCase();
            if(null == beforeSortMap.get(sortedTarget)){
                beforeSortMap.put(sortedTarget, in);
            }else{
                beforeForeachList.add(in.getOrigin());
                List<String> samePendingDateList = beforeSortMap.get(in.getTarget()).getSamePendingDataList();
                if(CollectionUtils.isEmpty(samePendingDateList)){
                    samePendingDateList = new ArrayList<>();
                    samePendingDateList.add(in.getOrigin());
                }else{
                    samePendingDateList.add(in.getOrigin());
                }
            }
         });
        return input.stream().filter(in -> {
            if(beforeForeachList.contains(in.getOrigin())){
                return false;
            }
            return true;
        }).collect(Collectors.toList());

    }

    @Override
    public Object convert(Object o) {
        return null;
    }
}
