package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bazaarvoice.BazaarRequest;
import com.bazaarvoice.DisplayParams;
import com.bazaarvoice.OnBazaarResponse;
import com.bazaarvoice.types.Equality;
import com.bazaarvoice.types.RequestType;

public class MainActivity extends Activity {

	//private static final String TAG = "MainActivity";
	
	private TextView textView;
	private Map<String, JSONObject> allCategories;
	
	
	private int initialItemCount;
	private int processedItemCount;
	private int batchSize;
	
	private JSONArray itemsToProcess;
	
	private BazaarRequest request;
	private DisplayParams params;
	
	RelativeLayout.LayoutParams layoutParams;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		textView = (TextView) findViewById(R.id.textView);
		textView.setText("Yo! We are loading your data!");
		textView.setMovementMethod(new ScrollingMovementMethod());

		layoutParams = (RelativeLayout.LayoutParams)textView.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
		textView.setLayoutParams(layoutParams);
		
		allCategories = new LinkedHashMap<String, JSONObject>();
		initialItemCount = 0;
		processedItemCount = 0;
		batchSize = 100;

		execParseTopCategories();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void parseTopCategories(OnBazaarResponse response) {
		/*
		 * BazaarRequest will be used to make our API calls. The parameters are set in the BVReviewBrowsingApplication class
		 */
		request = new BazaarRequest(BVReviewBrowsingApplication.domain, BVReviewBrowsingApplication.passKey, BVReviewBrowsingApplication.apiVersion);
		
		/*
		 * Get all the top level categories and sort on their name
		 */
		params = new DisplayParams();
		params.setOffset(processedItemCount);
		params.setLimit(batchSize);
		params.addSort("Name", true);
		params.addFilter("ParentId", Equality.EQUAL, "null");
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
	}
	
	private void execParseTopCategories() {
		
		initialItemCount = 0;
		processedItemCount = 0;
		
		/*
		 * We will call the method and pass it the listener to execute when the
		 * request's data returns
		 */
		parseTopCategories(new BazaarUIThreadResponse(this) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				try {
					itemsToProcess = response.getJSONArray("Results");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				JSONObject currentObj;
				JSONArray children;
				for (int i = 0; i < itemsToProcess.length(); i++) {
					try {
						currentObj = itemsToProcess.getJSONObject(i);
						children = new JSONArray();
						currentObj.put("Children", children);
						currentObj.put("HasChildren", false);
						allCategories.put(currentObj.getString("Id"), currentObj);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
					processedItemCount++;
				}
				
				if (initialItemCount > processedItemCount) {
					execParseTopCategories();
				} else {
					execParseChildrenCategories();
				}
			}
		});	
		
	}
	
	private void parseChildrenCategories(OnBazaarResponse response) {
		 
		/*
		 * BazaarRequest will be used to make our API calls. The parameters are set in the BVReviewBrowsingApplication class
		 */
		request = new BazaarRequest(BVReviewBrowsingApplication.domain, BVReviewBrowsingApplication.passKey, BVReviewBrowsingApplication.apiVersion);
		
		/*
		 * Get all the top child categories
		 */
		params = new DisplayParams();
		params.setOffset(processedItemCount);
		params.setLimit(batchSize);
		ArrayList<String> topCategoryIds = new ArrayList<String>();
		
		for (JSONObject id : allCategories.values()) {
			try {
				topCategoryIds.add(id.getString("Id"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String[] ids = new String[topCategoryIds.size()];
		topCategoryIds.toArray(ids);
		
		params.addFilter("ParentId", Equality.EQUAL, ids);
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
		
	}
	
	private void execParseChildrenCategories() {
		
		initialItemCount = 0;
		processedItemCount = 0;
		
		parseChildrenCategories(new BazaarUIThreadResponse(this) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				try {
					itemsToProcess = response.getJSONArray("Results");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				JSONObject currentObj;
				JSONArray children;
				JSONObject parent;
				for (int i = 0; i < itemsToProcess.length(); i++) {
					try {
						currentObj = itemsToProcess.getJSONObject(i);
						children = new JSONArray();
						currentObj.put("Children", children);
						currentObj.put("HasChildren", false);
						allCategories.put(currentObj.getString("Id"), currentObj);
						
						parent = allCategories.get(currentObj.getString("ParentId"));
						parent.put("HasChildren", true);
						children = parent.getJSONArray("Children");
						children.put(currentObj.getString("Id"));
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
					processedItemCount++;
				}
				
				if (initialItemCount > processedItemCount) {
					execParseChildrenCategories();
				} else {
					displayCategories();
				}
			}
		});	
		
	}
	
	//TODO remove this
	@SuppressLint("NewApi")
	private void displayCategories() {
		StringBuilder tree = new StringBuilder();
		boolean hasChildren;
		JSONArray children;
		
		for (JSONObject obj : allCategories.values()) {
			try {
				hasChildren = obj.getBoolean("HasChildren");
				
				if (hasChildren) {
					tree.append(obj.getString("Name") + "\n");
					children = obj.getJSONArray("Children");
					
					for (int i = 0; i < children.length(); i++) {
						String childID = (String) children.get(i);
						JSONObject child = allCategories.get(childID);
						tree.append("       " + child.getString("Name") + "\n");
					}
					
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
		textView.setLayoutParams(layoutParams);
		textView.setText(tree.toString());
	}

}
