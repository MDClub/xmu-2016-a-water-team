package cn.edu.xmu.hotel;

/**
 * Created by Administrator on 2016/3/20.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
