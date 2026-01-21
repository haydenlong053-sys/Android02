package com.cboda.baselibrary.view;

import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
    public enum State { EXPANDED, COLLAPSED, IDLING }

    private State currentState = State.IDLING;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        if (offset == 0) {
            if (currentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED);
            }
            currentState = State.EXPANDED;
        } else if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
            if (currentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED);
            }
            currentState = State.COLLAPSED;
        } else {
            if (currentState != State.IDLING) {
                onStateChanged(appBarLayout, State.IDLING);
            }
            currentState = State.IDLING;
        }
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
}
