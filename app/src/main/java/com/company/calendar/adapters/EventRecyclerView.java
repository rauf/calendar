package com.company.calendar.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.company.calendar.R;
import com.company.calendar.models.Event;

import java.util.ArrayList;

/**
 * Created by abdul on 17-Jun-17.
 */

public class EventRecyclerView extends RecyclerView.Adapter<EventRecyclerView.EventItemViewHolder> {


    private ArrayList<Event> eventsSet;
    private Context context;

    public EventRecyclerView(Context context, ArrayList<Event> events) {
        this.context = context;
        this.eventsSet = events;
    }


    @Override
    public EventItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.listitem_events_list, parent, false);
        return new EventItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventItemViewHolder holder, int position) {
        final Event singleEvent = eventsSet.get(position);

        holder.eventName.setText("<EVENT NAME>");
    }

    @Override
    public int getItemCount() {
        return eventsSet.size();
    }

    static class EventItemViewHolder extends RecyclerView.ViewHolder {

        private TextView eventName;

        public EventItemViewHolder(View itemView) {
            super(itemView);
            eventName = (TextView) itemView.findViewById(R.id.event_name);
        }
    }
}
