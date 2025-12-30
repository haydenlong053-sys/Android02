package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfirechat.message.BiaoQingMessageContent;

@MessageContentType(BiaoQingMessageContent.class)
@EnableContextMenu
public class BiaoQingMessageContentViewHolder extends NormalMessageContentViewHolder{
    ImageView imageView;

    public BiaoQingMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        imageView =itemView.findViewById(R.id.stickerImageView);
    }

    @Override
    protected void onBind(UiMessage message) {
        BiaoQingMessageContent stickerMessage = (BiaoQingMessageContent) message.message.content;
        Glide.with(fragment)
                .load(stickerMessage.emoji)
                .into(imageView);
    }
}
