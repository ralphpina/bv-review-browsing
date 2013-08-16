package com.bazaarvoice.example.bvreviewbrowsing;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bazaarvoice.types.RequestType;

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
	/*
	 * To pass activity inside the anonymous inner methods
	 */
	private Activity thisActivity = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayout);
		
		navUtility = NavUtility.getInstanceOf();
		
		/*
		 * Get the top categories
		 */
		navUtility.productTree.setCurrentNode(null);
		navUtility.getChildren(null, this);	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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
	    
	    // We are at the top of the tree
	    if (navUtility.productTree.getCurrentNode() == null) {
	        finish(); 
	    } else if (navUtility.productTree.getCurrentNode().getParent() == null) {
	        finish(); 
	    } else {
	        BVNode parent = navUtility.productTree.getCurrentNode().getParent().getParent();
    	    
    	    if (parent == null) {
    	        finish();
    	    } else {
                doAnotherTransaction = true;
                navUtility.productTree.setCurrentNode(parent);
                if (parent.getChildren().size() > 0) {
                    displayCategories();
                } else {
                    navUtility.getChildren(parent, this);
                }
    	    }
	    }
	}

	@Override
	public void networkTransactionDone(BVNode itemPulled) {		
		if (doAnotherTransaction) {		
			numberOfChildrenToPull = itemPulled.getChildren().size();
			
			for (BVNode child : itemPulled.getChildren()) {		
				/*
				 * Get first row of subcategories
				 */
				navUtility.getChildren(child, this);
			}
			
			doAnotherTransaction = false;
		} else {
			if (--numberOfChildrenToPull <= 0) {
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
		//ImageView imageItem;
		
		this.linearLayoutMain.removeAllViews();
		
		
		//convert Image size from dp to ints
		int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		
		try {
			/*
			 * Display the header for this category
			 */
		    BVNode topNode = navUtility.productTree.getCurrentNode();
		    if (topNode == null) {
		        topNode = navUtility.productTree.getRoot();
		    }
		    
		    //Categories with parent children
		    Log.e(TAG, "topNode.getTypeForChildren() = " + topNode.getTypeForChildren());
		    Log.e(TAG, "topNode.getChildren().size() = " + topNode.getChildren().size());
		    if (topNode.getTypeForChildren() == RequestType.CATEGORIES) {
		    
    			for (BVNode parent : topNode.getChildren()) {
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
            					    BVNode itemSelected = navUtility.bvNodeMap.get(idClicked);
            						try {
                                        Log.i(TAG, "On click = " + idClicked + " name = " + itemSelected.getData().getString("Name"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
            						doAnotherTransaction = true;
            						navUtility.productTree.setCurrentNode(itemSelected);
            						if (itemSelected.getTypeForNode() == RequestType.CATEGORIES) {
                						if (itemSelected.getChildren().size() > 0) {
                		                    displayCategories();
                		                } else {
                		                    navUtility.getChildren(itemSelected, thisActivity);
                		                }     
            						} else {
                                        Intent goToProduct = new Intent(thisActivity, ProductActivity.class);
                                        goToProduct.putExtra("idClicked", idClicked);
                                        startActivity(goToProduct);
            						}
            					}
            				});
            				frameLayoutItem.addView(textViewCategoryName);
            			}
    			}
    		
    		//These are products	
		    } else {
		        
		        // How many products are we adding
		        int numberOfProducts = topNode.getChildren().size();
		        // into rows with 3 each
		        // Add this later
		        /*
		        int rowsOfProducts = numberOfProducts / 3;
		        // do we have any left over
		        if (numberOfProducts % 3 != 0) {
		            rowsOfProducts++;
		        } 
		        */
		        
		        for (int i = 0; i < numberOfProducts; i = i + 3) {
		            linearLayoutItems = (LinearLayout) getLayoutInflater().inflate(R.layout.linear_layout_items_template, null);
		            this.linearLayoutMain.addView(linearLayoutItems);
		            
		            for (int j = 0; j < 3; j++) {
    		            frameLayoutItem = (FrameLayout) getLayoutInflater().inflate(R.layout.frame_layout_item_template, null);
                        linearLayoutItems.addView(frameLayoutItem);
                        
                        if ((i + j) < topNode.getChildren().size()) {
                            final BVNode child = topNode.getChildren().get(i + j);
                            
                            if (child != null) {
                            
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
                                        BVNode itemSelected = navUtility.bvNodeMap.get(idClicked);
                                        doAnotherTransaction = true;
                                        navUtility.productTree.setCurrentNode(itemSelected);
                                        Intent goToProduct = new Intent(thisActivity, ProductActivity.class);
                                        goToProduct.putExtra("idClicked", idClicked);
                                        startActivity(goToProduct);
                                    }
                                });
                                frameLayoutItem.addView(textViewCategoryName);
                                
                            }
                        }
		            }
		        }
		        
		    }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
