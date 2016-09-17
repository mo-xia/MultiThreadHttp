package study.luofeng.com.multithreadhttp;

import android.content.Context;
import android.preference.PreferenceActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Client
 * Created by weixi on 2016/9/14.
 */
public class Client {

    private OkHttpClient okHttpClient;

    private static Client client;

    private Client() {
        okHttpClient = new OkHttpClient();
    }

    private Map<String,List<Call>> map = new HashMap<>();

    public static Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    /**
     * get方式请求
     * @param list list
     * @param url      url
     * @param callback 回掉接口
     */
    public void get(String url,List<Call> list, Callback callback) {
        map.put(url,list);
        get(url, -1, -1, callback);
    }

    /**
     * 支持断点续传的get请求
     * @param url url
     * @param start start
     * @param end end
     * @param callback callback
     */
    public void get(String url, long start, long end, Callback callback) {

        List<Call> list = map.get(url);

//        Range: bytes=0-1024 代表请求数据的范围
        Request.Builder builder = new Request.Builder();
        if (start != -1 && end != -1) {
            builder=builder.header("Range", "bytes=" + start + "-" + end);
        }
        Request request = builder.url(url).build();
        Call call = okHttpClient.newCall(request);

        list.add(call);
        call.enqueue(callback);
    }

    public void cancel(String downloadUrl){
        List<Call> list = map.get(downloadUrl);
        for (Call call:list) {
            call.cancel();
        }
    }

    public void post() {

    }


}
