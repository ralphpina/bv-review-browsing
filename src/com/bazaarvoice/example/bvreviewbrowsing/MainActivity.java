package com.bazaarvoice.example.bvreviewbrowsing;

import org.json.JSONException;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements NetworkListener {

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
	
		/*
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		textView = (TextView) findViewById(R.id.textView);
		
		textView.setText("Yo! We are loading your data!");

		layoutParams = (RelativeLayout.LayoutParams)textView.getLayoutParams();
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
		textView.setLayoutParams(layoutParams);
		*/
		
		navUtility = NavUtility.getInstanceOf(this);
		
		/*
		 * Get the top categories
		 */
		navUtility.getChildren(null);
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
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
				//displayCategories();
			}
		}
	}
	
	
	public void displayCategories() {
		
		Log.e(TAG, "entered displayCategories()");
		
		TextView newTextView;
		//layout parameters for my new TextViews
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		if (firstDisplay) {
			//hide other textView and start with a clean cabvas
			this.relativeLayout.removeView(textView);
			firstDisplay = false;
		}
		
		try {
			/*
			 * Display the header for this category
			 */
			for (BVNode parent : navUtility.productTree.getRoot().getChildren()) {
				newTextView = new TextView(this);
				newTextView.setLayoutParams(layoutParams);
				newTextView.setText(parent.getData().getString("Name"));
				this.linearLayout.addView(newTextView);
			
				for (BVNode child : parent.getChildren()) {
				
					newTextView = new TextView(this);
					newTextView.setLayoutParams(layoutParams);
					newTextView.setText("       " + child.getData().getString("Name"));
					/*
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
					*/
					this.linearLayout.addView(newTextView);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	/*
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
	*/

}
