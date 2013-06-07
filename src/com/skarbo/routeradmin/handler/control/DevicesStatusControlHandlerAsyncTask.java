package com.skarbo.routeradmin.handler.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.util.Log;

import com.skarbo.routeradmin.container.DevicesContainer;
import com.skarbo.routeradmin.container.DevicesContainer.Device;
import com.skarbo.routeradmin.handler.RouterControlHandler;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandleResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandlerAsyncTaskResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.HandleAsyncTask;
import com.skarbo.routeradmin.handler.RouterControlHandler.InvalidPageException;
import com.skarbo.routeradmin.handler.RouterControlHandler.NotLoggedinException;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler;
import com.skarbo.routeradmin.utils.RestClient;
import com.skarbo.routeradmin.utils.RestClient.RequestMethod;
import com.skarbo.routeradmin.utils.RestClient.Response;
import com.skarbo.routeradmin.utils.Utils;

public class DevicesStatusControlHandlerAsyncTask extends HandleAsyncTask<DevicesContainer, DevicesContainer> {

	private static final String TAG = DevicesStatusControlHandlerAsyncTask.class.getSimpleName();

	public DevicesStatusControlHandlerAsyncTask(RouterControlHandler routerControlHandler,
			ControlHandleResult<DevicesContainer> handleResult) {
		super(routerControlHandler, handleResult);
	}

	// ... GET

	@Override
	public String getKey() {
		return TAG;
	}

	// ... /GET

	// ... IS

	public boolean isPageDevices(Pattern pattern, String result) {
		return pattern.matcher(result).find();
	}

	// ... IS

	// ... ON

	@Override
	protected void onProgressUpdate(DevicesContainer... values) {
		Log.d(TAG, "OnProgressUpdate: " + values.length);
		if (values.length > 0) {
			getHandleResult().handleResult(values[0]);
		}
	}

	// ... /ON

	// ... DO

	@Override
	public ControlHandlerAsyncTaskResult<DevicesContainer> doHandle(Void... params) throws Exception {
		DevicesContainer devicesContainer = new DevicesContainer();
		Log.d(TAG, "DoHandle");

		int count = 0;
		for (RouterPreferencesHandler.RouterParsing.StatusDeviceRouterParsing statusDeviceRouterParsing : getRouterParsing().status_devices) {
			// Get access control page
			RestClient restClient = createRestClient(getRouterPage(statusDeviceRouterParsing.page));
			Response response = restClient.execute(RequestMethod.GET);
			Log.d(TAG, "StatusDeviceRouterParsing: " + count + " , " + response.getResponse().length());
			// Is logged in
			if (isNotLoggedIn(response))
				throw new NotLoggedinException();

			// Is correct page
			if (!isPageDevices(Pattern.compile(statusDeviceRouterParsing.regexIsPage), response.getResponse()))
				throw new InvalidPageException();

			List<Device> devices = createDevices(statusDeviceRouterParsing, response.getResponse());
			for (Device device : devices)
				devicesContainer.addDevice(device);

			if (++count < getRouterParsing().status_devices.length)
				publishProgress(devicesContainer);
		}
		return new ControlHandlerAsyncTaskResult<DevicesContainer>(devicesContainer);
	}

	// ... /DO

	// ... CREATE

	public List<DevicesContainer.Device> createDevices(
			RouterPreferencesHandler.RouterParsing.StatusDeviceRouterParsing statusDevice, String response) {
		List<DevicesContainer.Device> devices = new ArrayList<DevicesContainer.Device>();
Log.d(TAG, "CreateDevices");
		Matcher matcher = Pattern.compile(statusDevice.deviceRegex, Pattern.DOTALL | Pattern.MULTILINE)
				.matcher(response);
		Log.d(TAG, "DeviceRegex: " + matcher.toString());		
		while (matcher.find())
			devices.add(createDevice(statusDevice, matcher));

		return devices;
	}

	public DevicesContainer.Device createDevice(
			RouterPreferencesHandler.RouterParsing.StatusDeviceRouterParsing statusDevice, Matcher matcher) {
		DevicesContainer.Device device = new DevicesContainer.Device();
Log.d(TAG, "CreateDevice:\n" + matcher.group(0));
		if (statusDevice.deviceObject.name != 0)
			device.name = matcher.group(statusDevice.deviceObject.name);
		if (statusDevice.deviceObject.ip != 0)
			device.ipAddress = matcher.group(statusDevice.deviceObject.ip);
		if (statusDevice.deviceObject.mac != 0)
			device.macAddress = matcher.group(statusDevice.deviceObject.mac);
		if (statusDevice.deviceObjectActive != null && statusDevice.deviceObjectActive != "")
			device.inactive = matcher.group(statusDevice.deviceObject.inactive).toLowerCase()
					.matches(statusDevice.deviceObjectActive);

		// Type
		if (statusDevice.deviceObject.type != 0) {
			Map<String, String> deviceType = statusDevice.deviceObjectType;
			for (Entry<String, String> entry : deviceType.entrySet()) {
				if (matcher.group(statusDevice.deviceObject.type).toLowerCase().matches(entry.getValue()))
					device.type = DevicesContainer.Device.getType(entry.getKey());
			}
		}

		// Interface
		if (statusDevice.deviceObject.intrface != 0) {
			Map<String, String> deviceInterfaces = statusDevice.deviceObjectInterface;
			for (Entry<String, String> entry : deviceInterfaces.entrySet()) {
				if (matcher.group(statusDevice.deviceObject.intrface).toLowerCase().matches(entry.getValue()))
					device.intrface = DevicesContainer.Device.getInterface(entry.getKey());
			}
		}

		return device;
	}

	// ... /CREATE

}
