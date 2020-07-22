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

import com.mapbox.geojson.Feature;
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
import org.smartregister.reveal.presenter.ListTaskPresenter;
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
import static org.smartregister.reveal.util.FamilyConstants.Intent.START_REGISTRATION;

public class EligibilityCompoundActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_GET_JSON = 1234;
    private static final String TAG = EligibilityCompoundActivity.class.getCanonicalName();
    public final static String REGISTER_FAMILY = "org.smartregister.reveal.view.registerFamily";
    ListTasksActivity listTasksActivity;
    ListTaskPresenter listTaskPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.native_form_basic).setOnClickListener(this);
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            String jsonString = data.getStringExtra("json");
//            Timber.i("Result json String !!!! %s", jsonString);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//        Intent intent = new Intent(this, ListTasksActivity.class);
//        intent.putExtra(REGISTER_FAMILY, "registerFamily");

//        if (resultCode == RESULT_OK) {
//            String jsonString = data.getStringExtra("json");
//            Timber.i("Result json String !!!! %s", jsonString);
//        }

        super.onActivityResult(requestCode, resultCode, data);
//        listTasksActivity.clearSelectedFeature();
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(START_REGISTRATION, true);
        Feature feature = listTaskPresenter.getSelectedFeature();
        intent.putExtra(Constants.Properties.LOCATION_UUID, feature.id());
        intent.putExtra(Constants.Properties.TASK_IDENTIFIER, feature.getStringProperty(Constants.Properties.TASK_IDENTIFIER));
        intent.putExtra(Constants.Properties.TASK_BUSINESS_STATUS, feature.getStringProperty(Constants.Properties.TASK_BUSINESS_STATUS));
        intent.putExtra(Constants.Properties.TASK_STATUS, feature.getStringProperty(Constants.Properties.TASK_STATUS));
        if (feature.hasProperty(Constants.Properties.STRUCTURE_NAME))
            intent.putExtra(Constants.Properties.STRUCTURE_NAME, feature.getStringProperty(Constants.Properties.STRUCTURE_NAME));
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startForm(int jsonFormActivityRequestCode, String formName, String entityId, boolean translate) throws Exception {

//        final String STEP1 = "step1";
//        final String FIELDS = "fields";
//        final String KEY = "key";
//        final String ZEIR_ID = "ZEIR_ID";
//        final String VALUE = "value";

        String currentLocationId = "Nigeria";

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
                form.setActionBarBackground(R.color.family_actionbar);
                form.setNavigationBackground(R.color.family_navigation);
                form.setHideSaveLabel(true);
                intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                startActivityForResult(intent, jsonFormActivityRequestCode);
            } else {
                if (entityId == null) {
                    entityId = "ABC" + Math.random();
                }

//                // Inject zeir id into the form
//                JSONObject stepOne = jsonForm.getJSONObject(STEP1);
//                JSONArray jsonArray = stepOne.getJSONArray(FIELDS);
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    if (jsonObject.getString(KEY)
//                            .equalsIgnoreCase(ZEIR_ID)) {
//                        jsonObject.remove(VALUE);
//                        jsonObject.put(VALUE, entityId);
//                    }
//                }

                Intent intent = new Intent(this, JsonWizardFormActivity.class);
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
        int id = view.getId();
        try {
            if (id == R.id.native_form_basic) {
                startForm(REQUEST_CODE_GET_JSON, "nigeria_eligibility_compound", null, false);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
