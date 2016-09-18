package study.luofeng.com.multitask;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ThreadHttpDBHelper
 * Created by weixi on 2016/9/14.
 */
public class ThreadHttpDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "thread_http_db";
    public static final int START_VERSION = 1;

    public static final String TABLE_NAME = "thread_info";
    public static final String _id ="_id";
    public static final String thread_id ="thread_id";
    public static final String start ="start";
    public static final String end ="end";
    public static final String finished ="finished";
    public static final String downloadUrl ="downloadUrl";

    private static final String SQL_TABLE_CREATE="create table thread_info (" +
            "_id Integer primary key autoincrement,"+
            "thread_id Integer ,"+
            "start long ,"+
            "end long ,"+
            "finished long ,"+
            "downloadUrl varchar"+
            ");";
    private static final String SQL_TABLE_DROP = "drop table if exist thread_info";

    public ThreadHttpDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public ThreadHttpDBHelper(Context context) {
        this(context, DB_NAME, null, START_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_TABLE_DROP);
        db.execSQL(SQL_TABLE_CREATE);
    }
}
