package com.maoniu.core;

/**
 * Created by Administrator on 2018/4/9.
 */
public interface Convertor<T, S> {
    /**
     * 将外部数据转化成符合规则的数据
     * @param s
     * @return
     */
     T convert(S s);

     void init();
}
