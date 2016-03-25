package com.liron.ots.users;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.liron.ots.MainActivity;
import com.liron.ots.OTSUser;
import com.liron.ots.R;
import com.liron.ots.WelcomeActivity;
import com.liron.ots.analytic.AnalyticsApplication;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity
{
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        // Set up the login form.

        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });

        Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin()
    {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username))
        {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username))
        {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            new CheckTeamMemeber(this, username, password).execute();
        }
    }

    private boolean isUsernameValid(String username)
    {
        //TODO: Replace this with your own logic
        return username.length() > 4;
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private class CheckTeamMemeber extends AsyncTask<String, Void, ParseException>
    {
        String mUserName;
        LoginActivity mActivity;
        String mPassword;

        public CheckTeamMemeber(LoginActivity activity, String userName, String password)
        {
            mUserName = userName;
            mActivity = activity;
            mPassword = password;
        }

        private ParseException TryLogin()
        {
            try
            {
                ParseUser.logIn(mUserName, mPassword);
                return null;
            } catch (ParseException e)
            {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected ParseException doInBackground(String... params)
        {
            try
            {
                ParseException result = TryLogin();
                if (result == null)
                {
                    return null;
                }

                ParseQuery<OTSUser> query = ParseQuery.getQuery(OTSUser.class);
                query.whereEqualTo("email", mUserName);
                query.whereEqualTo("phone", mPassword);
                OTSUser foundUser = query.getFirst();

                if (!foundUser.isActive())
                {
                    foundUser.setIsActive(true);
                    foundUser.save();

                    ParseUser user = new ParseUser();
                    user.setUsername(foundUser.getEmail());
                    user.setPassword(foundUser.getPhone());

                    // other fields can be set just like with ParseObject
                    user.put("phone", foundUser.getPhone());

                    //TODO: is manager logic
                    user.put("is_manager", false);
                    user.put("team", foundUser.getTeamName());
                    user.put("manager_name", foundUser.getManagerName());
                    user.put("is_active", false);

                    user.signUp();
                }

                return null;

            } catch (ParseException e)
            {
                e.printStackTrace();
                return e;
            }
        }

        @Override
        protected void onPostExecute(ParseException result)
        {
            mActivity.showProgress(false);
            showProgress(false);
            if (result == null)
            {
                ParseUser user = ParseUser.getCurrentUser();
                // Hooray! The user is logged in.
                boolean is_manager = user.getBoolean("is_manager");
                boolean is_active = user.getBoolean("is_active");
                if (is_manager)
                {
                    Snackbar.make(LoginActivity.this.findViewById(R.id.root_view), R.string.logged_in_success, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    if  (is_active)
                    {
                        Snackbar.make(LoginActivity.this.findViewById(R.id.root_view), R.string.logged_in_success, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Snackbar.make(LoginActivity.this.findViewById(R.id.root_view), R.string.logged_in_success, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

            } else
            {
                Snackbar.make(LoginActivity.this.findViewById(R.id.root_view), result.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        @Override
        protected void onPreExecute()
        {
            mActivity.showProgress(true);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mTracker.setScreenName("Login");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}

