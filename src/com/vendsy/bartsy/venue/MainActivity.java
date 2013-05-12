package com.vendsy.bartsy.venue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.venue.dialog.PeopleDialogFragment;
import com.vendsy.bartsy.venue.model.AppObservable;
import com.vendsy.bartsy.venue.model.Order;
import com.vendsy.bartsy.venue.model.Profile;
import com.vendsy.bartsy.venue.utils.CommandParser;
import com.vendsy.bartsy.venue.utils.CommandParser.BartsyCommand;
import com.vendsy.bartsy.venue.utils.Constants;
import com.vendsy.bartsy.venue.utils.Utilities;
import com.vendsy.bartsy.venue.utils.WebServices;
import com.vendsy.bartsy.venue.view.AppObserver;
import com.vendsy.bartsy.venue.view.BartenderSectionFragment;
import com.vendsy.bartsy.venue.view.InventorySectionFragment;
import com.vendsy.bartsy.venue.view.PeopleSectionFragment;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, PeopleDialogFragment.UserDialogListener,
		AppObserver {

	/****************
	 * 
	 * 
	 * TODO - global variables
	 * 
	 */

	public static final String TAG = "Bartsy";
	private BartenderSectionFragment mBartenderFragment = null;
	private PeopleSectionFragment mPeopleFragment = null;

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("in broadcast receiver:::::::");
			String newMessage = intent.getExtras().getString(
					Utilities.EXTRA_MESSAGE);
			System.out.println("the message is ::::" + newMessage);

			processPushNotification(newMessage);

		}

	};

	public void appendStatus(String status) {
		Log.d(TAG, status);
	}

	// A pointer to the parent application. In the MVC model, the parent
	// application is the Model
	// that this observe changes and observes

	public BartsyApplication mApp = null;

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

	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
	private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
	private static final int HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT = 2;
	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;

	/**************************************
	 * 
	 * 
	 * TODO - Save/restore state
	 * 
	 * 
	 */
	/*
	 * static final String STATE_SCORE = "playerScore"; static final String
	 * STATE_LEVEL = "playerLevel"; ...
	 * 
	 * @Override public void onSaveInstanceState(Bundle savedInstanceState) { //
	 * Save the user's current game state savedInstanceState.putInt(STATE_SCORE,
	 * mCurrentScore); savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
	 * savedInstanceState.
	 * 
	 * // Always call the superclass so it can save the view hierarchy state
	 * super.onSaveInstanceState(savedInstanceState); }
	 * 
	 * 
	 * public void onRestoreInstanceState(Bundle savedInstanceState) { // Always
	 * call the superclass so it can restore the view hierarchy
	 * super.onRestoreInstanceState(savedInstanceState);
	 * 
	 * // Restore state members from saved instance mCurrentScore =
	 * savedInstanceState.getInt(STATE_SCORE); mCurrentLevel =
	 * savedInstanceState.getInt(STATE_LEVEL); }
	 */

	/**********************
	 * 
	 * 
	 * TODO - Activity lifecycle management
	 * 
	 * 
	 **********************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Log function call
		Log.i(TAG, this.toString() + "onCreate()");

		// Setup application pointer
		mApp = (BartsyApplication) getApplication();

		// Set base view for the activity
		setContentView(R.layout.activity_main);

		initializeFragments();

		// GCM registration
		//--------------------------------------------------
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, Utilities.SENDER_ID);
		} else {
			Log.v(TAG, "Already registered");
		}
		System.out.println("the registration id is:::::" + regId);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Utilities.DISPLAY_MESSAGE_ACTION));
		//--------------------------------------------

		// Set up the action bar custom view
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowHomeEnabled(true);

		// Create the adapter that will return a fragment for each of the
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
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
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	private void initializeFragments() {
		// Initialize bartender view
		if (mBartenderFragment == null) {
			mBartenderFragment = new BartenderSectionFragment();
			mBartenderFragment.mApp = mApp;
		}

		// Initialize people view
		if (mPeopleFragment == null) {
			mPeopleFragment = new PeopleSectionFragment();
			mPeopleFragment.mApp = mApp;
		}
	}

	private void processPushNotification(String message) {
		initializeFragments();

		try {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();

			JSONObject json = new JSONObject(message);
			if (json.has("messageType")) {
				if (json.getString("messageType").equals("placeOrder")) {
					Order order = new Order(json);
					mBartenderFragment.addOrder(order);
					updateOrdersCount();
				}
			}
			json.getString("");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.

		// Log function call
		appendStatus(this.toString() + "onCreate()");

		/*
		 * Keep a pointer to the Android Application class around. We use this
		 * as the Model for our MVC-based application. Whenever we are started
		 * we need to "check in" with the application so it can ensure that our
		 * required services are running.
		 */

		mApp.checkin();

		/*
		 * Now that we're all ready to go, we are ready to accept notifications
		 * from other components.
		 */
		mApp.addObserver(this);

		// This initiates a series of events from the application, handled
		// by the hander
		mApp.hostInitChannel();

		// update the state of the action bar depending on our connection state.
		updateActionBarStatus();
		updateOrdersCount();

		// If the tablet hasn't yet been registered started the registration
		// activity
		SharedPreferences sharedPref = getSharedPreferences(getResources()
				.getString(R.string.config_shared_preferences_name),
				Context.MODE_PRIVATE);
		String venueId = sharedPref.getString("RegisteredVenueId", null);
		if (venueId == null) {
			Log.i(TAG, "Unregistered device. Starting Venue Registration...");
			Intent intent = new Intent().setClass(this,
					VenueRegistrationActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			// finish();
			return;
		} else {
			Log.i(TAG, "Proceeding with startup...");

		}
	}

	@Override
	public void onStop() {
		super.onStop();
		appendStatus("onStop()");
		mApp = (BartsyApplication) getApplication();
		mApp.deleteObserver(this);
	}

	/******
	 * 
	 * 
	 * TODO - Action bar (menu) helper functions
	 * 
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);

		// Calling super after populating the menu is necessary here to ensure
		// that the action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_profile:
			Intent intent = new Intent().setClass(this,
					VenueRegistrationActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		case R.id.action_settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					SettingsActivity.class);
			startActivity(settingsActivity);
			break;

		case R.id.action_quit:
			mApp.quit();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateActionBarStatus() {

		Log.i(TAG, "updateChannelState()");

		if (mApp.venueProfileID == null || mApp.venueProfileName == null)
			getActionBar()
					.setTitle(
							"Invalid venue configuration. Please uninstall then reinstall Bartsy.");
		else
			getActionBar().setTitle(mApp.venueProfileName);
	}

	/**
	 * Updates the action bar tab with the number of open orders
	 */

	void updateOrdersCount() {
		// Update tab title with the number of orders - for now hardcode the tab
		// at the right position
		getActionBar().getTabAt(0).setText(
				"Orders (" + mApp.mOrders.size() + ")");
	}

	/***********
	 * 
	 * TODO - Views management
	 * 
	 */

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	MainActivity main_activity = this;

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private int mTabs[] = { R.string.title_bartender,
				R.string.title_people, R.string.title_inventory };

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (mTabs[position]) {
			case R.string.title_bartender: // The order tab (for bar owners)
				return (mBartenderFragment);
			case R.string.title_inventory: // The customers tab (for bar owners)
				return (new InventorySectionFragment());
			case R.string.title_people: // The people tab shows who's local,
										// allows to send them a drink or a chat
										// request if they're available and
										// allows to leave comments for others
										// on the venue
				return (mPeopleFragment);
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			// Show total pages.
			return mTabs.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();

			return getString(mTabs[position]);
		}
	}

	void createNotification(String title, String text) {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(text);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(0, mBuilder.build());

	}

	/*********************
	 * 
	 * 
	 * TODO - Bartsy protocol command handling and order management TODO - TODO
	 * - General command parsing/second TODO - Order command TODO - Order reply
	 * command TODO - Profile command TODO - User interaction commands.
	 * 
	 * 
	 */

	@Override
	public synchronized void update(AppObservable o, Object arg) {
		Log.i(TAG, "update(" + arg + ")");
		String qualifier = (String) arg;

		if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
			Message message = mHandler
					.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
			mHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.HISTORY_CHANGED_EVENT)) {
			Message message = mHandler
					.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
			mHandler.sendMessage(message);
		} else if (qualifier
				.equals(BartsyApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
			Message message = mHandler
					.obtainMessage(HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT);
			mHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
			Message message = mHandler
					.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
			mHandler.sendMessage(message);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_APPLICATION_QUIT_EVENT:
				Log.i(TAG,
						"BartsyActivity.mhandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
				finish();
				break;
			case HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT:
				Log.i(TAG,
						"BartsyActivity.mhandler.handleMessage(): HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT");
				updateActionBarStatus();
				break;
			case HANDLE_HISTORY_CHANGED_EVENT: {
				Log.i(TAG,
						"BartsyActivity.mhandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");

				String message = mApp.getLastMessage();

				// The history could be empty because this event is sent even on
				// a channel init
				if (message == null)
					break;

				BartsyCommand command = parseMessage(message);
				if (command != null) {
					processCommand(command);
				} else {
					Log.d(TAG, "Invalid command received");
				}
				break;
			}
			case HANDLE_ALLJOYN_ERROR_EVENT: {
				Log.i(TAG,
						"BartsyActivity.mhandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
				alljoynError();
			}
				break;
			default:
				break;
			}
		}
	};
	private Order order;

	private void alljoynError() {
		if (mApp.getErrorModule() == BartsyApplication.Module.GENERAL
				|| mApp.getErrorModule() == BartsyApplication.Module.USE) {
			appendStatus("AllJoyn ERROR!!!!!!");
			// showDialog(DIALOG_ALLJOYN_ERROR_ID);
		}
	}

	public BartsyCommand parseMessage(String readMessage) {
		appendStatus("Message received: " + readMessage);

		// parse the command
		BartsyCommand command = null;
		ByteArrayInputStream stream = new ByteArrayInputStream(
				readMessage.getBytes());
		CommandParser commandParser = new CommandParser();

		try {
			command = commandParser.parse(stream);
		} catch (XmlPullParserException e) {
			// Auto-generated catch block
			e.printStackTrace();
			appendStatus("Invalid command format received");
			return null;
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
			appendStatus("Parser IO exception");
			return null;
		} finally {
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
					appendStatus("Stream close IO exception");
					return null;
				}
			}
		}

		// check to make sure there was a
		if (command == null) {
			appendStatus("Parser succeeded but command is null");
			return null;
		}

		// Return successfully processed command
		return command;
	}

	void processCommand(BartsyCommand command) {
		if (command.opcode.equalsIgnoreCase("order")) {
			appendStatus("Opcode: " + command.opcode + "");
			processCommandOrder(command);
		} else if (command.opcode.equalsIgnoreCase("profile")) {
			processProfile(command);
		} else
			appendStatus("Unknown command: " + command.opcode);
	}

	/******
	 * 
	 * 
	 * TODO - Receive drink order
	 * 
	 */

	void processCommandOrder(BartsyCommand command) {

		appendStatus("Processing command for order:" + command.arguments.get(0));

		// Find the person who placed the order in the list of people in this
		// bar. If not found, don't accept the order
		Profile person = null;
		for (Profile p : mApp.mPeople) {
			if (p.userID.equalsIgnoreCase(command.arguments.get(6))) {
				// User found
				person = p;
				break;
			}
		}
		if (person == null) {
			appendStatus("Error processing command. Profile placing order is missing from the list");
			return;
		}

		// Create a new order
		Order order = new Order();
		order.initialize(Integer.parseInt(command.arguments.get(0)), // client
																		// order
																		// ID
				mApp.mSessionID++, // server order ID
				command.arguments.get(2), // Title
				command.arguments.get(3), // Description
				command.arguments.get(4), // Price
				command.arguments.get(5), // Image resource
				person); // Order sender ID
		mBartenderFragment.addOrder(order);

		updateOrdersCount();
	}

	/*
	 * 
	 * TODO - Send/receive order status changed command
	 */

	public void sendOrderStatusChanged(Order order) {
		// Expects the order status and the server ID to be already set on this
		// end
		appendStatus("Sending order response for order: " + order.serverID);

		if (Constants.USE_ALLJOYN) {

			mApp.newLocalUserMessage("<command><opcode>order_status_changed</opcode>"
					+ "<argument>" + order.status + "</argument>" + // arg(0) -
																	// status is
																	// already
																	// updated
																	// on
																	// this end
					"<argument>" + order.serverID + "</argument>" + // arg(1)
					"<argument>" + order.clientID + "</argument>" + // arg(2)
					"<argument>" + order.orderSender.userID + "</argument>" + // arg(3)
					"</command>");
		} else {
			WebServices.orderStatusChanged(order, this);
		}

		// Update tab title with the number of open orders
		updateOrdersCount();

	}

	/*
	 * 
	 * TODO - User interaction commands
	 */

	@Override
	public void onUserDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button

		Person user = ((PeopleDialogFragment) dialog).mUser;

		appendStatus("Sending drink to: " + user.getNickname());

		mApp.newLocalUserMessage("<command><opcode>message</opcode>"
				+ "<argument>" + user.getNickname() + "</argument>"
				+ "<argument>" + "hi buddy" + "</argument>" + "</command>");
		appendStatus("Placed drink order");
	}

	@Override
	public void onUserDialogNegativeClick(DialogFragment dialog) {
		// User touched the dialog's positive button

		Person user = ((PeopleDialogFragment) dialog).mUser;

		appendStatus("Sending message to: " + user.getNickname());

		mApp.newLocalUserMessage("<command><opcode>message</opcode>"
				+ "<argument>" + user.getNickname() + "</argument>"
				+ "<argument>" + "hi buddy" + "</argument>" + "</command>");
		appendStatus("Sent message");
	}

	/*
	 * 
	 * TODO - Profile commands
	 */

	void processProfile(BartsyCommand command) {

		appendStatus("Process command: " + command.opcode);

		// Decode the user image and create a new incoming profile
		byte[] decodedString = Base64.decode(command.arguments.get(5),
				Base64.DEFAULT);
		Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0,
				decodedString.length);
		Profile profile = new Profile(command.arguments.get(0), // userid
				command.arguments.get(1), // username
				command.arguments.get(2), // location
				command.arguments.get(3), // info
				command.arguments.get(4), // description
				image // image
		);

		// Add the person to the list of people in the bar (this method doesn't
		// add duplicates)
		mPeopleFragment.addPerson(profile);
	}

	public static String readFileAsString(String filePath)
			throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line, results = "";
		while ((line = reader.readLine()) != null)
			results += line;
		reader.close();
		return results;
	}

}
