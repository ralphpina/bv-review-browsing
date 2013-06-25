package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	/*
	 * For logging to console
	 */
	private static final String TAG = "MainActivity";
	
	private TextView textView;
	
	private RelativeLayout.LayoutParams layoutParams;
	private RelativeLayout relativeLayout;
	private LinearLayout linearLayout;
	
	/*
	 * Singleton class to keep track of all our navigation
	 */
	private NavUtility navUtility;
	
	/*
	 * To pass to navActivity to run threads in the UI
	 */
	private MainActivity thisActivity;
	
	/*
	 * The selectionID for this Activity so that we can load the right
	 * content when the user is navigating the stack or re-entering the app
	 */
	private String activitySelectionID;
	
	/*
	 * Should the onResume method be called after onCreate at this instance
	 */
	private boolean shouldResume;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		thisActivity = this;
		shouldResume = false;
	
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		textView = (TextView) findViewById(R.id.textView);
		
		textView.setText("Yo! We are loading your data!");
		//textView.setMovementMethod(new ScrollingMovementMethod());

		layoutParams = (RelativeLayout.LayoutParams)textView.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
		textView.setLayoutParams(layoutParams);
		
		navUtility = BVReviewBrowsingApplication.navUtility;
		
		Intent intent = getIntent();	
		/*
		 * if the Activity is being opened by someone clicking on a category
		 */
		if (intent.hasExtra(navUtility.CATEGORIES_IN_ACTIVITY)) {
			navUtility.selectionID = intent.getStringExtra(navUtility.CATEGORIES_IN_ACTIVITY);
			activitySelectionID = navUtility.selectionID;
			Log.e(TAG, "before going into execParseTopCategories");
			navUtility.levelsToPull = 2;
			navUtility.execParseCategories(this);
			/*
			 * If the Activity is being opened by somoene clicking on a product
			 */
		} else if (intent.hasExtra(navUtility.PRODUCT_TO_DISPLAY)) {
			navUtility.selectionID = intent.getStringExtra(navUtility.PRODUCT_TO_DISPLAY);
			activitySelectionID = navUtility.selectionID;
			displaySelectedProduct();
		} else {		
			/*
			 * If this is the first time the application is opened
			 */
			Log.e(TAG, "before going into execParseCategories");
			navUtility.topCategoryIds = new ArrayList<String>();
			navUtility.topCategoryIds.add("null");
			navUtility.levelsToPull = 2;
			navUtility.execParseCategories(this);
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.i(TAG, "entered onPause()");
		shouldResume = true;
		Log.i(TAG, "activitySelectionID = " + activitySelectionID);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (shouldResume) {
			Log.i(TAG, "entered onResume()");
			navUtility.selectionID = this.activitySelectionID;
			Log.i(TAG, "navUtility.selectionID = " + navUtility.selectionID);
			navUtility.levelsToPull = 2;
			navUtility.execParseCategories(this);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		//Go to the previous part of the tree
		
	}
	
	
	@SuppressLint("NewApi") 
	public void displayCategories() {
		//boolean hasChildren;
		JSONArray children;
		TextView newTextView;
		JSONObject obj;
		
		Log.e(TAG, "entered displayCategories()");
		
		//hide other textView 
		this.relativeLayout.removeView(textView);
		
		//layout parameters for my new TextViews
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		
		for (String topId : navUtility.topCategoryIds) {
			try {
				obj = navUtility.allCategories.get(topId);
				newTextView = new TextView(this);
				newTextView.setLayoutParams(layoutParams);
				newTextView.setText(obj.getString("Name"));
				this.linearLayout.addView(newTextView);
				
				children = obj.getJSONArray("Children");
				
				for (int i = 0; i < children.length(); i++) {
					String childID = (String) children.get(i);
					final JSONObject child = navUtility.allCategories.get(childID);
					
					newTextView = new TextView(this);
					newTextView.setLayoutParams(layoutParams);
					newTextView.setText("       " + child.getString("Name"));
					newTextView.setOnClickListener(new OnClickListener() {
						String idClicked = child.getString("Id");
						@Override
						public void onClick(View v) {
							//navUtility.selectionID = idClicked;
							//change the dispplay
							linearLayout.removeAllViews();
							relativeLayout.addView(textView);
							
							navUtility.initialItemCount = 0;
							navUtility.processedItemCount = 0;	
							
							Intent intent = new Intent(thisActivity, MainActivity.class);
							intent.putExtra(navUtility.CATEGORIES_IN_ACTIVITY, idClicked);
							thisActivity.startActivity(intent);
						}
					});
					this.linearLayout.addView(newTextView);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	@SuppressLint("NewApi") 
	public void displayProducts() {
		//boolean hasChildren;
		JSONArray children;
		TextView newTextView;
		JSONObject obj;
		
		Log.e(TAG, "entered displayProducts()");
		
		//hide other textView 
		this.relativeLayout.removeView(textView);
		
		//layout parameters for my new TextViews
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);		
		
		for (String topId : navUtility.topCategoryIds) {
			try {
				obj = navUtility.allCategories.get(topId);
				newTextView = new TextView(this);
				newTextView.setLayoutParams(layoutParams);
				newTextView.setText(obj.getString("Name"));
				this.linearLayout.addView(newTextView);
				
				children = obj.getJSONArray("Children");
				
				Log.e(TAG, "displayProducts : children.length = " + children.length());
				
				if (children.length() == 0) {
					newTextView = new TextView(this);
					newTextView.setLayoutParams(layoutParams);
					newTextView.setText("       This category does not have any products!!!");
				} else {
					for (int i = 0; i < children.length(); i++) {
						String childID = (String) children.get(i);
						final JSONObject child = navUtility.allProducts.get(childID);
						
						newTextView = new TextView(this);
						newTextView.setLayoutParams(layoutParams);
						newTextView.setText("       " + child.getString("Name"));
						newTextView.setOnClickListener(new OnClickListener() {
							String idClicked = child.getString("Id");
							@Override
							public void onClick(View v) {
								//navUtility.selectionID = idClicked;
								//change the dispplay
								linearLayout.removeAllViews();
								relativeLayout.addView(textView);
								
								navUtility.initialItemCount = 0;
								navUtility.processedItemCount = 0;
								
								Intent intent = new Intent(thisActivity, MainActivity.class);
								intent.putExtra(navUtility.PRODUCT_TO_DISPLAY, idClicked);
								thisActivity.startActivity(intent);
							}
						});
					}
				}
				this.linearLayout.addView(newTextView);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	@SuppressLint("NewApi")
	private void displaySelectedProduct() {
		
		TextView newTextView;
		JSONObject obj;
		JSONObject tempObj;
		
		Log.e(TAG, "entered displaySelectedProduct()");
		
		//hide other textView 
		this.relativeLayout.removeView(textView);
		
		//layout parameters for my new TextViews
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);		
		
		try {
			obj = navUtility.allProducts.get(navUtility.selectionID);
			
			newTextView = new TextView(this);
			newTextView.setLayoutParams(layoutParams);
			tempObj = obj.getJSONObject("Brand");
			newTextView.setText(tempObj.getString("Name"));
			this.linearLayout.addView(newTextView);
			
			newTextView = new TextView(this);
			newTextView.setLayoutParams(layoutParams);
			newTextView.setText(obj.getString("Name"));
			this.linearLayout.addView(newTextView);
			
			newTextView = new TextView(this);
			newTextView.setLayoutParams(layoutParams);
			newTextView.setText("$XX.XX");
			this.linearLayout.addView(newTextView);
			
			newTextView = new TextView(this);
			newTextView.setLayoutParams(layoutParams);
			newTextView.setText(obj.getString("Description"));
			this.linearLayout.addView(newTextView);
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
				
	}

}
