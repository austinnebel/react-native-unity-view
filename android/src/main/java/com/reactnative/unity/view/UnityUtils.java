package com.reactnative.unity.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.unity3d.player.UnityPlayer;

import java.util.concurrent.CopyOnWriteArraySet;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


/**
 * Holds a set of utilities for interacting directly with the Unity interface.
 */
public class UnityUtils {
    public interface CreateCallback {
        void onReady();
    }

    private static UnityPlayer unityPlayer;
    private static boolean _isUnityReady;
    private static boolean _isUnityPaused;
    private static final String TAG = "UnityUtils";

    private static final CopyOnWriteArraySet<UnityEventListener> mUnityEventListeners = new CopyOnWriteArraySet<>();

    /**
     * Returns the current UnityPlayer.
     * @return UnityPlayer
     */
    public static UnityPlayer getPlayer() {
        if (!_isUnityReady) {
            return null;
        }
        return unityPlayer;
    }

    /**
     * Returns true if Unity is initialized.
     * @return bool
     */
    public static boolean isUnityReady() {
        return _isUnityReady;
    }

    /**
     * Returns true if Unity is paused.
     * @return bool
     */
    public static boolean isUnityPaused() {
        return _isUnityPaused;
    }

    /**
     * Creates a UnityPlayer object in a UIThread, modifies window, and calls callback function.
     * Immediately calls callback function if player is already created.
     * @param activity The current application Activity.
     * @param callback Function to call after the new UnityPlayer is initialized.
     */
    public static void createPlayer(final Activity activity, final CreateCallback callback) {
        if (unityPlayer != null) {
            callback.onReady();
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setFormat(PixelFormat.RGBA_8888);

                unityPlayer = new UnityPlayer(activity);

                try {
                    // wait a moment. fix unity cannot start when startup.
                    Thread.sleep( 1000 );
                } catch (Exception ignored) {
                }

                // start unity
                addUnityViewToBackground();
                unityPlayer.windowFocusChanged(true);
                unityPlayer.requestFocus();
                unityPlayer.resume();

                _isUnityReady = true;
                callback.onReady();
            }
        });
    }

    public static void postMessage(String gameObject, String methodName, String message) {
        if (!_isUnityReady) {
            return;
        }
        UnityPlayer.UnitySendMessage(gameObject, methodName, message);
    }

    /**
     * Pauses the UnityPlayer.
     */
    public static void pause() {
        if (unityPlayer != null) {
            unityPlayer.pause();
            _isUnityPaused = true;
        }
    }

    /**
     * Resumes the UnityPlayer.
     */
    public static void resume() {
        if (unityPlayer != null) {
            unityPlayer.resume();
            _isUnityPaused = false;
        }
    }

    /**
     * Invoke by unity C#
     */
    public static void onUnityMessage(String message) {
        for (UnityEventListener listener : mUnityEventListeners) {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
                Log.e(TAG, "Error calling onMessage for event listeners. Error: " + e);
            }
        }
    }

    /**
     * Adds an event listener to this module.
     * @param listener Listener to add.
     */
    public static void addUnityEventListener(UnityEventListener listener) {
        mUnityEventListeners.add(listener);
    }

    /**
     * Removes an event listener from this module.
     * @param listener Listener to remove.
     */
    public static void removeUnityEventListener(UnityEventListener listener) {
        mUnityEventListeners.remove(listener);
    }

    /**
     * Removes UnityPlayer view from parent ViewGroup and restores activity layout.
     */
    public static void addUnityViewToBackground() {
        if (unityPlayer == null) {
            return;
        }
        if (unityPlayer.getParent() != null) {
            ((ViewGroup)unityPlayer.getParent()).removeView(unityPlayer);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unityPlayer.setZ(-1f);
        }
        final Activity activity = ((Activity)unityPlayer.getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
        activity.addContentView(unityPlayer, layoutParams);
    }

    /**
     * Removes UnityPlayer from current ViewGroup and assigns it to a new one.
     * @param group ViewGroup to assign UnityPlayer to.
     */
    public static void addUnityViewToGroup(ViewGroup group) {
        if (unityPlayer == null) {
            return;
        }
        if (unityPlayer.getParent() != null) {
            ((ViewGroup)unityPlayer.getParent()).removeView(unityPlayer);
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        group.addView(unityPlayer, 0, layoutParams);
        unityPlayer.windowFocusChanged(true);
        unityPlayer.requestFocus();
        if(!_isUnityPaused) { unityPlayer.resume(); }
    }
}
