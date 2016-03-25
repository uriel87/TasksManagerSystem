package com.liron.ots;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.liron.ots.users.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CreateTeamActivity extends AppCompatActivity
{
    List<TeamMember> members;
    TeamRecyclerAdapter mAdapter;
    RecyclerView mMembersView;
    TextView mTeamName;
    TextView mMemberEmail;
    TextView mMemberPhone;
    View mProgressView;
    View mLoginFormView;
    ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        mMemberEmail = (TextView) findViewById(R.id.member_email);
        mMemberPhone = (TextView) findViewById(R.id.member_phone);
        mTeamName = (TextView) findViewById(R.id.team_name);

        if (!checkUser())
        {
            finish();
        }

        initMembersView();

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private boolean checkUser()
    {
        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser == null) {
            // show the signup or login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    private void initMembersView()
    {
        members = new ArrayList<>();

        mMembersView = (RecyclerView) findViewById(R.id.members_list);
        mMembersView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMembersView.setLayoutManager(linearLayoutManager);
        mMembersView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new TeamRecyclerAdapter(this, members);
        mMembersView.setAdapter(mAdapter);

        if (mCurrentUser.getString("team") != null)
        {
            mTeamName.setText(mCurrentUser.getString("team"));
            mTeamName.setKeyListener(null);
        }
    }


    public void onAddMemberClick(View view)
    {
        boolean cancel = false;
        View focusView = null;

        mMemberEmail.setError(null);
        mMemberPhone.setError(null);

        String email = mMemberEmail.getText().toString();
        String phone = mMemberPhone.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            mMemberEmail.setError(getString(R.string.error_invalid_email));
            focusView = mMemberEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone))
        {
            mMemberPhone.setError(getString(R.string.error_invalid_phone));
            focusView = mMemberPhone;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        TeamMember member = new TeamMember();

        member.setEmail(email);
        member.setPhone(phone);

        mAdapter.add(member);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == 177)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
            {
                TeamMember member = (TeamMember) data.getSerializableExtra("member");

                if (member != null)
                {
                    mAdapter.add(member);
                }
            }
        }
    }

    public void onSendInviteClick(View view)
    {
        //TODO: fix html and validate members and teamname
        List<String> emails = new ArrayList<>();
        for (TeamMember member : members)
        {
            emails.add(member.getEmail());
        }

        String packageName = getPackageName();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_EMAIL, emails.toArray(new String[emails.size()]));
        i.putExtra(Intent.EXTRA_SUBJECT, "Invitation to OTS");
        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml("Hi" +
                "\n<br>" +
                "\nYou have been invited to be a team member in an OTS Team created by me." +
                "\n<br>" +
                "\nUse this link to download and install the App from Google Play." +
                "\n<br>" +
                "\nhttps://play.google.com/store/apps/details?id=" + packageName + "" +
                "\n<br>" +
                "\n\nLogin with your email as user name and phone as your initial password."));

        try
        {
            startActivity(Intent.createChooser(i, "Send invite"));
        } catch (ActivityNotFoundException ex)
        {
            Toast.makeText(this, "No email clients installed", Toast.LENGTH_SHORT);
        }
    }

    public void onDoneClick(View view)
    {
        String teamName = mTeamName.getText().toString();

        new RegisterTeamMembers(this, mCurrentUser, teamName, getIntent().getStringExtra("password")).execute();
    }

    /**
     * Register all memebers
     */
    private class RegisterTeamMembers extends AsyncTask<String, Void, ParseException>
    {
        String mTeamName;
        ParseUser mCurrentUser;
        CreateTeamActivity mActivity;
        String mPassword;

        public RegisterTeamMembers(CreateTeamActivity activity, ParseUser currentUser, String teamName, String password)
        {
            mTeamName = teamName;
            mCurrentUser = currentUser;
            mActivity = activity;
            mPassword = password;
        }

        @Override
        protected ParseException doInBackground(String... params)
        {
            mCurrentUser.put("team", mTeamName);
            try
            {
                mCurrentUser.save();
                String manager = mCurrentUser.getUsername();

                for (TeamMember member : members)
                {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    //showProgress(true);

                    OTSUser user = new OTSUser();
                    user.setEmail(member.getEmail());
                    user.setPhone(member.getPhone());

                    user.setTeamName(mTeamName);
                    user.setManagerName(manager);
                    user.setIsActive(false);

                    user.save();
                }

                // all team members were registered successfully
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
            if (result != null)
            {
                Toast.makeText(mActivity, result.getMessage(), Toast.LENGTH_LONG).show();
            } else
            {
                Intent intent = new Intent(mActivity, MainActivity.class);
                mActivity.startActivity(intent);
                finish();
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
}
