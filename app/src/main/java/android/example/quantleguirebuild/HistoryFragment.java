package android.example.quantleguirebuild;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnHistoryFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class HistoryFragment extends Fragment {

    private OnHistoryFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private HistoryListAdapter mAdapter;
    private final LinkedList<String> mHistoryList = new LinkedList<>();


    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Put initial data into the word list.
        for (int i = 0; i < 20; i++) {
            mHistoryList.addLast("Word " + i);
        }

        // Inflate the layout for this fragment
        // Have to inflate first before we can find views by id
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        // Get a handle to the RecyclerView.
        mRecyclerView = view.findViewById(R.id.list_history);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new HistoryListAdapter(getContext(), mHistoryList); //maybe use getActivity instead here?
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onHistoryFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHistoryFragmentInteractionListener) {
            mListener = (OnHistoryFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnHistoryFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnHistoryFragmentInteractionListener {
        // TODO: Update argument type and name
        void onHistoryFragmentInteraction(Uri uri);
    }
}
