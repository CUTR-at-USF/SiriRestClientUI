/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.siri.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
/**
 * Android support library imports for Fragment support on pre-3.0 devices
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
/**
 * ActionBarSherlock imports for ActionBar support on pre-3.0 devices
 */
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.usf.cutr.siri.android.client.config.SiriJacksonConfig;
import edu.usf.cutr.siri.android.ui.fragments.StopMonRequestFragment;
import edu.usf.cutr.siri.android.ui.fragments.VehicleMonRequestFragment;
import edu.usf.cutr.siri.android.ui.loaders.StopMonResponseLoader;
import edu.usf.cutr.siri.android.ui.loaders.VehicleMonResponseLoader;

/**
 * This is a reference implementation for using the RESTful SIRI API from an
 * Android app. This activity is the entry point for the app, which contains
 * multiple fragments shown as tabs using the Android action bar.
 * 
 * @author Sean J. Barbeau
 * 
 */
public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.TabListener {

	public static String TAG = "SiriRestClientUI";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);

		// Set the default values from the XML file if this is the first
		// execution of the app
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Get whether or not to cache Jackson objects
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean cacheJacksonObjects = sharedPref.getBoolean(
				Preferences.KEY_CACHE_JACKSON_OBJECTS, false);

		if (cacheJacksonObjects) {
			Log.d(TAG,
					"Preference is set to cache Jackson objects, forcing a cache read now in "
							+ "MainActivity.onCreate() to hide latency from user...");
			/**
			 * Tell Jackson to retrieve any cached objects now, in an attempt to
			 * hide the initialization latency from the user. Caching allows the
			 * re-use of Jackson ObjectMapper, ObjectReader, and XmlReader
			 * objects from previous VM executions to reduce cold-start times.
			 */
			SiriJacksonConfig.setUsingCache(true, getApplicationContext());
			SiriJacksonConfig.forceCacheRead();
		}

		// Request use of spinner for showing indeterminate progress, to show
		// the user somethings going on during long-running operations
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the action bar.
		final com.actionbarsherlock.app.ActionBar actionBar = getSupportActionBar();
		actionBar
				.setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setTitle(getApplicationContext().getText(R.string.app_name));

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have a
		// reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the listener for when this tab is
			// selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		//Fix bug in HTTP connection pool for Android 2.1 and lower
		disableConnectionReuseIfNecessary();

		// Hide the indeterminate progress bar on the activity until we need it
		setProgressBarIndeterminateVisibility(Boolean.FALSE);
	}

	/**
	 * Create menu item for settings
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle menu item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			// Show settings menu
			startActivity(new Intent(this, Preferences.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onTabSelected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabUnselected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabReselected(Tab tab,
			android.support.v4.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public static final int NUMBER_OF_TABS = 4; // Used to set up
													// TabListener

		// Constants for the different fragments that will be displayed in tabs,
		// in numeric order
		public static final int STOP_REQUEST_FRAGMENT = 0;
		public static final int STOP_RESPONSE_FRAGMENT = 1;
		public static final int VEH_REQUEST_FRAGMENT = 2;
		public static final int VEH_RESPONSE_FRAGMENT = 3;

		// Maintain handle to Fragments to avoid recreating them if one already
		// exists
		Fragment vehicleRequest;
		Fragment vehicleResponse;
		Fragment stopRequest;
		Fragment stopResponse;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {

			switch (i) {
			case VEH_REQUEST_FRAGMENT:
				// Vehicle Monitoring Request
				if (vehicleRequest == null) {
					vehicleRequest = new VehicleMonRequestFragment();
				}
				return vehicleRequest;
			case VEH_RESPONSE_FRAGMENT:
				if (vehicleResponse == null) {
					vehicleResponse = new VehicleMonResponseLoader.AppListFragment();
				}
				return vehicleResponse;
			case STOP_REQUEST_FRAGMENT:
				// Stop Monitoring Request
				if (stopRequest == null) {
					stopRequest = new StopMonRequestFragment();
				}
				return stopRequest;
			case STOP_RESPONSE_FRAGMENT:
				// Stop Monitoring Response
				if (stopResponse == null) {
					stopResponse = new StopMonResponseLoader.AppListFragment();
				}
				return stopResponse;
			}

			return null; // This should never happen
		}

		@Override
		public int getCount() {
			return NUMBER_OF_TABS;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case VEH_REQUEST_FRAGMENT:
				// Vehicle Monitoring Request
				return getString(R.string.vehicle_req_tab_title).toUpperCase();
			case VEH_RESPONSE_FRAGMENT:
				// Vehicle Monitoring Response
				return getString(R.string.vehicle_res_tab_title).toUpperCase();
			case STOP_REQUEST_FRAGMENT:
				// Stop Monitoring Request
				return getString(R.string.stop_req_tab_title).toUpperCase();
			case STOP_RESPONSE_FRAGMENT:
				// Stop Monitoring Response
				return getString(R.string.stop_res_tab_title).toUpperCase();
			}
			return null; // This should never happen
		}
	}

	private void disableConnectionReuseIfNecessary() {
		// Work around pre-Froyo bugs in HTTP connection reuse.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}
}
