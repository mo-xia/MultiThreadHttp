package study.luofeng.com.multitask;

import android.content.Context;
import android.os.Environment;
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
 * MultiDownloadTask
 * Created by weixi on 2016/9/17.
 */
public class MultiDownloadTask implements Runnable {

    /**
     * 任务的5种下载状态
     */
    private static final int STATE_DOWNLOAD = 0;
    private static final int STATE_PAUSE = 1;
    private static final int STATE_CONTINUE = 2;
    private static final int STATE_FINISH = 3;
    private static final int STATE_ERROR = 4;

    private int taskState = -1; // 当前的任务状态
    private String downloadUrl;
    private int threadCount;
    private static ThreadHttpDaoImpl httpDao;
    private String fileName;
    private File downloadPath;

    private long downloadLength;
    private FileInfo fileInfo;

    public MultiDownloadTask(Context context, String downloadUrl, int threadCount) {
        this.downloadUrl = downloadUrl;
        this.threadCount = threadCount;
        fileInfo = new FileInfo();
        fileInfo.setDownloadUrl(downloadUrl);
        if (httpDao == null) {
            httpDao = new ThreadHttpDaoImpl(context);
        }
    }

    @Override
    public void run() {
        Response response = Client.getClient().syncGet(downloadUrl);
        if (response == null) {
            taskState = STATE_ERROR;
        } else {
            long contentLength = response.body().contentLength();
            fileName = Utils.getFileName(downloadUrl, response.headers());

            fileInfo.setFileSize(contentLength);
            fileInfo.setFileName(fileName);
            // 创建文件
            createFile(contentLength, fileName);

            // 开启任务
            taskState = STATE_DOWNLOAD;
            executeDownload();
        }
    }

    public void executeDownload() {
        List<ThreadInfo> threadInfoList = null;
        switch (taskState){
            case STATE_DOWNLOAD:
                // 1.得到需要的线程数
                int needThreadCount = getNeedThreadCount(fileInfo.getFileSize());
                // 2.根据文件长度和线程数，分配每个线程的任务
                threadInfoList = getThreadInfoList(fileInfo.getFileSize(), needThreadCount);
                break;
            case STATE_CONTINUE:
                threadInfoList = httpDao.selectThreadInfo(downloadUrl);
                break;
        }

        // 3.根据线程信息下载
        startDownload(threadInfoList);
    }

    /**
     * 获得每个线程下载信息
     * @param contentLength   contentLength
     * @param needThreadCount needThreadCount
     * @return 线程信息的集合
     */
    private List<ThreadInfo> getThreadInfoList(long contentLength, int needThreadCount) {
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
     * 文件太小直接使用单线程
     * @param contentLength contentLength
     * @return 最终确认的线程数
     */
    private int getNeedThreadCount(long contentLength) {
        // 多少MB
        long l = contentLength / 1024 / 1024;
        if (l > 10) {
            return threadCount;
        }
        return 1;
    }

    /**
     * 创建一个随机访问文件 设置大小
     * @param contentLength 文件的大小
     * @param fileName      文件的名字
     */
    private void createFile(long contentLength, String fileName) {
        downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPause(){
        taskState = STATE_PAUSE;
    }

    public void setContinue(){
        taskState = STATE_CONTINUE;
    }


    private void startDownload(List<ThreadInfo> threadInfoList) {

        //遍历
        for (ThreadInfo info : threadInfoList) {
            final String downloadUrl = info.getDownloadUrl();
            final int thread_id = info.getThread_id();
            final long start = info.getStart();
            final long finished = info.getFinished();
            final long end = info.getEnd();

            if (start + finished < end + 1) { // 该线程还没下载完毕

                // 开始任务
                Client.getClient().get(downloadUrl, start + finished, end, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        taskState = STATE_ERROR;
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
                            if (taskState != STATE_PAUSE) {
                                randomAccessFile.write(buffer, 0, len);

                                // 当前任务的下载了的总字节数
                                downloadLength = downloadLength+len;

                                // 当前线程完成的总数
                                finishLen = finishLen + len;
                                // 发送该次循环所写的数据的大小

                                fileInfo.setDownloadLength(downloadLength);
                                Message message = Message.obtain();
                                message.what = MultiTaskService.WHAT_PROGRESS_UPDATE;
                                message.obj = fileInfo;
                                MultiTaskService.handler.sendMessage(message);
                            } else {
                                // 写进度
                                httpDao.updateThreadInfo(downloadUrl,thread_id,finishLen);
                                randomAccessFile.close();
                                inputStream.close();
                                return;
                            }
                        }

                        randomAccessFile.close();
                        inputStream.close();
                        httpDao.deleteThreadInfo(downloadUrl,thread_id);
                        if (downloadLength == fileInfo.getFileSize()){
                            taskState = STATE_FINISH;
                        }
                    }
                });
            }
        }
    }


}
