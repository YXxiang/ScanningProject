package com.android.yyx.scanningproject.tools;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by yeyuanxiang on 2017/10/18.
 */

public class ScanTools {

    /**
     *
     * @return 当前的时间
     */
    public static String getNowTime(){
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = ft.format(date);
        return time;
    }


    /**
     * 解析XML数据
     * @param result  源数据（XML转String）
     * @return        String 字符串
     */
    public static String getContentFromTag(String result){
        String tag1 = "<string xmlns=\"http://tempuri.org/\">";
        String tag2 = "</string>";
        String s = result.substring(result.indexOf(tag1)+tag1.length(),result.indexOf(tag2));
        return s;
    }

    /**
     *
     * @param resultString  分割的字符串
     * @param pattern   分割的字符
     * @return   数组
     */
    public static String[] retultList(String resultString, String pattern) {
        Pattern pen = Pattern.compile(pattern);
        String[] temp = pen.split(resultString);
        return  temp;
    }


    /**
     *
     * @param
     * @return 判断数据是否成功（true） or 失败（false）
     */
    public static boolean returnTureOrFalse(String s){
        if (s == null) return false;
        String str = s.substring(0,1);
        if (str.equals("-")){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获取barcode
     * @return String
     */
    public static String getBarCodes(List CODELIST){
        if (CODELIST == null) return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < CODELIST.size(); i++){
            if (i == 0){
                stringBuilder.append(CODELIST.get(i));
            }else {
                stringBuilder.append(","+CODELIST.get(i));
            }
        }

        return stringBuilder.toString();
    }


}
