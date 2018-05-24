package me.czvn.blelibrary.utils;

/**
 * Created by andy on 2016/1/13.
 * 将比较大的数据拆分成蓝牙BLE能发送的小数据包
 */
public final class MsgSender {
    private static final int DEFAULT_SIZE = 20;
    private ISender sender;


    public MsgSender(ISender sender) {
        this.sender = sender;
    }


    public void sendMessage(byte[] data) {
        sendMessage(data, DEFAULT_SIZE);
    }


    public void sendMessage(byte[] data, int size) {
        int length = data.length;
        int counter = length / size;
        int rest = length % size;
        byte[] buffer = new byte[size];
        byte[] rests = new byte[rest];

        sender.inputData(MsgCommonUtil.goBytes(length));
        for (int i = 0; i < counter; i++) {
            System.arraycopy(data, i * size, buffer, 0, buffer.length);
            sender.inputData(buffer);
        }
        System.arraycopy(data, counter * size, rests, 0, rests.length);
        sender.inputData(rests);
    }

    public interface ISender {
        /**
         * 该方法将拆分的数据发送出去
         * @param bytes 拆分后的数据
         */
        void inputData(byte[] bytes);
    }
}
