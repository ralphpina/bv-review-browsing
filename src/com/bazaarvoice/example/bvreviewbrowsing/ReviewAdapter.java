package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bazaarvoice.example.bvreviewbrowsing.ProductActivity.ProductReviewsFragment;

public class ReviewAdapter extends BaseAdapter {
    
    private ProductReviewsFragment reviewsFragment;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    
    public ReviewAdapter(ProductReviewsFragment reviewsFragment, ArrayList<HashMap<String, String>> data) {
        this.reviewsFragment = reviewsFragment;
        this.data = data;
       
        inflater = (LayoutInflater) reviewsFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        this.reviewsFragment.setListAdapter(this);
    }
    
    
    public void updateData(ArrayList<HashMap<String, String>> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        
        if(convertView==null) {
            vi = inflater.inflate(R.layout.list_row_review, null);
        }

        RatingBar ratingBar = (RatingBar) vi.findViewById(R.id.reviewRatingBar); 
        TextView userNickname = (TextView) vi.findViewById(R.id.userNickname); 
        TextView reviewText  = (TextView) vi.findViewById(R.id.reviewText); 
        TextView reviewSubmissionTime = (TextView) vi.findViewById(R.id.reviewSubmissionTime); 
        
        HashMap<String, String> review = new HashMap<String, String>();
        review = data.get(position);
        
        // Setting all values in listview
        ratingBar.setRating(Float.valueOf(review.get(NavUtility.REVIEW_RATING)));
        userNickname.setText(review.get(NavUtility.REVIEW_USER_NICKNAME));
        reviewText.setText(review.get(NavUtility.REVIEW_TEXT));
        reviewSubmissionTime.setText(review.get(NavUtility.REVIEW_SUBMISSION_TIME).substring(0, 10));
        
        return vi;
    }

}
