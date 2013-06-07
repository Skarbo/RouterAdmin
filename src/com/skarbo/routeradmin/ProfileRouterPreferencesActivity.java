package com.skarbo.routeradmin;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler.RouterBrand;
import com.skarbo.routeradmin.model.RouterProfile;
import com.skarbo.routeradmin.utils.Utils;

public class ProfileRouterPreferencesActivity extends SherlockActivity {
	public static final String TAG = ProfileRouterPreferencesActivity.class.toString();

	public static final String EXTRA_TYPE_EDIT = "edit";
	public static final String EXTRA_RESULT_PROFILE_ID = "profile_id";

	public static final String PREF_ROUTER_PASSWORD_SHOW = "router_password_show";

	private static final int MENU_ACCEPT = 1;

	private RouterTypeListAdapter routerTypeListAdapter;
	private RouterPreferencesHandler routerPreferencesHandler;
	private String profileId;
	private boolean isInit = false;

	private EditText routerIpEditText;
	private ImageButton routerIpAutofillImageButton;
	private EditText routerUserEditText;
	private EditText routerPasswordEditText;
	private CheckBox routerPasswordShowCheckBox;
	private ExpandableListView routerTypeListView;

	public Intent resultData;
	public int resultCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_router_preferences_profile);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		routerPreferencesHandler = new RouterPreferencesHandler(this);
		boolean isRouterPasswordShow = savedInstanceState != null ? savedInstanceState.getBoolean(
				PREF_ROUTER_PASSWORD_SHOW, false) : false;

		// VIEW

		routerIpEditText = (EditText) findViewById(R.id.routerIpEditText);
		routerIpAutofillImageButton = (ImageButton) findViewById(R.id.routerIpAutofillImageButton);
		routerUserEditText = (EditText) findViewById(R.id.routerUserEditText);
		routerPasswordEditText = (EditText) findViewById(R.id.routerPasswordEditText);
		routerPasswordShowCheckBox = (CheckBox) findViewById(R.id.routerPasswordShowCheckBox);
		routerTypeListView = (ExpandableListView) findViewById(R.id.routerTypeExpandableListView);

		routerIpAutofillImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				doAutofillRouterIp();
			}
		});

		routerPasswordShowCheckBox.setChecked(isRouterPasswordShow);
		routerPasswordShowCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				doShowRouterPassword(isChecked);
			}
		});

		routerTypeListAdapter = new RouterTypeListAdapter(this, this.routerPreferencesHandler.getRouters());
		routerTypeListView.setAdapter(routerTypeListAdapter);

		routerTypeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		routerTypeListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				routerTypeListAdapter.doSelect(groupPosition, childPosition);
				return true;
			}
		});

		// /VIEW

		doShowRouterPassword(isRouterPasswordShow);
		doInit();
	}

	protected void doAutofillRouterIp() {
		WifiManager wifii = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		DhcpInfo d = wifii.getDhcpInfo();
		String defaultGateway = Utils.intToIp(d.gateway);
		Log.d(TAG, "Do auto fill router ip: " + defaultGateway);
		routerIpEditText.setText(defaultGateway);
	}

	@Override
	protected void onResume() {
		super.onResume();
		doInit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (outState != null) {
			outState.putBoolean(PREF_ROUTER_PASSWORD_SHOW, routerPasswordShowCheckBox.isChecked());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ACCEPT, 0, "Save").setIcon(R.drawable.accept).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ACCEPT:
			doSaveProfile();
			return true;
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public RouterPreferencesHandler getRouterPreferencesHandler() {
		return routerPreferencesHandler;
	}

	public RouterTypeListAdapter getRouterTypeListAdapter() {
		return routerTypeListAdapter;
	}

	private void doInit() {
		if (isInit)
			return;
		isInit = true;
		Log.d(TAG, "Do init, Is Edit: " + getIntent().hasExtra(EXTRA_TYPE_EDIT));
		// Edit profile
		if (getIntent().hasExtra(EXTRA_TYPE_EDIT)) {
			profileId = getIntent().getStringExtra(EXTRA_TYPE_EDIT);
			RouterProfile routerProfile = this.routerPreferencesHandler.getRouterProfile(profileId);
			RouterPreferencesHandler.Router router = this.routerPreferencesHandler.getRouter(routerProfile
					.getRouterId());

			routerIpEditText.setText(routerProfile.getIp());
			routerUserEditText.setText(routerProfile.getUser());
			routerPasswordEditText.setText(routerProfile.getPassword());

			setTitle("Edit Profile");
			getSupportActionBar().setTitle("Edit Profile");

			if (router != null)
				getSupportActionBar().setSubtitle("" + router.name);

			getRouterTypeListAdapter().doSelect(routerProfile.getRouterId());
		}
		// New profile
		else {
			setTitle("New Profile");
		}
	}

	public void doSaveProfile() {
		// New profile
		if (this.profileId == null) {
			String profileIdTemp = String.valueOf((new Date()).getTime());
			Log.d(TAG, "Creating profile id: " + profileIdTemp);

			if (!doSaveProfilePreference(profileIdTemp))
				return;

			this.profileId = profileIdTemp;
		}
		// Edit profile
		else {
			Log.d(TAG, "Saving edited profile");
			if (!doSaveProfilePreference(this.profileId))
				return;
		}

		this.resultData = new Intent();
		this.resultCode = Activity.RESULT_OK;
		this.resultData.putExtra(EXTRA_RESULT_PROFILE_ID, profileId);
		setResult(this.resultCode, this.resultData);
		finish();
	}

	private boolean doSaveProfilePreference(String profileId) {
		String routerIp = routerIpEditText.getText().toString();
		String routerUser = routerUserEditText.getText().toString();
		String routerPassword = routerPasswordEditText.getText().toString();
		String routerId = routerTypeListAdapter.getSelectedRouterId();

		RouterProfile routerProfile = new RouterProfile(profileId, routerIp, routerUser, routerPassword, routerId);

		if (routerProfile.getIp().equalsIgnoreCase("")) {
			Toast.makeText(this, "Router IP is not set", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (routerProfile.getRouterId() == null) {
			Toast.makeText(this, "Router Type is not set", Toast.LENGTH_SHORT).show();
			return false;
		}

		this.routerPreferencesHandler.saveRouterProfile(profileId, routerProfile);
		return true;
	}

	private void doShowRouterPassword(boolean isShow) {
		routerPasswordEditText.setInputType(isShow ? InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		Log.d(TAG, "Do show router password: " + isShow + ", " + routerPasswordEditText.getInputType());
	}

	// CLASS

	public class RouterTypeListAdapter extends BaseExpandableListAdapter {

		private Context context;
		private RouterBrand[] routerBrands;
		private LayoutInflater layoutInflater;

		public int groupSelected = -1;
		public int childSelected = -1;

		public RouterTypeListAdapter(Context context, RouterBrand[] routerBrands) {
			this.context = context;
			this.routerBrands = routerBrands;
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public String getSelectedRouterId() {
			Log.d(TAG, "Get selected router id: " + groupSelected + " , " + childSelected);
			if (groupSelected > -1 && childSelected > -1) {
				RouterPreferencesHandler.Router router = (RouterPreferencesHandler.Router) getChild(groupSelected,
						childSelected);
				if (router != null)
					return router.id;
			}
			return null;
		}

		public void doSelect(int groupSelected, int childSelected) {
			this.groupSelected = groupSelected;
			this.childSelected = childSelected;
			if (groupSelected > -1)
				routerTypeListView.expandGroup(groupSelected);
			Log.d(TAG, "Do select router: " + childSelected + ", " + groupSelected);
			notifyDataSetChanged();
		}

		public void doSelect(String routerId) {
			try {
				int groupPosition = -1;
				int childPosition = -1;
				for (int i = 0; i < this.routerBrands.length && groupPosition == -1; i++) {
					for (int j = 0; j < this.routerBrands[i].devices.length && childPosition == -1; j++) {
						if (this.routerBrands[i].devices[j].id.equalsIgnoreCase(routerId)) {
							groupPosition = i;
							childPosition = j;
						}
					}
				}
				doSelect(groupPosition, childPosition);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}

		@Override
		public Object getChild(int arg0, int arg1) {
			try {
				return ((RouterPreferencesHandler.RouterBrand) getGroup(arg0)).devices[arg1];
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		public Object getGroup(int arg0) {
			try {
				return this.routerBrands[arg0];
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		public long getGroupId(int arg0) {
			return arg0;
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			return arg1;
		}

		@Override
		public int getChildrenCount(int arg0) {
			try {
				return ((RouterPreferencesHandler.RouterBrand) getGroup(arg0)).devices.length;
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		public int getGroupCount() {
			return this.routerBrands.length;
		}

		@Override
		public View getChildView(int arg0, int arg1, boolean arg2, View arg3, ViewGroup arg4) {
			View view;
			if (arg3 == null) {
				view = layoutInflater.inflate(R.layout.list_expandable_router_item, null);
			} else {
				view = (View) arg3;
			}

			RouterPreferencesHandler.Router child = (RouterPreferencesHandler.Router) getChild(arg0, arg1);
			if (child != null)
				((TextView) view.findViewById(R.id.routerItem)).setText(child.name);

			if (groupSelected == arg0 && childSelected == arg1)
				view.setBackgroundResource(R.color.item_highlight);
			else
				view.setBackgroundResource(android.R.color.white);

			return view;
		}

		@Override
		public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {
			View view;
			if (arg2 == null) {
				view = layoutInflater.inflate(R.layout.list_expandable_router_group, null);

				RouterPreferencesHandler.RouterBrand group = (RouterPreferencesHandler.RouterBrand) getGroup(arg0);
				if (group != null)
					((TextView) view.findViewById(R.id.routerGroup)).setText(group.brand);
			} else {
				view = (View) arg2;
			}

			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

	}

	// /CLASS

}
