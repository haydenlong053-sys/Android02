package com.freddy.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.cboda.intelligent.R;

public class MessageImagePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_PATH = "extra_image_path";
    public static final String EXTRA_THUMB_IMAGE_PATH = "extra_thumb_image_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_image_preview);

        ImageView preview = findViewById(R.id.iv_preview);
        ImageButton close = findViewById(R.id.btn_close);
        close.setOnClickListener(v -> finish());

        String path = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        String thumbnailsImg = getIntent().getStringExtra(EXTRA_THUMB_IMAGE_PATH);
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, R.string.picture_path_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        Glide.with(this)
                .load(path)
                .thumbnail(
                        Glide.with(this)
                                .load(thumbnailsImg)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(preview);

    }
}
