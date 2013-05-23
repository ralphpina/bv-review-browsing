package com.bazaarvoice.example.bvreviewbrowsing;

import android.app.Application;

import com.bazaarvoice.types.ApiVersion;

public class BVReviewBrowsingApplication extends Application {
	
	/*
	 * To get an API key and domain please visit http://developer.bazaarvoice.com
	 */
	protected static final String domain = "bestbuybusiness.ugc.bazaarvoice.com";
	protected static final String passKey = "3khef54nqsldilas0ssrenmmw";
	protected static final ApiVersion apiVersion = ApiVersion.FIVE_FOUR;
	
	@Override
	public void onCreate() {
		
	}

}
