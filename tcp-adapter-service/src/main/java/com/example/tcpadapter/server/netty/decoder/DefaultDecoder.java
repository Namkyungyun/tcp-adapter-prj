package com.example.tcpadapter.server.netty.decoder;

import com.example.tcpadapter.util.ByteSumByte;
import com.example.tcpadapter.util.ConfigProperties;
import com.example.tcpadapter.util.TcpConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultDecoder extends AbstractMessageToMsgDecoder{

    private static Logger log = LoggerFactory.getLogger(DefaultDecoder.class);

    private static final long serialVersionUID = 1019400187032745812L;

    private ConfigProperties config;

    private String lengthDataSize_ref_yn;
    private int lengthOffSet;
    private int lengthDataSize;
    private String DataOffSet;
    private String encoding;

    public DefaultDecoder(ConfigProperties config) {
        this.lengthDataSize_ref_yn = config.getLengthDataSize_ref_yn();
        this.lengthDataSize = config.getLengthDataSize();
        this.DataOffSet =  config.getDataOffSet();
        this.lengthOffSet = config.getLengthOffSet();
        this.encoding = config.getTcpencoding();
        if(this.encoding == null || this.encoding.equals("")) {
            this.encoding = "EUC-KR";
        }
    }

    @Override
    protected void userDecode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // 길이 위치가 정해져 있는 가변 길이
        try{
            if(log.isInfoEnabled()) {
                byte[] readData = new byte[msg.readableBytes()];
                msg.getBytes(0, readData);
                log.info("# Buffer Data Recv CHAN ID [" + ctx.channel().remoteAddress() + "] =>" +
                        "[" + ctx.channel().localAddress() + "] RecvBufferSize [" + readData.length + "]" +
                        "[" + new String(readData, encoding));
            }

            if(Data_size == 0 ) { //이전에 전문이 버퍼사이즈보다 커 미완성된 전문이 없을 경우
                readNewData(ctx, out, msg);
            }else {     // 길이가 설정 버퍼보다 길어, 이전 전문이 미완성이였던 경우
                readContinueData(ctx, out, msg);
            }
        } catch(Exception e) {
            e.printStackTrace();
            log.info("userDecode Method Error of DefaultDecoder: [" + e.getMessage() + "]");
        }
    }

    private void readNewData(ChannelHandlerContext ctx, List<Object> out, ByteBuf msg) {
        // boolean isNomal = true;
        boolean lengthCheck;
        byte[] array = null;
        int readIndex = 0;
        int msgLength = msg.readableBytes();
        while(true) {
            if(readIndex == 0) { //테스트해보고 필요없는 파트면 내부 코드 밖으로 빼내기
                lengthCheck = LengthSet(msg, readIndex);
                if(!lengthCheck) { // false일 시,
                    Data_size = 0; // Data_size를 0으로 초기화
                    ctx.channel().close();
                    try {
                        throw new Exception("lengthCheck is false -> [case1 : Data_size is 0 " +
                                "/ case2: Data_size is Not format Integer");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            //msg.readableBytes < Data_size
            if(msgLength < Data_size) {
                firstArray = new byte[msgLength];
                msg.getBytes(0, firstArray);
                return;
            } else {    //msg.readableBytes == Data_size /or/ msg.readableBytes > Data_size
                if(msgLength == readIndex) {    //true 일 경우,
                    array = new byte[Data_size];
                    msg.getBytes(readIndex, array); //readIndex는 언제나 0
                    out.add(array);
                    this.Check_timer = false;
                    Data_size = 0;
                 /*   readIndex = 0; //어차피 0인디?*/
                    break;
                } else if((readIndex + Data_size) > msgLength) { //현재 버퍼 데이터가 부족할 시 다음으로 넘김
                    firstArray = new byte[msg.readableBytes() - readIndex];
                    msg.getBytes(readIndex, firstArray);
                    if(log.isDebugEnabled()) {
                        log.debug("데이터 부족 [readIndex : " + readIndex + "/" + firstArray.length +"]");
                        return;
                    }
                } else {
                    array = new byte[Data_size];
                    msg.getBytes(readIndex, array);
                    out.add(array);
                    readIndex = readIndex + Data_size;
                    if (log.isDebugEnabled()) {
                        log.debug("데이터 더 존재 [ readIndex + Data_Size < msgLength ]");
                        log.debug("readIndex [" + readIndex + "]");
                    }

                    if(msgLength != readIndex) {
                        if((readIndex + lengthOffSet + lengthDataSize) > msgLength) {
                            firstArray = new byte[msgLength - readIndex];
                            msg.getBytes(readIndex, firstArray);
                            return;
                        }

                        lengthCheck = LengthSet(msg, readIndex);
                        if(!lengthCheck) {
                            ctx.channel().close();
                            try {
                                throw new Exception("데이터 더 존재 시, LengthSet method 실패로 Error");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if((msgLength - readIndex) < Data_size) {
                            firstArray = new byte[msgLength - readIndex];
                            msg.getBytes(readIndex, firstArray);

                            if(log.isDebugEnabled()) {
                                log.debug("데이터부족\n");
                                log.debug("readIndex :"+readIndex +"/"+firstArray.length);
                            }
                            return;
                        }

                    } else {
                        Data_size = 0;
                        if(log.isDebugEnabled()){
                            log.debug("데이터 완료 종료");
                        }
                        this.Check_timer = false;	//20171123 추가되어야함.
                        return;
                    }

                }
            }
        }//while 읽은 데이터가 여러게 존재할수 있으므로.
        return;
    }

    //readNew 메서드에서 사용
    private boolean LengthSet(ByteBuf msg, int offSet) {
        boolean result = true;
        byte[] array = null;
        try{
            //msg 존재하는 경우
            if(msg.readableBytes() != 0 ) {
                array = new byte[lengthDataSize];
                if(lengthOffSet == 0) {
                    msg.getBytes(offSet, array);
                } else {
                    msg.getBytes(offSet+lengthOffSet, array);
                }
                
                if(lengthDataSize_ref_yn.equalsIgnoreCase(TcpConstants.useYn_Y)) {
                    Data_size = Integer.parseInt(new String (array)); //toString으로 변환할수있는지 전체 완성하고 적용해보기
                }else {
                    Data_size = Integer.parseInt(new String(array)) + lengthDataSize;
                    
                    if(log.isDebugEnabled()) {
                        log.debug("[DataRecv] 전문 길이 " + Data_size + "" +
                                "/ lengthDataSize :" + lengthDataSize + " " +
                                "/ lengthOffSet : " + lengthOffSet);
                    }
                }
                
                if(log.isDebugEnabled()) {
                    log.debug("[DataRecv] Length Size[" +
                            ""+lengthDataSize+"] -> ReadOffset["+offSet+"] " +
                            "-> RecvLength["+new String(array)+"] " +
                            "-> ReadData["+Data_size+"]");
                }

                //properties에서 설정한 DataOffSet 값이 적용되지 않았을 경우
                if(DataOffSet !=null&&!DataOffSet.isBlank() ) {
                    int iDataOffset = 0;
                    try{
                        iDataOffset = Integer.parseInt(DataOffSet);
                        if( iDataOffset != (lengthOffSet + lengthDataSize)) {
                            Data_size = Data_size + iDataOffset;
                            if(log.isDebugEnabled()) {
                                log.debug("[DataRecv] 총 읽을 전문길이 : "+Data_size + "  dataOffset : " + iDataOffset);
                            }
                        }
                    } catch (Exception e) {
                        if(log.isDebugEnabled()) {
                            log.debug("어댑터 설정에 Data OffSet 값이 정상적이지 않습니다. 0으로 세팅합니다.");
                        }
                        e.getMessage();
                    }
                }
            }
            if(Data_size == 0) {
                log.info("Data_Size is [" + Data_size + "] , Error!");
                result = false;
            }

        } catch (NumberFormatException e ) {
            log.error("Data_Size is Not Integer");
            result = false;
        }
        return result;
    }

    private void readContinueData(ChannelHandlerContext ctx, List<Object> out, ByteBuf msg) throws Exception{
        boolean lengthCheck;
        byte[] array = null;

        if((firstArray.length + msg.readableBytes()) < Data_size) { //받아야할 데이터가 더 존재할 경우
            byte[] lastArray = new byte[msg.readableBytes()];
            msg.getBytes(0, lastArray);

            byte[] receiveBytes = new byte[firstArray.length + msg.readableBytes()];
            firstArray = receiveBytes;
            
            receiveBytes = ByteSumByte.ByteSumByteSet(firstArray, lastArray);
            firstArray = ByteSumByte.ByteChangeByteSet(receiveBytes);
            
            if(log.isInfoEnabled()) {
                log.info("firstArray put : "+firstArray.length);
            }
            return;
            
        }else { //더 받아야할 데이터가 없는 경우
            array = new byte[msg.readableBytes()];
            msg.getBytes(0, array);
            byte[] receiveBytes = new byte[firstArray.length + array.length];
            receiveBytes = ByteSumByte.ByteSumByteSet(firstArray, array);
            if(Data_size == receiveBytes.length) {
                this.Check_timer = false;
                out.add(receiveBytes.length);
                Data_size = 0;          //초기화
                firstArray = null;
            }else {
                if(log.isInfoEnabled()) {
                    log.info("Read Data Exist");
                }

                if( receiveBytes.length < (lengthOffSet + lengthDataSize) ) {
                    firstArray = new byte[receiveBytes.length];
                    System.arraycopy(receiveBytes, 0, firstArray, 0, firstArray.length);
                    return;
                }
                lengthCheck = ByteLengthSet(receiveBytes, 0);
                if(!lengthCheck){
                    ctx.channel().close();
                    try {
                        throw new Exception("receive 데이터 가공 후 length와 ByteLengthSet 검증으로 나온 결과 불일치");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                byte[] returnByte = new byte[Data_size];
                System.arraycopy(receiveBytes, 0, returnByte, 0, Data_size);
                out.add(returnByte);

                firstArray = new byte[receiveBytes.length-Data_size];//남은 데이터를 firstArray로 copy
                System.arraycopy(receiveBytes, Data_size, firstArray, 0, receiveBytes.length-Data_size);

                if(log.isInfoEnabled()) {
                    log.info("NonComplete Data["+new String(firstArray)+"]");
                }

                int readIndex = 0;
                while(true) {
                    if(readIndex == 0) {
                        if(firstArray.length < (lengthOffSet + lengthDataSize)) {
                            if(log.isInfoEnabled()) {
                                log.info("Read Data Lack ["+firstArray.length+"]["+(lengthOffSet+lengthDataSize)+"]");
                            }
                            return;
                        }

                        lengthCheck = ByteLengthSet(firstArray, readIndex);
                        if(!lengthCheck) {
                            ctx.channel().close();
                            try {
                                throw new Exception(); //?
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if(firstArray.length < Data_size){
                        byte[] tgrmByte = new byte[firstArray.length - readIndex];
                        System.arraycopy(firstArray, readIndex, tgrmByte, 0, tgrmByte.length);
                        firstArray = new byte[tgrmByte.length];
                        System.arraycopy(tgrmByte, 0, firstArray, 0, firstArray.length);
                        if(log.isInfoEnabled())
                            log.info("readIndex:"+readIndex +"/"+firstArray.length);
                        return;
                    }else if(firstArray.length == readIndex){
                        log.debug("데이터 Add end");
                        returnByte = new byte[Data_size];
                        System.arraycopy(firstArray, readIndex, receiveBytes, 0, Data_size);
                        this.Check_timer = false;
                        out.add(returnByte);
                        Data_size = 0;
                        readIndex = 0;
                        firstArray = null;
                        break;
                    }else{
                        log.debug("데이터 존재");
                        returnByte = new byte[Data_size];
                        System.arraycopy(firstArray, readIndex, returnByte, 0, returnByte.length);
                        out.add(returnByte);
                        readIndex = readIndex + Data_size;
                        if(log.isDebugEnabled())
                            log.debug("readIndex ["+readIndex+"]");

                        if(firstArray.length != readIndex){
                            if((readIndex+lengthOffSet+lengthDataSize)>firstArray.length){
                                if(log.isInfoEnabled())
                                    log.info("Read Data Lack ["+firstArray.length+"]["+(lengthOffSet+lengthDataSize)+"]");
                                byte[] tgrmByte = new byte[firstArray.length - readIndex];
                                System.arraycopy(firstArray, readIndex, tgrmByte, 0, tgrmByte.length);
                                firstArray = new byte[tgrmByte.length];
                                System.arraycopy(tgrmByte, 0, firstArray, 0, firstArray.length);
                                return;
                            }

                            lengthCheck = ByteLengthSet(firstArray, readIndex);

                            if(!lengthCheck){
                                ctx.channel().close();
                                throw new Exception();
                            }

                            if((firstArray.length -readIndex)<Data_size){
                                log.debug("데이터 부족");
                                byte[] tgrmByte = new byte[firstArray.length - readIndex];
                                System.arraycopy(firstArray, readIndex, tgrmByte, 0, tgrmByte.length);
                                firstArray = new byte[tgrmByte.length];
                                System.arraycopy(tgrmByte, 0, firstArray, 0, firstArray.length);

                                lengthCheck = ByteLengthSet(firstArray, 0);

                                if(log.isDebugEnabled()) {
                                    log.debug("["+new String(firstArray)+"]");
                                    log.debug("["+Data_size+"]");
                                    log.debug("readIndex:"+readIndex +"/"+firstArray.length);
                                }
                                return;
                            }
                        }else{
                            log.info("Data Complete");
                            firstArray = null;
                            Data_size = 0;
                            this.Check_timer = false;	//20171123 추가되어야함.
                            break;
                        }
                    }
                }
            }
        }
        return;
    }

    private boolean ByteLengthSet(byte[] array, int offSet) {
        boolean result = true;
        byte[] length_array = null;
        try{
            if(log.isInfoEnabled())
                log.info("lengthDataSize :"+lengthDataSize +" / lengthOffSet : "+lengthOffSet);

            if(array.length!=0){
                if(lengthOffSet==0){
                    length_array = new byte[lengthDataSize];
                    System.arraycopy(array, 0+offSet, length_array, 0, lengthDataSize);
                    if(lengthDataSize_ref_yn.equalsIgnoreCase(TcpConstants.useYn_Y)){
                        Data_size = Integer.parseInt(new String(array));
                    }else{
                        Data_size = Integer.parseInt(new String(array))+lengthDataSize;
                    }
                    if(log.isInfoEnabled())
                        log.info("TgrmLength "+Data_size+ "/ lengthDataSize :"+lengthDataSize +" / lengthOffSet : "+lengthOffSet);
                }else{
                    length_array = new byte[lengthDataSize];
                    System.arraycopy(array, lengthOffSet+offSet, length_array, 0, lengthDataSize);
                    if(lengthDataSize_ref_yn.equalsIgnoreCase(TcpConstants.useYn_Y)){
                        Data_size = Integer.parseInt(new String(array));
                    }else{
                        Data_size = Integer.parseInt(new String(array))+lengthDataSize;
                    }

                    if(log.isInfoEnabled())
                        log.info("TgrmLength "+Data_size+ "/ lengthDataSize :"+lengthDataSize +" / lengthOffSet : "+lengthOffSet);
                }

                if(log.isInfoEnabled())
                    log.info("DataOffSet ["+DataOffSet+"]");

                if(DataOffSet!=null&&!DataOffSet.equals("")){
                    if(Integer.parseInt(DataOffSet) != (lengthOffSet+lengthDataSize)){
                        if(log.isInfoEnabled())
                            log.info("[DataRecv] Data_size : "+Data_size);
                        Data_size = Data_size+Integer.parseInt(DataOffSet);
                    }
                }

                if(DataOffSet!=null&&!DataOffSet.equals("")){
                    if(DataOffSet!=null&&!DataOffSet.equals("")){
                        if(Integer.parseInt(DataOffSet) != (lengthOffSet+lengthDataSize)){
                            if(log.isInfoEnabled())
                                log.info("[DataRecv] Data_size : "+Data_size);

                            Data_size = Data_size+Integer.parseInt(DataOffSet);
                        }
                    }
                }

            }
            if(Data_size == 0){
                if(log.isInfoEnabled())
                    log.info("Data Size ["+Data_size+"]  Error");
                result = false;
            }
        }catch(NumberFormatException e){
            log.error("Data is Not Integer. offset["+offSet+"] size["+lengthDataSize+"]" );
            e.printStackTrace();
            result = false;
        }
        return result;
    }


}
