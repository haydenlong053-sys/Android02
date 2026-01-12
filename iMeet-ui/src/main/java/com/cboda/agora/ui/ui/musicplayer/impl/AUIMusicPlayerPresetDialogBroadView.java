package com.cboda.agora.ui.ui.musicplayer.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.cboda.agora.ui.R;
import com.cboda.agora.ui.basic.AUIRecyclerBroadView;
import com.cboda.agora.ui.musicplayer.ControllerEffectInfo;

public class AUIMusicPlayerPresetDialogBroadView extends FrameLayout {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mMusicPlayerPresetAdapter;

    private int currentId;

    private IMusicPlayerPresetActionListener actionListener;

    public AUIMusicPlayerPresetDialogBroadView(@NonNull Context context, List<ControllerEffectInfo> list) {
        this(context, null, list);
    }

    public AUIMusicPlayerPresetDialogBroadView(@NonNull Context context, @Nullable AttributeSet attrs, List<ControllerEffectInfo> list) {
        this(context, attrs, 0, list);
    }

    public AUIMusicPlayerPresetDialogBroadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, List<ControllerEffectInfo> list) {
        this(context, attrs, defStyleAttr, 0, list);
    }

    public AUIMusicPlayerPresetDialogBroadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes, List<ControllerEffectInfo> list) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, list);
    }

    private void initView(@NonNull Context context, List<ControllerEffectInfo> presetList) {
        View.inflate(context, R.layout.aui_musicplayer_preset_dialog_broad_view, this);
        AUIRecyclerBroadView rvAudioPreset = findViewById(R.id.rv_audio_preset);
        mMusicPlayerPresetAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new AUIMusicPlayerEffectItemBroadView(parent.getContext())) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AUIMusicPlayerEffectItemBroadView itemView = (AUIMusicPlayerEffectItemBroadView) holder.itemView;
                final ControllerEffectInfo effectVoiceInfo = presetList.get(position);
                if (effectVoiceInfo == null) {
                    return;
                }
                itemView.setPresetInnerIcon(effectVoiceInfo.getIcon());
                itemView.setItemSelected(currentId == effectVoiceInfo.getIndex());
                itemView.setPresetName(itemView.getContext().getString(effectVoiceInfo.getTitle()));
                itemView.setOnClickListener(v -> {
                    currentId = effectVoiceInfo.getIndex();
                    mMusicPlayerPresetAdapter.notifyDataSetChanged();
                    if(actionListener!= null){
                        actionListener.onVoiceConversionChanged(effectVoiceInfo.getEffectId());
                    }
                });
            }

            @Override
            public int getItemCount() {
                return presetList.size();
            }
        };
        rvAudioPreset.setAdapter(mMusicPlayerPresetAdapter);
    }

    public void setActionListener(IMusicPlayerPresetActionListener actionListener) {
        this.actionListener = actionListener;
    }

    private List<ControllerEffectInfo> buildPresetData() {
        List<ControllerEffectInfo> list = new ArrayList<>();
        list.add(new ControllerEffectInfo(0, 0, R.drawable.aui_musicplayer_preset_broad_none, R.string.aui_musicplayer_preset_original));
        list.add(new ControllerEffectInfo(1, 1, R.drawable.aui_musicplayer_preset_broad_child, R.string.aui_musicplayer_preset_child));
        list.add(new ControllerEffectInfo(2, 2, R.drawable.aui_musicplayer_preset_broad_lolita, R.string.aui_musicplayer_preset_lolita));
        list.add(new ControllerEffectInfo(3, 3, R.drawable.aui_musicplayer_preset_broad_uncle, R.string.aui_musicplayer_preset_uncle));
        list.add(new ControllerEffectInfo(4, 4, R.drawable.aui_musicplayer_preset_broad_airy, R.string.aui_musicplayer_preset_airy));
        return list;
    }

    public interface IMusicPlayerPresetActionListener {
        void onVoiceConversionChanged(int id);
    }
}
