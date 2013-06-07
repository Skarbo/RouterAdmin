package com.skarbo.routeradmin.container;

import java.util.ArrayList;
import java.util.List;

public class AccessControlContainer {

	public boolean enabled;

	private List<AccessControlContainer.Policy> policies;

	public AccessControlContainer() {
		this.policies = new ArrayList<AccessControlContainer.Policy>();
	}

	public List<AccessControlContainer.Policy> getPolicies() {
		return policies;
	}

	public static class Policy {

		public int id = 0;
		public boolean enable = false;
		public String policy = "";
		public String machine = "";
		public int filtering = 0;
		public int logged = 0;
		public int schedule = 0;

		public String toString() {
			return String.format("Enable: %s, Policy: %s, Machine: %s, Filtering: %d, Logged: %d, Schedule: %d",
					this.enable, this.policy, this.machine, this.filtering, this.logged, this.schedule);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("Enabled: %s, Policies: %d\n", this.enabled, this.getPolicies().size()));
		
		for (Policy policy : this.getPolicies()) {
			stringBuilder.append(policy.toString() + "\n");
		}
		
		return stringBuilder.toString();
	}
}