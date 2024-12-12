package com.example.tcpadapter.util;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ByteDataLengthSet {

    private final Logger logger = LoggerFactory.getLogger(ByteDataLengthSet.class);


    public byte[] LengthSetChange(byte[] data, ConfigProperties properties) {
        int LengthOffSet = properties.getLengthOffSet();
        int LengthDataSize = properties.getLengthDataSize();
        String DataOffSet = properties.getDataOffSet();
        int length_yn_size = 0;
        int dataOffSet = 0;

        if (DataOffSet != null && !DataOffSet.equals("")) {
            dataOffSet = Integer.parseInt(DataOffSet);
        }

        if (properties.getLengthDataSize_ref_yn().equalsIgnoreCase("N")) {
            if (dataOffSet == LengthOffSet) {
                length_yn_size = LengthDataSize;
            }
        }

        byte[] firByteData = ArrayUtils.subarray(data, 0, LengthOffSet);
        String LengthData = String.format("%0" + LengthDataSize + "d", data.length - dataOffSet - length_yn_size);
        byte[] endByteData = ArrayUtils.subarray(data, LengthOffSet + LengthDataSize, data.length);
        byte[] resultByteData = ArrayUtils.addAll(firByteData, LengthData.getBytes());
        resultByteData = ArrayUtils.addAll(resultByteData, endByteData);

        if (logger.isInfoEnabled()) {
            String logText = "\n------ Change length data -------\n";
            logText += "length offset : [" + LengthOffSet + "]\n";
            logText += "length data size : [" + LengthDataSize + "]\n";
            logText += "data offset : [" + dataOffSet + "]\n";
            logText += "--------------------------------";

            logger.info(logText);
        }

        return resultByteData;
    }
}
