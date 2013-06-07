package com.skarbo.routeradmin.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.skarbo.routeradmin.R;
import com.skarbo.routeradmin.RouterAdminActivity;
import com.skarbo.routeradmin.container.AccessControlContainer;
import com.skarbo.routeradmin.handler.RouterHandler;
import com.skarbo.routeradmin.listener.RouterHandlerListener;

public class AccessControlAdvancedFragment extends Fragment implements RouterHandlerListener {

	private static final String TAG = AccessControlAdvancedFragment.class.getSimpleName();

	private RouterHandler routerHandler;
	private PoliciesAdapter policiesAdapter;

	private ListView policiesListView;
	private ToggleButton accessControlEnableToggleButton;

	// ... ON

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "On activity created");
		routerHandler = ((RouterAdminActivity) getActivity()).getRouterHandler();

		policiesAdapter = new PoliciesAdapter(getActivity(), R.layout.fragment_access_control_policy_row,
				new ArrayList<AccessControlContainer.Policy>());

		if (policiesListView != null)
			policiesListView.setAdapter(policiesAdapter);
		doUpdateView();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "On resume");
		if (routerHandler != null) {
			routerHandler.addListener(AccessControlAdvancedFragment.class.getSimpleName(), this);
			this.routerHandler.doRefresh();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "On pause");
		if (routerHandler != null)
			routerHandler.removeListener(AccessControlAdvancedFragment.class.getSimpleName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_access_control, container, false);

		accessControlEnableToggleButton = (ToggleButton) view.findViewById(R.id.access_control_enable_toggle_button);
		policiesListView = (ListView) view.findViewById(R.id.access_control_policy_list);

		accessControlEnableToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (buttonView.isPressed()) {
					Log.d(TAG, "Access control toggle button: " + isChecked + ", " + buttonView.isPressed());
					routerHandler.getControlHandler().doAdvancedAccessControl(isChecked);
					Toast.makeText(getActivity(), String.format("%s Access Control", isChecked ? "Enabling" : "Disabling"), Toast.LENGTH_SHORT).show();
				}
			}
		});
		policiesListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				TextView machineTextView = (TextView) view.findViewById(R.id.access_control_policy_machine_text_view);
				if (machineTextView != null) {
					machineTextView.setMaxLines(10);
				}
				policiesListView.setItemChecked(position, true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		return view;
	}

	// ... ... ROUTER HANDLER

	@Override
	public void onRefresh() {
		Log.d(TAG, "OnRefresh");
		if (routerHandler != null)
			routerHandler.getControlHandler().doAdvancedAccessControl();
	}

	@Override
	public void onUpdating() {
		Log.d(TAG, "OnUpdating");
		doUpdateView();
	}

	@Override
	public void onUpdated() {
		Log.d(TAG, "OnUpdated");
		doUpdateView();
	}

	// ... ... /ROUTER HANDLER

	// ... /ON

	private void doUpdateView() {
		boolean isUpdating = this.routerHandler.getControlHandler().isQueueHandling();
		AccessControlContainer accessControlContainer = this.routerHandler.getControlHandler().getContainers()
				.getAccessControlContainer();
		policiesAdapter.enabled = accessControlContainer.enabled && !isUpdating;

		accessControlEnableToggleButton.setEnabled(!isUpdating);
		accessControlEnableToggleButton.setChecked(accessControlContainer.enabled);

		policiesAdapter.clear();
		for (AccessControlContainer.Policy policy : accessControlContainer.getPolicies()) {
			policiesAdapter.add(policy);
		}

		policiesAdapter.notifyDataSetChanged();
	}

	// ... CLASS

	
	public class PoliciesAdapter extends ArrayAdapter<AccessControlContainer.Policy> {

		private List<AccessControlContainer.Policy> policies;
		public boolean enabled = false;

		public PoliciesAdapter(Context context, int textViewResourceId, List<AccessControlContainer.Policy> policies) {
			super(context, textViewResourceId, policies);
			this.policies = policies;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				view = layoutInflater.inflate(R.layout.fragment_access_control_policy_row, null);
			}

			final AccessControlContainer.Policy policy = getItem(position);
			if (policy != null) {
				ImageButton enableToggleButton = (ImageButton) view
						.findViewById(R.id.access_control_policy_enable_button);
				TextView policyTextView = (TextView) view.findViewById(R.id.access_control_policy_text_view);
				TextView machineTextView = (TextView) view.findViewById(R.id.access_control_policy_machine_text_view);

				enableToggleButton.setImageResource(policy.enable ? R.drawable.access_secure
						: R.drawable.access_not_secure);
				enableToggleButton.setEnabled(enabled);
				policyTextView.setText(policy.policy);
				machineTextView.setText(policy.machine);

				enableToggleButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "Policy toggle button: " + policy.id + ", " + !policy.enable);
						// routerHandler.doUpdateAccessControlContainer.Policy(policy.id,
						// !policy.enable);
						routerHandler.getControlHandler().doAdvancedAccessControl(policy.id, !policy.enable);
						Toast.makeText(getActivity(),
								String.format("%s Policy", policy.enable ? "Disabling" : "Enabling"),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			return view;
		}

		public void addPolicy(AccessControlContainer.Policy policy) {
			policies.add(policy);
		}

	}

	// ... /CLASS

}
