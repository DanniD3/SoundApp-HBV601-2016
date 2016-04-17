package thepack.soundapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import thepack.soundapp.entities.User;
import thepack.soundapp.utils.Util;

/**
 * A login screen that offers login via email/password.
 */
public class LoginFragment extends Fragment {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private MainActivity act;

    private static final String REST_USER_URL =
            "http://" + Util.HOST_URL + "/rest/api/user/crud/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the calling activity as MainActivity for reference and changing user
        act = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        // Set up the login form.
        mNameView = (EditText) rootView.findViewById(R.id.name);
        mPasswordView = (EditText) rootView.findViewById(R.id.password);

        /*
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        }); */

        Button mNameSignInButton = (Button) rootView.findViewById(R.id.name_sign_in_button);
        mNameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = rootView.findViewById(R.id.login_form);
        mProgressView = rootView.findViewById(R.id.login_progress);

        return rootView;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        Util.hideKeyboardFromView(act,mPasswordView);
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String pw = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(pw)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(name, pw);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, User> {

        private final String mName;
        private final String mPassword;

        UserLoginTask(String name, String pw) {
            mName = name;
            mPassword = pw;
        }

        @Override
        protected User doInBackground(Void... params) {
            User responseUser = null;
            HttpURLConnection conn = null;

            try {
                conn = (HttpURLConnection) new URL(REST_USER_URL + mName).openConnection();

                // If no user is found, create new user
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    // Disconnect and reconnect
                    conn.disconnect();
                    conn = (HttpURLConnection) new URL(REST_USER_URL).openConnection();

                    // set to POST
                    conn = Util.setPostConnection(conn);
                    JSONObject POST_PARAM = new JSONObject();
                    POST_PARAM.put("name", mName);
                    POST_PARAM.put("pw", mPassword);
                    Util.sendPostJSON(conn, POST_PARAM);

                    if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED){
                        throw new IOException(conn.getResponseMessage() +": with " + REST_USER_URL);
                    }

                    conn.disconnect();
                    conn = (HttpURLConnection) new URL(REST_USER_URL + mName).openConnection();
                }

                responseUser = Util.parseUserJson(Util.getResponseString(conn));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return responseUser;
        }

        @Override
        protected void onPostExecute(User responseUser) {
            mAuthTask = null;
            showProgress(false);

            if (responseUser == null) {
                Toast.makeText(act, R.string.error_incorrect_name, Toast.LENGTH_LONG).show();
            } else {
                if (!responseUser.getPw().equals(mPassword)) {
                    // TODO mPassword hashing
                    Toast.makeText(act, R.string.error_incorrect_password, Toast.LENGTH_LONG).show();
                } else {

                    // TODO finish Login and go back to Main and set user to Navigation
                    Toast.makeText(act, R.string.success_sign_in, Toast.LENGTH_LONG).show();

                    Bundle data = new Bundle();
                    data.putString("username", responseUser.getName());
                    act.displayHome(data);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

