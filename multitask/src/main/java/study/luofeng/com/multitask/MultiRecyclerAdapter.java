package study.luofeng.com.multitask;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * MultiRecyclerAdapter
 * Created by weixi on 2016/9/17.
 */
public class MultiRecyclerAdapter extends RecyclerView.Adapter<MultiViewHolder> {

    private Context context;
    private List<FileInfo> list;
    private onItemButtonClickListener listener;


    public MultiRecyclerAdapter(List<FileInfo> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setOnItemButtonClickListener(onItemButtonClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MultiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_multi_task, parent, false);
        return new MultiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MultiViewHolder holder, int position) {
        // 首先声明一点，这里直接给按钮设置点击事件

        holder.displayName.setText(list.get(position).getDisplayName());
        holder.progressText.setText("0%");
        holder.taskDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onButtonClick(holder.taskDownload, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface onItemButtonClickListener {
        void onButtonClick(Button button, int position);
    }
}
