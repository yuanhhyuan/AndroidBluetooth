package me.czvn.blelibrary.utils;

/**
 * Created by andy on 2016/1/13.
 *
 */
public final class MsgCommonUtil {

    private MsgCommonUtil(){

    }

    public static byte[] merge(byte[] bytes1, byte[] bytes2) {
        int length1 = bytes1.length;
        int length2 = bytes2.length;
        byte[] bytes = new byte[length1 + length2];
        System.arraycopy(bytes1, 0, bytes, 0, length1);
        System.arraycopy(bytes2, 0, bytes, length1, length2);
        return bytes;
    }

    public static int goInt(byte[] bytes) {
        return Integer.parseInt(new String(bytes));
    }


    public static byte[] goBytes(int i) {
        return String.valueOf(i).getBytes();
    }


}
