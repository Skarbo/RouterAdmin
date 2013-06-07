package com.skarbo.routeradmin;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler.Router;
import com.skarbo.routeradmin.model.RouterProfile;

public class RouterPreferencesActivity extends SherlockFragmentActivity implements OnItemClickListener {

	protected static final String TAG = RouterPreferencesActivity.class.getSimpleName();

	private static final int MENU_REMOVE = 1;
	private static final int MENU_EDIT = 2;
	private static final int MENU_NEW = 3;

	private TextView emptyProfilesTextView;
	private ListView profilesListView;
	private String profileIdSelected;

	private MenuItem menuRemove;
	private MenuItem menuEdit;
	private MenuItem menuNew;

	private RouterPreferencesHandler routerPreferencesHandler;
	private ProfileAdapter profileAdapter;

	// ... ON

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "On Create");
		setContentView(R.layout.activity_router_preferences);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		routerPreferencesHandler = new RouterPreferencesHandler(this);

		emptyProfilesTextView = (TextView) findViewById(R.id.emptyProfilesTextView);
		profilesListView = (ListView) findViewById(R.id.profilesListView);

		profilesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		profileAdapter = new ProfileAdapter(this, new ArrayList<RouterProfile>());
		profilesListView.setAdapter(profileAdapter);
		profilesListView.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "On resume");

		RouterProfile routerProfile = this.routerPreferencesHandler.getRouterProfileSelected();
		if (routerProfile != null)
			this.profileIdSelected = routerProfile.getId();
		doProfilesUpdate();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "On activity result: " + requestCode + ", " + resultCode);

		if (resultCode == Activity.RESULT_OK) {
			String profileId = data.getStringExtra(ProfileRouterPreferencesActivity.EXTRA_RESULT_PROFILE_ID);

			if (!profileId.equalsIgnoreCase("")) {
				this.doProfileSet(profileId);
			}

			Toast.makeText(this, "Saved profile", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Saving profile canceled", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menuRemove = menu.add(0, MENU_REMOVE, 1, "Remove");
		menuRemove.setIcon(R.drawable.remove).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuEdit = menu.add(0, MENU_EDIT, 1, "Edit");
		menuEdit.setIcon(R.drawable.edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menuNew = menu.add(0, MENU_NEW, 2, "New");
		menuNew.setIcon(R.drawable.new_).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menuRemove.setEnabled(this.profileIdSelected != null);
		menuEdit.setEnabled(this.profileIdSelected != null);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REMOVE:
			doProfileRemove(false);
			return true;
		case MENU_EDIT:
			doProfilePreferences(true);
			return true;
		case MENU_NEW:
			doProfilePreferences(false);
			return true;
		case android.R.id.home:
			setResult(Activity.RESULT_OK);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		RouterProfile routerProfile = profileAdapter.getItem(arg2);
		if (routerProfile != null) {
			Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
			doProfileSet(routerProfile.getId());
		}
	}

	// ... /ON

	// ... DO

	public void doProfilesUpdate() {
		Log.d(TAG, "Do profile update");
		this.profileAdapter.clear();
		for (RouterProfile routerProfile : this.routerPreferencesHandler.getRouterProfiles()) {
			Log.d(TAG, "Adding Profile to adapter: " + routerProfile);
			this.profileAdapter.add(routerProfile);
		}

		if (!profileAdapter.isEmpty())
			this.emptyProfilesTextView.setVisibility(View.GONE);

		this.profileAdapter.notifyDataSetChanged();
	}

	public void doProfileSet(String profileId) {
		Log.d(TAG, "Do profile set: " + profileId);
		this.routerPreferencesHandler.setRouterProfileSelected(profileId);
		this.profileIdSelected = profileId;
		doProfilesUpdate();
	}

	private void doProfilePreferences(boolean isEdit) {
		Intent profilePreferenceIntent = new Intent(this, ProfileRouterPreferencesActivity.class);
		Log.d(TAG, "Do profile preference, Is edit: " + isEdit);
		if (isEdit) {
			if (this.profileIdSelected == null || this.profileIdSelected.equalsIgnoreCase("")) {
				Toast.makeText(this, "Profile is not selected", Toast.LENGTH_SHORT).show();
				return;
			}

			Bundle bundle = new Bundle();
			bundle.putString(ProfileRouterPreferencesActivity.EXTRA_TYPE_EDIT, this.profileIdSelected);

			profilePreferenceIntent.putExtras(bundle);
		}

		startActivityForResult(profilePreferenceIntent, 0);
	}

	protected void doProfileRemove(boolean isRemove) {
		if (this.profileIdSelected == null) {
			Toast.makeText(this, "No profile selected", Toast.LENGTH_SHORT);
			return;
		}

		Log.d(TAG, "Do profile remove: " + isRemove);

		if (isRemove) {
			routerPreferencesHandler.deleteRouterProfile(this.profileIdSelected);

			this.doProfilesUpdate();
			List<String> routerProfileIds = routerPreferencesHandler.getRouterProfileIds();

			if (!routerProfileIds.isEmpty()) {
				this.profileIdSelected = routerProfileIds.get(0);
				this.routerPreferencesHandler.setRouterProfileSelected(this.profileIdSelected);
				Log.d(TAG, "Set first profile as selected: " + this.profileIdSelected);
			}
			this.profileAdapter.notifyDataSetChanged();
		} else {
			DialogFragment newFragment = RemoveProfileDialogFragment.newInstance("Remove profile?");
			newFragment.show(getSupportFragmentManager(), "dialog");
		}
	}

	// ... /DO

	// ... GET

	public RouterPreferencesHandler getRouterPreferencesHandler() {
		return routerPreferencesHandler;
	}

	public ProfileAdapter getProfileAdapter() {
		return profileAdapter;
	}

	// ... ADAPTER

	public class ProfileAdapter extends ArrayAdapter<RouterProfile> {

		private LayoutInflater layoutInfalter;
		private List<RouterProfile> profiles;

		public ProfileAdapter(Context context, List<RouterProfile> profiles) {
			super(context, R.layout.list_item_router_profile, profiles);
			this.profiles = profiles;
			layoutInfalter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void doSelectProfile(int position) {
			RouterProfile routerProfile = profileAdapter.getItem(position);
			Log.d(TAG, "Do select profile: " + position + ", " + routerProfile);
			if (routerProfile != null) {
				// profilesListView.setItemChecked(position, true);
				if (menuRemove != null)
					menuRemove.setEnabled(true);
				if (menuEdit != null)
					menuEdit.setEnabled(true);
			} else
				Log.e(TAG, "Selected Profile is null: " + position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			final RouterProfile profile = getItem(position);

			if (view == null) {
				view = layoutInfalter.inflate(R.layout.list_item_router_profile, null);
			}

			if (profile != null) {
				TextView routerTextView = (TextView) view.findViewById(R.id.routerTextView);
				TextView ipTextView = (TextView) view.findViewById(R.id.ipTextView);
				TextView userTextView = (TextView) view.findViewById(R.id.userTextView);

				RouterPreferencesHandler.Router router = routerPreferencesHandler.getRouter(profile.getRouterId());
				if (router != null)
					routerTextView.setText(router.name);
				ipTextView.setText(profile.getIp());
				userTextView.setText(profile.getUser());

				if (profile.getId().equalsIgnoreCase(profileIdSelected)) {
					view.setBackgroundResource(R.color.item_highlight);
				} else {
					view.setBackgroundResource(android.R.color.white);
				}
			}

			return view;
		}

	}

	// ... /ADAPTER

	// ... DIALOG

	public static class RemoveProfileDialogFragment extends DialogFragment {

		public static RemoveProfileDialogFragment newInstance(String title) {
			RemoveProfileDialogFragment frag = new RemoveProfileDialogFragment();
			Bundle args = new Bundle();
			args.putString("title", title);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String title = getArguments().getString("title");

			return new AlertDialog.Builder(getActivity()).setTitle(title)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							((RouterPreferencesActivity) getActivity()).doProfileRemove(true);
						}
					}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					}).create();
		}
	}

	// ... /DIALOG

}
