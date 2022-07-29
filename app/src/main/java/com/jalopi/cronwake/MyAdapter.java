package com.jalopi.cronwake;

import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    public static MyAdapter adapter;
    private List<AlarmData> alarms;
    private int alarmIndex;

    public int getAlarmIndex() {
        return alarmIndex;
    }
    public List<AlarmData> getAlarms() {
        return alarms;
    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeTextView;
        private final TextView nameTextView;
        private final Switch alarmSwitch;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            timeTextView = view.findViewById(R.id.timeTextView);
            nameTextView = view.findViewById(R.id.nameTextView);
            alarmSwitch = view.findViewById(R.id.alarmSwitch);
            alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    alarms.get(getAdapterPosition()).setActivated(b);

                    AlarmData.writeToFile(adapter.getAlarms(), view.getContext());
                }
            });
        }

        public TextView getNameTextView() {
            return nameTextView;
        }

        public TextView getTimeTextView() {
            return timeTextView;
        }

        public Switch getAlarmSwitch() {
            return alarmSwitch;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param alarms List<AlarmData></AlarmData></AlarmDdata></> containing the data to populate views to be used
     * by RecyclerView.
     */
    public MyAdapter(List<AlarmData> alarms) {
        this.alarms = alarms;
        MyAdapter.adapter = this;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
       // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_element, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                alarmIndex = viewHolder.getAdapterPosition();
                AlarmData alarm = alarms.get(viewHolder.getAdapterPosition());

                Intent launch_intent = new Intent(viewGroup.getContext(), AlarmEditActivity.class);
                launch_intent.putExtra("alarm", alarm.toString());
                viewGroup.getContext().startActivity(launch_intent);
            }
        });

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        AlarmData alarmData = alarms.get(position);
        String timeString = alarmData.getHour() + ":" + (alarmData.getMinute() < 10 ? "0" : "") + alarmData.getMinute();
        viewHolder.getNameTextView().setText(alarmData.getName());
        viewHolder.getTimeTextView().setText(timeString);
        viewHolder.getAlarmSwitch().setChecked(alarmData.getActivated());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return alarms.size();
    }
}

