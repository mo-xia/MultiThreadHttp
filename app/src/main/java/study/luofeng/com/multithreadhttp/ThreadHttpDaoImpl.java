package study.luofeng.com.multithreadhttp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadHttpDaoImpl
 * Created by weixi on 2016/9/14.
 */
public class ThreadHttpDaoImpl implements ThreadHttpDao {

    private ThreadHttpDBHelper dbHelper;

    public ThreadHttpDaoImpl(Context context) {
        dbHelper = new ThreadHttpDBHelper(context);
    }

    @Override
    public void insertThreadInfo(ThreadInfo threadInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //利用insert方法来插入数据
        /*ContentValues values = new ContentValues();
        values.put(ThreadHttpDBHelper.thread_id,threadInfo.getThread_id());
        values.put(ThreadHttpDBHelper.start,threadInfo.getStart());
        values.put(ThreadHttpDBHelper.end,threadInfo.getEnd());
        values.put(ThreadHttpDBHelper.finished,threadInfo.getFinished());
        values.put(ThreadHttpDBHelper.downloadUrl,threadInfo.getDownloadUrl());
        db.insert(ThreadHttpDBHelper.TABLE_NAME,null,values);*/

        //执行sql插入数据
        db.execSQL("insert into thread_info (thread_id,start,end,finished,downloadUrl) values (?,?,?,?,?)",
                new Object[]{threadInfo.getThread_id(), threadInfo.getStart(), threadInfo.getEnd(),
                        threadInfo.getFinished(), threadInfo.getDownloadUrl()});
        db.close();
    }

    /**
     * 如果不传入线程id，则删除所有该url匹配的threadInfo
     *
     * @param downloadUrl downloadUrl
     */
    public void deleteThreadInfo(String downloadUrl) {
        deleteThreadInfo(downloadUrl, MyConstant.DEFAULT_THREAD_ID);
    }

    /**
     * 删除指定url下指定线程id的threadInfo
     *
     * @param downloadUrl downloadUrl
     * @param thread_id   thread_id
     */
    @Override
    public void deleteThreadInfo(String downloadUrl, int thread_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (thread_id == MyConstant.DEFAULT_THREAD_ID) {
            db.execSQL("delete from thread_info where downloadUrl= '" + downloadUrl+"'");
//            db.delete(ThreadHttpDBHelper.TABLE_NAME,ThreadHttpDBHelper.downloadUrl+"=?",new String[]{downloadUrl});
        } else {
            db.execSQL("delete from thread_info where downloadUrl = '" + downloadUrl + "' and thread_id = " + thread_id);
//            db.delete(ThreadHttpDBHelper.TABLE_NAME,ThreadHttpDBHelper.downloadUrl+"=? and "+ThreadHttpDBHelper.thread_id+"=?",new String[]{downloadUrl,thread_id+""});
        }
        db.close();
    }

    /**
     * 单线程模式下
     *
     * @param downloadUrl downloadUrl
     * @param finished    finished
     */
    public void updateThreadInfo(String downloadUrl, long finished) {
        updateThreadInfo(downloadUrl, MyConstant.DEFAULT_THREAD_ID, finished);
    }

    /**
     * 多线程模式下
     *
     * @param downloadUrl downloadUrl
     * @param thread_id   thread_id
     * @param finished    finished
     */
    @Override
    public void updateThreadInfo(String downloadUrl, int thread_id, long finished) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(ThreadHttpDBHelper.finished,finished);
        if (thread_id == MyConstant.DEFAULT_THREAD_ID) {
            db.execSQL("update thread_info set finished = " + finished + " where downloadUrl = '" + downloadUrl+"'");
//            db.update(ThreadHttpDBHelper.TABLE_NAME,values,ThreadHttpDBHelper.downloadUrl+"=?",new String[]{downloadUrl});
        } else {
            String sql = "update thread_info set finished = " + finished + " where downloadUrl = '" + downloadUrl + "' and thread_id = " + thread_id;
            db.execSQL(sql);
//            db.update(ThreadHttpDBHelper.TABLE_NAME,values,ThreadHttpDBHelper.downloadUrl+"=?"+" and "+ThreadHttpDBHelper.thread_id+"=?",new String[]{downloadUrl,thread_id+""});
        }
        db.close();
    }

    @Override
    public List<ThreadInfo> selectThreadInfo(String downloadUrl) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<>();

//        Cursor cursor = db.rawQuery("select * from thread_info where downloadUrl = '"+downloadUrl+"'",null);
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
        db.close();
        return list;
    }

    public boolean isExist(String downloadUrl) {
        return isExist(downloadUrl, MyConstant.DEFAULT_THREAD_ID);
    }

    @Override
    public boolean isExist(String downloadUrl, int thread_id) {
        boolean isExist;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection;
        String[] selectionArgs;
        if (thread_id == MyConstant.DEFAULT_THREAD_ID) {
            selection = ThreadHttpDBHelper.downloadUrl + "=?";
            selectionArgs = new String[]{downloadUrl};
        } else {
            selection = ThreadHttpDBHelper.downloadUrl + "=? and "+ThreadHttpDBHelper.thread_id+"=?";
            selectionArgs = new String[]{downloadUrl,thread_id+""};
        }

        Cursor cursor = db.query(ThreadHttpDBHelper.TABLE_NAME, null,
                selection,selectionArgs,null,null,
                ThreadHttpDBHelper.thread_id);

//        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
//                new String[]{downloadUrl, thread_id + ""});
        isExist = cursor.moveToNext();
        cursor.close();
        db.close();
        return isExist;
    }
}
