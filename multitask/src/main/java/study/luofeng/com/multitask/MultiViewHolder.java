package study.luofeng.com.multitask;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * MultiViewHolder
 * Created by weixi on 2016/9/17.
 */
public class MultiViewHolder extends RecyclerView.ViewHolder {

    public TextView displayName,progressText;
    public Button taskDownload;

    public MultiViewHolder(View itemView) {
        super(itemView);
        displayName = (TextView) itemView.findViewById(R.id.tv_display_name);
        progressText = (TextView) itemView.findViewById(R.id.tv_progress);
        taskDownload = (Button) itemView.findViewById(R.id.btn_task_download);
    }
}
