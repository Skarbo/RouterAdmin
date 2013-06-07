package com.skarbo.routeradmin.container;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class DevicesContainer {

	private static final String TAG = DevicesContainer.class.getSimpleName();
	
	private List<DevicesContainer.Device> devices;

	public DevicesContainer() {
		devices = new ArrayList<DevicesContainer.Device>();
	}

	public List<DevicesContainer.Device> getDevices() {
		return devices;
	}

	public void setDevices(List<DevicesContainer.Device> devices) {
		this.devices = devices;
	}

	public void addDevice(DevicesContainer.Device device) {
		int indexOf = getDevices().indexOf(device);
		
		if (indexOf == -1)
			getDevices().add(device);
		else
			getDevices().get(indexOf).merge(device);
	}

	public static class Device {
		public enum Type {
			Pc, Laptop, Mobile, Tablet
		}

		public enum Interface {
			Ethernet, Wifi
		}

		public Type type;
		public String ipAddress;
		public String macAddress;
		public String name;
		public boolean inactive;
		public Interface intrface;

		public Device() {
		}

		public Device(Type type, String ipAddress, String macAddress, String name, boolean inactive, Interface intrface) {
			this.type = type;
			this.ipAddress = ipAddress;
			this.macAddress = macAddress;
			this.name = name;
			this.inactive = inactive;
			this.intrface = intrface;
		}

		public static Type getType(String typeSearch) {
			if (typeSearch != null) {
				for (Type type : Type.values()) {
					if (typeSearch.equalsIgnoreCase(type.name()))
						return type;
				}
			}
			return null;
		}

		public static Interface getInterface(String interfaceSearch) {
			if (interfaceSearch != null) {
				for (Interface intrface : Interface.values()) {
					if (interfaceSearch.equalsIgnoreCase(intrface.name()))
						return intrface;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return String.format("Name: %s, Type: %s, Ip: %s, Mac: %s, Interface: %s, Inactive: %s", this.name,
					this.type, this.ipAddress, this.macAddress, this.intrface, this.inactive);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Device)
				return this.ipAddress.equalsIgnoreCase(((Device) o).ipAddress);
			return super.equals(o);
		}

		public void merge(Device device) {
			if (this.name == null || this.name == "")
				this.name = device.name;
			if (this.ipAddress == null || this.ipAddress == "")
				this.ipAddress = device.ipAddress;
			if (this.macAddress == null || this.macAddress == "")
				this.macAddress = device.macAddress;
			if (this.type == null)
				this.type = device.type;
			if (this.intrface == null)
				this.intrface = device.intrface;
			if (!device.inactive)
				this.inactive = device.inactive;
		}

	}

}
