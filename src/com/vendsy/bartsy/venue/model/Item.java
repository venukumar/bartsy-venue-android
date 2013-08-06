package com.vendsy.bartsy.venue.model;

import java.text.DecimalFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.field.DatabaseField;
import com.vendsy.bartsy.venue.R;

/**
 * @author Seenu
 * 
 *         A MenuDrink object we are creating and persisting to the database.
 */
public class Item {
	// id is generated by the database and set on the object automatically
	@DatabaseField(id = true)
	private String itemId = null;
	@DatabaseField
	private String title = null;
	@DatabaseField
	private String image = null;
	@DatabaseField
	private String description = null;
	@DatabaseField
	private String optionsDescription = null;
	@DatabaseField
	private String glass = null;
	@DatabaseField
	private String ingredients = null;
	@DatabaseField
	private String instructions = null;
	@DatabaseField
	private String specialInstructions = null;
	@DatabaseField
	private String category = null;
	@DatabaseField
	private String menuPath = null;
	@DatabaseField
	private String price = null;
	@DatabaseField
	private String specialPrice = null;
	@DatabaseField
	private String venueId = null;

	@DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true, columnName = "section_id")
	private Section section;
	

	/**
	 * TODO - Constructors / parsers
	 */
	
	public Item() {
	}

	public Item(JSONObject json) throws JSONException {

		if (json.has("itemName"))
			this.title = json.getString("itemName");
		if (json.has("name"))
			this.title = json.getString("name");
		
		if (json.has("description"))
			this.description = json.getString("description");

		if (json.has("options_description"))
			this.optionsDescription = json.getString("options_description");
		
		if (json.has("price"))
			this.price = json.getString("price");
		if (json.has("basePrice"))
			this.price = json.getString("basePrice");
		
		if (json.has("id"))
			this.itemId = json.getString("id");

		if (json.has("glass"))
			glass = json.getString("glass");
		if (json.has("ingredients"))
			ingredients = json.getString("ingredients");
		if (json.has("instructions"))
			instructions = json.getString("instructions");
		if (json.has("special_instructions"))
			this.specialInstructions = json.getString("special_instructions");
		if (json.has("category"))
			category = json.getString("category");


		// For now always use the same image for the drink
		this.image = Integer.toString(R.drawable.drink);
	} 

	
	/**
	 * 
	 * TODO - Views
	 * 
	 */
	
	public View orderView(LayoutInflater inflater) {

	    DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(0);
		df.setMinimumFractionDigits(0);

		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.item_order, null);
		((TextView) view.findViewById(R.id.view_order_mini_base_amount)).setText(df.format(Float.parseFloat(getPrice())));
		((TextView) view.findViewById(R.id.view_order_title)).setText(getTitle());
		
		if (has(getOptionsDescription()))
			((TextView) view.findViewById(R.id.view_order_description)).setText(getOptionsDescription());
		else
			view.findViewById(R.id.item_order_description_field).setVisibility(View.GONE);
		
		if (has(specialInstructions))
			((TextView) view.findViewById(R.id.item_order_special_instructions)).setText(specialInstructions);
		else
			view.findViewById(R.id.item_order_special_instructions_field).setVisibility(View.GONE);
		
		
		return view;
	}
	
	
	/**
	 * 
	 * TODO Getters and setters
	 * 
	 */

	public boolean has(String field) {
		return !(field == null || field.equals(""));
	}
	
	public String getVenueId() {
		return venueId;
	}

	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOptionsDescription() {
		return optionsDescription;
	}

	public void setOptionsDescription(String description) {
		this.optionsDescription = description;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getSpecialPrice() {
		return specialPrice;
	}

	public void setSpecialPrice(String price_special) {
		this.specialPrice = price_special;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String drinkId) {
		this.itemId = drinkId;
	}
	
	
}