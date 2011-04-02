package edu.mit.mitmobile2.shuttles;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

public class MITShuttleSmartActivity extends ModuleActivity {

	public static final int NUM_LOCATIONS = 5;
	
	private ShuttleSmartAsyncListView shuttleSmartAsyncListView;
	
	SharedPreferences pref;
	
	Context ctx;
	
	/****************************************************/
   
	@Override
    public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		ctx = this;
       
		pref = getSharedPreferences(Global.PREFS_SHUTTLES,Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	createView();


	}

    void createView() {

    	shuttleSmartAsyncListView = new ShuttleSmartAsyncListView(ctx);
		setContentView(shuttleSmartAsyncListView);

		//FIXME: The following handler and corresponding fetch call is only necessary because it is the only way to get stop titles since I wasn't allowed as a student to change the web service API.  
		final Handler myHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.arg1 == MobileWebApi.SUCCESS) {
					shuttleSmartAsyncListView.lb.setLastLoaded(new Date());
					shuttleSmartAsyncListView.lb.endLoading();
					shuttleSmartAsyncListView.getData();
				}
				else{
					Toast.makeText(ctx, MobileWebApi.NETWORK_ERROR, Toast.LENGTH_LONG).show();
					shuttleSmartAsyncListView.lb.errorLoading();
				}
			}
		};
		shuttleSmartAsyncListView.lb.startLoading();
		if (ShuttleModel.getSortedRoutes().size() == 0)
		{
			ShuttleModel.fetchRoutesAndDetails(ctx, myHandler, true);
		}
		else
		{
			shuttleSmartAsyncListView.getData();
		}

    }	
    

	/****************************************************/	
	@Override
	protected void prepareActivityOptionsMenu(Menu menu) {
//		menu.add(0, MENU_REFRESH, Menu.NONE, "Refresh")
//		  .setIcon(R.drawable.menu_refresh);	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
		return true;
	}

	@Override
    protected void onPause() {
		super.onPause();
		if (shuttleSmartAsyncListView!=null) shuttleSmartAsyncListView.terminate();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (shuttleSmartAsyncListView!=null) shuttleSmartAsyncListView.terminate();
	}
}
