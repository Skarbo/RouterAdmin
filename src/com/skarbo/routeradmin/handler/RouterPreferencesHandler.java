package com.skarbo.routeradmin.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.skarbo.routeradmin.model.RouterProfile;
import com.skarbo.routeradmin.utils.Utils;

public class RouterPreferencesHandler {

	private static final String TAG = RouterPreferencesHandler.class.toString();
	private static final String PREF_PROFILE_ID = "profile_id";
	private static final String PREF_PROFILE_IDS = "profile_ids";

	private static final String ROUTERS_JSON = "routerconfig/routers.json";

	private static final String PREF_ROUTER_PROFILE_ID = "router_id";
	private static final String PREF_ROUTER_PROFILE_TYPE = "router_type";
	private static final String PREF_ROUTER_PROFILE_IP = "router_ip";
	private static final String PREF_ROUTER_PROFILE_USER = "router_user";
	private static final String PREF_ROUTER_PROFILE_PASSWORD = "router_password";

	private Context context;
	private JSONObject routersJson;
	private SharedPreferences sharedPreferences;

	public RouterPreferencesHandler(Context context) {
		this.context = context;

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		doInit();
	}

	// ... GET

	public static String getProfilePreferenceFilename(String profileId) {
		return "routerprofile_" + profileId;
	}

	public SharedPreferences getSharedPreference() {
		return this.sharedPreferences;
	}

	public SharedPreferences getRouterProfilePreference(String profileId) {
		return this.context.getSharedPreferences(getProfilePreferenceFilename(profileId), Activity.MODE_PRIVATE);
	}

	// ... ... ROUTERS

	public JSONArray getRoutersJson() {
		if (this.routersJson == null)
			return null;
		return this.routersJson.optJSONArray("routers");
	}

	public RouterBrand[] getRouters() {
		JSONArray routers = getRoutersJson();
		if (routers == null)
			return null;

		Gson gson = new Gson();
		return gson.fromJson(routers.toString(), RouterBrand[].class);
	}

	// ... ... /ROUTERS

	// ... ... ROUTER

	public JSONObject getRouterJson(String routerId) {
		JSONArray routers = getRoutersJson();
		if (routers == null)
			return null;

		for (int i = 0; i < routers.length(); i++) {
			JSONObject brand = routers.optJSONObject(i);
			if (brand != null) {
				JSONArray devices = brand.optJSONArray("devices");
				if (devices != null) {
					for (int j = 0; j < routers.length(); j++) {
						JSONObject router = devices.optJSONObject(j);
						if (router != null && router.optString("id").equalsIgnoreCase(routerId))
							return router;
					}
				}
			}
		}
		return null;
	}

	public JSONObject getRouterJson(int brandIndex, int routerIndex) {
		JSONObject brand = getRoutersJson().optJSONObject(brandIndex);
		if (brand != null) {
			JSONArray devices = brand.optJSONArray("devices");
			if (devices != null) {
				return devices.optJSONObject(routerIndex);
			}
		}
		return null;
	}

	public Router getRouter(String routerId) {
		RouterBrand[] routerObjects = getRouters();
		if (routerObjects == null)
			return null;

		for (RouterBrand routerBrand : routerObjects) {
			for (Router router : routerBrand.devices) {
				if (router.id.equalsIgnoreCase(routerId))
					return router;
			}
		}
		return null;
	}

	public Router getRouter(int brandIndex, int routerIndex) {
		RouterBrand[] routerObjects = getRouters();
		if (routerObjects == null)
			return null;

		try {
			return routerObjects[brandIndex].devices[routerIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return null;
	}

	// ... ... /ROUTER

	// ... ... ROUTER BRAND

	public JSONObject getRouterBrandJson(String brandId) {
		JSONArray routers = getRoutersJson();
		if (routers == null)
			return null;

		for (int i = 0; i < routers.length(); i++) {
			JSONObject brand = routers.optJSONObject(i);
			if (brand != null && brand.optString("id").equalsIgnoreCase(brandId))
				return brand;
		}
		return null;
	}

	public JSONObject getRouterBrandJson(int index) {
		JSONArray routers = getRoutersJson();
		if (routers == null)
			return null;
		return routers.optJSONObject(index);
	}

	public RouterBrand getRouterBrand(String brandId) {
		RouterBrand[] routerObjects = getRouters();
		if (routerObjects == null)
			return null;

		for (RouterBrand routerBrand : routerObjects) {
			if (routerBrand.id.equalsIgnoreCase(brandId))
				return routerBrand;
		}
		return null;
	}

	public RouterBrand getRouterBrand(int brandIndex) {
		RouterBrand[] routerObjects = getRouters();
		if (routerObjects == null)
			return null;

		try {
			return routerObjects[brandIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
		}
		return null;
	}

	// ... ... /ROUTER BRAND

	// ... ... ROUTER PARSING

	public JSONObject getRouterParsingJson(String routerId) {
		JSONObject router = getRouterJson(routerId);
		if (router == null)
			return null;

		JSONObject brand = getRouterBrandJson(router.optString("brandId"));
		if (brand == null)
			return null;

		JSONObject parsing = this.routersJson.optJSONObject("parsing");
		JSONObject brandParsing = brand.optJSONObject("parsing");
		JSONObject routerParsing = router.optJSONObject("parsing");

		if (parsing == null)
			return null;

		try {
			if (brandParsing != null)
				parsing = Utils.mergeJsonObject(parsing, brandParsing);
			if (routerParsing != null)
				parsing = Utils.mergeJsonObject(parsing, routerParsing);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return parsing;
	}

	public RouterParsing getRouterParsing(String routerId) {
		JSONObject routerParsing = getRouterParsingJson(routerId);

		if (routerParsing == null)
			return null;

		Gson gson = new Gson();
		return gson.fromJson(routerParsing.toString(), RouterParsing.class);
	}

	// ... ... /ROUTER PARSING

	// ... ... ROUTER SUPPORT

	public JSONObject getRouterSupportJson(String routerId) {
		JSONObject router = getRouterJson(routerId);
		if (router == null)
			return null;

		JSONObject brand = getRouterBrandJson(router.optString("brandId"));
		if (brand == null)
			return null;

		JSONObject support = this.routersJson.optJSONObject("support");
		JSONObject brandSupport = brand.optJSONObject("support");
		JSONObject routerSupport = router.optJSONObject("support");

		if (support == null)
			return null;

		try {
			if (brandSupport != null)
				support = Utils.mergeJsonObject(support, brandSupport);
			if (routerSupport != null)
				support = Utils.mergeJsonObject(support, routerSupport);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return support;
	}

	public RouterSupport getRouterSupport(String routerId) {
		JSONObject routerSupport = getRouterSupportJson(routerId);

		if (routerSupport == null)
			return null;

		Gson gson = new Gson();
		return gson.fromJson(routerSupport.toString(), RouterSupport.class);
	}

	// ... ... /ROUTER SUPPORT

	// ... ... ROUTER PROFILE

	public static String getRouterProfilePreferenceFilename(String profileId) {
		return "routerprofile_" + profileId;
	}

	public List<String> getRouterProfileIds() {
		SharedPreferences sharedPreferences = getSharedPreference();
		return new ArrayList<String>(Arrays.asList(sharedPreferences.getString(PREF_PROFILE_IDS, "").split(",")));
	}

	/**
	 * @return Selected Router Profile
	 */
	public RouterProfile getRouterProfileSelected() {
		SharedPreferences sharedPreference = getSharedPreference();
		String profileId = sharedPreference.getString(PREF_PROFILE_ID, "");
		if (!profileId.equalsIgnoreCase(""))
			return getRouterProfile(profileId);
		return null;
	}

	public RouterProfile getRouterProfile(String profileId) {
		return retrieveRouterProfile(profileId);
	}

	public List<RouterProfile> getRouterProfiles() {
		List<String> routerProfileIds = getRouterProfileIds();
		List<RouterProfile> profiles = new ArrayList<RouterProfile>();
		for (String profileId : routerProfileIds) {
			if (!profileId.equalsIgnoreCase(""))
				profiles.add(retrieveRouterProfile(profileId));
		}
		return profiles;
	}

	// ... ... /ROUTER PROFILE

	// ... /GET

	// ... SET

	public void setSharedPreferences(SharedPreferences sharedPreferences) {
		this.sharedPreferences = sharedPreferences;
	}

	/**
	 * Set Router Profile
	 * 
	 * @param profileId
	 */
	public void setRouterProfileSelected(String profileId) {
		SharedPreferences sharedPreference = getSharedPreference();

		SharedPreferences.Editor editor = sharedPreference.edit();
		editor.putString(PREF_PROFILE_ID, profileId);
		editor.commit();
	}

	// ... /SET

	// ... DO

	private void doInit() {
		this.routersJson = null;
		try {
			InputStream routersFile = context.getAssets().open(ROUTERS_JSON);

			BufferedReader r = new BufferedReader(new InputStreamReader(routersFile));
			StringBuilder routersLine = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				routersLine.append(line);
			}

			this.routersJson = new JSONObject(routersLine.toString());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	// ... /DO

	/**
	 * @param profileId
	 * @param sharedPreferences
	 * @return Router Profile from preference
	 */
	public RouterProfile retrieveRouterProfile(String profileId) {
		SharedPreferences sharedPreferences = getRouterProfilePreference(profileId);
		RouterProfile routerProfile = new RouterProfile();

		routerProfile.setId(profileId);
		routerProfile.setIp(sharedPreferences.getString(PREF_ROUTER_PROFILE_IP, ""));
		routerProfile.setUser(sharedPreferences.getString(PREF_ROUTER_PROFILE_USER, ""));
		routerProfile.setPassword(sharedPreferences.getString(PREF_ROUTER_PROFILE_PASSWORD, ""));
		routerProfile.setRouterId(sharedPreferences.getString(PREF_ROUTER_PROFILE_TYPE, ""));

		return routerProfile;
	}

	// ... SAVE

	public void saveRouterProfileIds(List<String> profileIds) {
		SharedPreferences sharedPreferences = getSharedPreference();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_PROFILE_IDS, Utils.implode(profileIds.toArray(), ","));
		editor.commit();
	}

	public void saveRouterProfile(String profileId, RouterProfile routerProfile) {
		// Save Profile preference
		SharedPreferences profilePreference = getRouterProfilePreference(profileId);

		SharedPreferences.Editor editor = profilePreference.edit();
		editor.putString(PREF_ROUTER_PROFILE_ID, profileId);
		editor.putString(PREF_ROUTER_PROFILE_IP, routerProfile.getIp());
		editor.putString(PREF_ROUTER_PROFILE_USER, routerProfile.getUser());
		editor.putString(PREF_ROUTER_PROFILE_PASSWORD, routerProfile.getPassword());
		editor.putString(PREF_ROUTER_PROFILE_TYPE, routerProfile.getRouterId());
		editor.commit();

		// Save Profile ids
		List<String> routerProfileIds = getRouterProfileIds();
		if (!routerProfileIds.contains(profileId))
			routerProfileIds.add(profileId);
		this.saveRouterProfileIds(routerProfileIds);
	}

	// ... /SAVE

	public void deleteRouterProfile(String profileId) {
		SharedPreferences profileSharedPreferences = getRouterProfilePreference(profileId);

		// Remove profile preference
		SharedPreferences.Editor editor = profileSharedPreferences.edit();
		editor.clear();
		editor.commit();

		// Save new profile ids
		List<String> routerProfileIds = getRouterProfileIds();
		routerProfileIds.remove(profileId);
		saveRouterProfileIds(routerProfileIds);

		// Delete preferences file
		File routerProfilePreferenceFile = new File(String.format("/data/data/%s/shared_prefs/%s.xml",
				"com.skarbo.routeradmin", getProfilePreferenceFilename(profileId)));

		if (routerProfilePreferenceFile.exists())
			routerProfilePreferenceFile.delete();
		else
			Log.e(TAG, routerProfilePreferenceFile.getAbsolutePath() + " does not exist");
	}

	public void deleteSharedPreference() {
		SharedPreferences sharedPreference = getSharedPreference();

		SharedPreferences.Editor editor = sharedPreference.edit();
		editor.clear();
		editor.commit();
	}

	// ... CLASS

	public static class Router {
		public String brandId;
		public String id;
		public String name;
		public boolean auth;
	}

	public static class RouterBrand {
		public String id;
		public String brand;
		public Router[] devices;
	}

	public static class RouterSupport {

		public StatusRouterSupport status;
		public ToolsRouterSupport tools;
		public AdvancedRouterSupport advanced;

		public static class StatusRouterSupport {
			public boolean info;
			public boolean devices;
		}

		public static class ToolsRouterSupport {
			public boolean restart;
		}

		public static class AdvancedRouterSupport {
			public boolean accesscontrol;
		}

	}

	public static class RouterParsing {

		public LoginRouterParsing login;
		public ToolsRestartRouterParsing tools_restart;
		public AdvancedAccesscontrolRouterParsing advanced_accesscontrol;
		public StatusDeviceRouterParsing[] status_devices;
		public StatusInfoRouterParsing[] status_info;
		public String notloggedin;

		public static class LoginRouterParsing {
			public String page;
			public String regexIsPage;
			public String regexLoginDetails;
			public Map<String, String> post;
			public String regexLoginFailed;
		}

		public static class ToolsRestartRouterParsing {
			public String page;
			public String regexIsPage;
			public Map<String, String> post;
			public String success;
		}

		public static class AdvancedAccesscontrolRouterParsing {
			public String page;
			public String regexIsPage;
			public String regexPolicy;
			public PolicyContainerAdvancedAccesscontrolRouterParsing policyContainer;
			public String regexEnabled;
			public Map<String, String> postEnabled;
			public String[] postEnabledToggle;
			public Map<String, String> postPolicy;

			public static class PolicyContainerAdvancedAccesscontrolRouterParsing {
				public int enable;
				public int policy;
				public int machine;
				public int filtering;
				public int logged;
				public int schedule;
			}
		}

		public static class StatusDeviceRouterParsing {
			public String page;
			public String regexIsPage;
			public String deviceRegex;
			public DeviceObjectStatusDeviceRouterParsing deviceObject;
			public Map<String, String> deviceObjectType;
			public Map<String, String> deviceObjectInterface;
			public String deviceObjectActive;

			public static class DeviceObjectStatusDeviceRouterParsing {
				public int name;
				public int mac;
				public int inactive;
				public int type;
				public int ip;
				public int intrface;
			}

		}

		public static class StatusInfoRouterParsing {
			public String title;
			public String page;
			public String regexIsPage;
			public ContainerStatusInfoRouterParsing[] containers;

			public static class ContainerStatusInfoRouterParsing {
				public String title;
				public String regex;
				public Map<String, String> object;
			}
		}

	}

	// ... /CLASS

}
