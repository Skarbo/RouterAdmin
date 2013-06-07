package com.skarbo.routeradmin.listener;

import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandleResult;

public interface ErrorListener extends RouterHandlerListener {

	public void onError(Exception exception);
	
	public void onError(Exception exception, ControlHandleResult<?> controlHandleResult);

}
