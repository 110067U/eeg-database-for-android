package cz.zcu.kiv.eeg.mobile.base.ws.eegbase;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import cz.zcu.kiv.eeg.mobile.base.R;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonActivity;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonService;
import cz.zcu.kiv.eeg.mobile.base.data.Values;
import cz.zcu.kiv.eeg.mobile.base.ws.ssl.HttpsClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.DONE;
import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.ERROR;
import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.RUNNING;

/**
 * @author Petr Miko
 */
public class UploadDataFile extends CommonService<String, Void, URI> {

    private final static String TAG = UploadDataFile.class.getSimpleName();

    public UploadDataFile(CommonActivity context) {
        super(context);
    }


    @Override
    protected URI doInBackground(String... dataFileContents) {
        SharedPreferences credentials = getCredentials();
        String username = credentials.getString("username", null);
        String password = credentials.getString("password", null);
        String url = credentials.getString("url", null) + Values.SERVICE_BASE + "datafile";

        setState(RUNNING, R.string.working_ws_upload_data_file);
        HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAuthorization(authHeader);
        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));


        // Create a new RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpsClient.getClient()));
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());


        try {
            Log.d(TAG, url);
            FileSystemResource toBeUploadedFile = new FileSystemResource(dataFileContents[2]);
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<String, Object>();
            form.add("experimentId", dataFileContents[0]);
            form.add("description", dataFileContents[1]);
            form.add("file", toBeUploadedFile);

            HttpEntity<Object> entity = new HttpEntity<Object>(form, requestHeaders);
            // Make the network request
            return restTemplate.postForLocation(url, entity);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            setState(ERROR, e);
        } finally {
            setState(DONE);
        }
        return null;
    }

    @Override
    protected void onPostExecute(URI uri) {
        if (uri != null) {
            Toast.makeText(activity, "File was successfully uploaded and now is available on location:\n " + uri.toString(), Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(activity, "File upload was unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }
}
