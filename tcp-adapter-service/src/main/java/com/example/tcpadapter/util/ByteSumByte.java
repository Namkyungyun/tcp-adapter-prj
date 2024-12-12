package com.example.tcpadapter.util;

import java.nio.ByteBuffer;

public class ByteSumByte {

    public static byte[] ByteSumByteSet(byte[] firstByte, byte[] secondByte){
        byte[] returnData = new byte[firstByte.length+secondByte.length];
     /*   System.arraycopy(firstByte, 0, returnData, 0, firstByte.length);  //사용되어지지 않음
        System.arraycopy(secondByte, 0, returnData, firstByte.length, secondByte.length);*/
        return returnData;
    }


    public static byte[] ByteChangeByteSet(byte[] firstByte){
        byte[] returnData = new byte[firstByte.length];
        //isDirect를 사용해서 allocateDirect로 생성된 객체면 allocate -> allocateDirect
        ByteBuffer dataStorage = ByteBuffer.allocate(returnData.length);
        dataStorage.put(firstByte);
        dataStorage.position(0);
        dataStorage.get(returnData);
        dataStorage.clear();
        return returnData;
    }
}
