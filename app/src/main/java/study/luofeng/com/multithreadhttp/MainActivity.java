package study.luofeng.com.multithreadhttp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isDownload = false; // 是不是在下载状态
    private boolean isFinished = false; // 是不是下载完成

//    public static Handler mainHandler = new MyHandler();
    private ProgressBar progressBar;
    private TextView downloadName;


    private DownloadBroadcastReceiver broadcastReceiver;
    private Button button;
    private TextView tv_progress;
    private EditText et_thread_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_thread_count = (EditText) findViewById(R.id.et_thread_count);
        downloadName = (TextView) findViewById(R.id.tv_download_name);
        progressBar = (ProgressBar) findViewById(R.id.pb_download);
        tv_progress = (TextView) findViewById(R.id.tv_download_progress);

        broadcastReceiver = new DownloadBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        intentFilter.addAction(DownloadService.ACTION_INIT);
        registerReceiver(broadcastReceiver,intentFilter);

        button = (Button) findViewById(R.id.btn_download);
        button.setText("下载");
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (isFinished){
            // 已经完成了 点击安装
            Toast.makeText(this,"点击安装应用",Toast.LENGTH_SHORT).show();
            // 已经安装了 点击打开应用
        }else {
            if (et_thread_count.getText() == null){
                et_thread_count.setText("1");
            }
            int threadCount = Integer.parseInt(et_thread_count.getText().toString());
            if (threadCount>8||threadCount<=0){
                Toast.makeText(this,"请输入数字1~8，其余均不合法",Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);
                intent.setAction(isDownload?DownloadService.ACTION_PAUSE:
                        DownloadService.ACTION_DOWNLOAD);
                String url = "http://downmobile.kugou.com/Android/KugouPlayer/8281/KugouPlayer_219_V8.2.8.apk";
                ThreadInfo threadInfo = new ThreadInfo(MyConstant.DEFAULT_THREAD_ID,MyConstant.DEFAULT_START,MyConstant.DEFAULT_END,MyConstant.DEFAULT_FINISHED,url);
                intent.putExtra(DownloadService.TAG_FILE, threadInfo);
                intent.putExtra(DownloadService.TAG_THREAD_COUNT,threadCount);
                isDownload = !isDownload;
                button.setText(isDownload?"暂停":"下载");
                startService(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public class DownloadBroadcastReceiver extends BroadcastReceiver {
        private long contentLength;
        private String fileName;
        private long totolFinish;

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            switch (action){
                case DownloadService.ACTION_INIT:
                    contentLength =  intent.getExtras().getLong("contentLength");
                    fileName = intent.getExtras().getString("fileName");
                    progressBar.setMax((int) (contentLength/1024.00));
                    downloadName.setText(fileName);
                    break;
                case DownloadService.ACTION_UPDATE:
                    String downloadUrl = intent.getExtras().getString("downloadUrl");

                    long singleFinished = intent.getExtras().getLong(downloadUrl);

                    totolFinish =totolFinish+singleFinished;

                    //这里的finished 是long类型，初始化进度条的时候 设置max为length/1024
                    //long数字太大，超过int范围
                    progressBar.setProgress((int) (totolFinish/1024.00));
                    int progress = (int) (totolFinish * 100.00 / contentLength);

                    tv_progress.setText(progress+"%");

                    if (totolFinish == contentLength){ //传输完毕
                        isFinished = true;
                        tv_progress.setText("下载完成");
                        button.setText("安装");
                    }

                    break;
            }
        }
    }
}
