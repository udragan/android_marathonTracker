package com.udragan.android.marathontracker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.models.CheckpointModel;

import java.util.ArrayList;

/**
 * Adapter for {@link com.udragan.android.marathontracker.models.CheckpointModel}
 */
public class CheckpointAdapter extends RecyclerView.Adapter<CheckpointAdapter.CheckpointViewHolder> {

    // members **********************************************************************************************************

    private Context mContext;
    private ArrayList<CheckpointModel> mCheckpoints;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.adapters.CheckpointAdapter} class.
     *
     * @param context     the calling context/activity.
     * @param checkpoints initial list of checkpoints.
     */
    public CheckpointAdapter(Context context, ArrayList<CheckpointModel> checkpoints) {
        mContext = context;
        mCheckpoints = checkpoints;
    }

    // RecyclerView.Adapter *********************************************************************************************

    @Override
    public CheckpointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.checkpoint_view, parent, false);
        return new CheckpointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CheckpointViewHolder holder, int position) {
        long latitude = mCheckpoints.get(position).getLatitude();
        long longitude = mCheckpoints.get(position).getLongitude();
        holder.latitudeText.setText(String.valueOf(latitude));
        holder.longitudeText.setText(String.valueOf(longitude));
    }

    @Override
    public int getItemCount() {
        if (mCheckpoints == null) {
            return 0;
        }

        return mCheckpoints.size();
    }

    // ViewHolder class *************************************************************************************************

    class CheckpointViewHolder extends RecyclerView.ViewHolder {

        private TextView latitudeText;
        private TextView longitudeText;

        CheckpointViewHolder(View itemView) {
            super(itemView);

            latitudeText = itemView.findViewById(R.id.latitude_checkpoint_text);
            longitudeText = itemView.findViewById(R.id.longitude_checkpoint_text);
        }
    }
}
