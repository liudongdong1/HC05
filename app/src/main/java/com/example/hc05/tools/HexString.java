package com.example.hc05.tools;

import java.util.ArrayList;

/**
 * @author liudongdong
 * @Date 2021.12.2
 * @function: 将字节数组与16进制字符串数据进行转化函数
 * */
public class HexString {

    private HexString() {
    }

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexRepresentation) {
        if (hexRepresentation.length() % 2 == 1) {
            throw new IllegalArgumentException("hexToBytes requires an even-length String parameter");
        }

        int len = hexRepresentation.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexRepresentation.charAt(i), 16) << 4)
                    + Character.digit(hexRepresentation.charAt(i + 1), 16));
        }

        return data;
    }
    /**
     * @function: 判断是不是有效地flex传感器数据
     * @param flex_string : 字符串flex数据，例如：A0:403,446,386,358,439
     * @return: true:有效传感器数据
     *          false： 无效传感器数据
     * */
    public static Boolean isValidFlexData(String flex_string){
        if(flex_string.contains(",")&&flex_string.contains(":")&&flex_string.startsWith("A")){
            if(flex_string.split(",").length==5){
                return true;
            }
        }
        return false;
    }
    /**
     * @function: 从字符传数据中解析弯曲传感器数据
     * @param flexstring : 字符串flex数据，例如：A0:403,446,386,358,439
     * @return ArrayList<Double> 返回字符串数据数组
     * */
    public static ArrayList<ArrayList<Double>> getFlexFromString(String flexstring){
        ArrayList<ArrayList<Double>>arrayList=new ArrayList<>();
        int IndexA=flexstring.indexOf("A");
        int IndexN=flexstring.lastIndexOf("\n");
        flexstring=flexstring.substring(IndexA,IndexN);
        String[] flexStringList=flexstring.split("\n");
        for(int i=0;i<flexStringList.length;i++){
            //System.out.println("数据为："+flexStringList[i]);
            if(isValidFlexData(flexStringList[i])){
                ArrayList<Double>arrayList1=new ArrayList<>();
                String[] templist=flexStringList[i].split(":")[1].split(",");
                for(String temp: templist){
                    arrayList1.add(Double.valueOf(temp));
                }
                arrayList.add(arrayList1);
            }
        }
        return arrayList;
    }

    // 将字节数组转化为16进制字符串，确定长度
    public static String bytesToHexString(byte[] bytes, int a) {
        String result = "";
        for (int i = 0; i < a; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);// 将高24位置0
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }
    /**
     * @function:     将字节数组转化为16进制字符串，不确定长度
     * @param b : 字节数组
     * @return：16进制字符串数组
     * */
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i =0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);// 将高24位置0
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }
    /**
     * @function:     // 将16进制字符串转化为字节数组
     * @param paramString : 16进制字符串数组
     * @return：byte[] 字节数组
     * */
    public static byte[] hexStr2Bytes(String paramString) {
        int i = paramString.length() / 2;

        byte[] arrayOfByte = new byte[i];
        int j = 0;
        while (true) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + paramString.substring(j * 2, k)
                            + paramString.substring(k, l)).intValue());
            ++j;
        }
    }

}