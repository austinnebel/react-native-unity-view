package com.reactnative.unity.view;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;

/**
 * View object for displaying Unity activity. Is managed my UnityViewManager.
 */
public class UnityView extends FrameLayout {

    private UnityPlayer view;

    protected UnityView(Context context) {
        super(context);
    }

    /**
     * Sets this view to the specified UnityPlayer object.
     * A 'player' is considered a scene in Unity.
     * @param player UnityPlayer to assign to this view.
     */
    public void setUnityPlayer(UnityPlayer player) {
        this.view = player;
        UnityUtils.addUnityViewToGroup(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (view != null) {
            view.windowFocusChanged(hasWindowFocus);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (view != null) {
            view.configurationChanged(newConfig);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // todo: fix more than one unity view, don't add to background.
        // UnityUtils.addUnityViewToBackground();
        super.onDetachedFromWindow();
    }
}
