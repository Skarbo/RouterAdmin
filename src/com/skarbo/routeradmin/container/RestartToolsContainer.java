package com.skarbo.routeradmin.container;

public class RestartToolsContainer {
	public boolean restarting;
	public long time;
	public int delay;

	public RestartToolsContainer() {
	}

	public RestartToolsContainer(boolean restarting, int delay, long time) {
		this.restarting = restarting;
		this.time = time;
		this.delay = delay;
	}

}