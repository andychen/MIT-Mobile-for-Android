package edu.mit.mitmobile2.shuttles;

import android.app.Activity;
import edu.mit.mitmobile2.Module;
import edu.mit.mitmobile2.R;

public class ShuttlesModule2 extends Module {

	@Override
	public String getLongName() {
		return "ShuttleSmart";
	}

	@Override
	public String getShortName() {
		return "ShuttleSmart";
	}
	
	@Override
	public Class<? extends Activity> getModuleHomeActivity() {
		return ShuttlesActivity2.class;
	}

	@Override
	public int getMenuIconResourceId() {
		return R.drawable.menu_shuttles;
	}

	@Override
	public int getHomeIconResourceId() {
		return R.drawable.home_shuttles2;
	}
}
