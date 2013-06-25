package com.bazaarvoice.example.bvreviewbrowsing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.util.Log;

import com.bazaarvoice.types.ApiVersion;

public class BVReviewBrowsingApplication extends Application {
	
	/*
	 * To get an API key and domain please visit http://developer.bazaarvoice.com
	 */
	protected static final String domain = "";
	protected static final String passKey = "";
	protected static final ApiVersion apiVersion = ApiVersion.FIVE_FOUR;
	
	/*
	 * singleton to control and track navigation
	 */
	protected static final NavUtility navUtility = new NavUtility();
	
	@Override
	public void onCreate() {
		
		Log.i("Application", "entered onCreate");
		
		JSONObject topCategory = new JSONObject();
		try {
			topCategory.put("Id", "null");
			JSONArray children = new JSONArray();
			topCategory.put("Children", children);
			topCategory.put("HasChildren", false);
			navUtility.allCategories.put("null", topCategory);
			
			Log.i("Application", navUtility.allCategories.get("null").getString("Id"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
