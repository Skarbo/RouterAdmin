package com.skarbo.routeradmin.model;

public class RouterProfile {

	private String id;
	private String ip;
	private String user;
	private String password;
	private String routerId;

	public RouterProfile() {
	}

	public RouterProfile(String id, String ip, String user, String password, String routerId) {
		this.id = id;
		this.ip = ip;
		this.user = user;
		this.password = password;
		this.routerId = routerId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRouterId() {
		return routerId;
	}

	public void setRouterId(String routerId) {
		this.routerId = routerId;
	}

	@Override
	public String toString() {
		return String.format("Profile id: %s, Ip: %s, User: %s, Password: %s, Router id: %s", this.getId(), this.getIp(),
				this.getUser(), this.getPassword(), this.getRouterId());
	}

}
