package android.example.quantleguirebuild;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by Matthias Niel on 21.03.2019.
 */
public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder> {

    private final LinkedList<String> mHistoryItemList;
    private LayoutInflater mInflater;


    public HistoryListAdapter(Context context, LinkedList<String> historyList) {
        mInflater = LayoutInflater.from(context);
        this.mHistoryItemList = historyList;
    }

    @NonNull
    @Override
    public HistoryListAdapter.HistoryViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View mItemView = mInflater.inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(HistoryListAdapter.HistoryViewHolder holder, int i) {
        String mCurrent = mHistoryItemList.get(i);
        holder.tv.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mHistoryItemList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        public final TextView tv;
        final HistoryListAdapter mAdapter;

        public HistoryViewHolder(View view, HistoryListAdapter adapter) {
            super(view);
            tv = itemView.findViewById(R.id.history_item);
            this.mAdapter = adapter;
        }
    }
}
