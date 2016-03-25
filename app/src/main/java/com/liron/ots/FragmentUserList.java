package com.liron.ots;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liron.ots.users.LoginActivity;
import com.parse.ParseUser;

/**
 * Created by user on 05/03/2016.
 */
public class FragmentUserList extends Fragment {
    private View view;
    private Context context;
    private AdapterRecycleViewUser adapter;
    private RecyclerView recyclerView;
    FloatingActionButton fabAddTask;
    private ParseUser mCurrentUser;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_list, container, false);

        if (checkUser())
        {
            init();
        }
        return view;
    }

    private boolean checkUser()
    {
        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser == null) {
            // show the signup or login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return false;
        }
        return true;
    }

    public void init() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleViewTasks);

        adapter = new AdapterRecycleViewUser(getActivity(), mCurrentUser.getString("team"));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fabAddTask = (FloatingActionButton) view.findViewById(R.id.fab);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateTeamActivity.class);
                startActivity(intent);
            }
        });
    }
}
