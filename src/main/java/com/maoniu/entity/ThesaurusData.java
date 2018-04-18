package com.maoniu.entity;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/4/10.
 */
@Data
public class ThesaurusData {
    private String classify;//品类
    private Set<String> characteristicWords;//特征词
    private List<String> commonWords;//通用词
    private List<List<String>> synonymWords;//同义词
    private List<String> wordGroups;//词组
}
