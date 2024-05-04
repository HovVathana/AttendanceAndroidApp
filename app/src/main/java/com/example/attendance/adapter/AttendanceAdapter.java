package com.example.attendance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance.R;
import com.example.attendance.model.Attendance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);

        // Bind data to views
        holder.txtName.setText(attendance.getStudent().getFirst_name() + " " + attendance.getStudent().getLast_name());

        // Format register time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE HH:mm", Locale.getDefault());
        Date registerTime = attendance.getRegister_time().toDate();
        holder.txtTime.setText(dateFormat.format(registerTime));

        holder.txtState.setText(attendance.getState());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtTime, txtState;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtState = itemView.findViewById(R.id.textView4);
        }
    }
}
