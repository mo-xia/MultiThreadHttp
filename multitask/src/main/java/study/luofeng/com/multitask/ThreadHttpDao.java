package study.luofeng.com.multitask;

import java.util.List;

/**
 * ThreadHttpDao
 * Created by weixi on 2016/9/14.
 */
public interface ThreadHttpDao {
    /**
     * 增
     * @param threadInfo 线程信息
     */
    public void insertThreadInfo(ThreadInfo threadInfo);

    /**
     * 删
     * @param downloadUrl downloadUrl
     * @param thread_id thread_id
     */
    public void deleteThreadInfo(String downloadUrl, int thread_id);

    /**
     * 改
     * @param downloadUrl downloadUrl
     * @param thread_id thread_id
     * @param finished 已经完成的字节数
     */
    public void updateThreadInfo(String downloadUrl, int thread_id, long finished);

    /**
     * 查
     * @param downloadUrl downloadUrl
     * @return 当前url对应线程信息的集合
     */
    public List<ThreadInfo> selectThreadInfo(String downloadUrl);

    /**
     * 判断是否存在
     * @param downloadUrl downloadUrl
     * @return 是否存在
     */
    public boolean isExist(String downloadUrl);
}
