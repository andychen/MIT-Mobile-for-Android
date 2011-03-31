package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
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
	
//	private FullScreenLoader shuttleSmartLoader;

	protected String routeId, stopId;	//TODO: necessary?
	
	protected Stops stops;
	
	static final int MENU_REFRESH = Menu.FIRST;
	
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
    	
//        double lat = 42.350937;
//        double lon = -71.089429;
        
        double lat = 42.354758;
        double lon = -71.101858;
        
		closestStopIds = ShuttleModel.getClosestStopIds(lat, lon, 3);
    	shuttleSmartAsyncListView = new ShuttleSmartAsyncListView(ctx, closestStopIds);
		setContentView(shuttleSmartAsyncListView);

		//FIXME: The following handler and corresponding fetch call is only necessary because it is the only way to get stop titles since I wasn't allowed as a student to change the web service API.  
		final Handler myHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.arg1 == MobileWebApi.SUCCESS) {
					shuttleSmartAsyncListView.getData();
				}
				else{
					Toast.makeText(ctx, MobileWebApi.NETWORK_ERROR, Toast.LENGTH_LONG).show();
					shuttleSmartAsyncListView.lb.errorLoading();
				}
			}
		};
		shuttleSmartAsyncListView.lb.startLoading();
		ShuttleModel.fetchRoutesAndDetails(ctx, myHandler, true);

    }	
    

	/****************************************************/	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, Menu.NONE, "Refresh")
		  .setIcon(R.drawable.menu_refresh);	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH: 
			shuttleSmartAsyncListView.getData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
