package com.bazaarvoice.example.bvreviewbrowsing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.bazaarvoice.BazaarRequest;
import com.bazaarvoice.DisplayParams;
import com.bazaarvoice.types.ApiVersion;
import com.bazaarvoice.types.Equality;
import com.bazaarvoice.types.RequestType;

public class NavUtility {
	
	/*
	 * For logging to console
	 */
	private static final String TAG = "NavUtility";
	
	/*
	 * To make the API calls
	 */
	private static final String DOMAIN = "bestbuy.ugc.bazaarvoice.com";
	private static final String PASSKEY = "ax58agxc5oibip1dlzft2ej3f";
	private static final ApiVersion API_VERSION = ApiVersion.FIVE_FOUR;
	
	public final String CATEGORIES_IN_ACTIVITY = "categories_in_activity";
	public final String PRODUCT_TO_DISPLAY = "product_to_display";
	
	
	/*
	 * A tree for all categories products and reviews in the current activity
	 */
	public BVProductTree productTree;	
	/*
	 * What type of item are the children
	 */
	RequestType requestItemType;
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
	/*
	 * The number of items that the API call should pull at a time
	 */
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
	 * Current activity context
	 */
	Activity activity;
	
	
	/*
	 * Singleton!
	 */
	private static NavUtility navUtility;
	
	public static NavUtility getInstanceOf(Activity activity) {
		if (navUtility == null) {
			navUtility = new NavUtility();
		}
		navUtility.activity = activity;
		return navUtility;
	}
	
	private NavUtility() {
		productTree = BVProductTree.getInstance();
		processedItemCount = 0;
		batchSize = 100;
		selectionID = null;
	}
	
	
	public void getChildren(BVNode parent) {
		/*
		 * Used to make the API calls
		 */
		BazaarRequest request;
		DisplayParams params;
		/*
		 * Id of the item whose children I will be pulling down
		 */
		String Id;
		/*
		 * BazaarRequest will be used to make our API calls. The parameters are set in the BVReviewBrowsingApplication class
		 */
		request = new BazaarRequest(DOMAIN, PASSKEY, API_VERSION);
		
		/*
		 * Get all the top child categories
		 */
		params = new DisplayParams();
	
		/*
		 * Set the currentNode
		 */
		productTree.setCurrentNode(parent);
		
		/*
		 * Are we getting the top category
		 */
		if (productTree.getCurrentNode() == null) {
			Id = "null";		
			requestItemType = RequestType.CATEGORIES;
			
		} else {
			//get the id of the current type
			try {
				Id = productTree.getCurrentNode().getData().getString("Id");
			} catch (JSONException e) {
				Log.e(TAG, "The current node has no ID?!?!?");
				Id = "null";
				e.printStackTrace();
			}
			requestItemType = productTree.getCurrentNode().getTypeForChildren();
			
			/* 
			 * for this example, I am going to assume that children will just be categories, products, or reviews
			 * I am assuming if it is a category, and the requestType is null, I will try to pull subcategories.
			 * When parsing, if this subcategories call is null, I will set the children as products, and in the next
			 * try it will skip this block
			 */
			if (requestItemType == null) {
				if (productTree.getCurrentNode().getTypeForNode() == RequestType.CATEGORIES) {
					requestItemType = RequestType.CATEGORIES;
				} else {
					requestItemType = RequestType.REVIEWS;
				}
			}		
		}
		
		if (requestItemType == RequestType.CATEGORIES) {
			/*
			 * I will only deal with the top 10 categories for this demo
			 */
			params.setLimit(10);
			/*
			 * I am using the total questions and is active as a proxy for the more popular categories.
			 */
			params.addSort("TotalQuestionCount", false);
			params.addFilter("isActive", Equality.EQUAL, "true");
			
		} else if (requestItemType == RequestType.PRODUCTS) {
			/*
			 * I will only deal with the top 100 products to begin
			 */
			params.setLimit(batchSize);
			/*
			 * I am using the total reviews and is active as a proxy for the more popular products.
			 */
			params.addSort("TotalReviewCount", false);
			params.addFilter("isActive", Equality.EQUAL, "true");		
			
		} else { //we are getting reviews!
			/*
			 * I will only deal with the top 100 reviews to begin
			 */
			params.setLimit(batchSize);
			/*
			 * I am using the total reviews and is active as a proxy for the more popular products.
			 */
			params.addSort("TotalCommentCount", false);
		}
		
		params.addFilter("ParentId", Equality.EQUAL, Id);
		
		/*
		 * send the request and process the response
		 */
		request.sendDisplayRequest(requestItemType, params, new BazaarUIThreadResponse(activity) {
			
			@Override
			public void onUiResponse(JSONObject response) {
				try {
					if (initialItemCount == 0) {
						initialItemCount = response.getInt("TotalResults");
					}
					itemsToProcess = response.getJSONArray("Results");
					
					/*
					 * if there are no results means that these items have to child categories
					 * They either just have products or they are categories, which were called with 
					 * children type category, but have no such subcategories, so a new request should
					 * be made to check if they have products
					 */
					if (itemsToProcess.length() == 0) {
						initialItemCount = 0;
						processedItemCount = 0;
						 //The type for the children will always be set
						if (productTree.getCurrentNode().getTypeForNode() == RequestType.CATEGORIES 
								&& productTree.getCurrentNode().getTypeForChildren() == null) {
							productTree.getCurrentNode().setTypeForChildren(RequestType.PRODUCTS);
							getChildren(productTree.getCurrentNode());
						}
						
					//go get the children
					} else { 	
						if (productTree.getRoot() == null) {
							BVNode newRoot = new BVNode(productTree, RequestType.CATEGORIES, true);
							productTree.setCurrentNode(newRoot);
						}
						//if the type for the children was not set before, we do it now
						productTree.getCurrentNode().setTypeForChildren(requestItemType);
						
						Log.e(TAG, "itemsToProcess = " + itemsToProcess);
						
						for (int i = 0; i < itemsToProcess.length(); i++) {
							
							//make a node if the current JSONObject
							BVNode newNode = new BVNode(itemsToProcess.getJSONObject(i), productTree.getCurrentNode(), requestItemType);
							//add the current node to the 
							productTree.getCurrentNode().getChildren().add(newNode);
							processedItemCount++;
						}
						
						Log.e(TAG, "processedItemCount in  getChildren() = " + processedItemCount);
						
						/*
						 * Right now I am not really using these variables, but they will come in handy later for more\
						 * complex transactions.
						 */
						initialItemCount = 0;
						processedItemCount = 0;
					}
					
					((NetworkListener) activity).networkTransactionDone(productTree.getCurrentNode());
		
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
		});	
	}

}
