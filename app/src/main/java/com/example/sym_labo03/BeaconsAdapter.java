// Authors: Robin Demarta, Loïc Dessaules, Chau Ying Kot

package com.example.sym_labo03;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class BeaconsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<BeaconModel> beacons = null;
    private int maxProgressValue = 100;

    public BeaconsAdapter(Context context) {
        this.context = context;
        this.beacons = new ArrayList<>();
    }

    public void clearBeacons() {
        this.beacons.clear();
        notifyDataSetChanged();
    }

    public void addBeacon(BeaconModel b) {
        this.beacons.add(b);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public View getView(int position, View recycledView, ViewGroup viewGroup) {
        if(recycledView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            recycledView = inflater.inflate(R.layout.item_beacon, viewGroup, false);
        }

        TextView tvUuid = recycledView.findViewById(R.id.itemBeacon_tvUuid);
        TextView tvMinor = recycledView.findViewById(R.id.itemBeacon_tvMinor);
        TextView tvMajor = recycledView.findViewById(R.id.itemBeacon_tvMajor);
        TextView tvRssi = recycledView.findViewById(R.id.itemBeacon_tvRssi);
        ProgressBar pbRssi = recycledView.findViewById(R.id.itemBeacon_pbRssi);

        BeaconModel b = (BeaconModel) getItem(position);

        tvUuid.setText(b.getUuid());
        tvMinor.setText(b.getMinor());
        tvMajor.setText(b.getMajor());
        tvRssi.setText(String.valueOf(b.getRssi()));

        int pbRssiValue = maxProgressValue - Math.abs(b.getRssi()) ;
        pbRssi.setMax(maxProgressValue);
        pbRssi.setProgress(pbRssiValue);

        return recycledView;
    }


}
