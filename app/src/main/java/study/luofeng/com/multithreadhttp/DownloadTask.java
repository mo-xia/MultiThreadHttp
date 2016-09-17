package study.luofeng.com.multithreadhttp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * DownloadTask
 * Created by weixi on 2016/9/14.
 */
public class DownloadTask {

    private ThreadHttpDaoImpl httpDao;
    private boolean isPause;

    private Context context;
    private Client client;
    private String fileName;
    private long contentLength;

    private File downloadPath;
    private int threadCount;
    private static final int WHAT_UPDATE_THREAD = 100;
    private static final int WHAT_DELETE_THREAD = 200;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case WHAT_UPDATE_THREAD:
                    Bundle bundle = (Bundle) msg.obj;
                    String downloadUrl = bundle.getString("downloadUrl");
                    int thread_id = bundle.getInt("thread_id");
                    long finished = bundle.getLong("finished");
                    httpDao.updateThreadInfo(downloadUrl, thread_id, finished);
                    break;
                case WHAT_DELETE_THREAD:
                    Bundle bundle1 = (Bundle) msg.obj;
                    String downloadUrl1 = bundle1.getString("downloadUrl");
                    int thread_id1 = bundle1.getInt("thread_id");
                    httpDao.deleteThreadInfo(downloadUrl1, thread_id1);
                    break;
            }
        }
    };

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public DownloadTask(Context context, Client client, String fileName, long contentLength, int threadCount) {
        this.context = context;
        this.client = client;
        this.fileName = fileName;
        this.contentLength = contentLength;
        this.threadCount = threadCount;
        // 创建dao
        httpDao = new ThreadHttpDaoImpl(context);
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * 根据文件长度和是否允许多线程标记判断需要开启的线程数
     *
     * @param isAllowMutiThread isAllowMutiThread
     * @return NeedThreadCount 默认单线程
     */
    private int getNeedThreadCount(boolean isAllowMutiThread) {
        if (isAllowMutiThread) {
            // 根据文件长度判断需要开启多少线程
            long l = contentLength / 1024 / 1024;
            if (l > 10) {
                return threadCount;
            }
        }
        return 1;
    }

    /**
     * 执行任务
     *
     * @param downloadUrl downloadUrl
     */
    public void execute(String downloadUrl) {

        // 根据检查结果续传或者从头开始
        if (!httpDao.isExist(downloadUrl)) {
            // 没有进度，创建进度，开始下载
            // 获得线程数量
            int needThreadCount = getNeedThreadCount(true);
            // 获得线程信息的集合 创建进度
            List<ThreadInfo> threadInfoList = getDlThreadInfoList(downloadUrl,
                    contentLength, needThreadCount);

            startDownload(threadInfoList);
        } else {
            // 有进度，读取进度，设置请求的位置
            startDownload(httpDao.selectThreadInfo(downloadUrl));
        }
    }

    private List<ThreadInfo> getDlThreadInfoList(String downloadUrl, long contentLenght, int needThreadCount) {
        List<ThreadInfo> list = new ArrayList<>();
        int ave = (int) (contentLenght / needThreadCount);
        // 假设一共10个字节 0~9
        // 分三个线程
        // 第0个线程 0~2 应该完成 3
        // 第1个线程 3~5 应该完成 3
        // 第2个线程 6~10 应该完成 4
        for (int i = 0; i < needThreadCount; i++) {
            long end;
            long finished;
            if (i == needThreadCount - 1) {
                end = contentLenght - 1;
            } else {
                end = ave * (i + 1) - 1;
            }
            ThreadInfo threadInfo = new ThreadInfo(
                    i, ave * i, end, 0, downloadUrl
            );

            // 创建进度
            httpDao.insertThreadInfo(threadInfo);
            list.add(threadInfo);
        }
        return list;
    }

    /**
     * 发送更新广播
     *
     * @param downloadUrl downloadUrl
     * @param finished    finished
     */
    private void sendDownloadBroadcast(String downloadUrl, long finished) {
        Intent intent = new Intent();
        intent.setAction(DownloadService.ACTION_UPDATE);
        intent.putExtra("downloadUrl", downloadUrl);
        intent.putExtra(downloadUrl, finished);
        context.sendBroadcast(intent);
    }

    private void sendMessage(Bundle bundle, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = bundle;
        handler.sendMessage(msg);
    }

    /**
     * 真正的下载逻辑
     *
     * @param threadInfoList List<ThreadInfo>
     */
    private void startDownload(List<ThreadInfo> threadInfoList) {

        for (ThreadInfo info : threadInfoList) {
            final String downloadUrl = info.getDownloadUrl();
            final int thread_id = info.getThread_id();
            final long start = info.getStart();
            final long finished = info.getFinished();

            Log.d("****", start + "------" + thread_id + "------" + finished);

            long end = info.getEnd();
            if (start + finished < end + 1) {
                // 开始任务
                client.get(downloadUrl, start + finished, end, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("线程" + thread_id, e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        RandomAccessFile randomAccessFile = null;
                        try {
                            randomAccessFile = new RandomAccessFile(new File(downloadPath, fileName), "rw");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        InputStream inputStream = response.body().byteStream();
                        long finishLen = finished;
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        randomAccessFile.seek(start + finished);
                        while ((len = inputStream.read(buffer)) != -1) {
                            if (!isPause) {
                                randomAccessFile.write(buffer, 0, len);
                                finishLen = finishLen + len;
                                sendDownloadBroadcast(downloadUrl, len);
                            } else {
//                                httpDao.updateThreadInfo(downloadUrl, thread_id, finishLen);
//                                sendDownloadBroadcast(downloadUrl, finishLen);

                                inputStream.close();
                                randomAccessFile.close();

                                Bundle bundle = new Bundle();
                                bundle.putString("downloadUrl", downloadUrl);
                                bundle.putInt("thread_id", thread_id);
                                bundle.putLong("finished", finishLen);
                                sendMessage(bundle, WHAT_UPDATE_THREAD);

                                break;
                            }
                        }
                        if (finishLen == contentLength) {

                            randomAccessFile.close();
                            inputStream.close();

                            Bundle bundle = new Bundle();
                            bundle.putString("downloadUrl", downloadUrl);
                            bundle.putInt("thread_id", thread_id);
                            sendMessage(bundle, WHAT_DELETE_THREAD);
//                            httpDao.deleteThreadInfo(downloadUrl, thread_id);
                        }
                    }
                });
            }
        }
    }

}
