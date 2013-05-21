package com.vendsy.bartsy.venue.model;

import java.text.DateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vendsy.bartsy.venue.R;
import com.vendsy.bartsy.venue.utils.WebServices;

public class Order  {

	// Each order has an ID that is unique within a session number
	public String serverID; 
	
	// Title and description are arbitrary strings
	public String title, description;
	public String itemId;
	
	// The total price is in the local denomination and is the sum of price * quantity, fee and tax
	public float price, fee, tax;
	public int quantity = 1;
	public int image_resource;
	public float tipAmount;
	public double total;
	public String updatedDate;
	
	public String profileId;
	
	// Each order contains the sender and the recipient (another single in the bar or a friend to pick the order up)
	public Profile orderSender;
	public Profile orderReceiver;
	
	// The view displaying this order or null. The view is the display of the order in a list. 
	// The list could be either on the client or the server and it looks different in both cases
	// but the code manages the differences. 
	public View view = null;
	
	// Order states
	// (received) -> NEW -> (accepted) -> IN_PROGRESS -> (completed) -> READY   -> (picked_up) -> COMPLETE    -> (timed out, error, etc) -> CANCELLED
	//                      (rejected) -> REJECTED       (failed)    -> FAILED     (forgotten) -> INCOMPLETE  
	
	public static final int ORDER_STATUS_NEW			= 0;
    public static final int ORDER_STATUS_REJECTED    	= 1;
    public static final int ORDER_STATUS_IN_PROGRESS  	= 2;
    public static final int ORDER_STATUS_READY 			= 3;
	public static final int ORDER_STATUS_FAILED	 		= 4;
	public static final int ORDER_STATUS_COMPLETE	 	= 5;
	public static final int ORDER_STATUS_INCOMPLETE	 	= 6;
	public static final int ORDER_STATUS_CANCELLED	 	= 7;
	public static final int ORDER_STATUS_COUNT			= 8;
	
	// The states are implemented in a status variable and each state transition has an associated time
	public int status;	
    public Date[] state_transitions = new Date[ORDER_STATUS_COUNT];
	
	
	/* 
	 * When an order is initialized the state transition times are undefined except for the 
	 * first state, which is when the order is received
	 */
	public void initialize (String server_id, String title, String description, 
			String price, String image_resource, Profile order_sender) {
		this.serverID = server_id;
		this.title = title;
		this.description = description;
		this.price = Float.parseFloat(price);
//		this.image_resource = Integer.parseInt(image_resource); 
		this.orderSender = order_sender;

		// Orders starts in the "NEW" status
		this.status = ORDER_STATUS_NEW;
		this.state_transitions[this.status] = new Date();
		
		calculateTotalPrice();
	}
	/**
	 *  Default constructor
	 */
	public Order() {
	}
	/**
	 * Constructor to parse all the information from the JSON
	 * 
	 * @param json
	 */
	public Order(JSONObject json) {
		
		try {
			status = Integer.valueOf(json.getString("orderStatus"));
			title = json.getString("itemName");
			updatedDate = json.getString("orderTime");
			price = Float.valueOf(json.getString("basePrice"));
			serverID = json.getString("orderId");
			tipAmount = Float.valueOf(json.getString("tipPercentage"));
			total = Double.valueOf(json.getString("totalPrice"));
			profileId = json.getString("bartsyId");
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * To process next positive state for the 0rder
	 */
	public void nextPositiveState() {
		switch (this.status) {
		case ORDER_STATUS_NEW:
			this.status = ORDER_STATUS_IN_PROGRESS;
			break;
		case ORDER_STATUS_IN_PROGRESS:
			this.status = ORDER_STATUS_READY;
			break;
		case ORDER_STATUS_READY:
			this.status = ORDER_STATUS_COMPLETE;
			break;
		}
	}
	/**
	 * It will calculates the total price based on price, quantity and tipAmount
	 * 
	 */
	public void calculateTotalPrice() {
		float actualPrice = (price * quantity);
		float subTotal =  actualPrice * ((tipAmount + 8) / 100);
		
		total = actualPrice + subTotal;

	}
	/**
	 * It will returns JSON format to update order status
	 */
	public JSONObject statusChangedJSON(){
		final JSONObject orderData = new JSONObject();
		try {
			orderData.put("orderId", serverID);
			orderData.put("orderStatus", status);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return orderData;
	}
	
	public void updateView () {
		
		if (view == null) return;

		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title);
		((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);
		if(this.state_transitions[ORDER_STATUS_NEW]!=null){
			((TextView) view.findViewById(R.id.view_order_time))
					.setText(DateFormat.getTimeInstance().format(
							this.state_transitions[ORDER_STATUS_NEW]));
			((TextView) view.findViewById(R.id.view_order_date)).setText(DateFormat.getDateInstance().format(this.state_transitions[ORDER_STATUS_NEW]));
		}else{
			//TODO Need to format updatedDate string value to date and time
		}
		
		((TextView) view.findViewById(R.id.view_order_price)).setText("" + (int) this.price); // use int for now
//		((ImageView)view.findViewById(R.id.view_order_image_resource)).setImageResource(this.image_resource);
		
		if(this.orderSender!=null){
			// Update sender profile section
			ImageView profileImageView = ((ImageView)view.findViewById(R.id.view_order_profile_picture));
			if(this.orderSender.image==null){
				// To download image from the profile URL
				WebServices.downloadImage(this.orderSender.getProfileImageUrl(), this.orderSender, profileImageView);
			}else{
				// To set the saved image to the imageView
				profileImageView.setImageBitmap(this.orderSender.image);
			}
			((TextView) view.findViewById(R.id.view_order_profile_name)).setText(this.orderSender.username);
		}

		String positive="", negative="";
		switch (this.status) {
		case ORDER_STATUS_NEW:
			positive = "ACCEPT";
			negative = "REJECT";
			view.findViewById(R.id.view_order_header).setBackgroundResource(R.drawable.rounded_corner_red);
			break;
		case ORDER_STATUS_IN_PROGRESS:
			positive = "COMPLETED";
			negative = "FAILED";
			view.findViewById(R.id.view_order_header).setBackgroundResource(R.drawable.rounded_corner_orange);
			break;
		case ORDER_STATUS_READY:
			positive = "PICKED UP";
			negative = "NO SHOW";
			view.findViewById(R.id.view_order_header).setBackgroundResource(R.drawable.rounded_corner_green);
			break;
		}
		((TextView) view.findViewById(R.id.view_order_number)).setText(this.serverID);
		((Button) view.findViewById(R.id.view_order_button_positive)).setText(positive);
		((Button) view.findViewById(R.id.view_order_button_positive)).setTag(this);
		((Button) view.findViewById(R.id.view_order_button_negative)).setText(negative);
		((Button) view.findViewById(R.id.view_order_button_negative)).setTag(this);
		view.setTag(this);
		
	}
	
}
