package com.skarbo.routeradmin.handler.control;

import java.util.regex.Pattern;

import org.json.JSONObject;

import android.util.Log;

import com.skarbo.routeradmin.handler.RouterControlHandler;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandlerAsyncTaskResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.HandleAsyncTask;
import com.skarbo.routeradmin.handler.RouterControlHandler.InvalidPageException;
import com.skarbo.routeradmin.handler.RouterControlHandler.NotLoggedinException;
import com.skarbo.routeradmin.handler.RouterControlHandler.RestartToolsControlHandleResult;
import com.skarbo.routeradmin.utils.RestClient;
import com.skarbo.routeradmin.utils.RestClient.RequestMethod;
import com.skarbo.routeradmin.utils.RestClient.Response;

public class RestartToolsControlHandlerAsyncTask extends HandleAsyncTask<Boolean, Integer> {

	private static final String TAG = RestartToolsControlHandlerAsyncTask.class.getSimpleName();

	protected RestartToolsControlHandleResult<Boolean> handleRestartToolsResult;

	private Pattern regexPageRestart;

	public RestartToolsControlHandlerAsyncTask(RouterControlHandler routerControlHandler,
			RestartToolsControlHandleResult<Boolean> handleResult) {
		super(routerControlHandler, handleResult);
		this.handleRestartToolsResult = handleResult;
	}

	// ... GET

	@Override
	public String getKey() {
		return TAG;
	}

	// ... /GET

	// ... IS

	public boolean isPageRestart(String result) {
		return this.regexPageRestart.matcher(result).find();
	}

	// ... IS

	// ... DO

	@Override
	public ControlHandlerAsyncTaskResult<Boolean> doHandle(Void... params) throws Exception {
		// Get restart page
		RestClient restClient = createRestClient(getRouterPage(this.getRouterParsing().tools_restart.page));
		Response response = restClient.execute(RequestMethod.GET);

		// Is logged in
		if (isNotLoggedIn(response))
			throw new NotLoggedinException();

		// Is restart page
		if (!isPageRestart(response.getResponse()))
			throw new InvalidPageException();

		int delay = 10;
		publishProgress(delay);

		Log.d(TAG, "DoHandle: Sleep");
		Thread.sleep(delay * 1000);

		return new ControlHandlerAsyncTaskResult<Boolean>(true);
	}

	@Override
	public void doInitParsing() throws Exception {
		super.doInitParsing();
		this.regexPageRestart = Pattern.compile(this.getRouterParsing().tools_restart.regexIsPage);
	}

	// ... /DO

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values.length > 0) {
			Log.d(TAG, "OnPregressUpdate: " + values[0]);
			this.handleRestartToolsResult.onRestarting(values[0]);
		}
	}

}
