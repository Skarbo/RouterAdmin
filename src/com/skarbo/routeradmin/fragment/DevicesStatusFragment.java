package com.skarbo.routeradmin.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.skarbo.routeradmin.R;
import com.skarbo.routeradmin.RouterAdminActivity;
import com.skarbo.routeradmin.container.DevicesContainer;
import com.skarbo.routeradmin.handler.RouterHandler;
import com.skarbo.routeradmin.listener.DevicesStatusListener;
import com.skarbo.routeradmin.listener.RouterHandlerListener;

public class DevicesStatusFragment extends Fragment implements RouterHandlerListener, DevicesStatusListener {

	// ... ON

	private static final String TAG = DevicesStatusFragment.class.getSimpleName();

	private RouterHandler routerHandler;
	private DevicesAdapter devicesAdapter;

	private TextView noDevicesTextView;
	private ListView devicesListView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "On activity created");

		this.routerHandler = ((RouterAdminActivity) getActivity()).getRouterHandler();

		devicesAdapter = new DevicesAdapter(getActivity());
		if (devicesListView != null)
			devicesListView.setAdapter(devicesAdapter);

		doUpdateView();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "OnResume");
		if (this.routerHandler != null) {
			this.routerHandler.addListener(TAG, this);
			this.routerHandler.doRefresh();
		}

		doUpdateView();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause");
		if (this.routerHandler != null)
			this.routerHandler.removeListener(TAG);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_status_devices, container, false);

		noDevicesTextView = (TextView) view.findViewById(R.id.devices_none_status_devices_text_view);
		devicesListView = (ListView) view.findViewById(R.id.devices_status_devices_list);

		return view;
	}

	// ... ... ROUTER HANDLER

	@Override
	public void onUpdating() {
		doUpdateView();
	}

	@Override
	public void onUpdated() {
		doUpdateView();
	}

	@Override
	public void onRefresh() {
		Log.d(TAG, "OnRefresh");
		if (this.routerHandler != null)
			this.routerHandler.getControlHandler().doStatusDevices();
	}

	@Override
	public void onDevicesUpdated(DevicesContainer devicesContainer) {
		Log.d(TAG, "OnDevicesUpdated");
		doUpdateView();
	}

	// ... ... /ROUTER HANDLER

	// ... ON

	// ... DO

	private void doUpdateView() {
		Log.d(TAG, "DoUpdateView");

		DevicesContainer devicesContainer = this.routerHandler.getControlHandler().getContainers()
				.getDevicesContainer();

		devicesAdapter.clear();
		for (DevicesContainer.Device device : devicesContainer.getDevices()) {
			devicesAdapter.add(device);
		}
		devicesAdapter.notifyDataSetChanged();

		if (devicesContainer.getDevices().isEmpty()) {
			devicesListView.setVisibility(View.GONE);
			noDevicesTextView.setVisibility(View.VISIBLE);
		} else {
			devicesListView.setVisibility(View.VISIBLE);
			noDevicesTextView.setVisibility(View.GONE);
		}

	}

	// ... /DO

	// ADAPTER

	public static class DevicesAdapter extends ArrayAdapter<DevicesContainer.Device> {

		private static final int LIST_ITEM_STATUS_DEVICE = R.layout.list_item_status_device;
		private LayoutInflater layoutInflater;

		public DevicesAdapter(Context context) {
			super(context, LIST_ITEM_STATUS_DEVICE, new ArrayList<DevicesContainer.Device>());
			layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = layoutInflater.inflate(LIST_ITEM_STATUS_DEVICE, null);

				viewHolder = new ViewHolder();
				viewHolder.deviceImageView = (ImageView) convertView.findViewById(R.id.deviceDeviceImageView);
				viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
				viewHolder.ipTextView = (TextView) convertView.findViewById(R.id.deviceIpAddressTextView);
				viewHolder.macTextView = (TextView) convertView.findViewById(R.id.deviceMacTextView);
				viewHolder.interfaceImageView = (ImageView) convertView.findViewById(R.id.deviceInterfaceImageView);

				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();

			final DevicesContainer.Device device = getItem(position);
			if (device != null) {

				// Device type
				if (device.type != null) {
					viewHolder.deviceImageView.setVisibility(View.VISIBLE);
					switch (device.type) {
					case Pc:
						viewHolder.deviceImageView.setImageResource(R.drawable.pc);
						break;

					case Laptop:
						viewHolder.deviceImageView.setImageResource(R.drawable.laptop);
						break;

					case Mobile:
						viewHolder.deviceImageView.setImageResource(R.drawable.mobile);
						break;

					case Tablet:
						viewHolder.deviceImageView.setImageResource(R.drawable.tablet);
						break;
					}
				}

				// Name
				viewHolder.nameTextView.setText(device.name);

				// Ip address
				viewHolder.ipTextView.setText(device.ipAddress);

				// Mac
				viewHolder.macTextView.setText(device.macAddress);

				// Interface
				if (device.intrface != null) {
					viewHolder.interfaceImageView.setVisibility(View.VISIBLE);
					switch (device.intrface) {
					case Wifi:
						viewHolder.interfaceImageView.setImageResource(R.drawable.wifi);
						break;

					case Ethernet:
						viewHolder.interfaceImageView.setImageResource(R.drawable.ethernet);
						break;
					}
				}

				// Alpha
				int alpha = 255;
				if (device.inactive) {
					alpha = 150;

					viewHolder.nameTextView.setTextColor(getContext().getResources().getColor(
							android.R.color.darker_gray));
					viewHolder.ipTextView.setTextColor(getContext().getResources()
							.getColor(android.R.color.darker_gray));
				} else {
					viewHolder.nameTextView.setTextColor(getContext().getResources().getColor(android.R.color.black));
					viewHolder.ipTextView.setTextColor(getContext().getResources().getColor(android.R.color.black));
				}

				if (device.type == null)
					alpha = 150;
				if (device.intrface == null)
					alpha = 150;

				viewHolder.deviceImageView.setAlpha(alpha);
				viewHolder.interfaceImageView.setAlpha(alpha);
			}
			return convertView;
		}

		private static class ViewHolder {
			ImageView deviceImageView;
			TextView nameTextView;
			TextView ipTextView;
			TextView macTextView;
			ImageView interfaceImageView;
		}

	}

	// /ADAPTER

}
