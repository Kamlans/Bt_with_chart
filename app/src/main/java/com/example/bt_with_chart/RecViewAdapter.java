package com.example.bt_with_chart;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.ViewHolder> {
    private static final String TAG = "kamlans";

    private Context context;
    private List<Model> list;

    public RecViewAdapter(Context context, List<Model> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_device_row , parent , false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.name.setText(list.get(position).getName());
        holder.mac.setText(list.get(position).getMacAddress());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , MainActivity.class);
                intent.putExtra("name" , list.get(position).getName() );
                intent.putExtra("mac" , list.get(position).getMacAddress() );
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                MainActivity.ClientClass clientClass = new MainActivity.ClientClass(list.get(position).getDevice());
                try {
                    clientClass.start();
                    context.startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "onClick: "+e);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name , mac;
        private LinearLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.singleRowName);
            mac = itemView.findViewById(R.id.singleRowMacAddress);
            layout = itemView.findViewById(R.id.linearLayoutofsinglerow);
        }
    }
}
