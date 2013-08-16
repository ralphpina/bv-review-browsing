package com.bazaarvoice.example.bvreviewbrowsing;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductActivity extends FragmentActivity {
    
    public static final String TAG = "ProductDetail";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());
        
        mSectionsPagerAdapter.passProductIntent(getIntent());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }
    
    @Override
    public void onResume() {
        super.onResume();
        
    }
    
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        String productId;
        
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ProductDetailFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            if (position == 0) {
                Fragment fragment = new ProductDetailFragment();
                Bundle args = new Bundle();
                args.putString("idClicked", productId);
                fragment.setArguments(args);
                return fragment;
            } else if (position == 1) {
                Fragment fragment = new ProductReviewsFragment();
                Bundle args = new Bundle();
                args.putString("idClicked", productId);
                fragment.setArguments(args);
                return fragment;
            } else {
                Fragment fragment = new ProductBuyFragment();
                Bundle args = new Bundle();
                args.putString("idClicked", productId);
                fragment.setArguments(args);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return "About";
            case 1:
                return "Reviews";
            case 2:
                return "Buy";
            }
            return null;
        }
        
        public void passProductIntent(Intent intent) {
            this.productId = intent.getStringExtra("idClicked");
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class ProductDetailFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
       
        ImageView imageView;
        TextView productName;
        TextView brandName;
        TextView productPrice;
        TextView productDescription;
        
        String imageUrl;
        private static Handler handler;
        Bitmap bitmap = null;
        
        NavUtility navUtility;

        public ProductDetailFragment() {
            navUtility = NavUtility.getInstanceOf();
        }

        @SuppressLint("HandlerLeak")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_product_detail,
                    container, false);
            
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    imageView.setImageBitmap(bitmap);
                }
            };
            
            //Get the item I will be displaying
            BVNode product = navUtility.bvNodeMap.get(getArguments().getString("idClicked"));
            
            imageView = (ImageView) rootView.findViewById(R.id.productImage);
            
            productName  = (TextView) rootView.findViewById(R.id.productName);
            brandName = (TextView) rootView.findViewById(R.id.productBrand);
            productPrice = (TextView) rootView.findViewById(R.id.productPrice);
            productDescription = (TextView) rootView.findViewById(R.id.productDescription);  
            
            try {
                imageUrl = product.getData().getString("ImageUrl");
                productName.setText(product.getData().getString("Name"));
                brandName.setText(product.getData().getJSONObject("Brand").getString("Name"));
                productPrice.setText("$300");
                productDescription.setText(product.getData().getString("Description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            
            if (bitmap == null) {
                loadImage();
             } else {
                 handler.sendEmptyMessage(0);
             }
            
            return rootView;
        }
        
        private void loadImage() {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "imageURL = " + imageUrl);
                        bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageUrl).getContent());
                        handler.sendEmptyMessage(0); 
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
            //gettingImage.start();
            
        }       
    }
    
    public static class ProductReviewsFragment extends ListFragment implements NetworkListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        
        Context context;
        public View mheaderView;
        ReviewAdapter adapter;
        ArrayList<HashMap<String, String>> reviewData;
        NavUtility navUtility;
        
        public ProductReviewsFragment() {       
            navUtility = NavUtility.getInstanceOf();
            reviewData = new ArrayList<HashMap<String,String>>();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            
            mheaderView = inflater.inflate(R.layout.fragment_product_reviews, null);
            
            return super.onCreateView(inflater, container, savedInstanceState);          
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            
            navUtility.getReviews(navUtility.bvNodeMap.get(getArguments().getString("idClicked")), this);
            
            if (mheaderView != null) {
                this.getListView().addHeaderView(mheaderView);
            }
            
            super.onActivityCreated(savedInstanceState);
        }
        
        /*
         * this gets called after the reviews are pulled and fills in the data
         */
        @Override
        public void networkTransactionDone(BVNode product) {     
            for (BVNode review : product.getChildren()) {
                HashMap<String, String> map = new HashMap<String, String>();
                
                try {
                    map.put(NavUtility.REVIEW_RATING, review.getData().getString(NavUtility.REVIEW_RATING));
                    map.put(NavUtility.REVIEW_USER_NICKNAME, review.getData().getString(NavUtility.REVIEW_USER_NICKNAME));
                    map.put(NavUtility.REVIEW_TEXT, review.getData().getString(NavUtility.REVIEW_TEXT));
                    map.put(NavUtility.REVIEW_SUBMISSION_TIME, review.getData().getString(NavUtility.REVIEW_SUBMISSION_TIME));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
                // adding HashMap to ArrayList
                reviewData.add(map);
            }
            
            adapter = new ReviewAdapter(this, reviewData);
             
            /** Setting the list adapter for the ListFragment */
            setListAdapter(adapter);
        }
    }
    
    public static class ProductBuyFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        
        public static final String TAG = "ReviewDashboard";
        
       
          
        View rootView;
        Context context;
        String value;
        TextView textView;
          
        public ProductBuyFragment() {
        }
    
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
                rootView = inflater.inflate(R.layout.fragment_product_reviews,
                    container, false);
                
                context = getActivity().getApplicationContext();
     
                return rootView;
        }       

    }

}
