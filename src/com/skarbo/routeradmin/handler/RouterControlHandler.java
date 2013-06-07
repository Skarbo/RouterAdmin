package com.skarbo.routeradmin.handler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.os.AsyncTask;
import android.util.Log;

import com.skarbo.routeradmin.container.AccessControlContainer;
import com.skarbo.routeradmin.container.DevicesContainer;
import com.skarbo.routeradmin.container.InfoStatusContainer;
import com.skarbo.routeradmin.container.DevicesContainer.Device;
import com.skarbo.routeradmin.container.RestartToolsContainer;
import com.skarbo.routeradmin.handler.RouterHandler.NotifyListener;
import com.skarbo.routeradmin.handler.control.AccesscontrolAdvancedControlHandlerAsyncTask;
import com.skarbo.routeradmin.handler.control.AccesscontrolAdvancedControlHandlerAsyncTask.HandleParam;
import com.skarbo.routeradmin.handler.control.DevicesStatusControlHandlerAsyncTask;
import com.skarbo.routeradmin.handler.control.InfoStatusControlHandlerAsyncTask;
import com.skarbo.routeradmin.handler.control.LoginControlHandlerAsyncTask;
import com.skarbo.routeradmin.handler.control.RestartToolsControlHandlerAsyncTask;
import com.skarbo.routeradmin.listener.DevicesStatusListener;
import com.skarbo.routeradmin.listener.ErrorListener;
import com.skarbo.routeradmin.listener.InfoStatusListener;
import com.skarbo.routeradmin.listener.RestartToolsListener;
import com.skarbo.routeradmin.listener.RouterHandlerListener;
import com.skarbo.routeradmin.model.RouterProfile;
import com.skarbo.routeradmin.utils.RestClient;
import com.skarbo.routeradmin.utils.RestClient.Response;

public class RouterControlHandler {

	// VARIABLES

	public static final String TAG = RouterControlHandler.class.getSimpleName();

	private RouterHandler routerHandler;
	private List<RouterControlHandler.HandleAsyncTask<?, ?>> handleQueue;
	public RouterControlHandler.HandleAsyncTask<?, ?> handlingQueue;
	private Containers containers;
	public int notLoggedInCount = 0;

	private final Comparator<HandleAsyncTask<?, ?>> handleQueueComperator = new Comparator<HandleAsyncTask<?, ?>>() {
		@Override
		public int compare(HandleAsyncTask<?, ?> lhs, HandleAsyncTask<?, ?> rhs) {
			return (Integer.valueOf(lhs.getOrder())).compareTo(Integer.valueOf(rhs.getOrder())) * -1;
		}
	};

	// /VARIABLES

	public RouterControlHandler(RouterHandler routerHandler) {
		this.routerHandler = routerHandler;
		this.handleQueue = new ArrayList<RouterControlHandler.HandleAsyncTask<?, ?>>();
		this.containers = new Containers();
	}

	// FUNCTIONS

	// ... GET

	public RouterHandler getRouterHandler() {
		return routerHandler;
	};

	public RouterPreferencesHandler getPreferencesHandler() {
		return this.getRouterHandler().getPreferenceHandler();
	}

	public Containers getContainers() {
		return containers;
	}

	// ... /GET

	// ... IS

	public boolean isQueueHandling() {
		return this.handlingQueue != null;
	}

	// ... /IS

	// ... DO

	public void doReset() {
		Log.d(TAG, "DoReset");
		this.containers = new Containers();
		this.handlingQueue = null;
		this.handleQueue.clear();
		this.notLoggedInCount = 0;

		this.routerHandler.doNotifyListeners(RouterHandlerListener.class, new NotifyListener<RouterHandlerListener>() {
			@Override
			public void doNotify(RouterHandlerListener listener) {
				listener.onUpdated();
			}
		});
	}

	public void doLogin() {
		handleQueue(new LoginControlHandlerAsyncTask(this, new ControlHandleResult<Boolean>() {
			@Override
			public boolean handleResult(Boolean result) {
				Log.d(TAG, "Handle login result: " + result);
				// Notify error
				if (!result) {
					getRouterHandler().doNotifyListeners(ErrorListener.class, new NotifyListener<ErrorListener>() {
						@Override
						public void doNotify(ErrorListener listener) {
							listener.onError(new ErrorSettingsException("Illegal user or password"));
						}
					});
				}

				return result;
			}

			@Override
			public boolean handleError(Exception exception) {
				return false;
			}
		}));
	}

	// ... ... TOOLS

	public void doToolsRestart() {
		handleQueue(new RestartToolsControlHandlerAsyncTask(this, new RestartToolsControlHandleResult<Boolean>() {
			@Override
			public boolean handleResult(Boolean result) {
				Log.d(TAG, "Restart handle result: " + result);
				getContainers().setRestartToolsContainer(new RestartToolsContainer());

				// Notify restarting
				getRouterHandler().doNotifyListeners(RestartToolsListener.class,
						new NotifyListener<RestartToolsListener>() {
							@Override
							public void doNotify(RestartToolsListener listener) {
								listener.onRestarted();
							}
						});
				return true;
			}

			@Override
			public boolean handleError(Exception exception) {
				Log.d(TAG, "Restart handle error: " + exception.getMessage());
				return false;
			}

			@Override
			public void onRestarting(int delay) {
				Log.d(TAG, "OnRestarting: " + delay);
				getContainers()
						.setRestartToolsContainer(new RestartToolsContainer(true, delay, (new Date()).getTime()));

				// Notify restarting
				getRouterHandler().doNotifyListeners(RestartToolsListener.class,
						new NotifyListener<RestartToolsListener>() {
							@Override
							public void doNotify(RestartToolsListener listener) {
								listener.onRestarting();
							}
						});
			}

			@Override
			public void doResubHandle() {
				doToolsRestart();
			}
		}));
	}

	// ... ... /TOOLS

	// ... ... ADVANCED

	public void doAdvancedAccessControl(final HandleParam handleParam) {
		handleQueue(new AccesscontrolAdvancedControlHandlerAsyncTask(this,
				new ControlHandleResult<AccessControlContainer>() {
					@Override
					public boolean handleResult(AccessControlContainer result) {
						getContainers().setAccessControlContainer(result);
						return true;
					}

					@Override
					public boolean handleError(Exception exception) {
						return false;
					}

					@Override
					public void doResubHandle() {
						doAdvancedAccessControl(handleParam);
					}
				}, handleParam));
	}

	public void doAdvancedAccessControl() {
		doAdvancedAccessControl(new HandleParam());
	}

	public void doAdvancedAccessControl(boolean isChecked) {
		doAdvancedAccessControl(new HandleParam(isChecked));
	}

	public void doAdvancedAccessControl(int policyId, boolean enabled) {
		doAdvancedAccessControl(new HandleParam(policyId, enabled));
	}

	// ... ... /ADVANCED

	// ... ... STATUS

	public void doStatusDevices() {
		handleQueue(new DevicesStatusControlHandlerAsyncTask(this, new ControlHandleResult<DevicesContainer>() {
			@Override
			public boolean handleResult(final DevicesContainer result) {
				List<Device> devices = result.getDevices();
				Log.d(TAG, "DoStatusDevices: HandleResult: " + result.getDevices().size());
				for (Device device : devices) {
					getContainers().getDevicesContainer().addDevice(device);
				}

				// Sort Devices
				Collections.sort(getContainers().getDevicesContainer().getDevices(), new Comparator<Device>() {
					@Override
					public int compare(Device lhs, Device rhs) {
						int result = 0;
						if (lhs.inactive == rhs.inactive) {
							if (lhs.ipAddress.contains("\\.") && rhs.ipAddress.contains("\\."))
								result = Integer.valueOf(lhs.ipAddress.split(".")[3]).compareTo(
										Integer.valueOf(rhs.ipAddress.split(".")[3]));
							else
								result = lhs.ipAddress.compareTo(rhs.ipAddress);
						} else
							result = lhs.inactive ? 1 : -1;
						return result;
					}
				});

				// Notify DevicesStatusListener
				getRouterHandler().doNotifyListeners(DevicesStatusListener.class,
						new NotifyListener<DevicesStatusListener>() {
							@Override
							public void doNotify(DevicesStatusListener listener) {
								listener.onDevicesUpdated(result);
							}
						});

				return true;
			}

			@Override
			public boolean handleError(Exception exception) {
				return false;
			}

			@Override
			public void doResubHandle() {
				doStatusDevices();
			}
		}));
	}

	public void doStatusInfo() {
		handleQueue(new InfoStatusControlHandlerAsyncTask(this, new ControlHandleResult<InfoStatusContainer>() {
			@Override
			public boolean handleResult(final InfoStatusContainer result) {
				Log.d(TAG, "DoStatusInfo: " + result.containers.size());
				getContainers().infoStatusContainer.merge(result);

				// Notify InfoStatusListener
				getRouterHandler().doNotifyListeners(InfoStatusListener.class,
						new NotifyListener<InfoStatusListener>() {
							@Override
							public void doNotify(InfoStatusListener listener) {
								listener.onInfoUpdated(result);
							}
						});

				return true;
			}

			@Override
			public void doResubHandle() {
				doStatusInfo();
			}
		}));
	}

	// ... ... /STATUS

	// ... /DO

	// ... HANDLE

	public void handleQueue(HandleAsyncTask<?, ?> handleAsyncTask) {
		Log.d(TAG, "Handle queue: " + this.handleQueue.size());
		for (HandleAsyncTask<?, ?> queue : this.handleQueue) {
			Log.d(TAG, "Queue: " + queue.getKey());
		}

		// Add handle to queue
		if (handleAsyncTask != null) {
			if (!this.handleQueue.contains(handleAsyncTask)) {
				this.handleQueue.add(handleAsyncTask);
				Collections.sort(this.handleQueue, this.handleQueueComperator);
			} else {
				Log.d(TAG, "Task already in queue: " + handleAsyncTask);
				int indexOf = this.handleQueue.indexOf(handleAsyncTask);
				if (indexOf > -1)
					this.handleQueue.set(indexOf, handleAsyncTask);
			}
		}

		if (this.handlingQueue != null) {
			Log.w(TAG, "Handle queue is not null");
			return;
		}

		if (!this.handleQueue.isEmpty()) {
			// Notify updating
			this.routerHandler.doNotifyListeners(RouterHandlerListener.class,
					new NotifyListener<RouterHandlerListener>() {
						@Override
						public void doNotify(RouterHandlerListener listener) {
							listener.onUpdating();
						}
					});

			// Handle first in queue
			HandleAsyncTask<?, ?> handle = this.handleQueue.remove(0);
			this.handlingQueue = handle;
			Log.d(TAG, "Handle first in queue: " + handle.getKey());
			handle.execute();
		} else {
			// Notify updated
			this.routerHandler.doNotifyListeners(RouterHandlerListener.class,
					new NotifyListener<RouterHandlerListener>() {
						@Override
						public void doNotify(RouterHandlerListener listener) {
							listener.onUpdated();
						}
					});
			this.handlingQueue = null;
		}
	}

	public void handledQueue() {
		this.handlingQueue = null;
		handleQueue(null);
	}

	// ... /HANDLE

	// /FUNCTIONS

	// CLASS

	// ... HANDLE RESULT

	public static abstract class ControlHandleResult<T> {
		/**
		 * @param result
		 * @return True if handle is to be removed from queue
		 */
		public abstract boolean handleResult(T result);

		/**
		 * @param exception
		 * @return True if handled, false if not handled
		 */
		public boolean handleError(Exception exception) {
			return false;
		}

		public void doResubHandle() {

		}
	}

	public static abstract class RestartToolsControlHandleResult<T> extends ControlHandleResult<T> {
		public abstract void onRestarting(int delay);
	}

	// ... /HANDLE RESULT

	// ... CONTAINERS

	public static class Containers {

		private RestartToolsContainer restartToolsContainer;
		private AccessControlContainer accessControlContainer;
		private DevicesContainer devicesContainer;
		private InfoStatusContainer infoStatusContainer;

		public Containers() {
			this.restartToolsContainer = new RestartToolsContainer();
			this.accessControlContainer = new AccessControlContainer();
			this.devicesContainer = new DevicesContainer();
			this.infoStatusContainer = new InfoStatusContainer();
		}

		public RestartToolsContainer getRestartToolsContainer() {
			return restartToolsContainer;
		}

		public void setRestartToolsContainer(RestartToolsContainer restartToolsContainer) {
			this.restartToolsContainer = restartToolsContainer;
		}

		public AccessControlContainer getAccessControlContainer() {
			return accessControlContainer;
		}

		public void setAccessControlContainer(AccessControlContainer accessControlContainer) {
			this.accessControlContainer = accessControlContainer;
		}

		public DevicesContainer getDevicesContainer() {
			return devicesContainer;
		}

		public void setDevicesContainer(DevicesContainer devicesContainer) {
			this.devicesContainer = devicesContainer;
		}

		public InfoStatusContainer getInfoStatusContainer() {
			return infoStatusContainer;
		}

	}

	// ... /CONTAINERS

	// ... EXCEPTION

	public static class InvalidPageException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class NotLoggedinException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	public static class ErrorSettingsException extends Exception {
		private static final long serialVersionUID = 1L;

		public ErrorSettingsException(String message) {
			super(message);
		}
	}

	// ... /EXCEPTION

	public static class ControlHandlerAsyncTaskResult<T> {
		private T result;
		private Exception error;

		public T getResult() {
			return result;
		}

		public boolean isError() {
			return getError() != null;
		}

		public Exception getError() {
			return error;
		}

		public ControlHandlerAsyncTaskResult(T result) {
			this.result = result;
		}

		public ControlHandlerAsyncTaskResult(Exception error) {
			this.error = error;
		}
	}

	// ... ASYNC TASK

	public static abstract class HandleAsyncTask<T, E> extends AsyncTask<Void, E, ControlHandlerAsyncTaskResult<T>> {

		private ControlHandleResult<T> handleResult;
		private RouterControlHandler routerControlHandler;
		private RouterProfile routerProfile;
		private RouterPreferencesHandler.Router router;
		private RouterPreferencesHandler.RouterParsing routerParsing;

		public HandleAsyncTask(RouterControlHandler routerControlHandler, ControlHandleResult<T> handleResult) {
			this.routerControlHandler = routerControlHandler;
			this.handleResult = handleResult;
		}

		// ... GET

		public abstract String getKey();

		public ControlHandleResult<T> getHandleResult() {
			return handleResult;
		}

		public RouterControlHandler getRouterControlHandler() {
			return routerControlHandler;
		}

		public RouterProfile getRouterProfile() {
			return routerProfile;
		}

		public RouterPreferencesHandler.Router getRouter() {
			return router;
		}

		public RouterPreferencesHandler.RouterParsing getRouterParsing() {
			return routerParsing;
		}

		/**
		 * @return Higher number is sorted higher on list
		 */
		public int getOrder() {
			return 0;
		}

		public String getRouterPage(String page) {
			return String.format("http://%s/%s", getRouterProfile().getIp(), page);
		}

		// ... /GET

		// ... IS

		protected boolean isPage(String result, Pattern regex) {
			Matcher regexMatcher = regex.matcher(result);
			return regexMatcher.find();
		}

		public boolean isNotLoggedIn(Response response) {
			boolean result = false;

			if (this.getRouter().auth)
				result = response.getResponseCode() != 200;
			else {
				String stringRegexNotLoggedIn = this.getRouterParsing().notloggedin;
				Pattern regexNotLoggedIn = Pattern.compile(stringRegexNotLoggedIn);
				result = regexNotLoggedIn.matcher(response.getResponse()).find();
			}

			if (result)
				this.routerControlHandler.notLoggedInCount++;
			else
				this.routerControlHandler.notLoggedInCount = 0;
			return result;
		}

		// ... /IS

		@Override
		public boolean equals(Object o) {
			if (o instanceof HandleAsyncTask)
				return ((HandleAsyncTask<?, ?>) o).getKey() == getKey();
			return super.equals(o);
		}

		// ... HANDLE

		protected void handleError(final Exception exception) {
			Log.e(TAG, "Handle Async error", exception);
			if (exception instanceof ClientProtocolException) {
				this.routerControlHandler.getRouterHandler().doNotifyListeners(ErrorListener.class,
						new NotifyListener<ErrorListener>() {
							@Override
							public void doNotify(ErrorListener listener) {
								listener.onError(new ErrorSettingsException("Error while connecting to the router"));
							}
						});
			} else if (exception instanceof URISyntaxException) {
				this.routerControlHandler.getRouterHandler().doNotifyListeners(ErrorListener.class,
						new NotifyListener<ErrorListener>() {
							@Override
							public void doNotify(ErrorListener listener) {
								listener.onError(new ErrorSettingsException("Illegal URL requested to router"));
							}
						});
			} else if (exception instanceof IOException) {
				this.routerControlHandler.getRouterHandler().doNotifyListeners(ErrorListener.class,
						new NotifyListener<ErrorListener>() {
							@Override
							public void doNotify(ErrorListener listener) {
								listener.onError(new ErrorSettingsException("Error while connecting to the router"));
							}
						});
			} else {
				this.routerControlHandler.getRouterHandler().doNotifyListeners(ErrorListener.class,
						new NotifyListener<ErrorListener>() {
							@Override
							public void doNotify(ErrorListener listener) {
								listener.onError(exception, getHandleResult());
							}
						});
			}
		}

		// ... /HANDLE

		// ... ON

		@Override
		protected void onPostExecute(ControlHandlerAsyncTaskResult<T> result) {
			if (result == null)
				return;
			if (result.isError()) {
				if (!handleResult.handleError(result.getError()))
					handleError(result.getError());
			} else {
				if (handleResult.handleResult(result.getResult()))
					this.routerControlHandler.handledQueue();
			}
		}

		// ... /ON

		// ... DO

		public abstract ControlHandlerAsyncTaskResult<T> doHandle(Void... params) throws Exception;

		public void doInitParsing() throws Exception {
			this.routerProfile = this.routerControlHandler.getRouterHandler().getRouterProfile();
			this.router = this.routerControlHandler.getPreferencesHandler().getRouter(this.routerProfile.getRouterId());

			this.routerParsing = this.routerControlHandler.getPreferencesHandler().getRouterParsing(
					this.routerProfile.getRouterId());
			if (routerParsing == null)
				throw new Exception("Router parsing is null");
		}

		@Override
		protected ControlHandlerAsyncTaskResult<T> doInBackground(Void... params) {
			try {
				doInitParsing();
				return doHandle(params);
			} catch (Exception e) {
				return new ControlHandlerAsyncTaskResult<T>(e);
			}
		}

		// ... /DO

		// ... CREATE

		public RestClient createRestClient(String url) {
			RestClient restClient = new RestClient(url);

			// Auth
			if (this.getRouter() != null && this.getRouter().auth)
				restClient.setAuthenticate(this.getRouterProfile().getUser(), this.getRouterProfile().getPassword());

			return restClient;
		}

		// ... /CREATE

	}

	// ... /ASYNC TASK

	// /CLASS

}
