package com.liron.ots;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.liron.ots.db.TasksProvider;
import com.liron.ots.users.LoginActivity;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by user on 04/03/2016.
 */
public class FragmentUserTasks extends Fragment
{
    private View view;
    private Context context;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    ParseUser mCurrentUser;
    FloatingActionButton fabAddTask;
    TextView mUserNameTitleView;
    public static TextView mNumWaitingTasksView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.activity_user_tasks, container, false);
        init();
        return view;
    }

    public void init()
    {
        if (!checkUser())
        {
            return;
        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) view.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fabAddTask = (FloatingActionButton) view.findViewById(R.id.fab);
        fabAddTask.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getActivity(), SaveTaskActivity.class);
                startActivity(intent);
            }
        });

        mUserNameTitleView = (TextView) view.findViewById(R.id.txtUserNameTitle);
        mNumWaitingTasksView = (TextView) view.findViewById(R.id.txtNumWaitingTasks);

        fabAddTask.setVisibility(View.VISIBLE);
        if (!mCurrentUser.getBoolean("is_manager"))
        {
            fabAddTask.setVisibility(View.GONE);
        }

        mUserNameTitleView.setText(mCurrentUser.getUsername());

        TasksProvider provider = new TasksProvider(getActivity());
        try
        {
            int numWaitingTasks = provider.getWaitingTasks().size();
            if (numWaitingTasks == 0)
            {
                mNumWaitingTasksView.setVisibility(View.INVISIBLE);
            } else
            {
                mNumWaitingTasksView.setVisibility(View.VISIBLE);
                mNumWaitingTasksView.setText(String.valueOf(numWaitingTasks));
            }
        } finally
        {
            provider.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkUser())
        {
            return;
        }
        //mViewPager.setAdapter(mSectionsPagerAdapter);
//        TasksProvider provider = new TasksProvider(getActivity());
//        mNumWaitingTasksView.setText(String.valueOf(provider.getWaitingTasks().size()));
    }


    private boolean checkUser()
    {
        mCurrentUser = ParseUser.getCurrentUser();
        if (mCurrentUser == null)
        {
            // show the signup or login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getActivity().getMenuInflater().inflate(R.menu.menu_user_tasks, menu);
        //inflater.inflate(R.menu.menu_user_tasks, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TasksFragment extends android.support.v4.app.Fragment
    {
        TasksRecyclerAdapter mAdapter;
        RecyclerView mTasksView;
        RadioGroup mSortByView;

        private static final String ARG_SHOW_WAITING_ONLY = "show_waiting_only";

        public TasksFragment()
        {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static TasksFragment newInstance(boolean showWaitingOnly)
        {
            TasksFragment fragment = new TasksFragment();
            Bundle args = new Bundle();
            args.putBoolean(ARG_SHOW_WAITING_ONLY, showWaitingOnly);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mAdapter = new TasksRecyclerAdapter(getActivity(), ParseUser.getCurrentUser(), getArguments().getBoolean(ARG_SHOW_WAITING_ONLY));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_user_tasks, container, false);

            initTasksView(rootView, getArguments().getBoolean(ARG_SHOW_WAITING_ONLY));

            return rootView;
        }

        @Override
        public void onResume()
        {
            super.onResume();
            mAdapter.refresh();
        }

        private void initTasksView(final View rootView, Boolean showWaitingOnly)
        {
            mTasksView = (RecyclerView) rootView.findViewById(R.id.tasks_list);
            mTasksView.setHasFixedSize(true);

            mAdapter.setSwipeRefreshLayout((SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout));

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mTasksView.setLayoutManager(linearLayoutManager);
            mTasksView.setItemAnimator(new DefaultItemAnimator());
            mTasksView.setAdapter(mAdapter);

            mSortByView = (RadioGroup) rootView.findViewById(R.id.sortBy);

            mSortByView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {
                    RadioButton selectedTaskSortBy = (RadioButton) rootView.findViewById(checkedId);
                    mAdapter.setTaskSortBy(Enum.valueOf(TasksRecyclerAdapter.TaskSortBy.class,
                            selectedTaskSortBy.getText().toString().toUpperCase()));
                    mAdapter.refresh();
                }
            });

            ArrayList<View> btns = new ArrayList<>();
            mSortByView.findViewsWithText(btns, TasksRecyclerAdapter.TaskSortBy.TIME.name(), View.FIND_VIEWS_WITH_TEXT);
            ((RadioButton) btns.get(0)).setChecked(true);

            if (showWaitingOnly)
            {
                mSortByView.setVisibility(View.GONE);
            } else
            {
                mSortByView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position)
            {
                case 0:
                    return TasksFragment.newInstance(true);
                case 1:
                    return TasksFragment.newInstance(false);
            }
            return null;
        }

        @Override
        public int getCount()
        {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "Waiting Tasks";
                case 1:
                    return "All Task";
            }
            return null;
        }
    }


}
