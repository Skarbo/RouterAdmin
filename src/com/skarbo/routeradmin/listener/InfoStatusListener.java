package com.skarbo.routeradmin.listener;

import com.skarbo.routeradmin.container.InfoStatusContainer;

public interface InfoStatusListener extends RouterHandlerListener {

	public void onInfoUpdated(InfoStatusContainer infoStatusContainer);

}
