package com.lxkj.aiim.widge;

import android.animation.Animator;

/**
 * 不做任何事的AnimatorListener
 */
public abstract class SimpleAnimatorListener implements Animator.AnimatorListener {

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
