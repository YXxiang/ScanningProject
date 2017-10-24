package com.android.yyx.scanningproject.network;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by yeyuanxiang on 2017/9/5.
 */

public interface ApiService {


    @FormUrlEncoded
    @POST("Car_UserChk")
    Observable<ResponseBody> getUserCheckIfo(@Field("p_rdcheck") int rdcheck,
                                             @Field("p_appid") String appid,
                                             @Field("p_Barcode") String barcodes,
                                             @Field("p_sessionid") String sessionId);

    @FormUrlEncoded
    @POST("GetEmpinfo")
    Observable<ResponseBody> getEmpinfo(@Field("p_rdcheck") int rdcheck,
                                        @Field("p_Barcode") String barcodes,
                                        @Field("p_sessionid") String sessionId);


    @FormUrlEncoded
    @POST("EntryData_Save")
    Observable<ResponseBody> saveEntryDataInfoCodes(@Field("p_Barcode") String barcodes,
                                                    @Field("p_IO") String ioStr,
                                                    @Field("p_RMK") String text,
                                                    @Field("p_sessionid") String sessionId,
                                                    @Field("p_empno") String empno);

}
