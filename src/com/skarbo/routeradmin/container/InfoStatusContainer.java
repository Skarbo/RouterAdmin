package com.skarbo.routeradmin.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InfoStatusContainer {

	public Map<String, Map<String, String>> containers = new HashMap<String, Map<String,String>>();

	public void merge(InfoStatusContainer infoStatusContainer) {
		for (Entry<String, Map<String, String>> entryContainer : infoStatusContainer.containers.entrySet()) {
			if (this.containers.containsKey(entryContainer.getKey())) {
				for (Entry<String, String> entryContainerMap : entryContainer.getValue().entrySet()) {
					if (!this.containers.get(entryContainer.getKey()).containsKey(entryContainerMap.getKey())
							|| this.containers.get(entryContainer.getKey()).get(entryContainerMap.getKey()) == null
							|| this.containers.get(entryContainer.getKey()).get(entryContainerMap.getKey())
									.equalsIgnoreCase(""))
						this.containers.get(entryContainer.getKey()).put(entryContainerMap.getKey(),
								entryContainerMap.getValue());
				}
			} else
				this.containers.put(entryContainer.getKey(), entryContainer.getValue());
		}
	}

}
