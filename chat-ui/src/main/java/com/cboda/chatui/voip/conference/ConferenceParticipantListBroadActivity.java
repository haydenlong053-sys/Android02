/*
 * Copyright (c) 2021 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip.conference;

import android.content.Intent;
import android.view.MenuItem;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.voip.conference.model.ConferenceBroadInfo;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.message.ConferenceInviteMessageContent;

public class ConferenceParticipantListBroadActivity extends WfcBaseBroadActivity {
    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }

    @Override
    protected void afterViews() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerFrameLayout, new ConferenceParticipantListBroadFragment())
            .commit();
    }

    @Override
    protected int menu() {
        return R.menu.conference_participant_list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_conference_participant_add) {
            addParticipant();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addParticipant() {
        AVEngineKit.CallSession session = AVEngineKit.Instance().getCurrentSession();
        ConferenceBroadManager conferenceManager = ConferenceBroadManager.getManager();
        ConferenceBroadInfo conferenceInfo = conferenceManager.getCurrentConferenceInfo();
        ConferenceInviteMessageContent invite = new ConferenceInviteMessageContent(session.getCallId(), conferenceInfo.getOwner(), session.getTitle(), session.getDesc(), session.getStartTime(), session.isAudioOnly(), session.isDefaultAudience(), session.isAdvanced(), session.getPin());

        Intent intent = new Intent(this, ConferenceInviteBroadActivity.class);
        intent.putExtra("inviteMessage", invite);
        startActivity(intent);
    }

}
