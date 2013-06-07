package com.skarbo.routeradmin.listener;

import com.skarbo.routeradmin.container.DevicesContainer;

public interface DevicesStatusListener extends RouterHandlerListener {

	public void onDevicesUpdated(DevicesContainer devicesContainer);

}
