package cz.zcu.kiv.eeg.mobile.base.ui.base.experiment;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import cz.zcu.kiv.eeg.mobile.base.R;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonActivity;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonService;
import cz.zcu.kiv.eeg.mobile.base.data.Values;
import cz.zcu.kiv.eeg.mobile.base.data.container.Experiment;
import cz.zcu.kiv.eeg.mobile.base.data.container.ExperimentAdapter;
import cz.zcu.kiv.eeg.mobile.base.utils.ConnectionUtils;
import cz.zcu.kiv.eeg.mobile.base.ws.eegbase.FetchExperiments;

import java.util.ArrayList;

/**
 * @author Petr Miko
 *         Date: 19.2.13
 */
public class ListAllExperimentsFragment extends ListFragment {

    private final static String TAG = ListAllExperimentsFragment.class.getSimpleName();
    private static ExperimentAdapter adapter;
    private boolean isDualView;
    private int cursorPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_experiment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        setListAdapter(null);
        View detailsFrame = getActivity().findViewById(R.id.details);
        isDualView = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            cursorPosition = savedInstanceState.getInt("cursorPos", 0);
        }

        if (isDualView) {
            getListView().setSelector(R.drawable.list_selector);
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            showDetails(cursorPosition);
            this.setSelection(cursorPosition);
        }
        setListAdapter(getAdapter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exp_refresh:
                update();
                Log.d(TAG, "Refresh data button pressed");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void update() {

        CommonActivity activity = (CommonActivity) getActivity();
        if (ConnectionUtils.isOnline(activity)) {
            getAdapter().clear();
            (ExperimentActivity.service) = (CommonService) new FetchExperiments(activity, getAdapter(), Values.SERVICE_QUALIFIER_ALL).execute();
        } else
            activity.showAlert(activity.getString(R.string.error_offline));
    }

    public ExperimentAdapter getAdapter() {
        if (adapter == null)
            adapter = new ExperimentAdapter(getActivity(), R.layout.base_experiment_row, new ArrayList<Experiment>());

        return adapter;
    }

    /**
     * Helper function to show the details of a selected item, either by displaying a fragment in-place in the current UI, or starting a whole new
     * activity in which it is displayed.
     */
    void showDetails(int index) {
        cursorPosition = index;

        ExperimentAdapter dataAdapter = getAdapter();
        if (dataAdapter != null && !dataAdapter.isEmpty())
            if (isDualView) {
                getListView().setItemChecked(index, true);

                ExperimentDetailsFragment oldDetails = (ExperimentDetailsFragment) getFragmentManager().findFragmentByTag(ExperimentDetailsFragment.TAG);
                ExperimentDetailsFragment details = new ExperimentDetailsFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();

                if (oldDetails == null) {
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                } else {
                    ft.detach(oldDetails);
                    ft.remove(oldDetails);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                }
                Bundle args = new Bundle();
                args.putInt("index", index);
                args.putSerializable("data", dataAdapter.getItem(index));
                details.setArguments(args);

                ft.replace(R.id.details, details, ExperimentDetailsFragment.TAG);
                ft.commit();

            } else {
                Intent intent = new Intent();
                intent.setClass(getActivity(), ExperimentDetailsActivity.class);
                intent.putExtra("index", index);
                intent.putExtra("data", dataAdapter.getItem(index));
                startActivity(intent);
            }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("cursorPos", cursorPosition);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        showDetails(pos);
        this.setSelection(pos);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.exp_all_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
