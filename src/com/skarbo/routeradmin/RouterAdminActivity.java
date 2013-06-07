package com.skarbo.routeradmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.skarbo.routeradmin.fragment.MenuFragment;
import com.skarbo.routeradmin.handler.RouterControlHandler.ControlHandleResult;
import com.skarbo.routeradmin.handler.RouterControlHandler.ErrorSettingsException;
import com.skarbo.routeradmin.handler.RouterControlHandler.InvalidPageException;
import com.skarbo.routeradmin.handler.RouterControlHandler.NotLoggedinException;
import com.skarbo.routeradmin.handler.RouterHandler;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler;
import com.skarbo.routeradmin.listener.ErrorListener;
import com.skarbo.routeradmin.listener.RouterHandlerListener;
import com.skarbo.routeradmin.model.RouterProfile;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class RouterAdminActivity extends SlidingFragmentActivity implements RouterHandlerListener, ErrorListener {

	private static final String TAG = RouterAdminActivity.class.getSimpleName();

	private static final String CONTENT_SAVE = "content";

	private static final int MENU_SETTINGS = 1;
	private static final int MENU_REFRESH = 2;

	private MenuItem menuRefresh;
	private MenuItem menuSettings;

	private RouterHandler routerHandler;
	private RouterProfile routerProfile;

	private Fragment mContent;
	private MenuFragment menuFragment;

	// ... ON

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "OnCreate");

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);

		// ACTIONBAR

		getSupportActionBar().setTitle("Router Admin");

		// /ACTIONBAR

		// ROUTER HANDLER

		this.routerHandler = new RouterHandler(this);

		// /ROUTER HANDLER

		// CONTENT

		menuFragment = new MenuFragment();

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, CONTENT_SAVE);
		// else
		// mContent = new DevicesStatusFragment();

		// /CONTENT

		// VIEW

		// Set the Above View
		setContentView(R.layout.frame_content);
		if (mContent != null)
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mContent).commit();

		// Set the Behind View
		setBehindContentView(R.layout.frame_menu);
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, menuFragment).commit();

		// Customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		sm.setOnOpenListener(new OnOpenListener() {
			@Override
			public void onOpen() {
				Log.d(TAG, "OnMenuOpen");
				menuFragment.onMenuOpen();
			}
		});

		// Set home as up
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// /VIEW
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "On Resume");

		// ROUTER HANDLER

		this.routerHandler.addListener(RouterAdminActivity.class.getSimpleName(), this);
		this.routerProfile = this.routerHandler.getPreferenceHandler().getRouterProfileSelected();
		this.routerHandler.setRouterProfile(this.routerProfile);
		this.routerHandler.getControlHandler().doReset();

		// /ROUTER HANDLER

		if (this.routerProfile == null) {
			SettingsDialogFragment.showMessage("No profile selected", this);
		} else {
			// Actionbar
			RouterPreferencesHandler.Router router = this.routerHandler.getPreferenceHandler().getRouter(
					this.routerProfile.getRouterId());
			if (router != null)
				getSupportActionBar().setSubtitle(router.name);

			// Refresh router handler
			// this.routerHandler.doRefresh();
			this.routerHandler.getControlHandler().doLogin();
		}

		// CONTENT

		this.menuFragment.doUpdateMenuList();
		Class<? extends Fragment> firstFragment = this.menuFragment.getFirstFragment();
		if (firstFragment != null)
			doSwitchContent(firstFragment);
		else
			Log.w(TAG, "Could not find a first fragment from menu");

		// /CONTENT
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause");

		// ROUTER HANDLER

		this.routerHandler.removeListener(RouterAdminActivity.class.getSimpleName());

		// /ROUTER HANDLER
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menuRefresh = menu.add(0, MENU_REFRESH, 1, "Refresh");
		menuRefresh.setIcon(R.drawable.navigation_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menuSettings = menu.add(0, MENU_SETTINGS, 0, "Settings");
		menuSettings.setIcon(R.drawable.action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			doStartSettings();
			return true;
		case MENU_REFRESH:
			this.routerHandler.doRefresh();
			return true;
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "On Activity Result: " + requestCode + ", " + resultCode);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mContent != null)
			getSupportFragmentManager().putFragment(outState, CONTENT_SAVE, mContent);
	}

	// ... ... ROUTER HANDLER

	@Override
	public void onRefresh() {
		Log.d(TAG, "On refresh");
		// routerHandler.doUpdateLogin();
		// this.routerHandler.getControlHandler().doLogin();
	}

	@Override
	public void onUpdating() {
		Log.d(TAG, "On updating");
		setSupportProgressBarIndeterminateVisibility(true);
		if (menuRefresh != null)
			menuRefresh.setEnabled(false);
	}

	@Override
	public void onUpdated() {
		Log.d(TAG, "On updated");
		setSupportProgressBarIndeterminateVisibility(false);
		if (menuRefresh != null)
			menuRefresh.setEnabled(true);
	}

	@Override
	public void onError(Exception exception) {
		onError(exception, null);
	}

	@Override
	public void onError(Exception exception, ControlHandleResult<?> controlHandleResult) {
		Log.w(TAG, "OnError: " + exception.getClass().getSimpleName() + ", " + exception.getMessage());

		// Error settings
		if (exception instanceof ErrorSettingsException) {
			SettingsDialogFragment.showMessage(exception.getMessage(), this);
		}
		// Invalid page
		if (exception instanceof InvalidPageException) {
			ErrorDialogFragment.showMessage("Invalid page retrieved", this);
		}
		// Not logged in
		else if (exception instanceof NotLoggedinException) {
			Log.w(TAG, "Not logged in");
			// ErrorDialogFragment.showMessage("Not logged in", this);
			this.routerHandler.getControlHandler().handlingQueue = null;
			this.routerHandler.getControlHandler().doLogin();

			if (this.routerHandler.getControlHandler().notLoggedInCount < 2) {
				if (controlHandleResult != null)
					controlHandleResult.doResubHandle();
			} else
				SettingsDialogFragment.showMessage("Could not login correcly", this);
		}
		// Error
		else {
			ErrorDialogFragment.showMessage("Something wrong happened", this);
		}
	}

	// ... ... /ROUTER HANDLER

	// ... /ON

	// ... DO

	public void doSwitchContent(Class<? extends Fragment> fragmentClass) {
		try {
			doSwitchContent(fragmentClass.newInstance());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void doSwitchContent(Fragment fragment) {
		Log.d(TAG, "DoSwitchContent: " + fragment.getClass().getSimpleName());
		mContent = fragment;
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

	private void doStartSettings() {
		Log.d(TAG, "Do start settings");

		Intent settingsIntent = new Intent(this, RouterPreferencesActivity.class);
		startActivityForResult(settingsIntent, 0);
	}

	// ... /DO

	// ... GET

	public RouterHandler getRouterHandler() {
		return routerHandler;
	}

	// ... /GET

	// ... DIALOG

	public static class SettingsDialogFragment extends DialogFragment {

		private static final String ARG_MESSAGE = "message";

		public static void showMessage(String message, FragmentActivity fragmentActivity) {
			SettingsDialogFragment settingsErrorDialogFragment = new SettingsDialogFragment();

			Bundle args = new Bundle();
			args.putString(ARG_MESSAGE, message);
			settingsErrorDialogFragment.setArguments(args);

			Log.d(TAG, "Settings Dialog: " + message);
			FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
			settingsErrorDialogFragment.show(supportFragmentManager, "settings_dialog");
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String message = getArguments().getString(ARG_MESSAGE);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setIcon(android.R.drawable.alert_light_frame);
			builder.setIcon(android.R.drawable.alert_light_frame).setCancelable(false).setMessage(message)
					.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							((RouterAdminActivity) getActivity()).getRouterHandler().getControlHandler().doReset();
							((RouterAdminActivity) getActivity()).doStartSettings();
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							((RouterAdminActivity) getActivity()).getRouterHandler().getControlHandler().doReset();
						}
					});
			AlertDialog dialog = builder.create();
			dialog.setIcon(android.R.drawable.alert_light_frame);
			return dialog;
		}
	}

	public static class ErrorDialogFragment extends DialogFragment {

		private static final String ARG_MESSAGE = "message";

		public static void showMessage(String message, FragmentActivity fragmentActivity) {
			SettingsDialogFragment settingsErrorDialogFragment = new SettingsDialogFragment();

			Bundle args = new Bundle();
			args.putString(ARG_MESSAGE, message);
			settingsErrorDialogFragment.setArguments(args);

			Log.d(TAG, "Error Dialog: " + message);
			FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
			settingsErrorDialogFragment.show(supportFragmentManager, "error_dialog");
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String message = getArguments().getString(ARG_MESSAGE);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setIcon(android.R.drawable.alert_light_frame);
			builder.setMessage(message).setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
				}
			}).setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					((RouterAdminActivity) getActivity()).getRouterHandler().getControlHandler().doReset();
				}
			});
			AlertDialog dialog = builder.create();
			return dialog;
		}
	}

	// ... /DIALOG

}
