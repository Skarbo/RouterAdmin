package com.skarbo.routeradmin.fragment;

import java.util.Map;
import java.util.Map.Entry;

import com.skarbo.routeradmin.R;
import com.skarbo.routeradmin.RouterAdminActivity;
import com.skarbo.routeradmin.container.InfoStatusContainer;
import com.skarbo.routeradmin.handler.RouterHandler;
import com.skarbo.routeradmin.listener.InfoStatusListener;
import com.skarbo.routeradmin.listener.RouterHandlerListener;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class InfoStatusFragment extends Fragment implements RouterHandlerListener, InfoStatusListener {

	private static final String TAG = InfoStatusFragment.class.getSimpleName();

	private RouterHandler routerHandler;
	private LayoutInflater inflater;
	private TableLayout tableLayout;

	// ... ON

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		routerHandler = ((RouterAdminActivity) getActivity()).getRouterHandler();

		inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		doUpdateView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_status_info, container, false);

		tableLayout = (TableLayout) view.findViewById(R.id.statusInfoTableLayout);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "OnResume");

		if (this.routerHandler != null) {
			this.routerHandler.addListener(TAG, this);
			this.routerHandler.doRefresh();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause");

		if (this.routerHandler != null)
			this.routerHandler.removeListener(TAG);
	}

	@Override
	public void onInfoUpdated(InfoStatusContainer infoStatusContainer) {
		doUpdateView();
	}

	// ... /ON

	// ... ON

	@Override
	public void onUpdating() {

	}

	@Override
	public void onUpdated() {
		doUpdateView();
	}

	@Override
	public void onRefresh() {
		Log.d(TAG, "OnRefresh");
		if (this.routerHandler != null)
			this.routerHandler.getControlHandler().doStatusInfo();
	}

	// ... /ON

	// ... DO

	private void doUpdateView() {
		tableLayout.removeAllViews();

		InfoStatusContainer infoStatusContainer = routerHandler.getControlHandler().getContainers()
				.getInfoStatusContainer();

		for (Entry<String, Map<String, String>> entryContainer : infoStatusContainer.containers.entrySet()) {
			doCreateTableRowHeader(entryContainer.getKey());

			for (Entry<String, String> entryContainerMap : entryContainer.getValue().entrySet()) {
				doCreateTableRowFieldValue(entryContainerMap.getKey(), entryContainerMap.getValue());
			}
		}

		// // General
		// doCreateTableRowHeader("General");
		// doCreateTableRowFieldValue("Time", infoStatusContainer.general.time);
		// doCreateTableRowFieldValue("Firmware",
		// infoStatusContainer.general.firmware);
		//
		// // WAN
		// doCreateTableRowHeader("WAN");
		// doCreateTableRowFieldValue("Connection Type",
		// infoStatusContainer.wan.connectionType);
		// doCreateTableRowFieldValue("Cable Status",
		// infoStatusContainer.wan.cableStatus);
		// doCreateTableRowFieldValue("Network Status",
		// infoStatusContainer.wan.networkStatus);
		// doCreateTableRowFieldValue("MAC Address",
		// infoStatusContainer.wan.macAddress);
		// doCreateTableRowFieldValue("IP Address",
		// infoStatusContainer.wan.ipAddress);
		// doCreateTableRowFieldValue("Subnet Mask",
		// infoStatusContainer.wan.subnetMask);
		// doCreateTableRowFieldValue("Default Gateway",
		// infoStatusContainer.wan.defaultGateway);
		// doCreateTableRowFieldValue("Primary DNS Server",
		// infoStatusContainer.wan.dnsPrimaryServer);
		// doCreateTableRowFieldValue("Secondary DNS Server",
		// infoStatusContainer.wan.dnsSecondaryServer);
		//
		// // LAN
		// doCreateTableRowHeader("LAN");
		// doCreateTableRowFieldValue("MAC Address",
		// infoStatusContainer.lan.macAddress);
		// doCreateTableRowFieldValue("IP Address",
		// infoStatusContainer.lan.ipAddress);
		// doCreateTableRowFieldValue("Subnet Mask",
		// infoStatusContainer.lan.subnetMask);
		// doCreateTableRowFieldValue("DHCP Server",
		// infoStatusContainer.lan.dhcpServer);
		//
		// // WLAN
		// doCreateTableRowHeader("WLAN");
		// doCreateTableRowFieldValue("Wireless Radio",
		// infoStatusContainer.wlan.radioEnabled);
		// doCreateTableRowFieldValue("MAC Address",
		// infoStatusContainer.wlan.macAddress);
		// doCreateTableRowFieldValue("Network Name",
		// infoStatusContainer.wlan.ssid);
		// doCreateTableRowFieldValue("Security",
		// infoStatusContainer.wlan.security);
	}

	private void doCreateTableRowFieldValue(String field, String value) {
		TableRow tableRow = (TableRow) inflater.inflate(R.layout.table_row_status_info_fieldvalue, tableLayout, false);
		tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		// Field
		TextView profileInfoFieldTextView = (TextView) tableRow.findViewById(R.id.statusInfoFieldTextView);
		profileInfoFieldTextView.setText(field);

		// Value
		TextView profileInfoValueTextView = (TextView) tableRow.findViewById(R.id.statusInfoValueTextView);
		profileInfoValueTextView.setText(value);

		// Add TableRow to View
		tableLayout.addView(tableRow,
				new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	private void doCreateTableRowHeader(String header) {
		TableRow tableRow = (TableRow) inflater.inflate(R.layout.table_row_status_info_header, tableLayout, false);
		tableRow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		TextView profileInfoFieldTextView = (TextView) tableRow.findViewById(R.id.statusInfoHeaderTextView);
		profileInfoFieldTextView.setText(header);

		tableLayout.addView(tableRow,
				new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	// ... /DO

}
