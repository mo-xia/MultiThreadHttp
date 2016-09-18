package study.luofeng.com.multitask;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    /*******************************以下是同步请求****************************/
    public Response syncGet(String downloadUrl){
        Response response = null;
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


    /*******************************以下是异步请求****************************/


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
        Call call = okHttpClient.newCall(request);

        call.enqueue(callback);
    }


    public void post() {

    }


}
