package com.marc.helmet.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.marc.helmet.R;

/** Marc Core HUD orb presets (idle, warning, unleashed, listen, processing). */
public final class MarcCoreUiHelper {

    private MarcCoreUiHelper() {
    }

    private static void cancelTaggedAnimator(View orbView) {
        if (orbView == null) {
            return;
        }
        Object tag = orbView.getTag(R.id.marc_orb_animator_holder);
        if (tag instanceof Animator) {
            ((Animator) tag).cancel();
        }
        orbView.setTag(R.id.marc_orb_animator_holder, null);
    }

    private static void cancelOrbAnimations(View orbView) {
        if (orbView == null) {
            return;
        }
        cancelTaggedAnimator(orbView);
        orbView.animate().cancel();
        orbView.clearAnimation();
    }

    private static void storeOrbAnimator(View orbView, Animator animator) {
        cancelTaggedAnimator(orbView);
        orbView.setTag(R.id.marc_orb_animator_holder, animator);
    }

    public static void setOrbNormal(
            View orbView, Context context, TextView stateText, TextView listenText) {
        if (orbView == null || context == null) {
            return;
        }
        cancelOrbAnimations(orbView);
        orbView.setBackground(
                ContextCompat.getDrawable(context, R.drawable.bg_orb_normal));
        orbView.setScaleX(1f);
        orbView.setScaleY(1f);
        orbView.setAlpha(1f);
        orbView.setRotation(0f);

        if (stateText != null) {
            stateText.setTextColor(Color.parseColor("#555555"));
        }
        if (listenText != null) {
            listenText.setText("");
        }

        ObjectAnimator sx =
                ObjectAnimator.ofFloat(orbView, View.SCALE_X, 0.97f, 1.03f);
        ObjectAnimator sy =
                ObjectAnimator.ofFloat(orbView, View.SCALE_Y, 0.97f, 1.03f);
        ObjectAnimator al = ObjectAnimator.ofFloat(orbView, View.ALPHA, 0.7f, 1.0f);
        sx.setDuration(2000);
        sy.setDuration(2000);
        al.setDuration(2000);
        sx.setRepeatMode(ObjectAnimator.REVERSE);
        sy.setRepeatMode(ObjectAnimator.REVERSE);
        al.setRepeatMode(ObjectAnimator.REVERSE);
        sx.setRepeatCount(ObjectAnimator.INFINITE);
        sy.setRepeatCount(ObjectAnimator.INFINITE);
        al.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy, al);
        set.start();
        storeOrbAnimator(orbView, set);
    }

    public static void setOrbCoreWarning(View orbView, Context context, TextView stateText) {
        if (orbView == null || context == null) {
            return;
        }
        cancelOrbAnimations(orbView);
        orbView.setBackground(
                ContextCompat.getDrawable(context, R.drawable.bg_orb_core));
        orbView.setScaleX(1f);
        orbView.setScaleY(1f);
        orbView.setRotation(0f);

        ObjectAnimator pulse = ObjectAnimator.ofFloat(orbView, View.ALPHA, 0.4f, 0.8f);
        pulse.setDuration(1500);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.start();
        storeOrbAnimator(orbView, pulse);

        if (stateText != null) {
            stateText.setTextColor(Color.parseColor("#CC1A1A"));
            stateText.setText("MARC CORE // STANDBY");
        }
    }

    public static void setOrbCoreUnleashed(
            View orbView, Context context, TextView stateText, TextView listenText) {
        if (orbView == null || context == null) {
            return;
        }
        cancelOrbAnimations(orbView);
        orbView.setBackground(
                ContextCompat.getDrawable(context, R.drawable.bg_orb_core));
        orbView.setRotation(0f);

        ObjectAnimator sx =
                ObjectAnimator.ofFloat(orbView, View.SCALE_X, 0.93f, 1.1f);
        ObjectAnimator sy =
                ObjectAnimator.ofFloat(orbView, View.SCALE_Y, 0.93f, 1.1f);
        sx.setDuration(350);
        sy.setDuration(350);
        sx.setRepeatMode(ObjectAnimator.REVERSE);
        sy.setRepeatMode(ObjectAnimator.REVERSE);
        sx.setRepeatCount(ObjectAnimator.INFINITE);
        sy.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator al = ObjectAnimator.ofFloat(orbView, View.ALPHA, 0.78f, 1.0f);
        al.setDuration(180);
        al.setRepeatMode(ObjectAnimator.REVERSE);
        al.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy, al);
        set.start();
        storeOrbAnimator(orbView, set);

        if (stateText != null) {
            stateText.setTextColor(Color.parseColor("#FF2020"));
            stateText.setText("MARC CORE // UNLEASHED");
        }
        if (listenText != null) {
            listenText.setText("NO FILTER. NO MERCY. NO HUMANITY.");
            listenText.setTextColor(Color.parseColor("#661111"));
        }
    }

    public static void setOrbListening(View orbView) {
        if (orbView == null) {
            return;
        }
        cancelOrbAnimations(orbView);
        ObjectAnimator sx = ObjectAnimator.ofFloat(orbView, View.SCALE_X, 1.0f, 1.12f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(orbView, View.SCALE_Y, 1.0f, 1.12f);
        sx.setDuration(500);
        sy.setDuration(500);
        sx.setRepeatMode(ObjectAnimator.REVERSE);
        sy.setRepeatMode(ObjectAnimator.REVERSE);
        sx.setRepeatCount(ObjectAnimator.INFINITE);
        sy.setRepeatCount(ObjectAnimator.INFINITE);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(sx, sy);
        set.start();
        storeOrbAnimator(orbView, set);
    }

    public static void setOrbProcessing(View orbView) {
        if (orbView == null) {
            return;
        }
        cancelOrbAnimations(orbView);
        ObjectAnimator rot = ObjectAnimator.ofFloat(orbView, View.ROTATION, 0f, 360f);
        rot.setDuration(2000);
        rot.setRepeatCount(ObjectAnimator.INFINITE);
        rot.setInterpolator(new LinearInterpolator());
        rot.start();
        storeOrbAnimator(orbView, rot);
    }

    public static void cancelAll(View orbView, TextView stateText) {
        if (orbView != null) {
            cancelOrbAnimations(orbView);
        }
        if (stateText != null) {
            stateText.clearAnimation();
            stateText.animate().cancel();
        }
    }
}
