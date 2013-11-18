/*
 * Copyright (c) 2013. All Rights Reserved
 * Yavor Paunov
 */

package walk.around.view;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import walk.around.api.GoogleMaps;

public class LocationSearchAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> mResultList;

    public  LocationSearchAdapter(Context context, int textViewResource){
        super(context, textViewResource);
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public String getItem(int index) {
        return mResultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<String> results = new ArrayList<String>();

                if (constraint != null) {
                    String rawResult = GoogleMaps.autocomplete(constraint.toString());
                    try {
                        JSONObject jsonResult = new JSONObject(rawResult);
                        String status = jsonResult.getString("status");
                        if(status.equals("OK")) {
                            JSONArray jsonPredictions = jsonResult.getJSONArray("predictions");
                            for(int i=0; i < jsonPredictions.length(); i++) {
                                JSONObject jsonPrediction = jsonPredictions.getJSONObject(i);
                                String description = jsonPrediction.getString("description");
                                results.add(description);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    filterResults.values = results;
                    filterResults.count = results.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResultList = (ArrayList<String>)results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};

        return filter;
    }

}
