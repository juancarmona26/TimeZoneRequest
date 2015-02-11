package co.mobilemakers.githubrepos;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TimeZoneFragment extends Fragment {

    private static final String LOG_TAG = TimeZoneFragment.class.getSimpleName();
    EditText mEditTextLocation;
    TextView mTextViewTimeZone;


    public TimeZoneFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time_zone, container, false);
        mEditTextLocation = (EditText) rootView.findViewById(R.id.edit_text_location);
        mTextViewTimeZone = (TextView) rootView.findViewById(R.id.text_view_time_zone);
        Button button_get_Repos = (Button) rootView.findViewById(R.id.button_get_locations);
        button_get_Repos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = mEditTextLocation.getText().toString();
                String message = String.format(getString(R.string.getting_locations), location);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                new FetchTimeZoneTask().execute(location);


            }
        });
        return rootView;
    }

    private URL constructQuery(String location) throws MalformedURLException {
        final String WORLD_WEATHER_BASE_URL = "api.worldweatheronline.com";
        final String TIME_ZONE_PATH = "free/v2/";
        final String REPOS_ENDPOINT  = "tz.ashx";
        final String KEY_NAME = "key";
        final String LOCATION_PARAMETER_NAME = "q";
        final String FORMAT_PARAMETER_NAME = "format";
        final String FORMAT = "json";
        final String API_KEY_ACCESS = "26204372f38320e5b6c04305fd0ed";


        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").authority(WORLD_WEATHER_BASE_URL).
                appendEncodedPath(TIME_ZONE_PATH).appendPath(REPOS_ENDPOINT).appendQueryParameter(KEY_NAME, API_KEY_ACCESS).

                appendQueryParameter(LOCATION_PARAMETER_NAME, location).appendQueryParameter(FORMAT_PARAMETER_NAME, FORMAT);


        Uri uri = builder.build();
        Log.d(LOG_TAG, "Built URI: " + uri.toString());


        return new URL(uri.toString());
    }

    private String readFullResponse(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String response = "";
        String line;
        while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        if(stringBuilder.length() > 0){
            response = stringBuilder.toString();
        }

        return response;
    }

    class FetchTimeZoneTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String location;
            String listOfTimeZones = "";
            if(params.length > 0){
                location = params[0];
            } else {
                location = "Mexico";
            }
            try {
                URL url  = constructQuery(location);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                try {
                    String response = readFullResponse(httpConnection.getInputStream());
                    Log.d(LOG_TAG, "Response: " + response);
                    listOfTimeZones = parseResponse(response);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                } finally {
                    httpConnection.disconnect();
                }

            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            return listOfTimeZones;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            mTextViewTimeZone.setText(response);
        }
    }

    private String parseResponse(String response) {
        final String DATA_LOCATION = "data";
        final String LOCAL_ZONE = "time_zone";
        final String LOCAL_TIME = "localtime";
        final String UTC = "utcOffset";
        List<String> repos = new ArrayList<>();
        try {
            JSONObject responseJsonObject = new JSONObject(response);
            JSONArray data = responseJsonObject.getJSONObject(DATA_LOCATION).getJSONArray(LOCAL_ZONE);
            JSONObject localTime = data.getJSONObject(0);
            repos.add("Local Time: " + localTime.getString(LOCAL_TIME) + "\n");
            repos.add("UTC:" + localTime.getString(UTC));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return TextUtils.join(" ",repos);
    }
}
