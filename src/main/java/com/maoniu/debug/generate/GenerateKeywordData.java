package com.maoniu.debug.generate;

import lombok.Data;

/**
 * Created by Administrator on 2018/4/18.
 */
@Data
public class GenerateKeywordData {
    private String id;
    private String name;
    private String omittedName;
    private String keyword;
    private String heat;
    private String classify;
    private String model;
    private String lowerCaseModel;
    private String customModels;
    private String sort;
    private String bestCategory;
    private String intersectionSet;//交集
    private String diffSet;//差集
}
