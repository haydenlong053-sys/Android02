package com.cboda.intelligent.widge;

import android.animation.Animator;

/**
 * 不做任何事的AnimatorListener
 */
public abstract class SimpleAnimatorBroadListener implements Animator.AnimatorListener {

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
}
