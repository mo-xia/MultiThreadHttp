package study.luofeng.com.multithreadhttp;

import android.content.Context;
import android.preference.PreferenceActivity;

import java.io.IOException;
import java.net.HttpURLConnection;

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

    public static Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    /**
     * get方式请求
     *
     * @param url      url
     * @param callback 回掉接口
     */
    public void get(String url, Callback callback) {
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
//        Range: bytes=0-1024 代表请求数据的范围
        Request.Builder builder = new Request.Builder();
        if (start != -1 && end != -1) {
            builder=builder.header("Range", "bytes=" + start + "-" + end);
        }
        Request request = builder.url(url).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public void post() {

    }


}
