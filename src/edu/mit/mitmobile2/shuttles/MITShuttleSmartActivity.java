package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import edu.mit.mitmobile2.FullScreenLoader;
import edu.mit.mitmobile2.Global;
import edu.mit.mitmobile2.MobileWebApi;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.ModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.objs.RouteItem.Stops;

public class MITShuttleSmartActivity extends ModuleActivity {
	
	private List<String> closestStopIds;
	static public ArrayList<String> stop_ids = new ArrayList<String>(); //TODO: necessary?

	private ShuttleSmartAsyncListView shuttleSmartAsyncListView;
//	private ShuttleSmartRouteArrayAdapter adapter;
	
	private FullScreenLoader shuttleSmartLoader;

	protected String routeId, stopId;	//TODO: necessary?
	
	protected Stops stops;
 
	static final int MENU_VIEW_MAP = Menu.FIRST;
	
	SharedPreferences pref;
	
	Context ctx;
	
	/****************************************************/
   
	@Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		ctx = this;

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
    	
        double lat = 42.350937;
        double lon = -71.089429;
        
		closestStopIds = ShuttleModel.getClosestStopIds(lat, lon, 3);
    	shuttleSmartAsyncListView = new ShuttleSmartAsyncListView(ctx, closestStopIds);
		setContentView(shuttleSmartAsyncListView); //TODO: place at end of code?
//		shuttleSmartLoader = (FullScreenLoader) findViewById(R.id.shuttlesmartLoader);
//		shuttleSmartLoader.showLoading();

		//FIXME: The following handler and corresponding fetch call is only necessary because it is the only way to get stop titles since I wasn't allowed as a student to change the web service API.  
		final Handler myHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.arg1 == MobileWebApi.SUCCESS) {
//					shuttleSmartLoader.setVisibility(View.GONE); //TODO: unnecessary?
					shuttleSmartAsyncListView.getData();
				}
			}
		};
		
		ShuttleModel.fetchRoutesAndDetails(ctx, myHandler, true);
		

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
