package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.mitmobile2.LoaderBar;
import edu.mit.mitmobile2.MobileWebApi;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.objs.RouteItem;
import edu.mit.mitmobile2.objs.RouteItem.Stops;
import edu.mit.mitmobile2.objs.ShuttleSmart_Predicted;
import edu.mit.mitmobile2.shuttles.ShuttleSmartRouteArrayAdapter.SectionListItemView;

public class ShuttleSmartAsyncListView  extends LinearLayout implements OnItemClickListener {

	ArrayList<ArrayList<Stops>> m_stops;
	MITShuttleSmartActivity top;
	CheckStopsTask stopsTask;
	ListView shuttlesmart_stopsLV;
	ShuttleSmartRouteArrayAdapter adapter;
	LocationManager lm;
	LoaderBar lb;
	ArrayList<String> sids;
	Context ctx;
	
	public static long unixTimeParser(long unixTime)
	{
		// add current time if mins < -1000000
		// no prediction if -1000000 < mins < 0
		// prediction if 0 < mins < 30 
		// no prediction if mins > 30

		long curTime = System.currentTimeMillis();
		long mins = (unixTime * 1000 - curTime) / 1000 / 60;
		
		if (mins < -1000000)
		{
			return mins+curTime/1000/60;
		}
		else if (mins < 0)
		{
			return -1;
		}
		else if (mins < 30)
		{
			return mins;
		}
		else 
		{
			return -1;
		}
	}

	/****************************************************/
	class CheckStopsTask extends AsyncTask<Void, Void, Void> {

		StopsParser sp;
		RoutesParser rp;
		
		boolean firstTime = true;

		@SuppressWarnings("unchecked")
		protected Void doInBackground(Void... v) {

			while (true) {

				if (isCancelled())
				{
					return null;
				}
				
				// Update stops...
				RoutesParser rparser = new RoutesParser();
				Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				sids = (ArrayList<String>) ShuttleModel.getClosestStopIds(location.getLatitude(), location.getLongitude());
				m_stops = new ArrayList<ArrayList<Stops>>();
				Log.e("sizes: ", sids.size()+"");
				int stopsAdded = 0;
				for (String sid : sids)
				{
					sp = new StopsParser();
					sp.getJSON(rparser.getBaseUrl()+"?command=stopInfo&id="+sid, true);
					ArrayList<Stops> stops = (ArrayList<Stops>) sp.items;
					int stopCount = stops.size();
//					long next;
					for (Stops s : stops)
					{
						s.next = unixTimeParser(s.next);
						ArrayList<Integer> predictions = new ArrayList<Integer>();
						for (long l : s.predictions)
						{
							predictions.add((int)unixTimeParser(l));
						}
						s.predictions = predictions;
						if (s.next == -1)
						{
							stopCount -= 1;
						}
					}
					if (stopCount > 0)
					{
						m_stops.add((ArrayList<Stops>) sp.items);
						stopsAdded += 1;
					}
					if (stopsAdded == MITShuttleSmartActivity.NUM_LOCATIONS)
					{
						break;
					}
				}

				publishProgress((Void) null);

				// Sleep...
				try {
					Thread.sleep(1000 * 30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

	    }

	    @Override
		protected void onProgressUpdate(Void... values) {
			
			super.onProgressUpdate(values);
			
			lb.setLastLoaded(new Date());
			lb.endLoading();
			 
			boolean no_data = false;
	    	if (sp==null) no_data = true;
	    	else if (sp.items.size() == 0) no_data = true;
	    	if (no_data) {
	    		if(m_stops.size() == 0) {
	    			Toast.makeText(ctx, MobileWebApi.NETWORK_ERROR, Toast.LENGTH_LONG).show();
	    			lb.errorLoading();
	    		}
	    		return;
	    	}
	    	 
	    	 if (!m_stops.isEmpty()) {

				 ArrayList<Stops> s;
	    			
	    		 // Initialize...
	    		 if (firstTime) {
	    			 	
					 	SectionListItemView itemBuilder = new SectionListItemView() {
					 		
						public View getView(Object item, View convertView, ViewGroup parent) {

							View v = convertView;
							if (v == null) {
								LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								v = vi.inflate(R.layout.shuttlesmart_stops_row, null);
							}

							ShuttleSmart_Predicted s = (ShuttleSmart_Predicted) item;

							String preds = "";
							
							for (long l : s.predictions) {
								String text = null;
								text = String.valueOf(l);
								preds = preds.concat(text+", ");
							}
							if (!preds.equals("-1, "))
							{
								preds = preds.substring(0, preds.length()-2).concat(" min");
							}
							else
							{
								preds = "No prediction";
							}
							
							TextView minsTV = (TextView) v.findViewById(R.id.shuttlesmart_stopsRowMinsTV);
							minsTV.setText(preds);
							
							TextView routeTV = (TextView) v.findViewById(R.id.shuttlesmart_stopsRowRouteTV);
							routeTV.setText(s.route_title);

							return v;

						}
						};
						
						adapter = new ShuttleSmartRouteArrayAdapter(ctx, itemBuilder);
	    			 
	    			 
	    			firstTime = false;
	    		}
	    		 
	    		adapter.clear();

	    		//Gets route titles.
				HashMap<String, String> routeTitles = new HashMap<String, String>();
				for (RouteItem aRouteItem : ShuttleModel.getSortedRoutes()) {
					routeTitles.put(aRouteItem.route_id, aRouteItem.title);
				}

				ArrayList<ArrayList<ShuttleSmart_Predicted>> sections = new ArrayList<ArrayList<ShuttleSmart_Predicted>>();
//				ArrayList<String> stopids = new ArrayList<String>();

	    		// Update
				ShuttleSmart_Predicted pi;
				
	    		for (int x=0; x<m_stops.size(); x++) {
	    			sections.add(new ArrayList<ShuttleSmart_Predicted>());
					s = m_stops.get(x);
	    			
					//Annoying way to get stop title because it's not provided in http://m.mit.edu/api/shuttles/?command=stopInfo&id=mass84_d
					RouteItem ri = ShuttleModel.getRoute(s.get(0).route_id);
					
					String stop_title = "";
					for (Stops stop : ri.stops)
					{
						if (stop.id.equals(s.get(0).id))
						{
							stop_title = stop.title;
							break;
						}
					}
    				
	    			for (Stops p : s) {
	    				pi = new ShuttleSmart_Predicted();
	    				pi.stop_id = p.id;
	    				pi.route_id = p.route_id;
	    				pi.predictions = new long[Math.min(p.predictions.size()+1, 3)];
	    				pi.predictions[0] = p.next;
	    				pi.route_title = routeTitles.get(pi.route_id);
	    				pi.stop_title = stop_title;
	    				for (int i=0; i<Math.min(p.predictions.size(),2); i++)
	    				{
	    					pi.predictions[i+1] = p.predictions.get(i);
	    				}
	    				sections.get(x).add(pi);
	    			}

	    		}	 // for stops

	    		// Reorganize results
				ArrayList<ArrayList<ShuttleSmart_Predicted>> smart_sections = new ArrayList<ArrayList<ShuttleSmart_Predicted>>();
    			ArrayList<ArrayList<ShuttleSmart_Predicted>> first_sections = new ArrayList<ArrayList<ShuttleSmart_Predicted>>();
    			ArrayList<ArrayList<ShuttleSmart_Predicted>> last_sections = new ArrayList<ArrayList<ShuttleSmart_Predicted>>();
    			
    			long curTime = System.currentTimeMillis();
    			
	    		for (ArrayList<ShuttleSmart_Predicted> preds : sections) {
	    			ArrayList<ShuttleSmart_Predicted> smart_preds = new ArrayList<ShuttleSmart_Predicted>();
	    			ArrayList<ShuttleSmart_Predicted> first_preds = new ArrayList<ShuttleSmart_Predicted>();
	    			ArrayList<ShuttleSmart_Predicted> last_preds = new ArrayList<ShuttleSmart_Predicted>();
	    			int predCount = preds.size();
	    			for (ShuttleSmart_Predicted pred : preds)
	    			{
    					// If no prediction, move to back of list.
    					if (pred.predictions[0] == -1)
    					{
    						last_preds.add(pred);
    						predCount -= 1;
    					}
    					else
    					{
    						first_preds.add(pred);
    					}
	    			}
	    			smart_preds.addAll(first_preds);
	    			smart_preds.addAll(last_preds);
	    			if (predCount == 0)
	    			{
	    				last_sections.add(smart_preds);
	    			}
	    			else
	    			{
	    				first_sections.add(smart_preds);
	    			}
	    		}
	    		smart_sections.addAll(first_sections);
	    		smart_sections.addAll(last_sections);

				// Add to adapter...
				for (ArrayList<ShuttleSmart_Predicted> stop : smart_sections) {
	    			adapter.addSection(stop.get(0).stop_title, stop);
	    		}
				shuttlesmart_stopsLV.setOnItemClickListener(ShuttleSmartAsyncListView.this);
				shuttlesmart_stopsLV.setVisibility(VISIBLE);
				shuttlesmart_stopsLV.setAdapter(adapter);
//				adapter.notifyDataSetChanged();
	    		 
	    	}  // isEmpty
		    	
	    }  // progressUpdate
	
	}  // class CheckStopsTask 
	
	/****************************************************/
	void terminate() {
		
		if (stopsTask!=null) {
			boolean isCanceled;
			isCanceled = stopsTask.cancel(true);
			while (!isCanceled) {
				 // Sleep...
				 try {
					 Thread.sleep(1000*10);
				 } catch (InterruptedException e) {
					 e.printStackTrace();
				 }
				isCanceled = stopsTask.cancel(true);
			}
			stopsTask = null;
		}
		
	}
	
	/**
	 * @param stops **************************************************/

	public ShuttleSmartAsyncListView(Context context) {
		
		super(context);

		ctx = context;
		
		top = (MITShuttleSmartActivity) context;
		
		lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
	    LocationListener locationListenerGps = new LocationListener() {
	        public void onLocationChanged(Location location)
	        {
	        	sids = (ArrayList<String>) ShuttleModel.getClosestStopIds(location.getLatitude(), location.getLongitude());
	        }
	        public void onProviderDisabled(String provider) {}
	        public void onProviderEnabled(String provider) {}
	        public void onStatusChanged(String provider, int status, Bundle extra) {}
	    };
	    
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerGps);
		}
		
		LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		LinearLayout topView = (LinearLayout) vi.inflate(R.layout.shuttlesmart_stops, null);

		shuttlesmart_stopsLV = (ListView) topView.findViewById(R.id.shuttlesmart_stopsLV);

        Display display = top.getWindowManager().getDefaultDisplay(); 
        int height = display.getHeight();
        topView.setMinimumHeight(height-30);
        
		lb = new LoaderBar(ctx);
		topView.addView(lb);
		
		addView(topView);
	}
	/****************************************************/
	void getData() {
		lb.startLoading();
		
		m_stops = new ArrayList<ArrayList<Stops>>();

		if (stopsTask!=null) {
			if (!stopsTask.isCancelled()) {
				stopsTask.cancel(true);
				//throw new RuntimeException("should have been canceled");
			}
		}
		
		stopsTask = new CheckStopsTask();

		stopsTask.execute();

	}

	
	/****************************************************/

	@Override
	 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	}
}
