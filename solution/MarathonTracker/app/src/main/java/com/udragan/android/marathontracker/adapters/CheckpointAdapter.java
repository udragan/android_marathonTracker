package com.udragan.android.marathontracker.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.providers.MarathonContract;

/**
 * Adapter for {@link com.udragan.android.marathontracker.providers.MarathonContract.CheckpointEntry}
 */
public class CheckpointAdapter extends RecyclerView.Adapter<CheckpointAdapter.CheckpointViewHolder> {

    // members **********************************************************************************************************

    private Context mContext;
    private Cursor mCursor;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.adapters.CheckpointAdapter} class.
     *
     * @param context the calling context/activity.
     * @param cursor  cursor to providing data
     */
    public CheckpointAdapter(Context context,
                             Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    // public methods ***************************************************************************************************

    /**
     * Swap adapter cursor when new data is available.
     *
     * @param cursor data provider
     */
    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = cursor;

        if (mCursor != null) {
            notifyDataSetChanged();
        }
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
        mCursor.moveToPosition(position);

        int idColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry._ID);
        int nameColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_NAME);
        int indexColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_INDEX);
        int latitudeColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_LATITUDE);
        int longitudeColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_LONGITUDE);
        int isCheckedColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_IS_CHECKED);
        int timeColumnIndex = mCursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_TIME);

        holder.id = mCursor.getInt(idColumnIndex);
        holder.index = mCursor.getInt(indexColumnIndex);
        holder.isCheckedCheckbox.setChecked(mCursor.getInt(isCheckedColumnIndex) == 1);
        holder.nameText.setText(mCursor.getString(nameColumnIndex));
        holder.latitudeText.setText(mCursor.getString(latitudeColumnIndex));
        holder.longitudeText.setText(mCursor.getString(longitudeColumnIndex));
        holder.timeText.setText(String.valueOf(mCursor.getLong(timeColumnIndex)));
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    // ViewHolder class *************************************************************************************************

    class CheckpointViewHolder extends RecyclerView.ViewHolder {

        private int id;
        private int index;

        private CheckBox isCheckedCheckbox;
        private TextView nameText;
        private TextView latitudeText;
        private TextView longitudeText;
        private TextView timeText;

        CheckpointViewHolder(View itemView) {
            super(itemView);

            isCheckedCheckbox = itemView.findViewById(R.id.is_checked_checkpoint_checkBox);
            nameText = itemView.findViewById(R.id.name_checkpoint_text);
            latitudeText = itemView.findViewById(R.id.latitude_checkpoint_text);
            longitudeText = itemView.findViewById(R.id.longitude_checkpoint_text);
            timeText = itemView.findViewById(R.id.time_checkpoint_text);
        }
    }
}
