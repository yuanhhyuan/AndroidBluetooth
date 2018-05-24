package me.czvn.blelibrary.utils;


import java.util.Arrays;

/**
 * Created by andy on 2016/1/13.
 * 将拆分的数据整合在一起
 */
public final class MsgReceiver {

    private boolean sending;
    private int length;
    private int nowLength;
    private byte[] totalBytes;
    private IReceiver receiver;


    public MsgReceiver(IReceiver receiver) {
        init();
        this.receiver = receiver;
    }

    private void init() {
        sending = false;
        length = 0;
        nowLength = 0;
        totalBytes = new byte[0];
    }

    public void outputData(byte[] bytes) {
        if (!sending) {
            length = MsgCommonUtil.goInt(bytes);
            sending = true;
        } else {
            nowLength += bytes.length;
            totalBytes = MsgCommonUtil.merge(totalBytes, bytes);
            if (nowLength >= length) {
                receiver.receiveData(Arrays.copyOf(totalBytes, totalBytes.length));
                init();
            }
        }

    }

    public interface IReceiver {
        /**
         * 接收整合后的数据
         * @param data 整合后的数据
         */
        void receiveData(byte[] data);
    }
}
