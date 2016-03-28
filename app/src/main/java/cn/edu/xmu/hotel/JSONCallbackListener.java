package cn.edu.xmu.hotel;

/**
 * Created by Administrator on 2016/3/27.
 */
public interface JSONCallbackListener {
    void onFinish(String data, String info, int code);
    void onError(Exception e);
}
