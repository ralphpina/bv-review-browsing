package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.bazaarvoice.BazaarRequest;
import com.bazaarvoice.DisplayParams;
import com.bazaarvoice.OnBazaarResponse;
import com.bazaarvoice.types.Equality;
import com.bazaarvoice.types.RequestType;

public class MainActivity extends Activity implements OnBazaarResponse {

	private static final String TAG = "MainActivity";
	
	private TextView textView;
	private Map<JSONObject, LinkedList<String>> categoryTree;
	private Map<String, JSONObject> allCategories;
	
	
	private int initialItemCount;
	private int processedItemCount;
	private int batchSize;
	
	private JSONArray itemsToProcess;
	
	private BazaarRequest request;
	private DisplayParams params;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textView = (TextView) findViewById(R.id.textView);
		textView.setMovementMethod(new ScrollingMovementMethod());
		categoryTree = new LinkedHashMap<JSONObject, LinkedList<String>>();
		allCategories = new LinkedHashMap<String, JSONObject>();
		initialItemCount = 0;
		processedItemCount = 0;
		batchSize = 100;
		
		/*
		 * BazaarRequest will be used to make our API calls. The parameters are set in the BVReviewBrowsingApplication class
		 */
		request = new BazaarRequest(BVReviewBrowsingApplication.domain, BVReviewBrowsingApplication.passKey, BVReviewBrowsingApplication.apiVersion);
		params = new DisplayParams();
		params.setLimit(batchSize);
		
		/*
		 * Get all the top level categories and sort on their name
		 */
		params.addFilter("ParentId", Equality.EQUAL, "null");
		params.addSort("Name", true);
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, this);
		
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onException(String message, Throwable exception) {
		Log.e(TAG, "Error = "+ message + "\n");
		exception.printStackTrace();
	}

	@Override
	public void onResponse(JSONObject data) {
		final JSONObject json = data;
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					//TODO make this a dialog box
					if (json.getBoolean("HasErrors")) {
						String hasErrors = "JSON response has errors";
						JSONArray errors = json.getJSONArray("Errors");
						StringBuilder errorList = new StringBuilder();
						
						for (int i = 0; i < errors.length(); i++) {
							errorList.append(errors.get(i));
						}
						
						textView.setText(hasErrors + " : " + errorList.toString());
						
					} else {
						/*
						 * If this is the first time this request is being called, 
						 * get the total number of results we will need to iterate.
						 */
						Log.e(TAG, "initialItemCount in onResponse = " + initialItemCount);
						if (initialItemCount == 0) {
							initialItemCount = json.getInt("TotalResults");
						}
						
						itemsToProcess = json.getJSONArray("Results");
						parseAndDisplayItems();
						
					}
				} catch (JSONException e) {
					Log.e(TAG, "Error = "+ e.getMessage() + "\n");
					e.printStackTrace();
				}
			}
		});
	}
	
	private void parseAndDisplayItems() {
		/*
		 * Loop to get all the categories and dump them into our map
		 */
		Log.e(TAG, "initialItemCount = " + initialItemCount);
		int itemNum;
		for (int i = 0; i < initialItemCount; i++) {
			itemNum = i % batchSize;
			Log.e(TAG, "itemNum = " + itemNum);
			if (itemNum < itemsToProcess.length()) {
				try {
					Log.e(TAG, "item name = " + itemsToProcess.getJSONObject(itemNum).getString("Name"));
					allCategories.put(itemsToProcess.getJSONObject(itemNum).getString("Id"), itemsToProcess.getJSONObject(itemNum));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				processedItemCount++;
			} else if (itemsToProcess.length() == batchSize) {
				params.setOffset(processedItemCount);
				request.sendDisplayRequest(RequestType.CATEGORIES, params, this);
			}
		}
		
		Log.e(TAG, "allCategories.size() = " + allCategories.size());
		
		StringBuilder displayItems = new StringBuilder();
		
		for (String s : allCategories.keySet()) {
				displayItems.append(s + "\n");
		}
		
		textView.setText(displayItems.toString());
	}

}
