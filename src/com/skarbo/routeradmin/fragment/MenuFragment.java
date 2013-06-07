package com.skarbo.routeradmin.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.skarbo.routeradmin.R;
import com.skarbo.routeradmin.RouterAdminActivity;
import com.skarbo.routeradmin.fragment.MenuFragment.Item;
import com.skarbo.routeradmin.handler.RouterHandler;
import com.skarbo.routeradmin.handler.RouterPreferencesHandler.RouterSupport;
import com.skarbo.routeradmin.model.RouterProfile;

public class MenuFragment extends Fragment {

	private static final String TAG = MenuFragment.class.getSimpleName();
	private ListView menuListView;
	private RouterAdminActivity routerAdminActivity;
	private RouterHandler routerHandler;
	private MenuAdapter menuAdapter;
	private List<Item> menuList;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "OnActivityCreated");

		routerAdminActivity = (RouterAdminActivity) getActivity();
		routerHandler = routerAdminActivity.getRouterHandler();

		menuAdapter = new MenuAdapter(getActivity(), R.id.frame_menu_list, new ArrayList<MenuFragment.Item>());

		if (menuListView != null) {
			menuListView.setAdapter(menuAdapter);

			menuListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (!menuAdapter.getItem(position).isSection()) {
						doMenuSwitch((MenuItem) menuAdapter.getItem(position));
					}
				}
			});
		}

		doUpdateMenuList();
		doUpdateView();
	}

	public void onMenuOpen() {
		doUpdateView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frame_menu, null);

		menuListView = (ListView) view.findViewById(R.id.frame_menu_list);

		return view;
	}

	public void doUpdateMenuList() {
		Log.d(TAG, "DoUpdateMenuList");
		menuList = createMenuList();
	}

	private void doUpdateView() {
		Log.d(TAG, "DoUpdateView");

		menuAdapter.clear();
		for (Item item : this.menuList) {
			menuAdapter.add(item);
		}
		menuAdapter.notifyDataSetChanged();
	}

	public Class<? extends Fragment> getFirstFragment() {
		for (Item item : menuList) {
			if (!item.isSection())
				return ((MenuItem) item).fragmentClass;
		}
		return null;
	}

	private List<Item> createMenuList() {
		List<Item> menuList = new ArrayList<Item>();

		RouterProfile routerProfile = this.routerHandler.getPreferenceHandler().getRouterProfileSelected();
		if (routerProfile == null)
			return menuList;

		RouterSupport routerSupport = this.routerHandler.getPreferenceHandler().getRouterSupport(
				routerProfile.getRouterId());
		if (routerSupport == null)
			return menuList;

		// Status
		if (routerSupport.status != null) {
			menuList.add(new SectionItem("Status"));
			
			// Info
			if (routerSupport.status.info)
				menuList.add(new MenuItem(InfoStatusFragment.class, R.drawable.devices, "Info"));

			// Devices
			if (routerSupport.status.devices)
				menuList.add(new MenuItem(DevicesStatusFragment.class, R.drawable.devices, "Devices"));
		}
		// Tools
		if (routerSupport.tools != null) {
			menuList.add(new SectionItem("Tools"));

			// Restart
			if (routerSupport.tools.restart)
				menuList.add(new MenuItem(RestartToolsFragment.class, R.drawable.restart, "Restart"));
		}
		// Advanced
		if (routerSupport.advanced != null) {
			menuList.add(new SectionItem("Advanced"));

			// Access control
			if (routerSupport.advanced.accesscontrol)
				menuList.add(new MenuItem(AccessControlAdvancedFragment.class, R.drawable.access_secure,
						"Access Control"));
		}

		return menuList;
	}

	private void doMenuSwitch(MenuItem menu) {
		routerAdminActivity.doSwitchContent(menu.getFragmentClass());
	}

	// ... CLASS

	interface Item {
		public boolean isSection();

		public String getTitle();
	}

	private class SectionItem implements Item {

		private String title;

		public SectionItem(String title) {
			this.title = title;
		}

		@Override
		public boolean isSection() {
			return true;
		}

		@Override
		public String getTitle() {
			return title;
		}

	}

	private class MenuItem implements Item {

		private String title;
		private int imageResource;
		private Class<? extends Fragment> fragmentClass;

		public MenuItem(Class<? extends Fragment> fragmentClass, int imageResource, String title) {
			this.fragmentClass = fragmentClass;
			this.imageResource = imageResource;
			this.title = title;
		}

		public boolean isSection() {
			return false;
		}

		public Class<? extends Fragment> getFragmentClass() {
			return fragmentClass;
		}

		public int getImageResource() {
			return imageResource;
		}

		@Override
		public String getTitle() {
			return title;
		}

	}

	private class MenuAdapter extends ArrayAdapter<Item> {

		private LayoutInflater layoutInfalter;

		public MenuAdapter(Context context, int textViewResourceId, List<Item> objects) {
			super(context, textViewResourceId, objects);
			layoutInfalter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			final Item item = getItem(position);
			if (item != null) {
				if (item.isSection()) {
					SectionItem section = (SectionItem) item;
					v = layoutInfalter.inflate(R.layout.list_item_section, null);

					final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
					sectionView.setText(section.getTitle());
				} else {
					MenuItem menu = (MenuItem) item;
					v = layoutInfalter.inflate(R.layout.list_item_entry, null);

					final TextView titleView = (TextView) v.findViewById(R.id.list_item_entry_title);
					final ImageView imageView = (ImageView) v.findViewById(R.id.list_item_entry_drawable);

					titleView.setText(menu.getTitle());
					imageView.setImageResource(menu.getImageResource());
				}
			}

			return v;
		}

	}

	// ... /CLASS

}
