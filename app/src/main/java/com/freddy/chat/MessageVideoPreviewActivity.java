package com.freddy.chat;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.cboda.intelligent.R;

public class MessageVideoPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_PATH = "extra_video_path";

    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_video_preview);

        videoView = findViewById(R.id.video_preview);
        ImageButton close = findViewById(R.id.btn_close);
        close.setOnClickListener(v -> finish());

        String path = getIntent().getStringExtra(EXTRA_VIDEO_PATH);
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, R.string.video_path_invalid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = path.startsWith("http://") || path.startsWith("https://")
                ? Uri.parse(path)
                : Uri.fromFile(new java.io.File(path));
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, R.string.video_preview_failed, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        });
    }

    @Override
    protected void onPause() {
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }
}
