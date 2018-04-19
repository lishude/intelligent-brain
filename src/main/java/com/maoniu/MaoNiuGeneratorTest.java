package com.maoniu;

import com.kevin.utils.GeneralExcelPoi;
import com.maoniu.core.MaoNiuMatcher;
import com.maoniu.debug.DebugCommon;
import com.maoniu.debug.match.MatchKeywordData;
import com.maoniu.entity.KeywordData;
import com.maoniu.entity.ProductAttrData;
import com.maoniu.entity.ThesaurusData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/12.
 */
public class MaoNiuGeneratorTest {

    private  static  final String prefix = "D:\\maoniu_test\\智能调试\\ortosport\\";

    public static void main(String[] args) throws Exception {
        Map<String, List<String>> classify_keyword_map = new HashMap<>();
        GeneralExcelPoi<MatchKeywordData> matchKeywordDataGeneralExcelPoi = new GeneralExcelPoi<>();
        List<MatchKeywordData> matchKeywordDataList = matchKeywordDataGeneralExcelPoi.parseExcel(new File(prefix + "keyword_match.xlsx"), MatchKeywordData.class);
        List<KeywordData> keywordData = DebugCommon.matchKeywordDataConvertToKeyword(matchKeywordDataList);

        List<ProductAttrData> productAttrData = DebugCommon.getProductAttrData(prefix);
        List<ThesaurusData> thesaurusData = DebugCommon.getThesaurusData(prefix);
        String position = DebugCommon.getPosition(prefix);

        MaoNiuMatcher maoNiuMatcher = new MaoNiuMatcher(classify_keyword_map, null, null);
        maoNiuMatcher.doMatch(keywordData, position, productAttrData, thesaurusData);

    }


}
