package org.smartregister.reveal.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.reveal.R;
import org.smartregister.reveal.contract.ChildFilterFragmentContract;
import org.smartregister.reveal.interactor.ChildFilterFragmentInteractor;
import org.smartregister.reveal.model.ChildFilterFragmentModel;
import org.smartregister.reveal.presenter.ChildFilterFragmentPresenter;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.view.ChildRegisterActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;


public class ChildFilterFragment extends Fragment implements ChildFilterFragmentContract.View {
    public static final String TAG = "ChildFilterFragment";
    private ChildFilterFragmentContract.Presenter presenter;
    private String schoolID = null; // set this value later
    protected ProgressBar progressBar;
    protected AtomicInteger incompleteRequests = new AtomicInteger(0);
    private View view;
    private LinearLayout linearLayoutGrades;
    private LinearLayout linearLayoutAges;
    private RadioButton radioGradeName;
    private RadioButton radioGradeAge;
    private RadioButton radioAge;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.child_register_filter_fragment, container, false);
        bindLayout();
        loadPresenter();
        reloadParameters();

        return view;
    }

    private AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public void bindLayout() {
        // toolbar
        Toolbar toolbar = view.findViewById(R.id.filter_tasks_toolbar);
        toolbar.setTitle(R.string.filter);
        getAppCompatActivity().setSupportActionBar(toolbar);
        getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getAppCompatActivity().getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        toolbar.setNavigationOnClickListener(v -> getActivity().finish());

        Button btnApplyFilters = view.findViewById(R.id.apply_filters);
        btnApplyFilters.setOnClickListener(v -> executeFilter());

        radioGradeName = view.findViewById(R.id.radioGradeName);
        radioGradeAge = view.findViewById(R.id.radioGradeAge);
        radioAge = view.findViewById(R.id.radioAge);

        view.findViewById(R.id.clear_filters).setOnClickListener(v -> clearFilters());

        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        linearLayoutGrades = view.findViewById(R.id.linearLayoutGrades);
        linearLayoutGrades.removeAllViews();

        linearLayoutAges = view.findViewById(R.id.linearLayoutAges);
        linearLayoutAges.removeAllViews();
    }

    @Override
    public ChildFilterFragmentContract.Presenter loadPresenter() {
        if (presenter == null) {
            presenter = new ChildFilterFragmentPresenter()
                    .usingView(this)
                    .usingInteractor(new ChildFilterFragmentInteractor<>())
                    .usingModel(new ChildFilterFragmentModel());
        }
        return presenter;
    }

    @Override
    public void reloadParameters() {
        presenter.fetchUniqueAges(schoolID);
        presenter.fetchUniqueGrades(schoolID);
    }

    @Override
    public void resetFilters(Map<String, List<String>> filters) {

    }

    @Override
    public HashMap<String, List<String>> getFilterValues() {
        HashMap<String, List<String>> result = new HashMap<>();
        // get sort
        List<String> sort = new ArrayList<>();
        if (radioGradeName.isChecked()) {
            sort.add(Constants.ChildFilter.SORT_GRADE);
            sort.add(Constants.ChildFilter.SORT_LAST_NAME);
        }

        if (radioGradeAge.isChecked()) {
            sort.add(Constants.ChildFilter.SORT_GRADE);
            sort.add(Constants.ChildFilter.SORT_AGE);
        }

        if (radioAge.isChecked())
            sort.add(Constants.ChildFilter.SORT_AGE);

        result.put(Constants.ChildFilter.SORT, sort);

        result.put(Constants.ChildFilter.FILTER_GRADE, getSelectedCheckBoxValues(linearLayoutGrades));
        result.put(Constants.ChildFilter.FILTER_AGE, getSelectedCheckBoxValues(linearLayoutAges));

        return result;
    }

    private void readLayoutCheckBoxes(LinearLayout rootLayout, Consumer<CheckBox> consumer) {
        int childCount = rootLayout.getChildCount();
        while (childCount > 0) {
            View view = rootLayout.getChildAt(childCount - 1);
            if (view instanceof CheckBox)
                consumer.accept(((CheckBox) view));

            childCount--;
        }
    }

    private void resetCheckBoxes(LinearLayout rootLayout) {
        readLayoutCheckBoxes(rootLayout, checkBox -> checkBox.setChecked(false));
    }

    private List<String> getSelectedCheckBoxValues(LinearLayout rootLayout) {
        List<String> selectedValues = new ArrayList<>();

        readLayoutCheckBoxes(rootLayout, checkBox -> {
            if (checkBox.isChecked())
                selectedValues.add(checkBox.getText().toString());
        });

        return selectedValues;
    }

    @Override
    public void clearFilters() {
        radioGradeName.setChecked(true);
        resetCheckBoxes(linearLayoutGrades);
        resetCheckBoxes(linearLayoutAges);
    }

    @Override
    public void onGradesFetched(List<String> grades) {
        linearLayoutGrades.removeAllViews();
        linearLayoutGrades.setVisibility(grades.size() > 0 ? View.VISIBLE : View.GONE);
        for (String grade : grades) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(StringUtils.isBlank(grade) ? "Unknown" : grade);
            linearLayoutGrades.addView(checkBox);
        }
    }

    @Override
    public void onAgesFetched(List<String> ages) {
        linearLayoutAges.removeAllViews();
        for (String age : ages) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(StringUtils.isBlank(age) ? "Unknown" : age);
            linearLayoutAges.addView(checkBox);
        }
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(getContext(), R.string.an_error_occured, Toast.LENGTH_SHORT).show();
        Timber.e(e);
    }

    @Override
    public void setLoadingState(boolean loadingState) {
        int result = loadingState ? incompleteRequests.incrementAndGet() : incompleteRequests.decrementAndGet();
        progressBar.setVisibility(result > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void executeFilter() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.ChildFilter.FILTER_PAYLOAD, getFilterValues());
        ChildRegisterActivity.startFragment(getActivity(), ChildRegisterFragment.TAG, null, true);
        getActivity().finish();
    }
}
