package com.maoniu.entity;

import com.sun.istack.internal.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2018/4/10.
 */
@Data
public class KeywordData {
    private String id;//用于更新数据的id
    private String name;//关键词
    private String omittedName;//处理后的关键词
    private String keyword;//核心词
    @NotNull
    private String classify;//品类
    private String model;//配对型号
    private Set<String> customModels;//自定义型号
    private String uuid;//用于将配置一起的关键词归类在一起,现在好像用不上
    private int sort;//经过智能整理后的顺序，用于后续处理
    @NotNull
    private String bestCategory;//最佳类目(这边需要保证最佳类目是存在的)
    private boolean emptyAdj;//关键词中只有核心词+通用词+介词 该值为true
    private Set<String> matchModelSet = new HashSet<>();//配对型号组，用于最终型号筛选
    private Set<String> intersectionSet = new HashSet<>();//交集
    private Set<String> diffSet = new HashSet<>();//差集
    private Set<String> intersectionAndDiffSet = new HashSet<>();//交集+差集

}
