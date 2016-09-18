package study.luofeng.com.multitask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String NAME_DOWNLOAD_SP = "download_sp";
    public static final String KEY_THREAD_COUNT = "thread_count";
    public static final String KEY_TASK_COUNT = "task_count";

    public static final int count_task_max = 5;
    public static final int count_thread_max = 5;
    public static final int count_thread_default = 1;
    public static final int count_task_default = 1;

    private EditText etMultiTask;
    private EditText etMultiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMultiTask = (EditText) findViewById(R.id.et_multi_task);
        etMultiThread = (EditText) findViewById(R.id.et_multi_thread);
        Button saveSetting = (Button) findViewById(R.id.btn_save_setting);
        Button multiTask = (Button) findViewById(R.id.btn_multi_task);
        saveSetting.setOnClickListener(this);
        multiTask.setOnClickListener(this);
    }

    private int getInputCount(EditText editText,int maxCount,int defaultCount){
        String text = editText.getText().toString();
        int count;
        if (text.isEmpty()) {
            count = defaultCount;
            editText.setText(defaultCount + "");
        } else {
            count = Integer.parseInt(text);
            if (count <= 0 || count > maxCount) {
                Toast.makeText(this, "数量必须大于等于1且小于等于为"+maxCount, Toast.LENGTH_SHORT).show();
                editText.setText(defaultCount + "");
                count = defaultCount;
            }
        }
        return count;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_setting:
                // 读取输入框的信息保存
                int taskCount = getInputCount(etMultiTask, count_task_max,count_task_default);
                int threadCount = getInputCount(etMultiThread, count_thread_max,count_thread_default);
                SharedPreferences.Editor editor = getSharedPreferences(NAME_DOWNLOAD_SP, MODE_PRIVATE).edit();
                editor.putInt(KEY_TASK_COUNT,taskCount);
                editor.putInt(KEY_THREAD_COUNT,threadCount);
                editor.apply();
                break;
            case R.id.btn_multi_task:
                Intent intent = new Intent(this,MultiDownloadActivity.class);
                startActivity(intent);
                break;
        }
    }
}
