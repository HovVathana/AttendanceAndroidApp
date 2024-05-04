package com.example.attendance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance.R;
import com.example.attendance.model.Schedule;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private List<Schedule> scheduleList;

    public ScheduleAdapter(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule schedule = scheduleList.get(position);

        // Bind data to views
        holder.txtClass.setText(schedule.getClass_course().getClass_name());
        holder.txtSchedule.setText(schedule.getDay() + " " + schedule.getStart_time() + " - " + schedule.getEnd_time());
        holder.txtTeacher.setText(schedule.getClass_course().getTeacher().getFirst_name() + " " + schedule.getClass_course().getTeacher().getLast_name());
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtClass, txtSchedule, txtTeacher;

        ViewHolder(View itemView) {
            super(itemView);
            txtClass = itemView.findViewById(R.id.txtClass);
            txtSchedule = itemView.findViewById(R.id.txtSchedule);
            txtTeacher = itemView.findViewById(R.id.txtTeacher);
        }
    }
}
