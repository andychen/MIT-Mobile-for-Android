package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import edu.mit.mitmobile2.Global;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.objs.RouteItem.Stops;

public class MITShuttleSmartActivity extends ModuleActivity {
	
	private List<String> closestStopIds;
	static public ArrayList<String> stop_ids = new ArrayList<String>(); //TODO: necessary?

	private ShuttleSmartAsyncListView shuttleSmartAsyncListView;
	private ShuttleSmartRouteArrayAdapter adapter;

	protected String routeId, stopId;	//TODO: necessary?
	
	protected Stops stops;
 
	static final int MENU_VIEW_MAP = Menu.FIRST;
	
	SharedPreferences pref;
	
	/****************************************************/
   
	@Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);

//    	Bundle extras = getIntent().getExtras();
//
//        if (extras!=null){ 
//        	routeId = extras.getString(ShuttleModel.KEY_ROUTE_ID);
//        	stopId = extras.getString(ShuttleModel.KEY_STOP_ID);
//        }
        
//		mStops = ShuttleModel.getRoute(routeId).stops;
//		last_pos = ShuttleModel.getStopPosition(mStops, stopId);
       
		pref = getSharedPreferences(Global.PREFS_SHUTTLES,Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE); 
		
    	setTitle("MIT Stops"); // TODO: does nothing?
    	
    	createView();

	}

    void createView() {
    	
//		setContentView(R.layout.shuttlesmart_stops); //TODO: place at end of code?
//		shuttleSmartAsyncListView = (ShuttleSmartAsyncListView) findViewById(R.id.shuttlesmart_stopsLV);
//		shuttleSmartLoader = (FullScreenLoader) findViewById(R.id.shuttleSmartLoader);

//		getData(false);

        double lat = 42.350937;
        double lon = -71.089429;
		closestStopIds = ShuttleModel.getClosestStopIds(lat, lon, 5);
		
		shuttleSmartAsyncListView = new ShuttleSmartAsyncListView(this, closestStopIds);
		setContentView(shuttleSmartAsyncListView);
//		shuttleSmartAsyncListView = (ShuttleSmartAsyncListView) findViewById(R.id.shuttleLV);

//		adapter = new ShuttleSmartRouteArrayAdapter(this,
//				itemBuilder);
//
//		for (String s: closestStops.keySet()){
//			adapter.addSection(s, closestStops.get(s));
//		}
//
//		shuttleSmartAsyncListView.setVisibility(View.VISIBLE);
//		shuttleSmartAsyncListView.setAdapter(adapter);
    }	
    

	/****************************************************/	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) {
		menu.add(0, MENU_VIEW_MAP, Menu.NONE, "View on Map")
		  .setIcon(R.drawable.menu_view_on_map);		
	}
	
	@Override
	protected Module getModule() {
		return new ShuttleSmartModule();
	}
	
	@Override
	public boolean isModuleHomeActivity() {
		return false;
	}
}
