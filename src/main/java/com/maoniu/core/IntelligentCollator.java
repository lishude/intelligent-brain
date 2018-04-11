package com.maoniu.core;

import java.util.List;

/**
 * Created by Administrator on 2018/4/9.
 * 智能整理器用于对关键词进行整理
 */
public interface IntelligentCollator<T, S>{

    /**
     * 前置工作，一般用于在初始化后进行的操作，比如数据归类
     *
     * @param input
     */
    List<T> preposition(List<T> input);

    /**
     * 后置操作，一般数据都处理完成后，进行额外的处理
     *
     * @param input
     */
    List<T> postposition(List<T> input);

    /**
     * 对数据进行清理工作
     *
     * @param input
     */
    void cleanup(List<T> input);


}
