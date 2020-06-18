package org.smartregister.reveal.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.reveal.R;
import org.smartregister.reveal.contract.OtherFormsContract;
import org.smartregister.reveal.presenter.OtherFormsPresenter;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.RevealJsonFormUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import timber.log.Timber;

import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.RequestCode.REQUEST_CODE_GET_JSON;

public class EligibilityCompoundActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_GET_JSON = 1234;
    private static final String TAG = EligibilityCompoundActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String jsonString = data.getStringExtra("json");
            Timber.i("Result json String !!!! %s", jsonString);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startForm(int jsonFormActivityRequestCode, String formName, String entityId, boolean translate) throws Exception {

        final String STEP1 = "step1";
        final String FIELDS = "fields";
        final String KEY = "key";
        final String ZEIR_ID = "ZEIR_ID";
        final String VALUE = "value";

        String currentLocationId = "NIGERIA";


        JSONObject jsonForm = getFormJson(formName);
        if (jsonForm != null) {
            jsonForm.getJSONObject("metadata").put("encounter_location", currentLocationId);

            if ("nigeria_eligibility_compound".equals(formName)) {
                Intent intent = new Intent(this, JsonWizardFormActivity.class);
                intent.putExtra("json", jsonForm.toString());
                Timber.d("form is %s", jsonForm.toString());

                Form form = new Form();
                form.setName("Eligibility Compound Form");
                form.setWizard(true);
                form.setNextLabel(getString(R.string.next));
                form.setPreviousLabel(getString(R.string.previous));
                form.setSaveLabel(getString(R.string.save));
                form.setActionBarBackground(R.color.customAppThemeBlue);
                form.setNavigationBackground(R.color.button_navy_blue);
                form.setHideSaveLabel(true);
                intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                startActivityForResult(intent, jsonFormActivityRequestCode);
            } else {
                if (entityId == null) {
                    entityId = "ABC" + Math.random();
                }

                // Inject zeir id into the form
                JSONObject stepOne = jsonForm.getJSONObject(STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(KEY)
                            .equalsIgnoreCase(ZEIR_ID)) {
                        jsonObject.remove(VALUE);
                        jsonObject.put(VALUE, entityId);
                    }
                }

                Intent intent = new Intent(this, JsonFormActivity.class);
                intent.putExtra("json", jsonForm.toString());
                intent.putExtra(Constants.JsonForm.PERFORM_FORM_TRANSLATION, translate);
                Timber.d("form is %s", jsonForm.toString());
                startActivityForResult(intent, jsonFormActivityRequestCode);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public JSONObject getFormJson(String formIdentity) {

        try {
            InputStream inputStream = getApplication().getAssets()
                    .open("json.form/" + formIdentity + ".json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                    StandardCharsets.UTF_8));
            String jsonString;
            StringBuilder stringBuilder = new StringBuilder();
            while ((jsonString = reader.readLine()) != null) {
                stringBuilder.append(jsonString);
            }
            inputStream.close();

            return new JSONObject(stringBuilder.toString());
        } catch (IOException e) {
            Timber.e(e);
            ;
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View view) {
        try {
            startForm(REQUEST_CODE_GET_JSON, "nigeria_eligibility_compound", null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//    private OtherFormsPresenter presenter;
//    private RevealJsonFormUtils jsonFormUtils;
//    private ProgressDialog progressDialog;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.compound_eligibility);
//        presenter = new OtherFormsPresenter(this);
//        Toolbar toolbar = this.findViewById(R.id.summary_toolbar);
//        toolbar.setTitle(R.string.eligibility_compound_return);
//        this.setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(view -> onBackPressed());
//        setupViews();
//    }
//
//    protected void setupViews() {
//        TabLayout tabLayout = findViewById(R.id.tabs);
//        ViewPager viewPager = findViewById(R.id.viewpager);
//        tabLayout.setupWithViewPager(setupViewPager(viewPager));
//    }
//    protected ViewPager setupViewPager(ViewPager viewPager) {
//        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
//
//        jsonFormUtils = new RevealJsonFormUtils();
//
//        viewPager.setAdapter(adapter);
//
//        return viewPager;
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == RESULT_OK && data.hasExtra(JSON_FORM_PARAM_JSON)) {
//            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
//            Timber.d( json);
//            getPresenter().saveJsonForm(json);
//        }
//    }
//
//    @Override
//    public void startFormActivity(JSONObject jsonObject) {
//        jsonFormUtils.startJsonForm(jsonObject, this);
//    }
//
//    @Override
//    public void saveJsonForm(String json) {
//
//    }
//
//    @Override
//    public void showProgressDialog(int titleIdentifier) {
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setCancelable(false);
//        progressDialog.setTitle(titleIdentifier);
//        progressDialog.setMessage(getString(R.string.please_wait_message));
//        if (!isFinishing())
//            progressDialog.show();
//    }
//
//    @Override
//    public void hideProgressDialog() {
//        if (progressDialog != null) {
//            progressDialog.dismiss();
//        }
//    }
//
//    private OtherFormsContract.Presenter getPresenter() {
//        return presenter;
//    }
}
