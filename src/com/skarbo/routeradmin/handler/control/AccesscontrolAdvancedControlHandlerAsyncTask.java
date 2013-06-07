package com.skarbo.routeradmin.handler.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.skarbo.routeradmin.container.AccessControlContainer;
import com.skarbo.routeradmin.container.AccessControlContainer.Policy;
import com.skarbo.routeradmin.handler.RouterControlHandler;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandleResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandlerAsyncTaskResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.HandleAsyncTask;
import com.skarbo.routeradmin.handler.RouterControlHandler.InvalidPageException;
import com.skarbo.routeradmin.handler.RouterControlHandler.NotLoggedinException;
import com.skarbo.routeradmin.handler.RouterControlHandler.RestartToolsControlHandleResult;
import com.skarbo.routeradmin.handler.control.AccesscontrolAdvancedControlHandlerAsyncTask.HandleParam;
import com.skarbo.routeradmin.handler.control.AccesscontrolAdvancedControlHandlerAsyncTask.HandleParam.Type;
import com.skarbo.routeradmin.utils.RestClient;
import com.skarbo.routeradmin.utils.Utils;
import com.skarbo.routeradmin.utils.RestClient.RequestMethod;
import com.skarbo.routeradmin.utils.RestClient.Response;

public class AccesscontrolAdvancedControlHandlerAsyncTask extends HandleAsyncTask<AccessControlContainer, Void> {

	private static final String TAG = AccesscontrolAdvancedControlHandlerAsyncTask.class.getSimpleName();

	private HandleParam handleParam;

	private Pattern regexPageAccesscontrol;
	private Pattern regexAccesscontrolEnabled;
	private Pattern regexAccesscontrolPolicy;

	public AccesscontrolAdvancedControlHandlerAsyncTask(RouterControlHandler routerControlHandler,
			ControlHandleResult<AccessControlContainer> handleResult, HandleParam handleParam) {
		super(routerControlHandler, handleResult);
		this.handleParam = handleParam;
	}

	// ... GET

	@Override
	public String getKey() {
		return TAG;
	}

	// ... /GET

	// ... IS

	public boolean isPageAccessControl(String result) {
		return this.regexPageAccesscontrol.matcher(result).find();
	}

	public boolean isAccessControlEnabled(String result) {
		Matcher matcher = this.regexAccesscontrolEnabled.matcher(result);
		if (matcher.find())
			return Integer.parseInt(matcher.group(1)) > 0;
		return false;
	}

	// ... IS

	// ... DO

	@Override
	public ControlHandlerAsyncTaskResult<AccessControlContainer> doHandle(Void... params) throws Exception {
		Log.d(TAG, "DoHandle: " + this.handleParam.type);

		switch (this.handleParam.type) {
		case AccessControl:
			doAccessControlEnabled(this.handleParam.enabled);
			break;

		case Policy:
			doAccessControlPolicy(this.handleParam.enabled, this.handleParam.policyId);
			break;
		}

		return doAccessControlRetrieve();
	}

	public ControlHandlerAsyncTaskResult<AccessControlContainer> doAccessControlRetrieve() throws Exception {
		Log.d(TAG, "DoAccessControlRetrieve");
		// Get access control page
		RestClient restClient = createRestClient(getRouterPage(this.getRouterParsing().advanced_accesscontrol.page));
		Response response = restClient.execute(RequestMethod.GET);

		// Is logged in
		if (isNotLoggedIn(response))
			throw new NotLoggedinException();

		// Is correct page
		if (!isPageAccessControl(response.getResponse()))
			throw new InvalidPageException();

		// Access control
		AccessControlContainer accessControlContainer = new AccessControlContainer();

		accessControlContainer.enabled = isAccessControlEnabled(response.getResponse());
		accessControlContainer.getPolicies().addAll(createPolicies(response.getResponse()));

		return new ControlHandlerAsyncTaskResult<AccessControlContainer>(accessControlContainer);
	}

	public void doAccessControlEnabled(boolean enabled) throws Exception {
		Log.d(TAG, "DoAccessControlEnabled: " + enabled);
		// Create post data
		Map<String, String> postData = createAccessControlEnabledPostData(enabled);

		Log.d(TAG, "Post Data:\n" + Utils.mapToString(postData));

		// Post access control data
		RestClient restClient = createRestClient(getRouterPage(this.getRouterParsing().advanced_accesscontrol.page));
		restClient.addData(postData);
		Response response = restClient.execute(RequestMethod.POST);

		// Is logged in
		if (isNotLoggedIn(response))
			throw new NotLoggedinException();
	}

	public void doAccessControlPolicy(boolean enabled, int policyId) throws Exception {
		Log.d(TAG, "doAccessControlPolicyEnabled: " + enabled + ", " + policyId);
		// Create post data
		Integer[] policyIds = { policyId };
		Map<String, String> postData = createAccessControlEnablePolicyPostData(enabled ? policyIds : null,
				!enabled ? policyIds : null);

		Log.d(TAG, "Post Data:\n" + Utils.mapToString(postData));

		// Post access control data
		RestClient restClient = createRestClient(getRouterPage(this.getRouterParsing().advanced_accesscontrol.page));
		restClient.addData(postData);
		Response response = restClient.execute(RequestMethod.POST);

		// Is logged in
		if (isNotLoggedIn(response))
			throw new NotLoggedinException();
	}

	@Override
	public void doInitParsing() throws Exception {
		super.doInitParsing();
		this.regexPageAccesscontrol = Pattern.compile(this.getRouterParsing().advanced_accesscontrol.regexIsPage);
		this.regexAccesscontrolEnabled = Pattern.compile(this.getRouterParsing().advanced_accesscontrol.regexEnabled);
		this.regexAccesscontrolPolicy = Pattern.compile(this.getRouterParsing().advanced_accesscontrol.regexPolicy);
	}

	// ... /DO

	// ... CREATE

	public AccessControlContainer.Policy createPolicy(int id, Matcher regexMatcher) {
		AccessControlContainer.Policy accessControlPolicy = new AccessControlContainer.Policy();
		accessControlPolicy.id = id;
		accessControlPolicy.enable = Integer
				.parseInt(regexMatcher.group(this.getRouterParsing().advanced_accesscontrol.policyContainer.enable)) > 0;
		accessControlPolicy.policy = regexMatcher
				.group(this.getRouterParsing().advanced_accesscontrol.policyContainer.policy);
		accessControlPolicy.machine = regexMatcher
				.group(this.getRouterParsing().advanced_accesscontrol.policyContainer.machine);
		accessControlPolicy.filtering = Integer
				.parseInt(regexMatcher.group(this.getRouterParsing().advanced_accesscontrol.policyContainer.filtering));
		accessControlPolicy.logged = Integer
				.parseInt(regexMatcher.group(this.getRouterParsing().advanced_accesscontrol.policyContainer.logged));
		accessControlPolicy.schedule = Integer
				.parseInt(regexMatcher.group(this.getRouterParsing().advanced_accesscontrol.policyContainer.schedule));
		return accessControlPolicy;
	}

	public List<AccessControlContainer.Policy> createPolicies(String response) {
		List<AccessControlContainer.Policy> policies = new ArrayList<AccessControlContainer.Policy>();

		Matcher matcher = this.regexAccesscontrolPolicy.matcher(response);
		int i = 0;
		while (matcher.find())
			policies.add(createPolicy(++i, matcher));

		return policies;
	}

	public Map<String, String> createAccessControlEnabledPostData(boolean enabled) {
		Map<String, String> postData = this.getRouterParsing().advanced_accesscontrol.postEnabled;

		String enabledReplace = "%enabled%";
		for (Entry<String, String> entry : postData.entrySet()) {
			if (entry.getValue().contains(enabledReplace))
				postData.put(entry.getKey(), entry.getValue().replace(enabledReplace, enabled ? "1" : "0"));
		}

		for (String toggleValue : this.getRouterParsing().advanced_accesscontrol.postEnabledToggle) {
			if (postData.containsKey(toggleValue) && !enabled)
				postData.remove(toggleValue);
		}

		return postData;
	}

	public Map<String, String> createAccessControlEnablePolicyPostData(Integer[] policyEnable, Integer[] policyDisable) {
		Map<String, String> postData = this.getRouterParsing().advanced_accesscontrol.postPolicy;

		String enabledReplace = "%enable%";
		String disableReplace = "%disable%";
		for (Entry<String, String> entry : postData.entrySet()) {
			if (entry.getValue().contains(enabledReplace))
				postData.put(entry.getKey(), entry.getValue().replace(enabledReplace, Utils.implode(policyEnable, ",")));
			if (entry.getValue().contains(disableReplace))
				postData.put(entry.getKey(), entry.getValue()
						.replace(disableReplace, Utils.implode(policyDisable, ",")));
		}

		return postData;
	}

	// ... /CREATE

	@Override
	public boolean equals(Object o) {
		if (super.equals(o))
			return this.handleParam.equals(((AccesscontrolAdvancedControlHandlerAsyncTask) o).handleParam);
		return super.equals(o);
	}

	// CLASS

	public static class HandleParam {

		public enum Type {
			Retrieve, AccessControl, Policy
		}

		public Type type;
		public int policyId;
		public boolean enabled;

		public HandleParam() {
			this.type = Type.Retrieve;
		}

		public HandleParam(int policyId, boolean enabled) {
			this.type = Type.Policy;
			this.policyId = policyId;
			this.enabled = enabled;
		}

		public HandleParam(boolean accessControlEnabled) {
			this.type = Type.AccessControl;
			this.enabled = accessControlEnabled;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HandleParam))
				return super.equals(o);

			HandleParam handleParam = (HandleParam) o;
			if (this.type == Type.Retrieve && this.type == handleParam.type)
				return true;
			if (this.type == Type.AccessControl && this.type == handleParam.type)
				return true;
			if (this.type == Type.Policy && this.type == handleParam.type && this.policyId == handleParam.policyId)
				return true;

			return false;
		}

	}

	// /CLASS

}
