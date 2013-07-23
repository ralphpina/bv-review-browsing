package com.bazaarvoice.example.bvreviewbrowsing;

import org.json.JSONException;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements NetworkListener {

	/*
	 * For logging to console
	 */
	private static final String TAG = "MainActivity";
	
	//private TextView textViewCategoryTitle;
	
	private LinearLayout linearLayoutMain;
	
	/*
	 * Singleton class to keep track of all our navigation
	 */
	private NavUtility navUtility;
	
	/*
	 * testing - just get one line of subcategories
	 */
	boolean doAnotherTransaction = true;
	/*
	 * Determine if I should clear the canvas
	 */
	boolean firstDisplay = true;
	/*
	 * transactions to pull down
	 */
	private int numberOfChildrenToPull;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayout);
		
		navUtility = NavUtility.getInstanceOf(this);
		
		/*
		 * Get the top categories
		 */
		navUtility.getChildren(null);
		
		Log.e(TAG, "onCreate");
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	    Log.e(TAG, "onStart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    /*
	     * Prevents the activity from going through the 
	     * onCreate -> onStart -> onResume cycle.
	     */
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

	@Override
	public void networkTransactionDone(BVNode itemPulled) {		
		if (doAnotherTransaction) {		
			numberOfChildrenToPull = itemPulled.getChildren().size();
			Log.e(TAG, "numberOfChildrenToPull first = " + (numberOfChildrenToPull));
			
			for (BVNode child : itemPulled.getChildren()) {		
				/*
				 * Get first row of subcategories
				 */
				navUtility.getChildren(child);
			}
			
			doAnotherTransaction = false;
		} else {
			Log.e(TAG, "numberOfChildrenToPull last = " + (numberOfChildrenToPull));
			if (--numberOfChildrenToPull == 0) {
				displayCategories();
			}
		}
	}
	
	
	public void displayCategories() {
		
		Log.e(TAG, "entered displayCategories()");
		
		/*
		 * For the category titles
		 */
		TextView textViewCategoryName;
		/*
		 * This scrollview holds the linear layout with items
		 */
		HorizontalScrollView horizontalScrollView;
		/*
		 * The linear layout that holds all the FrameLayouts with items
		 */
		LinearLayout linearLayoutItems;
		/*
		 * framelayout is used for navigation, we can put a TextView for categories and such
		 * or ImageView for items
		 */
		FrameLayout frameLayoutItem;
		/*
		 * For item images
		 */
		ImageView imageItem;
		
		this.linearLayoutMain.removeAllViews();
		
		
		//convert Image size from dp to ints
		int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		
		try {
			/*
			 * Display the header for this category
			 */
			for (BVNode parent : navUtility.productTree.getRoot().getChildren()) {
			    // if the category is empty, don't display it
			    if (parent.getChildren().size() != 0) {
        		    textViewCategoryName = (TextView) getLayoutInflater().inflate(R.layout.category_name_template, null);
        			textViewCategoryName.setText(parent.getData().getString("Name"));
        			this.linearLayoutMain.addView(textViewCategoryName);
        			
        			horizontalScrollView = (HorizontalScrollView) getLayoutInflater().inflate(R.layout.horizontal_scrollview_items_template, null);
        			this.linearLayoutMain.addView(horizontalScrollView);
        			
        			linearLayoutItems = (LinearLayout) getLayoutInflater().inflate(R.layout.linear_layout_items_template, null);
        			horizontalScrollView.addView(linearLayoutItems);
        			
        			for (int i = 0; i < parent.getChildren().size(); i++) {
        		
        			    final BVNode child = parent.getChildren().get(i);
        			    
        			    frameLayoutItem = (FrameLayout) getLayoutInflater().inflate(R.layout.frame_layout_item_template, null);
        			    linearLayoutItems.addView(frameLayoutItem);
        			    
        			    /*
        			    imageItem = new ImageView(this);
        			    imageItem.setImageResource(R.drawable.ic_launcher);
        			    imageItem.setLayoutParams(new LayoutParams(imageSize, imageSize));
        			    frameLayoutItem.addView(imageItem);
        			    */
        			    
        			    textViewCategoryName = new TextView(this);
        				textViewCategoryName.setLayoutParams(new LayoutParams(imageSize, imageSize));
        				textViewCategoryName.setText(child.getData().getString("Name"));
        				textViewCategoryName.setPadding(padding, padding, padding, padding);
        				textViewCategoryName.setTextColor(Color.WHITE);
        				textViewCategoryName.setBackgroundColor(getResources().getColor(R.color.bv_light_green));
        				textViewCategoryName.setOnClickListener(new OnClickListener() {
        					String idClicked = child.getData().getString("Id");
        					@Override
        					public void onClick(View v) {
        						Toast.makeText(v.getContext(), "On click = " + idClicked, Toast.LENGTH_SHORT).show();
        					}
        				});
        				frameLayoutItem.addView(textViewCategoryName);
        			}
			    }
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
		
	

/*	
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
	*/

}
