package com.liron.ots;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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

public class UserTasksActivity extends AppCompatActivity
{
    ParseUser mCurrentUser;
    FloatingActionButton fabAddTask;
    TextView mUserNameTitleView;
    TextView mNumWaitingTasksView;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fabAddTask = (FloatingActionButton) findViewById(R.id.fab);
        fabAddTask.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(UserTasksActivity.this, SaveTaskActivity.class);
                startActivity(intent);
            }
        });

        mUserNameTitleView = (TextView) findViewById(R.id.txtUserNameTitle);
        mNumWaitingTasksView = (TextView) findViewById(R.id.txtNumWaitingTasks);
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

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!checkUser())
        {
            finish();
        }

        fabAddTask.setVisibility(View.VISIBLE);
        if (!mCurrentUser.getBoolean("is_manager"))
        {
            fabAddTask.setVisibility(View.GONE);
        }


        if (mCurrentUser.getBoolean("is_manager"))
        {
            Intent intent = new Intent(UserTasksActivity.this, MainActivity.class);
            startActivity(intent);
        }

        mUserNameTitleView.setText(mCurrentUser.getUsername());

        onTasksRefreshed(new TasksProvider(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_tasks, menu);
        return true;
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

    public void onTasksRefreshed(TasksProvider provider)
    {
        try
        {
            int numWaitingTasks = provider.getWaitingTasks().size();
            if (numWaitingTasks == 0)
            {
                mNumWaitingTasksView.setVisibility(View.INVISIBLE);
            }
            else
            {
                mNumWaitingTasksView.setVisibility(View.VISIBLE);
                mNumWaitingTasksView.setText(String.valueOf(numWaitingTasks));
            }
        }
        finally
        {
            provider.close();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TasksFragment extends Fragment
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
        public void onCreate (Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            mAdapter = new TasksRecyclerAdapter((UserTasksActivity)getActivity(), ParseUser.getCurrentUser(), getArguments().getBoolean(ARG_SHOW_WAITING_ONLY));
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
            ((RadioButton)btns.get(0)).setChecked(true);

            if (showWaitingOnly)
            {
                mSortByView.setVisibility(View.GONE);
            }
            else
            {
                mSortByView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
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
