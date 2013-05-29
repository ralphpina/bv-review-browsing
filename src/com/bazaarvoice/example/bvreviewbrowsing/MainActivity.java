package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bazaarvoice.BazaarRequest;
import com.bazaarvoice.DisplayParams;
import com.bazaarvoice.OnBazaarResponse;
import com.bazaarvoice.types.Equality;
import com.bazaarvoice.types.RequestType;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private TextView textView;
	private Map<String, JSONObject> allCategories;
	private Map<String, JSONObject> allProducts;
	private ArrayList<String> topCategoryIds;
	private JSONArray itemsToProcess;
	
	
	private int initialItemCount;
	private int processedItemCount;
	private int batchSize;
	private String selectionID;
	
	private BazaarRequest request;
	private DisplayParams params;
	
	RelativeLayout.LayoutParams layoutParams;
	RelativeLayout relativeLayout;
	LinearLayout linearLayout;
	
	private Activity context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
	
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		textView = (TextView) findViewById(R.id.textView);
		
		textView.setText("Yo! We are loading your data!");
		//textView.setMovementMethod(new ScrollingMovementMethod());

		layoutParams = (RelativeLayout.LayoutParams)textView.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
		textView.setLayoutParams(layoutParams);
		
		allCategories = new LinkedHashMap<String, JSONObject>();
		initialItemCount = 0;
		processedItemCount = 0;
		batchSize = 100;

		selectionID = null;
		
		Log.e(TAG, "before going into execParseTopCategories");
		execParseTopCategories();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void parseTopCategories(OnBazaarResponse response) {
		
		Log.e(TAG, "entered parseTopCategories");
		
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
		Log.e(TAG, "parseTopCategories : selectionID = " + selectionID);
		
		if (selectionID == null) {
			params.addFilter("ParentId", Equality.EQUAL, "null");
		} else {
			params.addFilter("ParentId", Equality.EQUAL, selectionID);
		}
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
	}
	
	private void execParseTopCategories() {
		
		Log.e(TAG, "entered execParseTopCategories");
		
		/*
		 * We will call the method and pass it the listener to execute when the
		 * request's data returns
		 */
		parseTopCategories(new BazaarUIThreadResponse(this) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				Log.e(TAG, "execParseTopCategories : onUiResponse");
				try {
					if (initialItemCount == 0) {
						initialItemCount = response.getInt("TotalResults");
					}
					itemsToProcess = response.getJSONArray("Results");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				JSONObject currentObj;
				JSONArray children;
				topCategoryIds = new ArrayList<String>();
				
				for (int i = 0; i < itemsToProcess.length(); i++) {
					try {
						currentObj = itemsToProcess.getJSONObject(i);
						children = new JSONArray();
						currentObj.put("Children", children);
						currentObj.put("HasChildren", false);
						//put the item into our flat map with all categories
						allCategories.put(currentObj.getString("Id"), currentObj);
						//put the items into our arraylist to then query their children
						topCategoryIds.add(currentObj.getString("Id"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
					processedItemCount++;
				}
				
				Log.e(TAG, "execParseTopCategories : processedItemCount = " + processedItemCount);
				
				if (initialItemCount > processedItemCount) {
					execParseTopCategories();
				} else {
					initialItemCount = 0;
					processedItemCount = 0;
					execParseChildrenCategories();
				}
			}
		});	
		
	}
	
	private void parseChildrenCategories(OnBazaarResponse response) {
		
		Log.e(TAG, "entered parseChildrenCategories");
		 
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
		
		Log.e(TAG, "parseChildrenCategories : selectionID = " + selectionID);
		
		String[] ids = new String[topCategoryIds.size()];
		topCategoryIds.toArray(ids);
		
		//TODO remove
		Log.e(TAG, "parseChildrenCategories: ids = " + Arrays.toString(ids));
		
		params.addFilter("ParentId", Equality.EQUAL, ids);
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
		
	}
	
	private void execParseChildrenCategories() {
		
		parseChildrenCategories(new BazaarUIThreadResponse(this) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				try {
					if (initialItemCount == 0) {
						initialItemCount = response.getInt("TotalResults");
					}
					itemsToProcess = response.getJSONArray("Results");
					
					//if there are no results means that these items have to child categories
					//they just have products
					if (itemsToProcess == null) {
						execParseProducts();
						
					//go get the child categories
					} else { 
						
						//TODO remove
						Log.e(TAG, "execParseChildrenCategories: itemsToProcess = " + itemsToProcess);
						Log.e(TAG, "processedItemCount in  execParseChildrenCategories = " + processedItemCount);					
						
						JSONObject currentObj;
						JSONArray children;
						JSONObject parent;
						for (int i = 0; i < itemsToProcess.length(); i++) {
							
							currentObj = itemsToProcess.getJSONObject(i);
							children = new JSONArray();
							currentObj.put("Children", children);
							currentObj.put("HasChildren", false);
							allCategories.put(currentObj.getString("Id"), currentObj);
							
							parent = allCategories.get(currentObj.getString("ParentId"));
							parent.put("HasChildren", true);
							children = parent.getJSONArray("Children");
							children.put(currentObj.getString("Id"));
											
							processedItemCount++;
						}
						
						Log.e(TAG, "processedItemCount in  execParseChildrenCategories = " + processedItemCount);
						
						if (initialItemCount > processedItemCount) {
							execParseChildrenCategories();
						} else {
							initialItemCount = 0;
							processedItemCount = 0;
							displayCategories();
						}
						
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		});	
	}
	
	private void parseProducts(OnBazaarResponse response) {
		
		Log.e(TAG, "entered parseProducts");
		 
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
		
		Log.e(TAG, "parseChildrenCategories : selectionID = " + selectionID);
		
		String[] ids = new String[topCategoryIds.size()];
		topCategoryIds.toArray(ids);
		
		//TODO remove
		Log.e(TAG, "parseChildrenCategories: ids = " + Arrays.toString(ids));
		
		params.addFilter("ParentId", Equality.EQUAL, ids);
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
		
	}
	
	private void execParseProducts() {
		
		parseProducts(new BazaarUIThreadResponse(this) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				
			}
		});
	}
	
	
	@SuppressLint("NewApi")
	private void displayCategories() {
		//boolean hasChildren;
		JSONArray children;
		TextView newTextView;
		JSONObject obj;
		
		Log.e(TAG, "entered displayCategories()");
		
		//hide other textView 
		this.relativeLayout.removeView(textView);
		
		//layout parameters for my new TextViews
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		/*
		String parent;
		if (selectionID == null) {
			parent = "null";
		} else {
			parent = selectionID;
		}
		*/
		
		for (String topId : topCategoryIds) {
			try {
				obj = allCategories.get(topId);
				newTextView = new TextView(this);
				newTextView.setLayoutParams(layoutParams);
				newTextView.setText(obj.getString("Name"));
				this.linearLayout.addView(newTextView);
				
				children = obj.getJSONArray("Children");
				
				for (int i = 0; i < children.length(); i++) {
					String childID = (String) children.get(i);
					final JSONObject child = allCategories.get(childID);
					
					newTextView = new TextView(this);
					newTextView.setLayoutParams(layoutParams);
					newTextView.setText("       " + child.getString("Name"));
					newTextView.setOnClickListener(new OnClickListener() {
						String idClicked = child.getString("Id");
						@Override
						public void onClick(View v) {
							selectionID = idClicked;
							//change the dispplay
							linearLayout.removeAllViews();
							relativeLayout.addView(textView);
							
							initialItemCount = 0;
							processedItemCount = 0;								
							execParseTopCategories();
							Toast.makeText(context, "name = " + ((TextView) v).getText() + " and id = " + idClicked, Toast.LENGTH_LONG).show();
						}
					});
					this.linearLayout.addView(newTextView);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		/*
		for (JSONObject obj : allCategories.values()) {
			try {
				
				if (parent.equals(obj.getString("ParentId"))) {
				
					hasChildren = obj.getBoolean("HasChildren");
					
					if (hasChildren) {
						newTextView = new TextView(this);
						newTextView.setLayoutParams(layoutParams);
						newTextView.setText(obj.getString("Name"));
						this.linearLayout.addView(newTextView);
						
						children = obj.getJSONArray("Children");
						
						for (int i = 0; i < children.length(); i++) {
							String childID = (String) children.get(i);
							final JSONObject child = allCategories.get(childID);
							
							newTextView = new TextView(this);
							newTextView.setLayoutParams(layoutParams);
							newTextView.setText("       " + child.getString("Name"));
							newTextView.setOnClickListener(new OnClickListener() {
								String idClicked = child.getString("Id");
								@Override
								public void onClick(View v) {
									selectionID = idClicked;
									//change the dispplay
									linearLayout.removeAllViews();
									relativeLayout.addView(textView);
									
									initialItemCount = 0;
									processedItemCount = 0;								
									execParseTopCategories();
									Toast.makeText(context, "name = " + ((TextView) v).getText() + " and id = " + idClicked, Toast.LENGTH_LONG).show();
								}
							});
							this.linearLayout.addView(newTextView);
						}
						
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		//relativeLayout.refreshDrawableState();
	}
	
	public void loadSelection(View v) {
		
	}

}
