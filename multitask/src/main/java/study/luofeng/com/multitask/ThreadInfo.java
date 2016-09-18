package study.luofeng.com.multitask;


import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Created by weixi on 2016/9/14.
 */
public class ThreadInfo{

    private int thread_id;
    private long start;
    private long end;
    private long finished;
    private String downloadUrl;

    public ThreadInfo(int thread_id, long start, long end, long finished, String downloadUrl) {
        this.thread_id = thread_id;
        this.start = start;
        this.end = end;
        this.finished = finished;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "thread_id=" + thread_id +
                ", start=" + start +
                ", end=" + end +
                ", finished=" + finished +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }

    public int getThread_id() {
        return thread_id;
    }

    public void setThread_id(int thread_id) {
        this.thread_id = thread_id;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

}
