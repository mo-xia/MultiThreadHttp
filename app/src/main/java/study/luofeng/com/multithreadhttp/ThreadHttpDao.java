package study.luofeng.com.multithreadhttp;

import java.util.List;

/**
 * ThreadHttpDao
 * Created by weixi on 2016/9/14.
 */
public interface ThreadHttpDao {
    //增
    public void insertThreadInfo(ThreadInfo threadInfo);

    //删
    public void deleteThreadInfo(String downloadUrl,int thread_id);

    //改
    public void updateThreadInfo(String downloadUrl,int thread_id,long finished);

    //查
    public List<ThreadInfo> selectThreadInfo(String downloadUrl);

    //判断是否存在
    public boolean isExist(String downloadUrl,int thread_id);
}
