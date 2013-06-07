package com.skarbo.routeradmin.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Utils {

	private static final String TAG = Utils.class.getSimpleName();

	public static String implode(Object[] objects) {
		return implode(objects, ",");
	}

	public static String implode(Object[] objects, String seperator) {
		StringBuilder sb = new StringBuilder();

		if (objects == null)
			return sb.toString();
		if (objects.length > 0)
			sb.append(objects[0]);

		for (int i = 1; i < objects.length; i++) {
			sb.append(seperator);
			sb.append(objects[i]);
		}

		return sb.toString();
	}

	public static String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}

	public static JSONObject mergeJsonObject(JSONObject jsonObjectLeft, JSONObject jsonObjectRight)
			throws JSONException {
		@SuppressWarnings("rawtypes")
		Iterator keys = jsonObjectRight.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object valueRight = jsonObjectRight.get(key);
			if (jsonObjectLeft.has(key)) {
				Object valueLeft = jsonObjectLeft.get(key);
				if (valueLeft instanceof JSONObject && valueRight instanceof JSONObject)
					mergeJsonObject((JSONObject) valueLeft, (JSONObject) valueRight);
				else
					jsonObjectLeft.putOpt(key, valueRight);
			} else
				jsonObjectLeft.putOpt(key, valueRight);
		}
		return jsonObjectLeft;
	}

	public static Map<String, String> createMap(JSONObject jsonObject) {
		Map<String, String> map = new HashMap<String, String>();
		@SuppressWarnings("rawtypes")
		Iterator keys = jsonObject.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object value = jsonObject.opt(key);
			if (value != null)
				map.put(key, value.toString());
			else
				map.put(key, null);
		}
		return map;
	}

	public static Map<String, String> createMap(Matcher matcher) {
		Map<String, String> map = new HashMap<String, String>();

		for (int i = 1; i <= matcher.groupCount(); i++)
			map.put(String.valueOf(i), matcher.group(i));

		return map;
	}

	public static Map<String, String> createMap(Map<String, String> subject, Map<String, String> replace) {
		Map<String, String> map = new HashMap<String, String>(subject);

		for (Entry<String, String> entrySubject : map.entrySet()) {
			for (Entry<String, String> entryReplace : replace.entrySet()) {
				String replaceString = "%" + entryReplace.getKey() + "%";
				if (entrySubject.getValue().contains(replaceString))
					map.put(entrySubject.getKey(),
							entrySubject.getValue().replace(replaceString, entryReplace.getValue()));
			}
		}

		return map;
	}

	public static String retrieveContent(File file) throws IOException {
		return retrieveContent(new FileInputStream(file));
	}

	public static String retrieveContent(InputStream inputStream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder lines = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			lines.append(line);
		}
		return lines.toString();
	}

	public static String mapToString(Map<?, ?> map) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Entry<?, ?> entry : map.entrySet()) {
			stringBuilder.append(entry.getKey().toString() + ": " + entry.getValue().toString() + "\n");
		}

		return stringBuilder.toString();
	}

}
