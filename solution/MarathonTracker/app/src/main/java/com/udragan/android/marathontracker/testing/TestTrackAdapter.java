package com.udragan.android.marathontracker.testing;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.providers.MarathonContract;

/**
 * Adapter for {@link com.udragan.android.marathontracker.providers.MarathonContract.TrackEntry}.
 */
public class TestTrackAdapter extends RecyclerView.Adapter<TestTrackAdapter.TrackViewHolder> {

    // members **********************************************************************************************************

    private Context mContext;
    private Cursor mCursor;
    private int mSelectedIndex;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.testing.TestTrackAdapter} class.
     *
     * @param context context
     * @param cursor  cursor to providing data
     */
    public TestTrackAdapter(Context context,
                            Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mSelectedIndex = RecyclerView.NO_POSITION;
    }

    // overrides ********************************************************************************************************

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.test_track_view, parent, false);

        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder,
                                 int position) {
        mCursor.moveToPosition(position);

        int idColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry._ID);
        int nameColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry.COLUMN_NAME);
        int isCompleteColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry.COLUMN_IS_COMPLETE);
        int durationColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry.COLUMN_DURATION);

        holder.mIdView.setText(String.valueOf(mCursor.getLong(idColumnIndex)));
        holder.mNameView.setText(mCursor.getString(nameColumnIndex));
        holder.mIsComplete.setChecked(mCursor.getInt(isCompleteColumnIndex) == 1);
        holder.mDuration.setText(String.valueOf(mCursor.getLong(durationColumnIndex)));

        int backgroundColor = Color.TRANSPARENT;

        if (position == mSelectedIndex) {
            TypedValue typedValue = new TypedValue();

            if (mContext.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true)) {
                backgroundColor = typedValue.data;
            }
        }

        holder.itemView.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
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

    // ViewHolder class *************************************************************************************************

    class TrackViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mIdView;
        private TextView mNameView;
        private CheckBox mIsComplete;
        private TextView mDuration;

        TrackViewHolder(View itemView) {
            super(itemView);

            mIdView = itemView.findViewById(R.id.track_id_text);
            mNameView = itemView.findViewById(R.id.track_name_text);
            mIsComplete = itemView.findViewById(R.id.track_is_complete_checkbox);
            mDuration = itemView.findViewById(R.id.track_duration_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }

            notifyItemChanged(mSelectedIndex);
            mSelectedIndex = getAdapterPosition();
            notifyItemChanged(mSelectedIndex);

            // TODO: set checkpoints from selected track
        }
    }
}
