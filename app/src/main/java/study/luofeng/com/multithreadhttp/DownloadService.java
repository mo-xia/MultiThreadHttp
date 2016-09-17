package study.luofeng.com.multithreadhttp;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * public class DownloadService extends Service {
 * <p/>
 * Created by weixi on 2016/9/14.
 */
public class DownloadService extends Service {

    // 服务的两种Action
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String ACTION_PAUSE = "action_pause";

    // 广播的两种Action
    public static final String ACTION_UPDATE = "action_update";
    public static final String ACTION_INIT = "action_init";

    public static final String TAG_THREAD_INFO = "thread_info";
    public static final String TAG_THREAD_COUNT = "thread_count";

    private ThreadInfo threadInfo;
    private DownloadTask task;
    private int threadCount;

    private boolean isFirstDownload = true;
    private Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        threadInfo = intent.getParcelableExtra(TAG_THREAD_INFO);
        //获得传入的action
        String action = intent.getAction();
        switch (action) { //根据action作相应操作
            case ACTION_DOWNLOAD:
                //表示要下载时
                if (isFirstDownload) {
                    threadCount = intent.getExtras().getInt(TAG_THREAD_COUNT);
                    downloadInit();
                } else {
                    downloadContinue();
                }
                break;
            case ACTION_PAUSE:
                //表示要暂停时
                pause();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 暂停
     */
    private void pause() {
        if (task != null) {
            task.setPause(true); //设置task为pause状态
        }
    }

    /**
     * 续传
     */
    private void downloadContinue() {
        if (task != null) {
            task.setPause(false); //暂停时设置了true 现在要设置回来
            task.execute();
        }
    }

    /**
     * 从响应头或者url得到文件名
     *
     * @param url     url
     * @param headers 响应头
     * @return 文件名
     */
    private String getFileName(String url, Headers headers) {
        String disposition = headers.get("content-disposition");
        if (disposition != null) {
//            "attachment;filename=filename"
            int indexFileName;
            if ((indexFileName = disposition.indexOf("filename")) != -1) {
                return disposition.substring(indexFileName + 9);
            }
        }
        int startIndex = url.lastIndexOf("/") + 1;
        return url.substring(startIndex);
    }

    /**
     * 初始化RandomAccessFile，获得下载文件的长度
     */
    private void downloadInit() {
        final String downloadUrl = threadInfo.getDownloadUrl();
        List<Call> list =new ArrayList<>();
        Client.getClient().get(downloadUrl,list, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Log.d(TAG_URL,response.headers().toString());

                final String fileName = getFileName(downloadUrl, response.headers());

                final long contentLength = response.body().contentLength();

                //给界面发送初始化的数据
                Intent intent = new Intent();
                intent.setAction(ACTION_INIT);
                intent.putExtra("fileName", fileName);
                intent.putExtra("contentLength", contentLength);
                sendBroadcast(intent);

                // android目录下的download文件夹，默认是一直有的，可以手动删除
                File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadPath.exists()) { //不存在则创建
                    downloadPath.mkdir();
                }
                File file = new File(downloadPath, fileName);
                RandomAccessFile randomAccessFile = null;
                try {
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    randomAccessFile.setLength(contentLength);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                response.body().close();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        isFirstDownload = false;
                        task = new DownloadTask(DownloadService.this, Client.getClient(), fileName, contentLength, threadCount,downloadUrl);
                        task.execute();
                    }
                });

//                下面这个任务是同步开启的，因为我们的http请求是异步的，它不运行在主线程
//                isFirstDownload = false;
//                task = new DownloadTask(DownloadService.this, Client.getClient(), fileName,contentLength,threadCount);
//                task.execute(downloadUrl);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 服务被关闭 意味着所有开启的任务应该取消
        task.cancel(threadInfo.getDownloadUrl());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
