package com.budi.go_learn.Adapter;

/**
 * Created by root on 12/28/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.budi.go_learn.Models.mPengajar;
import com.budi.go_learn.R;

import java.util.List;

public class lPengajarAdapter extends RecyclerView.Adapter<lPengajarAdapter.ViewHolder> {

    Context context;
    List<mPengajar> listPengajar;

    public lPengajarAdapter(Context context, List<mPengajar> listPengajar) {
        this.context = context;
        this.listPengajar = listPengajar;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mPengajar getDataAdapter1 =  listPengajar.get(position);
        holder.SubjectName.setText(getDataAdapter1.getName());
        Bitmap bmp = getDataAdapter1.getFoto();
        if (bmp != null){
            holder.lpp.setImageBitmap(getDataAdapter1.getFoto());
        }
    }

    @Override
    public int getItemCount() {
        return listPengajar.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView SubjectName;
        public ImageView lpp;
        public ViewHolder(View itemView) {
            super(itemView);
            SubjectName = (TextView) itemView.findViewById(R.id.TextViewCard);
            lpp = (ImageView) itemView.findViewById(R.id.lpp);
        }
    }
}