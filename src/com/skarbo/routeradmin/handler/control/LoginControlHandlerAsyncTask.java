package com.skarbo.routeradmin.handler.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.util.Log;

import com.skarbo.routeradmin.handler.RouterControlHandler;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandleResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandlerAsyncTaskResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.HandleAsyncTask;
import com.skarbo.routeradmin.handler.RouterControlHandler.InvalidPageException;
import com.skarbo.routeradmin.utils.RestClient;
import com.skarbo.routeradmin.utils.RestClient.Response;
import com.skarbo.routeradmin.utils.Utils;
import com.skarbo.routeradmin.utils.RestClient.RequestMethod;

public class LoginControlHandlerAsyncTask extends HandleAsyncTask<Boolean, Void> {

	private static final String TAG = LoginControlHandlerAsyncTask.class.getSimpleName();

	private Pattern regexLoginDetails;
	private Pattern regexLoginPage;
	private Pattern regexLoginFailed;

	public LoginControlHandlerAsyncTask(RouterControlHandler routerControlHandler,
			ControlHandleResult<Boolean> handleResult) {
		super(routerControlHandler, handleResult);
	}

	// ... GET

	@Override
	public int getOrder() {
		return 1;
	}

	@Override
	public String getKey() {
		return TAG;
	}

	// ... /GET

	// ... IS

	public boolean isLoginPage(String result) {
		return regexLoginPage.matcher(result).find();
	}

	public boolean isLoginFailed(String result) {
		return regexLoginFailed.matcher(result).find();
	}

	// ... /IS

	// ... DO

	@Override
	public void doInitParsing() throws Exception {		
		super.doInitParsing();

		this.regexLoginPage = Pattern.compile(this.getRouterParsing().login.regexIsPage);
		this.regexLoginDetails = Pattern.compile(this.getRouterParsing().login.regexLoginDetails);
		this.regexLoginFailed = Pattern.compile(this.getRouterParsing().login.regexLoginFailed);
	}

	@Override
	public ControlHandlerAsyncTaskResult<Boolean> doHandle(Void... params) throws Exception {
		Log.d(TAG, "Handle Login: " + getRouterProfile().getUser() + ", " + getRouterProfile().getPassword());

		// Router uses auth to login
		if (getRouter().auth) {
			RestClient restClient = createRestClient(getRouterPage(this.getRouterParsing().login.page));
			Response response = restClient.execute(RequestMethod.GET);
			return new ControlHandlerAsyncTaskResult<Boolean>(response.getResponseCode() == 200);
		}
		// Router uses post to login
		else {
			// Get Login page
			RestClient restClient = createRestClient(getRouterPage(this.getRouterParsing().login.page));
			Response response = restClient.execute(RequestMethod.GET);
			String loginPageResult = response.getResponse();

			// Is login page
			if (!isLoginPage(loginPageResult))
				throw new InvalidPageException();

			// Create login post data
			Map<String, String> loginPostData = createLoginPostData(loginPageResult);

			// Do login
			restClient = createRestClient(getRouterPage(this.getRouterParsing().login.page));
			restClient.addData(loginPostData);
			response = restClient.execute(RequestMethod.POST);
			loginPageResult = response.getResponse();

			return new ControlHandlerAsyncTaskResult<Boolean>(!isLoginFailed(loginPageResult));
		}
	}

	// ... /DO

	public Map<String, String> createLoginPostDataReplace(String result) {
		Map<String, String> replace = new HashMap<String, String>();

		Matcher matcher = regexLoginDetails.matcher(result);
		if (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++)
				replace.put(String.valueOf(i), matcher.group(i));
		}

		replace.put("userid", getRouterProfile().getUser());
		replace.put("password", getRouterProfile().getPassword());

		return replace;
	}

	public Map<String, String> createLoginPostData(String result) {
		// Replace data
		Map<String, String> replace = createLoginPostDataReplace(result);

		// Post data
		Map<String, String> postData = this.getRouterParsing().login.post;

		String findReplace = null;
		for (Entry<String, String> entryReplace : replace.entrySet()) {
			for (Entry<String, String> entry : postData.entrySet()) {
				findReplace = "%" + entryReplace.getKey() + "%";
				if (entry.getValue().contains(findReplace))
					postData.put(entry.getKey(), entry.getValue().replace(findReplace, entryReplace.getValue()));
			}
		}

		return postData;
	}

}
