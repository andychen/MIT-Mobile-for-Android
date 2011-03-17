package edu.mit.mitmobile2.shuttles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.mit.mitmobile2.FullScreenLoader;
import edu.mit.mitmobile2.MobileWebApi;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.SliderActivity;
import edu.mit.mitmobile2.objs.RouteItem.Stops;
import edu.mit.mitmobile2.shuttles.ShuttleRouteArrayAdapter2.SectionListItemView;

public class ShuttlesActivity2 extends ModuleActivity {

	Context ctx;

	ListView shuttleListView;
	ShuttleRouteArrayAdapter2 adapter;
	private View mFooterView;

	private FullScreenLoader shuttleSmartLoader;

	static final int MENU_HOME = Menu.FIRST;
	static final int MENU_CALL_SAFERIDE = Menu.FIRST + 1;
	static final int MENU_CALL_PARKING = Menu.FIRST + 2;
	static final int MENU_REFRESH = Menu.FIRST + 3;

	/****************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ctx = this;

		createView();

	}

	/****************************************************/
	void createView() {

		setContentView(R.layout.shuttles2);
		shuttleListView = (ListView) findViewById(R.id.shuttleLV);
		shuttleSmartLoader = (FullScreenLoader) findViewById(R.id.shuttleSmartLoader);

		getData(false);

	}

	/****************************************************/
	void updateView() {

		shuttleSmartLoader.setVisibility(View.GONE);

		//TODO: Enable GPS
//        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        Location location = null;
//        
//        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
//        	location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        	double lat = location.getLatitude();
//        	double lon = location.getLongitude();
//        
//        Log.e(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)+"", ""+location.getLatitude());
//        Log.e(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)+"", ""+location.getLongitude());
        
        //TODO: Remove temp constants: 42.350937,-71.089429
        double lat = 42.350937;
        double lon = -71.089429;
        
		List<String> closestStopIds = ShuttleModel.getClosestStopIds(lat, lon);
		HashMap<String, List<Stops>> closestStops = new HashMap<String, List<Stops>>();
		Log.e("ANDREW stopids:", closestStopIds.get(0));
		for (String s:closestStopIds){
			closestStops.put(s, ShuttleModel.getStops(s));
		}
//		List<RouteItem> dayRoutes = ShuttleModel.getRoutes(false);
//		List<RouteItem> nightRoutes = ShuttleModel.getRoutes(true);

		SectionListItemView itemBuilder = new SectionListItemView() {
			public View getView(Object item, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = inflater.inflate(R.layout.shuttles_row2, null);
				}
				
//				RouteItem routeItem = (RouteItem) item;
				Stops stop = (Stops) item;

				//TODO: Support multiple predictions
				TextView shuttleTV = (TextView) v
						.findViewById(R.id.shuttlesRowShuttleTV);
				Date d = new Date();
				d.setTime(stop.next*1000);
				SimpleDateFormat df = new SimpleDateFormat("h:mm a");
				String formatted = df.format(d);
				shuttleTV.setText(formatted);

				//TODO: Replace route_id with the better names
				TextView timesTV = (TextView) v
						.findViewById(R.id.shuttlesRowTimesTV);
				timesTV.setText(stop.route_id);
				return v;
			}
		};

		ShuttleRouteArrayAdapter2 adapter = new ShuttleRouteArrayAdapter2(this,
				itemBuilder);

		for (String s: closestStops.keySet()){
			adapter.addSection(s, closestStops.get(s));
		}

		shuttleListView.setVisibility(View.VISIBLE);
		shuttleListView.setAdapter(adapter);

		OnItemClickListener listener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				if (view == mFooterView)
					return;

				Integer routeInt = (Integer) view.getTag();

				Intent i = new Intent(ctx, MITRoutesSliderActivity.class);
				i.putExtra(SliderActivity.KEY_POSITION, routeInt);

				startActivity(i);
			}
		};

		shuttleListView.setOnItemClickListener(listener);

	}

	/****************************************************/
	protected void getData(boolean forceRefresh) {

		shuttleSmartLoader.setVisibility(View.VISIBLE);
		shuttleSmartLoader.showLoading();
		shuttleListView.setVisibility(View.GONE);

		// this Handler will run on this thread (UI)
		final Handler myHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.arg1 == MobileWebApi.SUCCESS) {
					updateView();
				} 
				else {
					shuttleSmartLoader.showError();
				}
			}
		};
		ShuttleModel.fetchStopDetails("mass84_d", myHandler);
	}

	/****************************************************/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			getData(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Module getModule() {
		return new ShuttlesModule2();
	}

	@Override
	public boolean isModuleHomeActivity() {
		return true;
	}

	@Override
	protected void prepareActivityOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, Menu.NONE, "Refresh").setIcon(
				R.drawable.menu_refresh);
	}

}
