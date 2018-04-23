package com.maoniu.core;

import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2018/4/12.
 */
public abstract class AbstractPreprocessorIntelligent extends AbstractIntelligent implements Preprocessor<KeywordData>{

    public AbstractPreprocessorIntelligent(Map classify_keyword_map, Map classify_common_words_map, List prep_words) {
        super(classify_keyword_map, classify_common_words_map, prep_words);
    }

    @Override
    public void prepare(List<KeywordData> t, List<ProductAttrData> productAttrData, List<ThesaurusData> thesaurusData){
        t.forEach(kd -> {
            if(StringUtils.isNotEmpty(kd.getModel())){
                kd.setLowerCaseModel(kd.getModel().toLowerCase().trim());
                kd.setModel(kd.getModel().toLowerCase().trim());
            }
            if(Objects.isNull(kd.getHeat())){
                kd.setHeat(0);
            }
        });
        productAttrData.forEach(pad -> {
            if(StringUtils.isNotEmpty(pad.getClassify()))
                pad.setClassify(pad.getClassify().trim());
            if(StringUtils.isNotEmpty(pad.getModel()))
                pad.setModel(pad.getModel().toLowerCase().trim());
        });
        thesaurusData.forEach(td -> {
            if(StringUtils.isNotEmpty(td.getClassify()))
                td.setClassify(td.getClassify().trim());
        });
    };

}
