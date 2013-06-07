package com.skarbo.routeradmin.handler.control;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.skarbo.routeradmin.container.InfoStatusContainer;
import com.skarbo.routeradmin.handler.RouterControlHandler;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandleResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandlerAsyncTaskResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.HandleAsyncTask;
import com.skarbo.routeradmin.handler.RouterControlHandler.InvalidPageException;
import com.skarbo.routeradmin.handler.RouterControlHandler.NotLoggedinException;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler.RouterParsing.StatusInfoRouterParsing;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler.RouterParsing.StatusInfoRouterParsing.ContainerStatusInfoRouterParsing;
import com.skarbo.routeradmin.utils.RestClient;
import com.skarbo.routeradmin.utils.RestClient.RequestMethod;
import com.skarbo.routeradmin.utils.RestClient.Response;
import com.skarbo.routeradmin.utils.Utils;

public class InfoStatusControlHandlerAsyncTask extends HandleAsyncTask<InfoStatusContainer, InfoStatusContainer> {

	private static final String TAG = InfoStatusControlHandlerAsyncTask.class.getSimpleName();

	public InfoStatusControlHandlerAsyncTask(RouterControlHandler routerControlHandler,
			ControlHandleResult<InfoStatusContainer> handleResult) {
		super(routerControlHandler, handleResult);
	}

	// ... GET

	@Override
	public String getKey() {
		return TAG;
	}

	// ... /GET

	// ... ON

	@Override
	protected void onProgressUpdate(InfoStatusContainer... values) {
		if (values.length > 0)
			getHandleResult().handleResult(values[0]);
	}

	// ... /ON

	// ... DO

	@Override
	public ControlHandlerAsyncTaskResult<InfoStatusContainer> doHandle(Void... params) throws Exception {
		InfoStatusContainer infoStatusContainer = new InfoStatusContainer();

		int count = 0;
		for (StatusInfoRouterParsing statusInfoRouterParsing : getRouterParsing().status_info) {

			// Rest client
			RestClient restClient = createRestClient(getRouterPage(statusInfoRouterParsing.page));
			Response response = restClient.execute(RequestMethod.GET);

			// Correct page
			if (!isInfoStatusPage(statusInfoRouterParsing.regexIsPage, response.getResponse()))
				throw new InvalidPageException();

			// Is logged in
			if (isNotLoggedIn(response))
				throw new NotLoggedinException();

			for (ContainerStatusInfoRouterParsing container : statusInfoRouterParsing.containers) {
				Map<String, String> infoStatusMap = createInfoStatusContainer(container, response.getResponse());
				InfoStatusContainer infoStatusContainer2 = new InfoStatusContainer();
				infoStatusContainer2.containers.put(container.title, infoStatusMap);
				infoStatusContainer.merge(infoStatusContainer2);

			}

			if (++count < statusInfoRouterParsing.containers.length)
				publishProgress(infoStatusContainer);
		}

		return new ControlHandlerAsyncTaskResult<InfoStatusContainer>(infoStatusContainer);
	}

	public boolean isInfoStatusPage(String regexIsPage, String response) {
		return isPage(response, Pattern.compile(regexIsPage));
	}

	// ... /DO

	// ... CREATE

	public Map<String, String> createInfoStatusContainer(
			ContainerStatusInfoRouterParsing containerStatusInfoRouterParsing, String response) {
		// Log.d(TAG, "CreateInfoStatusContainer: " +
		// containerStatusInfoRouterParsing.title);
		Map<String, String> map = new HashMap<String, String>();
		Matcher matcher = Pattern.compile(containerStatusInfoRouterParsing.regex, Pattern.DOTALL | Pattern.MULTILINE)
				.matcher(response);
		if (matcher.find()) {
			// Log.d(TAG, "Matcher found: " + matcher.groupCount());

			Map<String, String> mapSubject = containerStatusInfoRouterParsing.object;
			Map<String, String> mapReplace = Utils.createMap(matcher);
			// Log.d(TAG, "Map Subject:\n" + Utils.mapToString(mapSubject));
			// Log.d(TAG, "Map Replace:\n" + Utils.mapToString(mapReplace));
			if (mapSubject != null && mapReplace != null)
				map = Utils.createMap(mapSubject, mapReplace);
			// Log.d(TAG, "Map:\n" + Utils.mapToString(map));
		}

		return map;
	}

	// ... /CREATE

}
