package com.skarbo.routeradmin.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.skarbo.routeradmin.listener.RouterHandlerListener;
import com.skarbo.routeradmin.model.RouterProfile;

public class RouterHandler {

	// VARIABLES

	private static final String TAG = RouterHandler.class.getSimpleName();

	private Context context;
	private RouterPreferencesHandler preferenceHandler;
	private RouterControlHandler controlHandler;
	private RouterProfile routerProfile;
	private HashMap<String, RouterHandlerListener> listeners;

	// /VARIABLES

	public RouterHandler(Context context) {
		Log.d(TAG, "Rouer Handler initiated");
		this.context = context;
		this.preferenceHandler = new RouterPreferencesHandler(this.context);
		this.controlHandler = new RouterControlHandler(this);
		this.listeners = new HashMap<String, RouterHandlerListener>();
		this.routerProfile = this.preferenceHandler.getRouterProfileSelected();
	}

	// FUNCTIONS

	// ... GET/SET

	public Context getContext() {
		return context;
	}

	public RouterPreferencesHandler getPreferenceHandler() {
		return preferenceHandler;
	}

	public RouterControlHandler getControlHandler() {
		return controlHandler;
	}

	public RouterProfile getRouterProfile() {
		return routerProfile;
	}

	public void setRouterProfile(RouterProfile routerProfile) {
		this.routerProfile = routerProfile;
	}

	// ... /GET/SET

	// ... GETTERS/SETTERS

	// ... ADD/REMOVE

	public void addListener(String key, RouterHandlerListener listener) {
		listeners.put(key, listener);
	}

	public void removeListener(String key) {
		listeners.remove(key);
	}

	// ... /ADD/REMOVE

	// ... DO

	public void doRefresh() {
		Log.d(TAG, "DoRefresh");
		List<RouterHandlerListener> listenerList = Collections.list(Collections.enumeration(this.listeners.values()));
		for (RouterHandlerListener listener : listenerList) {
			listener.onRefresh();
		}
	}

	@SuppressWarnings("unchecked")
	public <E extends RouterHandlerListener> void doNotifyListeners(Class<E> notifyClass,
			NotifyListener<E> notifyListener) {
		Log.d(TAG, "DoNotifyListeners: " + notifyClass.getSimpleName());
		List<RouterHandlerListener> listenerList = Collections.list(Collections.enumeration(this.listeners.values()));
		for (RouterHandlerListener listener : listenerList) {
			if (notifyClass.isAssignableFrom(listener.getClass())) {
				notifyListener.doNotify((E) listener);
			}
		}
	}

	// ... /DO

	// /FUNCTIONS

	// INTERFACE

	public static interface NotifyListener<E extends RouterHandlerListener> {
		public void doNotify(E listener);
	}

	// /INTERFACE

}
