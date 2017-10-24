package com.android.yyx.scanningproject.nfc;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import static android.nfc.tech.MifareClassic.KEY_DEFAULT;


/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  NfcDemo
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/2/14,  zhangyong, create
 ************************************************************/

public class MifareClassicUtils {
    private static MifareClassicUtils sMifareClassicUtils;
    public static final String TAG= "MF1";
    private String mType;
    private String mInfo;
    private int mSectorCount;
    private int mBlockCount;
    private MifareClassic mMfc;
    private MifareClassicUtils(){

    }

    public static synchronized MifareClassicUtils getInstance(){
        if (sMifareClassicUtils == null){
            sMifareClassicUtils = new MifareClassicUtils();
        }
        return sMifareClassicUtils;
    }

    public  String getType(){
        return mType;
    }
    public  String getInfo(){
        return mInfo;
    }
    public int getSectorCount() {
        return mSectorCount;
    }
    public int getBlockCount() {
        return mBlockCount;
    }
    public  void parseMifareClassic(final Tag tag,final ParseListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String metaInfo = "";
                boolean auth = false;
                mMfc = MifareClassic.get(tag);
                if (mMfc != null){
                    int type = mMfc.getType();
                    int sectorCount = mMfc.getSectorCount();
                    switch (type) {
                        case MifareClassic.TYPE_CLASSIC:
                            mType = "MIFARE_TYPE_CLASSIC";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            mType = "MIFARE_TYPE_PLUS";
                            break;
                        case MifareClassic.TYPE_PRO:
                            mType = "MIFARE_TYPE_PRO";
                            break;
                        case MifareClassic.TYPE_UNKNOWN:
                            mType = "MIFARE_TYPE_UNKNOWN";
                            break;
                    }
                    mInfo = sectorCount + ","
                            + mMfc.getBlockCount()/sectorCount + "," + mMfc.getSize()/mMfc.getBlockCount();
                    mSectorCount = sectorCount;
                    mBlockCount = mMfc.getBlockCount();
                    listener.onParseComplete(metaInfo);
                }
            }
        }).start();
    }

    public void readSectorData(final int serctorIndex,final int keyType,final String keyValue,final ParseListener parseListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                boolean auth = false;
                if (mMfc != null){
                    try{
                        if (!mMfc.isConnected()) {
                            mMfc.connect();
                        }
                        byte[] key = null;
                        if (!TextUtils.isEmpty(keyValue)){
                            key = Utils.stringToBytes(keyValue);
                        }
                        if (key == null){
                            key = KEY_DEFAULT;
                        }
                        if (keyType == 0) {
                            auth = mMfc.authenticateSectorWithKeyA(serctorIndex, key);
                        } else {
                            auth = mMfc.authenticateSectorWithKeyB(serctorIndex, key);
                        }

                        int bCount;
                        int bIndex;
                        if (auth) {
                            bCount = mMfc.getBlockCountInSector(serctorIndex);
                            bIndex = mMfc.sectorToBlock(serctorIndex);
                            for (int i = 0; i < bCount; i++) {
                                byte[] data = mMfc.readBlock(bIndex);
                                String data16 = Utils.bytesToHexString(data);
                                Log.d("输出",""+Utils.hexStringToAlgorism(data16.substring(0,2))+Utils.hexStringToAlgorism(data16.substring(2,6)));
                                if (bIndex != 0 && (((bIndex-3)%4 != 0))){
                                    result += "block " + bIndex + " :   "
                                            + Utils.bytesToHexString(data)+ "\n";
                                } else {
                                    result += "block " + bIndex + " :  "
                                            + Utils.bytesToHexString(data) + "\n";
                                }
                                bIndex++;
                            }
                        } else {
                            result = "";
                        }


                        parseListener.onParseComplete(result);
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        try {
                            if(mMfc.isConnected()){
                                mMfc.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();
    }




    public void readOneSectorData(final Context context, final Tag tag, final ParseListener parseListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                boolean auth = false;
                mMfc = MifareClassic.get(tag);
                    try{
                        if (!mMfc.isConnected()) {
                            mMfc.connect();
                        }
                        byte[] key = null;
                        String keyValue = "000000FFFFFF";
                        key = Utils.stringToBytes(keyValue);
                        auth = mMfc.authenticateSectorWithKeyA(1, key);

                        int bCount;
                        int bIndex;
                        if (auth) {
                            bCount = mMfc.getBlockCountInSector(1);
                            bIndex = mMfc.sectorToBlock(1);
                            for (int i = 0; i < bCount; i++) {
                                byte[] data = mMfc.readBlock(bIndex);
                                if (bIndex != 0 && (((bIndex-3)%4 != 0))){
                                    result += Utils.bytesToHexString(data)+ ",";
                                } else {
                                    result += Utils.bytesToHexString(data);
                                }

                                bIndex++;
                            }

                        } else {
                            Toast.makeText(context,"NFC識別廠證失敗，請重新嘗試!",Toast.LENGTH_SHORT).show();
                            result = "";
                        }
                        parseListener.onParseComplete(result);
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        try {
                            if(mMfc.isConnected()){
                                mMfc.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }).start();
    }



    public String readBlockData(int blockIndex,int keyType, String keyValue){
        String result = "";
        int sectorIndex = blockIndex/4;
        boolean auth = false;
        if (mMfc != null){
            try{
                if (!mMfc.isConnected()) {
                    mMfc.connect();
                }
                byte[] key = null;
                if (!TextUtils.isEmpty(keyValue)){
                    key = Utils.stringToBytes(keyValue);
                }
                if (key == null){
                    key = KEY_DEFAULT;
                }
                if (keyType == 0){
                    auth = mMfc.authenticateSectorWithKeyA(sectorIndex, key);
                } else {
                    auth = mMfc.authenticateSectorWithKeyB(sectorIndex, key);
                }

                if (auth) {
                    byte[] data = mMfc.readBlock(blockIndex);
                    result += "block" + blockIndex + ": "
                            + Utils.bytesToHexString(data) + "\n";
//                    result += "            text=" + "  "
//                            + new String(data) + "\n";
                } else {
                    result = null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if(mMfc.isConnected()){
                        mMfc.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return result;
    }

    public String writeSector(int sectorIndex,int keyType,String keyValue,String dataStr){
        if (TextUtils.isEmpty(dataStr)) {
            return "false";
        }
        byte[] key = null;
        if (!TextUtils.isEmpty(keyValue)){
            key = Utils.stringToBytes(keyValue);
        }
        if (key == null){
            key = KEY_DEFAULT;
        }
        byte[] dataBytes = Utils.hexString2Bytes(dataStr);
        try{
            if (mMfc != null){
                mMfc.connect();
            }
            boolean auth = false;
            if (keyType == 0){
                auth = mMfc.authenticateSectorWithKeyA(sectorIndex, key);
            } else {
                auth = mMfc.authenticateSectorWithKeyB(sectorIndex, key);
            }
            if (auth){
                int nCount=mMfc.getBlockCountInSector(sectorIndex)-1;
                int blockIndex=mMfc.sectorToBlock(sectorIndex);
                if (blockIndex == 0) {
                    //block 0 can not write
                    blockIndex++;
                }
                for(int i = 0;i < nCount; i++)
                {
                    if (blockIndex != 0 && (((blockIndex-3)%4 != 0))){
                        byte[] data = new byte[16];
                        if((dataBytes.length-i*16) >0){
                            System.arraycopy(dataBytes, i*16, data, 0, (dataBytes.length-i*16) > 16 ? 16
                                    : (dataBytes.length-i*16));
                        }
                        mMfc.writeBlock(blockIndex,data);
                    }

                    blockIndex++;

                }
                return "true";
            } else {
                return "";
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(mMfc.isConnected()){
                    mMfc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "false";
    }

    public String writeBlock(int blockIndex,int keyType,String keyValue,String dataStr){
        int sectorIndex = blockIndex/4;
        if (TextUtils.isEmpty(dataStr)) {
            return "false";
        }
        byte[] key = null;
        if (!TextUtils.isEmpty(keyValue)){
            key = Utils.stringToBytes(keyValue);
        }
        if (key == null){
            key = KEY_DEFAULT;
        }
        byte[] dataBytes =  Utils.hexString2Bytes(dataStr);
        try{
            if (mMfc != null){
                mMfc.connect();
            }
            boolean auth = false;
            if (keyType == 0){
                auth = mMfc.authenticateSectorWithKeyA(sectorIndex, key);
            } else {
                auth = mMfc.authenticateSectorWithKeyB(sectorIndex, key);
            }
            if (auth){
                if (blockIndex != 0 && (((blockIndex-3)%4 != 0))){
                    byte[] data = new byte[16];
                    System.arraycopy(dataBytes, 0, data, 0, (dataBytes.length) > 16 ? 16
                            : dataBytes.length);
                    mMfc.writeBlock(blockIndex,data);
                }
                return "true";
            } else {
                return "";
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(mMfc.isConnected()){
                    mMfc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "false";
    }

    public String modifykey(int sectorIndex,int keyType,String keyValue,int newKeyType,String newKeyValue){

        byte[] key = null;
        if (!TextUtils.isEmpty(keyValue)){
            key = Utils.stringToBytes(keyValue.trim());
        }
        if (key == null){
            key = KEY_DEFAULT;
        }
        byte[] newkeybuf = null;
        if(TextUtils.isEmpty(newKeyValue)) {
            newkeybuf = KEY_DEFAULT;
        } else {
            String newKeyStr = newKeyValue.trim();
            newkeybuf = Utils.stringToBytes(newKeyStr);
        }


        byte[] pwrite = new byte[16];

        if (newKeyType == 0) {
            System.arraycopy(newkeybuf, 0, pwrite, 0,newkeybuf.length );
            //The following 4 bytes of password control - bit, you need to consult the relevant documents as necessary to fill in
            pwrite[6]=(byte)0xff;
            pwrite[7]=(byte)0x07;
            pwrite[8]=(byte)0x80;
            pwrite[9]=(byte)0x69;
            //The following 4 bytes of password B
            pwrite[10]=(byte)0xff;
            pwrite[11]=(byte)0xff;
            pwrite[12]=(byte)0xff;
            pwrite[13]=(byte)0xff;
            pwrite[14]=(byte)0xff;
            pwrite[15]=(byte)0xff;
        } else {
            System.arraycopy(newkeybuf, 0, pwrite, 10,newkeybuf.length );
            //The following 4 bytes of password control - bit, you need to consult the relevant documents as necessary to fill in
            pwrite[0]=(byte)0xff;
            pwrite[1]=(byte)0xff;
            pwrite[2]=(byte)0xff;
            pwrite[3]=(byte)0xff;
            pwrite[4]=(byte)0xff;
            pwrite[5]=(byte)0xff;

            pwrite[6]=(byte)0x7f;
            pwrite[7]=(byte)0x07;
            pwrite[8]=(byte)0x88;
            pwrite[9]=(byte)0x69;
        }

        if(mMfc!=null)
        {
            try {
                mMfc.connect();

                if(sectorIndex<mMfc.getSectorCount())
                {
                    int add = mMfc.sectorToBlock(sectorIndex);
                    boolean auth = false;
                    String errorMsg = "";
                    if (keyType == 0){
                        auth = mMfc.authenticateSectorWithKeyA(sectorIndex, key);
                        errorMsg+="KeyA ";
                    } else {
                        pwrite[9]=(byte)0x69;
                        auth = mMfc.authenticateSectorWithKeyB(sectorIndex, key);
                        errorMsg+="KeyB ";
                    }
                    if(auth)
                    {
                        mMfc.writeBlock(add+mMfc.getBlockCountInSector(sectorIndex)-1,pwrite);
                        android.util.Log.d(TAG,"modify sector="+sectorIndex+"  key  success!\n");
                        return "true";
                    } else {
                        android.util.Log.d(TAG,"modify sector="+sectorIndex+"  key  Fail!\n");
                        return "";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(mMfc.isConnected()){
                        mMfc.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "false";
    }


}
