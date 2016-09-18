package study.luofeng.com.multitask;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * MultiDownloadActivity
 * Created by weixi on 2016/9/18.
 */
public class MultiDownloadActivity extends AppCompatActivity implements MultiRecyclerAdapter.onItemButtonClickListener {

    private static final String BTN_TEXT_DOWNLOAD = "下载";
    private static final String BTN_TEXT_PAUSE = "暂停";
    private static final String BTN_TEXT_CONTINUE = "续传";
    private static final String BTN_TEXT_FINISH = "安装";

    private List<FileInfo> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acty_multi_task);
        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        list = getFileInfoList();
        MultiRecyclerAdapter adapter = new MultiRecyclerAdapter(list,this);
        adapter.setOnItemButtonClickListener(this);
        recycler.setAdapter(adapter);
    }

    private List<FileInfo> getFileInfoList(){
        List<FileInfo> list = new ArrayList<>();
        String[] names = {MyFile.JINRITOUTIAO,MyFile.KUGOUYINYUE,MyFile.WANGYIYUNYINYUE,MyFile.WEIXIN,
                MyFile.JINRITOUTIAO,MyFile.KUGOUYINYUE,MyFile.WANGYIYUNYINYUE,MyFile.WEIXIN,
                MyFile.JINRITOUTIAO,MyFile.KUGOUYINYUE,MyFile.WANGYIYUNYINYUE,MyFile.WEIXIN,
                MyFile.JINRITOUTIAO,MyFile.KUGOUYINYUE,MyFile.WANGYIYUNYINYUE,MyFile.WEIXIN};
        String[] urls = {MyFile.JINRITOUTIAOURL,MyFile.KUGOUYINYUEURL,MyFile.WANGYIYUNYINYUEURL,MyFile.WEIXINURL,
                MyFile.JINRITOUTIAOURL,MyFile.KUGOUYINYUEURL,MyFile.WANGYIYUNYINYUEURL,MyFile.WEIXINURL,
                MyFile.JINRITOUTIAOURL,MyFile.KUGOUYINYUEURL,MyFile.WANGYIYUNYINYUEURL,MyFile.WEIXINURL,
                MyFile.JINRITOUTIAOURL,MyFile.KUGOUYINYUEURL,MyFile.WANGYIYUNYINYUEURL,MyFile.WEIXINURL};
        for (int i = 0; i < names.length; i++) {
            FileInfo fileInfo = new FileInfo(names[i],urls[i]);
            list.add(fileInfo);
        }
        return list;
    }

    @Override
    public void onButtonClick(Button button, int position) {

        if (button.getText() == BTN_TEXT_FINISH){
            // 跳转到安装操作
            return;
        }
        Intent intent = new Intent(this, MultiTaskService.class);
        switch (button.getText().toString()){
            case BTN_TEXT_DOWNLOAD:
                button.setText(BTN_TEXT_PAUSE);
                intent.setAction(MultiTaskService.ACTION_DOWNLOAD);
                break;
            case BTN_TEXT_CONTINUE:
                button.setText(BTN_TEXT_PAUSE);
                intent.setAction(MultiTaskService.ACTION_CONTINUE);
                break;
            case BTN_TEXT_PAUSE:
                button.setText(BTN_TEXT_CONTINUE);
                intent.setAction(MultiTaskService.ACTION_PAUSE);
                break;
        }
        intent.putExtra(MultiTaskService.KEY_URL, list.get(position).getDownloadUrl());
        startService(intent);
    }
}
