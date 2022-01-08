package com.reactnative.unity.view;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Map;

import javax.annotation.Nullable;


/**
 * Manages UnityView objects.
 */
public class UnityViewManager extends SimpleViewManager<UnityView> implements LifecycleEventListener, View.OnAttachStateChangeListener {

    private static final String REACT_CLASS = "RNUnityView";
    private static final String TAG = "UnityViewManager";
    private ReactApplicationContext context;

    UnityViewManager(ReactApplicationContext context) {
        super();
        this.context = context;
        context.addLifecycleEventListener(this);
    }

    @NonNull
    @Override
    public String getName() { return REACT_CLASS; }

    /**
     * Creates a Unity player object and assigns it to the view.
     * If a player already exists, assigns it to the view.
     * @param reactContext React application context.
     * @return UnityView object.
     */
    @NonNull
    @Override
    protected UnityView createViewInstance(@NonNull ThemedReactContext reactContext) {
        final UnityView view = new UnityView(reactContext);
        view.addOnAttachStateChangeListener(this);
        Log.d(TAG, "createViewInstance");

        if (UnityUtils.getPlayer() != null) {
            view.setUnityPlayer(UnityUtils.getPlayer());
        } else {
            UnityUtils.createPlayer(context.getCurrentActivity(), new UnityUtils.CreateCallback() {
                @Override
                public void onReady() {
                    view.setUnityPlayer(UnityUtils.getPlayer());
                }
            });
        }
        return view;
    }

    /**
     * Called when the UnityView object is destroyed.
     * @param view UnityView object that is destroyed.
     */
    @Override
    public void onDropViewInstance(UnityView view) {
        Log.d(TAG, "onDropViewInstance");
        view.removeOnAttachStateChangeListener(this);
        super.onDropViewInstance(view);
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume");
        if (UnityUtils.isUnityReady() && !UnityUtils.isUnityPaused()) {
            assert UnityUtils.getPlayer() != null;
            UnityUtils.getPlayer().resume();
        }
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause");
        if (UnityUtils.isUnityReady()) {
            // Don't use UnityUtils.pause()
            assert UnityUtils.getPlayer() != null;
            UnityUtils.getPlayer().pause();
        }
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy");
        if (UnityUtils.isUnityReady()) {
            assert UnityUtils.getPlayer() != null;
            UnityUtils.getPlayer().quit();
        }
    }

    /**
     * Called when view is attached from the window.
     * This happens when the view has entered the screen.
     * @param v View that was detached.
     */
    @Override
    public void onViewAttachedToWindow(View v) {
        Log.d(TAG, "onViewAttachedToWindow");
    }

    /**
     * Called when view is detached from the window. This usually
     * happens when the user has navigated to a different page.
     * @param v View that was detached.
     */
    @Override
    public void onViewDetachedFromWindow(View v) {
        Log.d(TAG, "onViewDetachedFromWindow");
    }

    // Required for rn built in EventEmitter Calls.
    @ReactMethod
    public void addListener(String eventName) {
        Log.d(TAG, "addListener " + eventName);
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        Log.d(TAG, "removeListeners " + count);
    }
}
