package edu.mit.mitmobile2.shuttles;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.mit.mitmobile2.Global;
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
    	createView();


	}

    void createView() {

    	shuttleSmartAsyncListView = new ShuttleSmartAsyncListView(ctx);
		setContentView(shuttleSmartAsyncListView);  

		shuttleSmartAsyncListView.lb.startLoading();
		shuttleSmartAsyncListView.getData();

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
	
	@Override
	protected void onResume() {
		super.onResume();
		// FIXME
		//if (curView!=null) curView.getData();
		if (shuttleSmartAsyncListView!=null) shuttleSmartAsyncListView.getData();
	}
}
