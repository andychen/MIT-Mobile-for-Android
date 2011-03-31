package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
import edu.mit.mitmobile2.LockingScrollView;
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
	LoaderBar lb;
	ArrayList<String> sids;
	Context ctx;
	
	/****************************************************/
	class CheckStopsTask extends AsyncTask<String, Void, Void> {

		StopsParser sp;
		RoutesParser rp;
		
		boolean firstTime = true;

		protected Void doInBackground(String... urls) {

			while (true) {
				
				m_stops = new ArrayList<ArrayList<Stops>>();
				
				// Update stops...
				for (int i=0; i<3; i++)	//TODO: assign 3 to a global variable
				{
					sp = new StopsParser();
					sp.getJSON(urls[i], true);
					m_stops.add((ArrayList<Stops>) sp.items);
				}
				
				// Warm up routes cache...
//				rp = new RoutesParser();
//				rp.getJSON(urls[3], true);	//TODO: assign 4 to be the above global variable + 1
				
				if (isCancelled()) {
					return null;
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
							long curTime = System.currentTimeMillis();

							// //////////
							// Mins
							String preds = "";
							
							for (long l : s.predictions) {
								long mins = (l * 1000 - curTime) / 1000 / 60;
								String text = null;
								if (mins < 0)
								{
									mins = mins+curTime/1000/60;
								}
								text = String.valueOf(mins);
								preds = preds.concat(text+", ");
							}
							preds = preds.substring(0, preds.length()-2).concat(" min");
							
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

			 	//key: route_id, value: Stop predictions 
				HashMap<String, ArrayList<ShuttleSmart_Predicted>> sections = new HashMap<String, ArrayList<ShuttleSmart_Predicted>>();

	    		// Update
				ShuttleSmart_Predicted pi;
				
	    		for (int x=0; x<m_stops.size(); x++) {
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
	   				if (!sections.containsKey(stop_title)) {
	   					sections.put(stop_title, new ArrayList<ShuttleSmart_Predicted>());
	   				}
	   				ArrayList<ShuttleSmart_Predicted> predictions = sections.get(stop_title);
	    			 
//		    		Log.d("StopsAsyncView", s.toString());

//    				pi = new ShuttleSmart_Predicted();
//    				
    				
    				Stops p;
	    			for (int z=0; z<s.size(); z++) {
	    				p = s.get(z);
	    				pi = new ShuttleSmart_Predicted();
	    				pi.stop_id = p.id;
	    				pi.route_id = p.route_id;
	    				pi.predictions = new long[p.predictions.size()+1];
	    				pi.predictions[0] = p.next;
	    				pi.route_title = routeTitles.get(pi.route_id);
	    				for (int i=0; i<p.predictions.size(); i++)
	    				{
	    					pi.predictions[i+1] = p.predictions.get(i);
	    				}
	    				predictions.add(pi);
	    			}

	    		}	 // for stops
	    		 
				
//				// Add current route first
//				String routeTitle;
//				ArrayList<ShuttleSmart_Predicted> c = sections.get(top.routeId);
//				if (c!=null) {
//					routeTitle = routeTitles.get(top.routeId);
//					if (routeTitle == null) routeTitle = top.routeId;
//					sections.remove(top.routeId);
//					adapter.addSection(routeTitle, c);
//				}

				// Add to adapter...
				String stopId = "";
				for (Entry<String, ArrayList<ShuttleSmart_Predicted>> entry : sections.entrySet()) {
	    			stopId = entry.getKey();
	    			adapter.addSection(stopId, entry.getValue());
	    			Log.e("COUNT", adapter.getCount()+"");
	    		}
				shuttlesmart_stopsLV.setOnItemClickListener(ShuttleSmartAsyncListView.this);	//TODO: Unnecessary?
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

	public ShuttleSmartAsyncListView(Context context, List<String> stopIds) {
		
		super(context);

		ctx = context;

		LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

	    LocationListener locationListenerGps = new LocationListener() {
	        public synchronized void onLocationChanged(Location location)
	        {
	        	Log.e("ANDREW", "CHEN");
	        }
	        public void onProviderDisabled(String provider) {}
	        public void onProviderEnabled(String provider) {}
	        public void onStatusChanged(String provider, int status, Bundle extra) {}
	    };
	    
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
		}


		sids = (ArrayList<String>) stopIds;
		
		top = (MITShuttleSmartActivity) context;
		
		LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		LinearLayout topView = (LinearLayout) vi.inflate(R.layout.shuttlesmart_stops, null);

		shuttlesmart_stopsLV = (ListView) topView.findViewById(R.id.shuttlesmart_stopsLV);
//		TextView titleTV = (TextView) topView.findViewById(R.id.shuttlesmart_stopsTitleTV);
//		titleTV.setText(sids.title);

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
		m_stops = new ArrayList<ArrayList<Stops>>(); //TODO: unnecessary?

		if (stopsTask!=null) {
			if (!stopsTask.isCancelled()) {
				stopsTask.cancel(true);
				//throw new RuntimeException("should have been canceled");
			}
		}

		stopsTask = new CheckStopsTask();
		RoutesParser rparser = new RoutesParser();
		stopsTask.execute(rparser.getBaseUrl()+"?command=stopInfo&id="+sids.get(0),		//TODO: Make these URL calls based on a global variable indicating how many closest stops are returned.
				rparser.getBaseUrl()+"?command=stopInfo&id="+sids.get(1),
				rparser.getBaseUrl()+"?command=stopInfo&id="+sids.get(2),
				rparser.getBaseUrl()+"?command=routes&compact=true");

	}

	
	/****************************************************/

	@Override
	 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	}
}
