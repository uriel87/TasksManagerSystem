package com.liron.ots;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

public class EditTeamMemberActivity extends AppCompatActivity
{
    TextView mMemberEmail;
    TextView mMemberPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team_member);

        mMemberEmail = (TextView) findViewById(R.id.member_email);
        mMemberPhone = (TextView) findViewById(R.id.member_phone);
    }

    public void onSaveMemberClick(View view)
    {
        Intent intent = new Intent(this, CreateTeamActivity.class);

        boolean cancel = false;
        View focusView = null;

        mMemberEmail.setError(null);
        mMemberPhone.setError(null);

        String email = mMemberEmail.getText().toString();
        String phone = mMemberPhone.getText().toString();

        if (!TextUtils.isEmpty(email))
        {
            mMemberEmail.setError(getString(R.string.error_invalid_email));
            focusView = mMemberEmail;
            cancel = true;
        }

        if (!TextUtils.isEmpty(phone))
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

        intent.putExtra("member", member);
        setResult(RESULT_OK, intent);
        finish();
    }
}
