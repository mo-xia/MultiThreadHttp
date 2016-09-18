package study.luofeng.com.multitask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadHttpDaoImpl
 * Created by weixi on 2016/9/14.
 */
public class ThreadHttpDaoImpl implements ThreadHttpDao {

    private ThreadHttpDBHelper dbHelper;
    private SQLiteDatabase db;

    public ThreadHttpDaoImpl(Context context) {
        dbHelper = new ThreadHttpDBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    @Override
    public void insertThreadInfo(ThreadInfo threadInfo) {

        //利用insert方法来插入数据
        ContentValues values = new ContentValues();
        values.put(ThreadHttpDBHelper.thread_id, threadInfo.getThread_id());
        values.put(ThreadHttpDBHelper.start, threadInfo.getStart());
        values.put(ThreadHttpDBHelper.end, threadInfo.getEnd());
        values.put(ThreadHttpDBHelper.finished, threadInfo.getFinished());
        values.put(ThreadHttpDBHelper.downloadUrl, threadInfo.getDownloadUrl());
        db.insert(ThreadHttpDBHelper.TABLE_NAME, null, values);

    }

    @Override
    public void deleteThreadInfo(String downloadUrl, int thread_id) {

        db.delete(ThreadHttpDBHelper.TABLE_NAME,
                ThreadHttpDBHelper.downloadUrl + "=? and " +
                        ThreadHttpDBHelper.thread_id + "=?",
                new String[]{downloadUrl, thread_id + ""});
    }

    @Override
    public void updateThreadInfo(String downloadUrl, int thread_id, long finished) {

        ContentValues values = new ContentValues();
        values.put(ThreadHttpDBHelper.finished, finished);
        db.update(ThreadHttpDBHelper.TABLE_NAME, values,
                ThreadHttpDBHelper.downloadUrl + "=?" + " and " +
                        ThreadHttpDBHelper.thread_id + "=?",
                new String[]{downloadUrl, thread_id + ""});
    }

    @Override
    public List<ThreadInfo> selectThreadInfo(String downloadUrl) {
        List<ThreadInfo> list = new ArrayList<>();

        Cursor cursor = db.query(ThreadHttpDBHelper.TABLE_NAME, null,
                ThreadHttpDBHelper.downloadUrl + "=?",
                new String[]{downloadUrl}, null, null,
                ThreadHttpDBHelper.thread_id);
        while (cursor.moveToNext()) {
            // 这个getInt()中的要求传入的索引值，是你查询列中的索引值
            // 我查询时是“*”，所以这里是1，0是_id的索引
            // 如果是自己写的要查询的列 例如：(thread_id,start,end) 那这里传入thread_id的索引就是0
            ThreadInfo info = new ThreadInfo(cursor.getInt(1), cursor.getLong(2),
                    cursor.getLong(3), cursor.getLong(4), cursor.getString(5));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    @Override
    public boolean isExist(String downloadUrl) {
        boolean isExist;

        String selection = ThreadHttpDBHelper.downloadUrl + "=?";
        String[] selectionArgs = new String[]{downloadUrl};

        Cursor cursor = db.query(ThreadHttpDBHelper.TABLE_NAME, null,
                selection, selectionArgs, null, null,
                null);

        isExist = cursor.moveToNext();
        cursor.close();
        return isExist;
    }

    /**
     * 关闭数据库
     */
    public void closeDatabase(){
        db.close();
        dbHelper.close();
    }
}
