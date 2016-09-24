package study.luofeng.com.multitask;

/**
 * DownloadObserver
 * Created by weixi on 2016/9/24.
 */
public interface DownloadObserver {

    void onDownloadStateChange(int state);
    void onDownloadProgressUpdate(int progress);
}
