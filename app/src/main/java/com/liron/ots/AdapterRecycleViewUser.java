package com.liron.ots;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 01/01/2016.
 */
public class AdapterRecycleViewUser extends RecyclerView.Adapter<AdapterRecycleViewUser.MyViewHolder> {

    private final String mTeamName;
    public LayoutInflater inflater;
    private List<ParseUser> users = new ArrayList<>();
    //private UserProvider userProvider;
    private Context context;

    public AdapterRecycleViewUser(Context context, String teamName) {
        this.context = context;
        //userProvider = new UserProvider(context);
        mTeamName = teamName;
        users = new ArrayList<ParseUser>();
        refreshTeamMembers();
        inflater = LayoutInflater.from(context);
    }

    private void refreshTeamMembers()
    {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("team", mTeamName);
        query.whereEqualTo("is_manager", false);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> members, ParseException e) {
                if (e == null) {
                    Log.d("get_team_members", "Retrieved " + members.size() + " members");
                    users = members;
                    notifyDataSetChanged();
                } else {
                    Log.d("get_team_members", "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.style_row_recycleview_users, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ParseUser current = users.get(position);
        holder.name.setText(current.getUsername());
        holder.phone.setText(current.getString("phone"));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView phone;

        public MyViewHolder(final View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.user_name_recycleview_layout);
            phone = (TextView) view.findViewById(R.id.user_phone_recycleview_layout);
        }
    }
}