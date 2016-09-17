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
    private File downloadPath;

    private Context context;
    private Client client;
    private String fileName;
    private long contentLength;
    private int threadCount;
    private String downloadUrl;

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
                    long finishLen = bundle.getLong("finishLen");
                    httpDao.updateThreadInfo(downloadUrl, thread_id, finishLen);
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

    public DownloadTask(Context context, Client client, String fileName, long contentLength, int threadCount, String downloadUrl) {
        this.context = context;
        this.client = client;
        this.fileName = fileName;
        this.contentLength = contentLength;
        this.threadCount = threadCount;
        this.downloadUrl = downloadUrl;
        // 创建dao
        httpDao = new ThreadHttpDaoImpl(context);
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * 执行任务
     */
    public void execute() {
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
     * 获得每个线程下载信息
     *
     * @param downloadUrl     downloadUrl
     * @param contentLength   contentLength
     * @param needThreadCount needThreadCount
     * @return 线程信息的集合
     */
    private List<ThreadInfo> getDlThreadInfoList(String downloadUrl, long contentLength, int needThreadCount) {
        List<ThreadInfo> list = new ArrayList<>();
        int ave = (int) (contentLength / needThreadCount);
        // 假设一共10个字节 0~9
        // 分三个线程
        // 第0个线程 0~2 应该完成 3
        // 第1个线程 3~5 应该完成 3
        // 第2个线程 6~10 应该完成 4
        for (int i = 0; i < needThreadCount; i++) {
            long end;
            if (i == needThreadCount - 1) {
                end = contentLength - 1;
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

    /**
     * handler发送消息
     *
     * @param bundle bundle
     * @param what   what
     */
    private void sendMessage(Bundle bundle, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = bundle;
        handler.sendMessage(msg);
    }

    /**
     * 发送一个完成消息
     * @param thread_id thread_id
     */
    private void sendFinishMessage(int thread_id){
        Bundle bundle = new Bundle();
        bundle.putString("downloadUrl", downloadUrl);
        bundle.putInt("thread_id", thread_id);
        sendMessage(bundle, WHAT_DELETE_THREAD);
    }

    /**
     * 暂停下载
     *
     * @param pause pause
     */
    public void setPause(boolean pause) {
        isPause = pause;
    }

    /**
     * 任务取消，http请求全部取消 数据库断点数据清空
     *  @param downloadUrl downloadUrl
     */
    public void cancel(String downloadUrl) {
        client.cancel(downloadUrl);
        httpDao.deleteThreadInfo(downloadUrl);
    }

    /**
     * 真正的下载逻辑
     *
     * @param threadInfoList List<ThreadInfo>
     */
    private void startDownload(List<ThreadInfo> threadInfoList) {

        //遍历
        for (ThreadInfo info : threadInfoList) {
            final String downloadUrl = info.getDownloadUrl();
            final int thread_id = info.getThread_id();
            final long start = info.getStart();
            final long finished = info.getFinished();
            final long end = info.getEnd();

//            Log.d("****", start + "------" + thread_id + "------" + finished);

            if (start + finished < end + 1) { // 该线程还没下载完毕

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

                        // 跳到上次完成的地方，要加上当前线程开始点
                        assert randomAccessFile != null;
                        randomAccessFile.seek(start + finished);
                        // 读写
                        while ((len = inputStream.read(buffer)) != -1) {
                            if (!isPause) {
                                randomAccessFile.write(buffer, 0, len);
                                // 当前线程完成的总数
                                finishLen = finishLen + len;
                                // 发送该次循环所写的数据的大小
                                sendDownloadBroadcast(downloadUrl, len);
                            } else {

                                // 多线程操作数据库 db.close 会报异常哦
                                //httpDao.updateThreadInfo(downloadUrl, thread_id, finishLen);

                                inputStream.close();
                                randomAccessFile.close();

                                // 如果暂停，把当前线程信息发送给handler
                                // 这么做的原因是 消息是发送到消息队列 是同步更新的
                                Bundle bundle = new Bundle();
                                bundle.putString("downloadUrl", downloadUrl);
                                bundle.putInt("thread_id", thread_id);
                                bundle.putLong("finishLen", finishLen);
                                sendMessage(bundle, WHAT_UPDATE_THREAD);

                                randomAccessFile.close();
                                inputStream.close();
                                return; //直接返回 onResponse方法直接结束
                            }
                        }

                        randomAccessFile.close();
                        inputStream.close();

                        // 如果能到这里，只能是当前线程的while循环结束，就是说所有下载都已经结束;
                        sendFinishMessage(thread_id);

                        // 下面这种写法看似很合逻辑，其实是浪费资源 ^_^
                        /*if (finishLen == end - start + 1 && thread_id != threadCount - 1 ) {
                            //不是最后一个线程 下载完成
                            sendFinishMessage(thread_id);

                        } else if (finishLen == end - start && thread_id == threadCount - 1) {
                            // 最后一个线程完成
                            sendFinishMessage(thread_id);
                        }*/
                    }
                });
            }
        }
    }

}
