package com.example.attendance.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.attendance.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendance.model.Class;
import com.example.attendance.model.Schedule;
import com.example.attendance.model.User;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {
    private List<Class> classList;

    public ClassAdapter(List<Class> classList) {
        this.classList = classList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Class myClass = classList.get(position);
        holder.txtClassName.setText(myClass.getClass_name());

        // Set teacher name
        User teacher = myClass.getTeacher();
        if (teacher != null) {
            String teacherName = teacher.getFirst_name() + " " + teacher.getLast_name();
            holder.txtTeacher.setText(teacherName);
        } else {
            holder.txtTeacher.setText("");
        }

        // Construct schedule text
//        StringBuilder scheduleText = new StringBuilder();
//        List<Schedule> schedules = myClass.getSchedules();
//
//        for (Schedule schedule : schedules) {
//            // Extract the first three letters of the day of the week
//            String abbreviatedDayOfWeek = schedule.getDayOfWeek().substring(0, Math.min(schedule.getDayOfWeek().length(), 3));
//
//            // Append the abbreviated day of the week along with other schedule details
//            scheduleText.append(abbreviatedDayOfWeek)
//                    .append(": ")
//                    .append(schedule.getStartTime())
//                    .append(" - ")
//                    .append(schedule.getEndTime())
//                    .append(" (")
//                    .append(schedule.getRoom())
//                    .append(")")
//                    .append("\n");
//        }

//        holder.txtSchedule.setText(scheduleText.toString());
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtClassName;
        TextView txtTeacher;
        TextView txtSchedule;
        TextView txtRoom;

        ViewHolder(View itemView) {
            super(itemView);
            txtClassName = itemView.findViewById(R.id.txtClass);
            txtTeacher = itemView.findViewById(R.id.txtTeacher);
            txtSchedule = itemView.findViewById(R.id.txtSchedule);
        }
    }
}

