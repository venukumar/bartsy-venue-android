package com.vendsy.bartsy.venue.adapter;
/**
 * @author Seenu malireddy
 */
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.vendsy.bartsy.venue.R;
import com.vendsy.bartsy.venue.model.Item;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> groups;
    private ArrayList<ArrayList<Item>> subItems;
    private LayoutInflater inflater;
    
    /**
     * Constructor having the parameters groups and sub items 
     * 
     * @param context
     * @param groups
     * @param subItems
     */
    public ExpandableListAdapter(Context context, 
                        ArrayList<String> groups,
						ArrayList<ArrayList<Item>> subItems ) { 
        this.context = context;
		this.groups = groups;
        this.subItems = subItems;
        inflater = LayoutInflater.from( context );
    }

    public Object getChild(int groupPosition, int childPosition) {
        return subItems.get( groupPosition ).get( childPosition );
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long)( groupPosition*1024+childPosition );  // Max 1024 children per group
    }
    
    /**
     * To get child view based on group position and child position
     */
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view = null;
        if( convertView != null )
            view = convertView;
        else
            view = inflater.inflate(R.layout.drink_item, parent, false); 
        Item c = (Item)getChild( groupPosition, childPosition );
		TextView textView = (TextView)view.findViewById( R.id.view_drink_title );
		if( textView != null )
			textView.setText( c.getTitle() );
		
		textView = (TextView)view.findViewById( R.id.view_drink_description );
		if( textView != null && c.has(c.getOptionsDescription()))
			textView.setText( c.getOptionsDescription() );

		TextView rgb = (TextView)view.findViewById( R.id.view_drink_price );
		if( rgb != null )
			rgb.setText( "$"+c.getPrice() );
		
        return view;
    }
    /**
     * To get children count
     */
    public int getChildrenCount(int groupPosition) {
        return subItems.get( groupPosition ).size();
    }
    /**
     * To get group based on the position
     */
    public Object getGroup(int groupPosition) {
        return groups.get( groupPosition );        
    }
    /**
     * To get group size
     */
    public int getGroupCount() {
        return groups.size();
    }
    /**
     * To get group id based on the group position
     */
    public long getGroupId(int groupPosition) {
        return (long)( groupPosition*1024 );  // To be consistent with getChildId
    } 
    /**
     * To get group view based on the position
     */
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = null;
        if( convertView != null )
            view = convertView;
        else
            view = inflater.inflate(R.layout.list_item_parent, parent, false); 
        String gt = (String)getGroup( groupPosition );
		TextView colorGroup = (TextView)view.findViewById( R.id.list_item_text_view);
		if( gt != null )
			colorGroup.setText( gt );
        return view;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    } 

    public void onGroupCollapsed (int groupPosition) {} 
    public void onGroupExpanded(int groupPosition) {}
}
