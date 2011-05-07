package edu.mit.mitmobile2.shuttles;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.mitmobile2.Global;
import edu.mit.mitmobile2.JSONParser;
import edu.mit.mitmobile2.objs.RouteItem.Stops;

public class ClosestStopsParser extends JSONParser {

	public String ROUTES_BASE_URL = "http://" + Global.getMobileWebDomain() + "/api/shuttles/";
	
	public String getBaseUrl() {
		return ROUTES_BASE_URL;
	}
	
	
	public ClosestStopsParser()
	{
		items = new ArrayList<ArrayList<Stops>>();
	}

	/****************************************/
	@SuppressWarnings("unchecked")
	@Override
	protected
	void parseObj(){
		
        try {

            JSONArray jClosestStops = jItem.optJSONArray("closestStops");
            
            if (jClosestStops!=null) {
            	for(int i=0; i<jClosestStops.length(); i++)
            	{
            		JSONObject jDict = jClosestStops.getJSONObject(i);
            		JSONArray jStops = jDict.optJSONArray("stops");
            		ArrayList<Stops> locationStops = new ArrayList<Stops>();
	                for(int s=0; s<jStops.length(); s++)
	                {
	                	JSONObject jStop = jStops.getJSONObject(s);
	                	
	                	Stops si = new Stops();
	                	
	                	si.id = jStop.getString("id");
	                	si.stop_title = jStop.getString("stop_title");
	                	si.route_title = jStop.getString("route_title");
	                	si.lat = jStop.getString("lat");
	                	si.lon = jStop.getString("lon");
	                	si.next = jStop.getInt("next");  // TODO long?
	
	                	//si.path = jStop.optJSONArray("path");
	                	si.direction = jStop.optString("direction");
	                	si.show_dir = jStop.optBoolean("show_dir");
	                	si.route_id = jStop.optString("route_id");
	                	si.gps = jStop.getBoolean("gps");
	                	
	                	
	                	// predicted delays
	                	JSONArray predictions = jStop.optJSONArray("predictions");
	                	if (predictions!=null) {
	                		int delay;
	                        for(int p=0; p<predictions.length(); p++)
	                        {
	                        	delay = predictions.getInt(p);
	                        	si.predictions.add(delay);
	                        }
	                	}
	                	
	                	// FIXME add Array<Array> 
	                	//r.stops.add(si);
	                	locationStops.add(si);
	                }
	                items.add(locationStops);
            	}
            }
	        
	        //JSONArray jCoords = jItem.optJSONArray("coordinate");
	        
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        
		
	}
	
	
}
