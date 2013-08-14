package com.bazaarvoice.example.bvreviewbrowsing;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
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
    
    static NavUtility navUtility = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        navUtility = NavUtility.getInstanceOf(this);
        
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
        Bitmap bitmap;

        public ProductDetailFragment() {
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
            
            Display  display = getActivity().getWindowManager().getDefaultDisplay();
            int swidth = display.getWidth();

            LayoutParams params = imageView.getLayoutParams();
            params.width = LayoutParams.FILL_PARENT;
            params.height = swidth ;
            imageView.setLayoutParams(params);
            
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
            
            loadImage();
            
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
    
    public static class ProductReviewsFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        
        Context context;

        public ProductReviewsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            
            String[] values = new String[] { "Review 1", "Review 2", "Review 2",
                "Review 3", "Review 4", "Review 5", "Review 6", "Review 7",
                "Review 8", "Review 9", "Review 10"};
            
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, values);
            
            /** Setting the list adapter for the ListFragment */
            setListAdapter(adapter);
     
            return super.onCreateView(inflater, container, savedInstanceState);

           
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
