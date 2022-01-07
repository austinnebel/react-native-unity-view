package com.reactnative.unity.view;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * This class holds all native functions that can be accessed directly from react.
 */
public class UnityNativeModule extends ReactContextBaseJavaModule implements UnityEventListener {

    private static final String REACT_CLASS = "UnityNativeModule";

    /**
     * Creates a module instance and adds a UnityEventListener to UnityUtils.
     * @param reactContext React application context.
     */
    public UnityNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        UnityUtils.addUnityEventListener(this);
    }

    /**
     * Returns the string used to access this module from react.
     */
    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    /**
     * React method that returns if Unity is initialized.
     * @param promise Promise to resolve; returns true if Unity is currently initialized.
     */
    @ReactMethod
    public void isReady(Promise promise) {
        promise.resolve(UnityUtils.isUnityReady());
    }

    /**
     * React method that creates a unity player instance.
     * @param promise Promise to resolve; returns true after Unity is initialized.
     */
    @ReactMethod
    public void createUnity(final Promise promise) {
        UnityUtils.createPlayer(getCurrentActivity(), new UnityUtils.CreateCallback() {
            @Override
            public void onReady() {
                promise.resolve(true);
            }
        });
    }

    /**
     * React method that sends a message to the UnityPlayer.
     * @param gameObject Game object.
     * @param methodName Type of method that is being sent.
     * @param message Message to send.
     */
    @ReactMethod
    public void postMessage(String gameObject, String methodName, String message) {
        UnityUtils.postMessage(gameObject, methodName, message);
    }

    /**
     * React method that pauses the Unity instance.
     */
    @ReactMethod
    public void pause() {
        UnityUtils.pause();
    }

    /**
     * React method that resumes the Unity instance.
     */
    @ReactMethod
    public void resume() {
        UnityUtils.resume();
    }

    /**
     * Emits a message to React.
     * @param message Message to send.
     */
    @Override
    public void onMessage(String message) {
        ReactContext context = getReactApplicationContext();
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onUnityMessage", message);
    }
}
