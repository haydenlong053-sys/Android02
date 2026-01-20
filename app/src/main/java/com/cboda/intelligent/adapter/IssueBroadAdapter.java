package com.cboda.intelligent.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.select.picture.entity.DataListBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/22
 * @Type:
 */
public class IssueBroadAdapter extends RecyclerView.Adapter<IssueBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public IssueBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public IssueBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_issue, parent, false);
        return new IssueBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(IssueBroadAdapter.MyHolder holder, final int position) {
        holder.setIsRecyclable(false);
        holder.tvTitle.setText(list.get(position).title);
        holder.webView.getSettings().setJavaScriptEnabled(true);
        holder.webView.getSettings().setAllowContentAccess(true);
        holder.webView.getSettings().setDatabaseEnabled(true);
        holder.webView.getSettings().setDomStorageEnabled(true);
        holder.webView.getSettings().setUseWideViewPort(true);
        holder.webView.getSettings().setLoadWithOverviewMode(true);
        int colorContent = ContextCompat.getColor(context, R.color.color_text0);
        int colorBg = ContextCompat.getColor(context, R.color.bg_main);
        String colorContentCode = String.format("#%06X", (0xFFFFFF & colorContent));
        String colorBgCode = String.format("#%06X", (0xFFFFFF & colorBg));


        String htmlContent = "<html><head><style type=\"text/css\">body { background-color: "+colorBgCode+"; color: +"+colorContentCode+"; }</style></head><body>"+list.get(position).context+"</body></html>";
        Log.e("html", htmlContent);
//        holder.webView.loadData(htmlContent, "text/html", "UTF-8");
        holder.webView.loadData(list.get(position).context, "text/html", "UTF-8");
        holder.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 这里可以检查是否有错误日志或尝试重新加载等操作
                Log.e("html","onPageFinished");

                view.evaluateJavascript(
                        "(function() {" +
                                "document.body.style.backgroundColor = '"+colorBgCode+"';" +
                                "document.body.style.color = '"+colorContentCode+"';" +
                                "document.body.style.fontSize = '50px';" +
                                "})();",
                        null
                );
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClickListener(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }


    public class MyHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        WebView webView;
        public MyHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            webView = itemView.findViewById(R.id.webView);
        }
    }
    private IssueBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(IssueBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
