package com.maoniu;

import com.kevin.utils.GeneralExcelPoi;
import com.maoniu.core.MaoNiuMatcher;
import com.maoniu.debug.DebugCommon;
import com.maoniu.debug.generate.GenerateKeywordData;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/13.
 */
public class MaoNiuMatcherTest {
    private  static  final String prefix = "D:\\maoniu_test\\智能调试\\deutrogge\\";
    public static void main(String[] args) throws Exception {
        Map<String, List<String>> classify_keyword_map = new HashMap<>();
        GeneralExcelPoi<GenerateKeywordData> generateKeywordDataGeneralExcelPoi = new GeneralExcelPoi<>();
        List<GenerateKeywordData> matchKeywordDataList = generateKeywordDataGeneralExcelPoi.parseExcel(new File(prefix + "keyword_generate.xlsx"), GenerateKeywordData.class);
        List<KeywordData> keywordData = DebugCommon.generateKeywordDataConvertToKeyword(matchKeywordDataList);

        List<ProductAttrData> productAttrData = DebugCommon.getProductAttrData(prefix);
        List<ThesaurusData> thesaurusData = DebugCommon.getThesaurusData(prefix);
        String position = DebugCommon.getPosition(prefix);

        MaoNiuMatcher maoNiuMatcher = new MaoNiuMatcher(classify_keyword_map, null, null);
        maoNiuMatcher.doMatch(keywordData, position, productAttrData, thesaurusData);
        System.out.println(111);
    }

}
