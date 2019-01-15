package org.chenming.btdemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LogViewAdapter extends RecyclerView.Adapter<LogViewAdapter.LogViewHolder> {

    private Context context;
    private List<String> logs;

    public LogViewAdapter(Context context, List<String> logs) {
        this.context = context;
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log, viewGroup, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder logViewHolder, int i) {
        TextView textLog = logViewHolder.textLog;
        textLog.setText(logs.get(i));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    class LogViewHolder extends RecyclerView.ViewHolder {

        TextView textLog;

        LogViewHolder(@NonNull View itemView) {
            super(itemView);
            textLog = itemView.findViewById(R.id.text_log);
        }
    }
}
