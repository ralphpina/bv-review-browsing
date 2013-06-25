package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.bazaarvoice.BazaarRequest;
import com.bazaarvoice.DisplayParams;
import com.bazaarvoice.OnBazaarResponse;
import com.bazaarvoice.types.Equality;
import com.bazaarvoice.types.RequestType;

public class NavUtility {
	
	/*
	 * For logging to console
	 */
	private static final String TAG = "NavUtility";
	
	public final String CATEGORIES_IN_ACTIVITY = "categories_in_activity";
	public final String PRODUCT_TO_DISPLAY = "product_to_display";
	
	/*
	 * Maps to hold the items downloaded
	 */
	public Map<String, JSONObject> allCategories;
	public Map<String, JSONObject> allProducts;
	public Map<String, JSONObject> allReviews;
	
	/*
	 * The top categories in the current activity
	 */
	public ArrayList<String> topCategoryIds;
	
	/*
	 * Items that were downloaded in API response
	 */
	private JSONArray itemsToProcess;
	
	/*
	 * This is set in the first response of the current API transaction
	 */
	public int initialItemCount;
	/*
	 * Keeps count of how many of the above items were added to their respective map
	 */
	public int processedItemCount;
	
	private int batchSize;
	
	/*
	 * Category or product Id that was clicked on
	 */
	public String selectionID;
	
	/*
	 * Keeps track of the levels of the tree for the one recursive function
	 */
	public int levelsToPull;
	
	/*
	 * Used to make the API calls
	 */
	private BazaarRequest request;
	private DisplayParams params;
	
	/*
	 * Current activity context
	 */
	Activity activity;
	
	
	public NavUtility() {
		allCategories = new LinkedHashMap<String, JSONObject>();
		allProducts = new LinkedHashMap<String, JSONObject>();
		allReviews = new LinkedHashMap<String, JSONObject>();
		initialItemCount = 0;
		processedItemCount = 0;
		batchSize = 100;
		selectionID = null;
	}
	
	/*
	private void parseTopCategories(OnBazaarResponse response) {
		
		Log.e(TAG, "entered parseTopCategories");
		
		
		 // BazaarRequest will be used to make our API calls. The parameters are set in the BVReviewBrowsingApplication class
		 
		request = new BazaarRequest(BVReviewBrowsingApplication.domain, BVReviewBrowsingApplication.passKey, BVReviewBrowsingApplication.apiVersion);
		
		
		 //Get all the top level categories and sort on their name
		 
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
		
		
		 // This request will get first 100
		 
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
	}
	
	public void execParseTopCategories(MainActivity activity) {

		final MainActivity finalActivity = activity;
			
		Log.e(TAG, "entered execParseTopCategories");
		
		
		 //We will call the method and pass it the listener to execute when the
		 //request's data returns
		 
		parseTopCategories(new BazaarUIThreadResponse(activity) {
			
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
					execParseTopCategories(finalActivity);
				} else {
					initialItemCount = 0;
					processedItemCount = 0;
					//execParseChildrenCategories(finalActivity);
				}
			}
		});	
		
	}
	*/
	
	private void parseCategories(OnBazaarResponse response) {
		
		Log.e(TAG, "entered parseCategories");
		 
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
		
		Log.e(TAG, "parseCategories : selectionID = " + selectionID);
		
		String[] ids = new String[topCategoryIds.size()];
		topCategoryIds.toArray(ids);
		
		//TODO remove
		Log.e(TAG, "parseCategories: ids = " + Arrays.toString(ids));
		
		params.addFilter("ParentId", Equality.EQUAL, ids);
		
		/*
		 * Clean out topCategoryIds for next pass
		 */
		topCategoryIds = new ArrayList<String>();
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.CATEGORIES, params, response);
		
	}
	
	public void execParseCategories(MainActivity activity) {

		final MainActivity finalActivity = activity;
		
		parseCategories(new BazaarUIThreadResponse(activity) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				try {
					if (initialItemCount == 0) {
						initialItemCount = response.getInt("TotalResults");
					}
					itemsToProcess = response.getJSONArray("Results");
					
					//if there are no results means that these items have to child categories
					//they just have products
					if (itemsToProcess.length() == 0) {
						initialItemCount = 0;
						processedItemCount = 0;
						execParseProducts(finalActivity);
						
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
							
							topCategoryIds.add(currentObj.getString("Id"));
							
							Log.i(TAG, currentObj.getString("ParentId"));
							
							parent = allCategories.get(currentObj.getString("ParentId"));
							parent.put("HasChildren", true);
							children = parent.getJSONArray("Children");
							children.put(currentObj.getString("Id"));
											
							processedItemCount++;
						}
						
						Log.e(TAG, "processedItemCount in  execParseChildrenCategories = " + processedItemCount);
						
						if (initialItemCount > processedItemCount) {
							execParseCategories(finalActivity);
						} else {
							initialItemCount = 0;
							processedItemCount = 0;
							if (levelsToPull == 0) {
								finalActivity.displayCategories();
							} else {
								--levelsToPull;
								execParseCategories(finalActivity);
							}
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
		//params.setLimit(batchSize);
		//I am just going to pull the top 25 products to start off
		params.setLimit(25);
		//I am going to pull the items with the highest number of reviews
		params.addSort("TotalReviewCount", false);
		
		Log.e(TAG, "parseProducts : selectionID = " + selectionID);
		
		String[] ids = new String[topCategoryIds.size()];
		topCategoryIds.toArray(ids);
		
		//TODO remove
		Log.e(TAG, "parseProducts: ids = " + Arrays.toString(ids));
		
		params.addFilter("CategoryId", Equality.EQUAL, ids);
		
		/*
		 * This request will get first 100
		 */
		request.sendDisplayRequest(RequestType.PRODUCTS, params, response);
		
	}
	
	public void execParseProducts(MainActivity activity) {
		final MainActivity finalActivity = activity;
		
		parseProducts(new BazaarUIThreadResponse(activity) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				try {
					if (initialItemCount == 0) {
						initialItemCount = response.getInt("TotalResults");
					}
					itemsToProcess = response.getJSONArray("Results");
						
					//TODO remove
					Log.e(TAG, "execParseProducts : itemsToProcess = " + itemsToProcess);
					Log.e(TAG, "execParseProducts : processedItemCount = " + processedItemCount);					
					
					JSONObject currentObj;
					JSONArray children;
					JSONObject parent;
					for (int i = 0; i < itemsToProcess.length(); i++) {
						
						currentObj = itemsToProcess.getJSONObject(i);
						allProducts.put(currentObj.getString("Id"), currentObj);
						
						parent = allCategories.get(currentObj.getString("CategoryId"));
						parent.put("HasChildren", true);
						children = parent.getJSONArray("Children");
						children.put(currentObj.getString("Id"));
										
						processedItemCount++;
					}
					
					Log.e(TAG, "execParseProducts : processedItemCount = " + processedItemCount);
					
					//TODO I am just pulling the first 100 products. Will need to get all of them
					/*
					if (initialItemCount > processedItemCount) {
						execParseChildrenCategories();
					} else {
						initialItemCount = 0;
						processedItemCount = 0;
						displayCategories();
					}
					*/
					
					finalActivity.displayProducts();
						
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

}
