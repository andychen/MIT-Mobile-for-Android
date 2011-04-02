package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import edu.mit.mitmobile2.MobileWebApi;
import edu.mit.mitmobile2.objs.RouteItem;
import edu.mit.mitmobile2.objs.RouteItem.Stops;

public class ShuttleModel {

	public static final String KEY_STOP_TITLE = "stop_title";
	public static final String KEY_STOP_ID = "stop_id";
	public static final String KEY_ROUTE_ID = "route_id";
	public static final String KEY_TIME = "time";

	@SuppressWarnings("serial")
	public static final HashMap<double[], String> STOP_LOCATIONS = new HashMap<double[], String>() {
		{
			put(new double[] { 42.35952, -71.09416 }, "mass84_d");
			put(new double[] { 42.35103, -71.08963 }, "massbeac");
			put(new double[] { 42.34915, -71.09463 }, "comm487");
			put(new double[] { 42.3492899, -71.0998196 }, "commsher");
			put(new double[] { 42.3488099, -71.094014 }, "comm478");
			put(new double[] { 42.3509163, -71.0894084 }, "beacmass");
			put(new double[] { 42.35927, -71.09368 }, "mass77");
			put(new double[] { 42.3601699, -71.0975194 }, "edge");
			put(new double[] { 42.36237, -71.08613 }, "kendsq_d");
			put(new double[] { 42.361272, -71.0843897 }, "amhewads");
			put(new double[] { 42.36032, -71.08686 }, "medilab");
			put(new double[] { 42.35797, -71.09421 }, "kres");
			put(new double[] { 42.35608, -71.09871 }, "burtho");
			put(new double[] { 42.35489, -71.10269 }, "tangwest");
			put(new double[] { 42.3548358, -71.1049941 }, "w92ames");
			put(new double[] { 42.3566938, -71.1021138 }, "simmhl");
			put(new double[] { 42.3603, -71.09452 }, "vassmass");
			put(new double[] { 42.3623986, -71.0902529 }, "statct");
			put(new double[] { 42.34831, -71.08834 }, "massnewb");
			put(new double[] { 42.3505001, -71.0908295 }, "beac528");
			put(new double[] { 42.35014, -71.09797 }, "bays111");
			put(new double[] { 42.3503297, -71.1005867 }, "bays155");
			put(new double[] { 42.3465563, -71.1165503 }, "stpaul259");
			put(new double[] { 42.34885, -71.1238599 }, "manc58");
			put(new double[] { 42.35029, -71.08636 }, "here32");
			put(new double[] { 42.3602019, -71.0975257 }, "nw10");
			put(new double[] { 42.3591086, -71.1002973 }, "nw30");
			put(new double[] { 42.360289, -71.1022784 }, "nw86");
			put(new double[] { 42.36204, -71.0981 }, "nw61");
			put(new double[] { 42.36318, -71.09654 }, "mainwinds");
			put(new double[] { 42.3661096, -71.0918302 }, "porthamp");
			put(new double[] { 42.3719808, -71.0874169 }, "camb638");
			put(new double[] { 42.37144, -71.0832 }, "camb5th");
			put(new double[] { 42.3681476, -71.0855705 }, "6thcharl");
			put(new double[] { 42.36264, -71.08908 }, "elotmain");
			put(new double[] { 42.36123, -71.08837 }, "amesbld66");
			put(new double[] { 42.3610355, -71.0862818 }, "mitmed");
			put(new double[] { 42.36237, -71.08613 }, "kendsq");
			put(new double[] { 42.3612719, -71.0843897 }, "wadse40");
			put(new double[] { 42.35766, -71.0947 }, "mccrmk");
			put(new double[] { 42.35608, -71.09871 }, "burtho");
			put(new double[] { 42.35559, -71.10021 }, "newho");
			put(new double[] { 42.3566938, -71.1021138 }, "simmhl");
			put(new double[] { 42.3557083, -71.1097716 }, "ww15");
			put(new double[] { 42.35654, -71.1089 }, "brookchest");
			put(new double[] { 42.35904, -71.11096 }, "putmag");
			put(new double[] { 42.3626179, -71.1124847 }, "rivfair");
			put(new double[] { 42.3639197, -71.1086206 }, "rivpleas");
			put(new double[] { 42.3648, -71.10594 }, "rivfrank");
			put(new double[] { 42.36246, -71.10016 }, "sydgreen");
			put(new double[] { 42.36023, -71.1023 }, "paci70");
			put(new double[] { 42.3590332, -71.1002949 }, "whou");
		}
	};

	static final String BASE_PATH = "/shuttles";

	static private long lastRouteFetchTime = -1;
	static private long ROUTE_CACHE_TIMEOUT = 20 * 60 * 1000; // 20 minutes
	static private ArrayList<RouteItem> routes = null;

	static final int ALERT_EXPIRE_TIME = 30 * 60 * 1000; // 30 minutes

	static private HashMap<String, List<Stops>> stops = new HashMap<String, List<Stops>>();	// 

	private static List<RouteItem> getRoutes() {
		if (routes != null) {
			return routes;
		} else {
			return Collections.<RouteItem> emptyList();
		}
	}

	public static List<RouteItem> getRoutes(boolean isSafeRide) {
		ArrayList<RouteItem> routes = new ArrayList<RouteItem>();

		for (RouteItem aRouteItem : getRoutes()) {
			if (aRouteItem.isSafeRide == isSafeRide) {
				routes.add(aRouteItem);
			}
		}
		return routes;
	}

	public static List<RouteItem> getSortedRoutes() {
		// reorder the routes
		// so that day time shuttles proceed night time saferides
		ArrayList<RouteItem> reorderedRoutes = new ArrayList<RouteItem>();
		reorderedRoutes.addAll(getRoutes(false));
		reorderedRoutes.addAll(getRoutes(true));
		return reorderedRoutes;
	}

	public static RouteItem getRoute(String routeId) {
		for (RouteItem routeItem : getRoutes()) {
			if (routeItem.route_id.equals(routeId)) {
				return routeItem;
			}
		}
		return null;
	}

	public static RouteItem getUpdatedRoute(RouteItem routeItem) {
		return getRoute(routeItem.route_id);
	}

//	// Returns unique stops; disregards route info.
//	public static List<Stops> getStops() {
//		ArrayList<Stops> results = new ArrayList<Stops>();
//
//		for (List<Stops> l : stops.values()) {
//			results.add(l.get(0));
//		}
//		return results;
//	}

	public static List<Stops> getStops(String stopId) {
		return stops.get(stopId);
	}

	public static Stops getStops(String stopId, String routeId) {
		for (Stops stops : getStops(stopId)) {
			if (stops.route_id.equals(routeId)) {
				return stops;
			}
		}
		return null;
	}

	public static int getStopPosition(List<Stops> stops, String stopId) {
		for (int position = 0; position < stops.size(); position++) {
			Stops stop = stops.get(position);
			if (stop.id.equals(stopId)) {
				return position;
			}
		}
		return -1;
	}

	public static void addRoute(RouteItem routeItem) {
		if (routes == null) {
			routes = new ArrayList<RouteItem>();
		}

		for (int index = 0; index < routes.size(); index++) {
			if (routes.get(index).route_id.equals(routeItem.route_id)) {
				routes.set(index, routeItem);
				return;
			}
		}
		routes.add(routeItem);
	}

	/*
	 * This function only updates the route data, if the route currently exists
	 * in the routes list
	 */
	private static void updateRouteItem(RouteItem routeItem) {
		if (routes != null) {
			for (int index = 0; index < routes.size(); index++) {
				if (routes.get(index).route_id.equals(routeItem.route_id)) {
					routes.set(index, routeItem);
					return;
				}
			}
		}
	}

	public static void fetchRoutes(Context context, final Handler uiHandler,
			boolean forceRefresh) {
		if (!forceRefresh
				&& (System.currentTimeMillis() - lastRouteFetchTime) < ROUTE_CACHE_TIMEOUT) {

			MobileWebApi.sendSuccessMessage(uiHandler);
			return;
		}

		HashMap<String, String> routesParameters = new HashMap<String, String>();
		routesParameters.put("command", "routes");
		routesParameters.put("compact", "true");

		MobileWebApi webApi = new MobileWebApi(false, true, "Shuttle Routes",
				context, uiHandler);
		webApi.requestJSONArray(
				BASE_PATH,
				routesParameters,
				new MobileWebApi.JSONArrayResponseListener(
						new MobileWebApi.DefaultErrorListener(uiHandler), null) {
					@Override
					public void onResponse(JSONArray array) {
						routes = new ArrayList<RouteItem>();
						routes.addAll(RoutesParser.routesParser(array));
						lastRouteFetchTime = System.currentTimeMillis();
						MobileWebApi.sendSuccessMessage(uiHandler);
					}
				});
	}
	
	public static void fetchRoutesAndDetails(Context context, final Handler uiHandler,
			boolean forceRefresh) {
		if (!forceRefresh
				&& (System.currentTimeMillis() - lastRouteFetchTime) < ROUTE_CACHE_TIMEOUT) {

			MobileWebApi.sendSuccessMessage(uiHandler);
			return;
		}

		HashMap<String, String> routesParameters = new HashMap<String, String>();
		routesParameters.put("command", "routes");

		MobileWebApi webApi = new MobileWebApi(false, true, "Shuttle Routes",
				context, uiHandler);
		webApi.requestJSONArray(
				BASE_PATH,
				routesParameters,
				new MobileWebApi.JSONArrayResponseListener(
						new MobileWebApi.DefaultErrorListener(uiHandler), null) {
					@Override
					public void onResponse(JSONArray array) {
						routes = new ArrayList<RouteItem>();
						routes.addAll(RoutesParser.routesParser(array));
						lastRouteFetchTime = System.currentTimeMillis();
						MobileWebApi.sendSuccessMessage(uiHandler);
					}
				});
	}

	public static void fetchRouteDetails(Context context, RouteItem routeItem,
			final Handler uiHandler) {
		// determine if any predictions for route details data exists
		// if some predictions exists do a silent request (i.e. dont show error
		// or loading messages);

		boolean silent = true;
		/*
		 * RouteItem cachedRouteItem = getRoute(routeItem.route_id); boolean
		 * silent = false; if(cachedRouteItem != null &&
		 * !cachedRouteItem.stops.isEmpty()) {
		 * if(cachedRouteItem.stops.get(0).predictions.isEmpty()) { silent =
		 * true; } }
		 */
		HashMap<String, String> routeInfoParameters = new HashMap<String, String>();
		routeInfoParameters.put("command", "routeInfo");
		routeInfoParameters.put("full", "true");
		routeInfoParameters.put("id", routeItem.route_id);

		MobileWebApi webApi = new MobileWebApi(!silent, !silent,
				"Shuttle Route", context, uiHandler);
		webApi.requestJSONObject(
				BASE_PATH,
				routeInfoParameters,
				new MobileWebApi.JSONObjectResponseListener(
						new MobileWebApi.DefaultErrorListener(uiHandler), null) {
					@Override
					public void onResponse(JSONObject object) {
						updateRouteItem(RoutesParser
								.parseJSONRouteObject(object));
						MobileWebApi.sendSuccessMessage(uiHandler);
					}
				});
	}

	public static List<String> getClosestStopIds(double lat, double lon, int num) {
		HashMap<Float, String> dists = new HashMap<Float, String>();
		float[] results = new float[1];
		ArrayList<String> closestStopIds = new ArrayList<String>();

		//Generate hashmap of distances to stopids.
		for (Map.Entry<double[], String> entry : STOP_LOCATIONS.entrySet()) {
			Location.distanceBetween(lat, lon, entry.getKey()[0], entry.getKey()[1], results);
			dists.put(results[0], entry.getValue());
		}
		
		TreeMap<Float, String> sortedDists = new TreeMap<Float, String>(dists);
		
		Iterator<Float> it = sortedDists.keySet().iterator();
		Log.e("shuttlemodel NUM: ", num+"");
		for (int i=0; i<num; i++)
		{
			closestStopIds.add(sortedDists.get(it.next()));
		}
		Log.e("shuttlemodel return size: ", closestStopIds.size()+"");
		return closestStopIds;
	}

	public static void fetchStopDetails(final String stopId,
			final Handler handler) {
		HashMap<String, String> stopInfoParameters = new HashMap<String, String>();
		stopInfoParameters.put("command", "stopInfo");
		stopInfoParameters.put("id", stopId);

		MobileWebApi webApi = new MobileWebApi();
		webApi.requestJSONObject(BASE_PATH, stopInfoParameters,
				new MobileWebApi.JSONObjectResponseListener(null, null) {

					@Override
					public void onResponse(JSONObject object) {
						stops.put(stopId,
								RoutesParser.parseJSONStopsArray(object));
						MobileWebApi.sendSuccessMessage(handler);
					}
				});
	}

	/****************************************************/
	public static HashMap<String, HashMap<String, Long>> getAlerts(
			SharedPreferences pref) {

		HashMap<String, HashMap<String, Long>> alertIdx = new HashMap<String, HashMap<String, Long>>();

		// SharedPreferences pref =
		// ctx.getSharedPreferences(Global.PREFS,Context.MODE_WORLD_READABLE|Context.MODE_WORLD_READABLE);

		String stop_id = pref.getString(ShuttleModel.KEY_STOP_ID, null);
		String routes = pref.getString(ShuttleModel.KEY_ROUTE_ID, null);
		String times = pref.getString(ShuttleModel.KEY_TIME, null);

		Log.d("ShuttleModel", "shuttle-alerts: get-> stop_id: " + stop_id);
		Log.d("ShuttleModel", "shuttle-alerts: get-> route_id: " + routes);
		Log.d("ShuttleModel", "shuttle-alerts: get-> times: " + times);

		if ((stop_id != null) && (routes != null) && (times != null)) {

			String[] stop_alarms = stop_id.split(",");
			String[] routes_alarms = routes.split(",");
			String[] times_alarms = times.split(",");

			if (stop_alarms.length < 1) {
				return null;
			}
			if (stop_alarms.length != routes_alarms.length) {
				Log.e("ShuttleModel", "shuttle-alerts: bad length 1");
				return null;
			}
			if (stop_alarms.length != times_alarms.length) {
				Log.e("ShuttleModel", "shuttle-alerts: bad length 2");
				return null;
			}

			Long time;
			HashMap<String, Long> route_times;

			for (int x = 0; x < stop_alarms.length; x++) {

				String s = stop_alarms[x];

				if ("".equals(s)) {
					Log.d("ShuttleModel", "shuttle-alerts: error?");
					continue;
				}

				route_times = alertIdx.get(s);
				if (route_times == null) {
					route_times = new HashMap<String, Long>();
				}

				if ("".equals(times_alarms[x]))
					continue;

				time = Long.valueOf(times_alarms[x]);
				route_times.put(routes_alarms[x], time);

				alertIdx.put(s, route_times);

			}

		}

		return alertIdx;

	}

	/**
	 * @param alertIdx
	 **************************************************/
	// public static void saveAlerts(Context ctx, HashMap<String,
	// HashMap<String, Long>> alertIdx ) {
	public static void saveAlerts(SharedPreferences pref,
			HashMap<String, HashMap<String, Long>> alertIdx) {

		// SharedPreferences pref =
		// ctx.getSharedPreferences(Global.PREFS,Context.MODE_PRIVATE);
		// SharedPreferences pref =
		// ctx.getSharedPreferences(Global.PREFS,Context.MODE_WORLD_READABLE|Context.MODE_WORLD_READABLE);

		SharedPreferences.Editor editor = pref.edit();

		String concat1 = "";
		String concat2 = "";
		String concat3 = "";

		HashMap<String, Long> route_times;

		for (String stop : alertIdx.keySet()) {

			route_times = alertIdx.get(stop);

			for (String r : route_times.keySet()) {
				Long t = route_times.get(r);
				concat1 += stop + ",";
				concat2 += r + ",";
				concat3 += t + ",";
			}

		}

		editor.putString(ShuttleModel.KEY_STOP_ID, concat1);
		editor.putString(ShuttleModel.KEY_ROUTE_ID, concat2);
		editor.putString(ShuttleModel.KEY_TIME, concat3);

		Log.d("ShuttleModel", "shuttle-alerts: set-> stop_id: " + concat1);
		Log.d("ShuttleModel", "shuttle-alerts: set-> route_id: " + concat2);
		Log.d("ShuttleModel", "shuttle-alerts: set-> times: " + concat3);

		boolean success = editor.commit();
		if (!success) {
			Log.e("ShuttleModel", "shuttle-alerts: failed shuttle commit");
		}

	}

}
