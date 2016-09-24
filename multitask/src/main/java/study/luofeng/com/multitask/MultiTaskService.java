package study.luofeng.com.multitask;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MultiTaskService
 * Created by weixi on 2016/9/14.
 */
public class MultiTaskService extends Service {

    /**
     * 前台对于下载任务的三种命令
     */
    public static final String ACTION_DOWNLOAD = "download";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_CONTINUE = "continue";

    // intent 传输数据的key
    public static final String KEY_URL = "downloadUrl";
    public static final String KEY_STATE = "taskState";

    private ExecutorService executorService;
    private int threadCount;
    private static Map<String, MultiDownloadTask> taskMap;

    public static final int WHAT_PROGRESS_UPDATE = 200;
    public static final int WHAT_STATE_CHANGE = 300;

    private static Map<String, List<DownloadObserver>> observerMap = new HashMap<>();

    public static Handler handler = new MyHandler();

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_PROGRESS_UPDATE) {
                FileInfo fileInfo = (FileInfo) msg.obj;
                long downloadLength = fileInfo.getDownloadLength();
                long fileSize = fileInfo.getFileSize();
                int progress = (int) (downloadLength * 100.00 / fileSize + 0.5);
//                Log.d(fileInfo.getFileName(),progress+"%");
                // 遍历当前url的观察者集合，调用进度更新方法
                List<DownloadObserver> observerList = observerMap.get(fileInfo.getDownloadUrl());
                for (DownloadObserver dObserver :
                        observerList) {
                    dObserver.onDownloadProgressUpdate(progress);
                }
            } else if (msg.what == WHAT_STATE_CHANGE) {
                Bundle bundle = (Bundle) msg.obj;
                int state = bundle.getInt(KEY_STATE);
                String url = bundle.getString(KEY_URL);
                List<DownloadObserver> observerList = observerMap.get(url);
                // 遍历当前url的观察者集合，调用状态改变方法
                for (DownloadObserver dObserver :
                        observerList) {
                    dObserver.onDownloadStateChange(state);
                }
                // 如果出错或者已经完成则从taskMap中移除
                if (state == MultiDownloadTask.STATE_ERROR ||
                        state == MultiDownloadTask.STATE_FINISH &&
                                taskMap.get(url) != null) {
                    taskMap.remove(url);
                }
            }
        }
    }

    /**
     * 注册一个观察者
     *
     * @param downloadUrl downloadUrl
     * @param observer    observer
     */
    public static void registObserver(String downloadUrl, DownloadObserver observer) {
        List<DownloadObserver> observerList = observerMap.get(downloadUrl);
        if (observerList == null) {
            observerList = new ArrayList<>();
            //这种逻辑比下面那种更清晰
//            observerList.add(observer);
//            observerMap.put(downloadUrl,observerList);
//            return;
        }
        if (observerList.indexOf(observer) == -1) { // 表示list中没找到当前这个observer
            observerList.add(observer);
            observerMap.put(downloadUrl, observerList);
        }
    }

    /**
     * 注销一个观察者
     *
     * @param downloadUrl downloadUrl
     * @param observer    observer
     */
    public static void unRegistObserver(String downloadUrl, DownloadObserver observer) {
        List<DownloadObserver> observerList = observerMap.get(downloadUrl);
        if (observerList.indexOf(observer) != -1) { //如果list中存在这个observer
            observerList.remove(observer);
            observerMap.put(downloadUrl, observerList);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sp = getSharedPreferences(MainActivity.NAME_DOWNLOAD_SP, MODE_PRIVATE);
        int taskCount = sp.getInt(MainActivity.KEY_TASK_COUNT, MainActivity.count_task_default);
        // 创建一个固定大小的线程池 可以往缓冲队列中无限添加任务
        threadCount = sp.getInt(MainActivity.KEY_THREAD_COUNT, MainActivity.count_thread_default);
        executorService = Executors.newFixedThreadPool(taskCount);
        taskMap = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String downloadUrl = intent.getExtras().getString(KEY_URL);

        switch (intent.getAction()) {
            case ACTION_DOWNLOAD:
                // 首次下载
                MultiDownloadTask task = new MultiDownloadTask(this, downloadUrl, threadCount);
                taskMap.put(downloadUrl, task);
                executorService.execute(task);
                break;
            case ACTION_PAUSE:
                if (taskMap.get(downloadUrl) != null) {
                    taskMap.get(downloadUrl).setPause();
                }
                break;
            case ACTION_CONTINUE:
                if (taskMap.get(downloadUrl) != null) {
                    taskMap.get(downloadUrl).setContinue(true);
                    taskMap.get(downloadUrl).executeDownload();
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
