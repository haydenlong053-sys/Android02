package com.cboda.chatui.search.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.R;

public class FriendViewBroadHolder extends RecyclerView.ViewHolder {
    public ImageView portraitImageView;
    public TextView nameTextView;
    public TextView descTextView;
   public TextView categoryTextView;
   public View dividerLine;
   public View contactLinearLayout;

    public FriendViewBroadHolder(@NonNull View itemView) {
        super(itemView);
        bindViews(itemView);
    }
    private void bindViews(View itemView) {
        portraitImageView = itemView.findViewById(R.id.portraitImageView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        descTextView = itemView.findViewById(R.id.descTextView);
        categoryTextView = itemView.findViewById(R.id.categoryTextView);
        dividerLine = itemView.findViewById(R.id.dividerLine);
        contactLinearLayout = itemView.findViewById(R.id.contactLinearLayout);
    }
}
