package com.liron.ots;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liron.ots.db.TasksDbConsts;
import com.liron.ots.db.TasksProvider;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder> implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener
{
    public enum TaskSortBy
    {
        STATUS,
        PRIORITY,
        TIME
    }

    private TaskSortBy taskSortBy;
    private List<Task> tasks;
    private Activity activity;
    private TasksProvider provider;
    private ParseUser currentUser;
    Boolean showWaitingOnly;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public TasksRecyclerAdapter(Activity activity, ParseUser currentUser, boolean showWaitingOnly)
    {
        this.activity = activity;
        this.provider = new TasksProvider(this.activity);
        provider.setAdapter(this);

        this.tasks = new ArrayList<>();
        this.currentUser = currentUser;
        this.showWaitingOnly = showWaitingOnly;
        taskSortBy = TaskSortBy.TIME;

        loadFromParse();

        refreshTasks();
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout layout)
    {
        mSwipeRefreshLayout = layout;
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void loadFromParse()
    {
        ParseQuery<Task> query = ParseQuery.getQuery(Task.class);
        String teamName = currentUser.getString("team");
        if (teamName != null)
        {
            query.whereEqualTo("team", teamName);
            if (!currentUser.getBoolean("is_manager"))
            {
                query.whereEqualTo("assignee", currentUser.getUsername());
            }
        }
        query.findInBackground(new FindCallback<Task>()
        {
            public void done(List<Task> tasks, ParseException e)
            {
                if (e == null)
                {
                    provider.truncate();
                    Log.d("load_parse", "Retrieved " + tasks.size() + " tasks");
                    for (Task task : tasks)
                    {
                        try
                        {
                            provider.insert(task);
                            //task.unpin();
                        } catch (Exception e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    refresh();
                } else
                {
                    Log.d("load_parse", "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onRefresh()
    {
        loadFromParse();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_row, parent, false);
        v.setOnClickListener(this);
        v.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (currentUser.getBoolean("is_manager"))
                {
                    remove((Task)v.getTag());
                }
                return true;
            }
        });
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        final Task task = tasks.get(position);

        holder.bind(task);
    }

    @Override
    public int getItemCount()
    {
        return tasks.size();
    }

    @Override
    public void onClick(View view)
    {
        Task task = (Task) view.getTag();
        Intent intent;

        if (!currentUser.getBoolean("is_manager"))
        {
            intent = new Intent(activity, SaveMemberTaskActivity.class);
        }
        else
        {
            intent = new Intent(activity, SaveTaskActivity.class);
        }

        intent.putExtra("task_id", task.getObjectIdCopy());
        activity.startActivity(intent);
    }

    public void setTaskSortBy(TaskSortBy taskSortBy)
    {
        this.taskSortBy = taskSortBy;
    }

    private void refreshTasks()
    {
        if (showWaitingOnly)
        {
            this.tasks = provider.getWaitingTasks();
        }
        else
        {
            switch (taskSortBy)
            {
                case STATUS:
                    this.tasks = provider.getAll(TasksDbConsts.STATUS);
                    break;
                case PRIORITY:
                    this.tasks = provider.getAll(TasksDbConsts.PRIORITY);
                    break;
                default:
                    this.tasks = provider.getAll(TasksDbConsts.DUE_DATE);
                    break;
            }
        }
    }

    public void refresh()
    {
        refreshTasks();
        notifyDataSetChanged();

        int numWaitingTasks = provider.getWaitingTasks().size();
        if (numWaitingTasks == 0)
        {
            FragmentUserTasks.mNumWaitingTasksView.setVisibility(View.INVISIBLE);
        } else
        {
            FragmentUserTasks.mNumWaitingTasksView.setVisibility(View.VISIBLE);
            FragmentUserTasks.mNumWaitingTasksView.setText(String.valueOf(numWaitingTasks));
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void add(Task task)
    {
        provider.insert(task);
        int position = tasks.indexOf(task);
        if (-1 == position)
        {
            tasks.add(task);
            notifyItemInserted(tasks.size());
        }
        else
        {
            tasks.remove(position);
            tasks.add(position, task);
            notifyItemChanged(position);
        }
    }

    public void remove(Task task)
    {
        int position = tasks.indexOf(task);
        if (-1 == position)
        {
            return;
        }

        tasks.remove(position);
        provider.delete(task);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView nameView;
        public TextView assigneeView;
        public TextView dueDateView;
        public TextView priorityView;
        public ImageView categoryView;
        public ImageView statusView;
        public ImageView acceptStatusView;
        private String check1;
        private String check2;
        private String check3;

        public ViewHolder(View itemView)
        {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.name);
            assigneeView = (TextView) itemView.findViewById(R.id.assignee);
            dueDateView = (TextView) itemView.findViewById(R.id.dueDate);
            priorityView = (TextView) itemView.findViewById(R.id.priority);
            categoryView = (ImageView) itemView.findViewById(R.id.category);
            statusView = (ImageView) itemView.findViewById(R.id.status);
            acceptStatusView = (ImageView) itemView.findViewById(R.id.acceptStatus);
        }

        public void bind(Task task)
        {
            itemView.setTag(task);
            nameView.setText(task.getName());
            assigneeView.setText(task.getAssignee());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            dueDateView.setText(formatter.format(new Date(task.getDueDate())));
            priorityView.setText(task.getPriority().name());


            check1= task.getCategory().toString();
            if (check1.equals("COMPUTERS")) {
                categoryView.setBackgroundResource(R.drawable.computers);
            }
            else if (check1.equals("CLEANING")) {
                categoryView.setBackgroundResource(R.drawable.cleaning);
            }
            else if (check1.equals("ELECTRICITY")) {
                categoryView.setBackgroundResource(R.drawable.electricity);
            }
            else if (check1.equals("GENERAL")) {
                categoryView.setBackgroundResource(R.drawable.general);
            }
            else if (check1.equals("OTHER")) {
                categoryView.setBackgroundResource(R.drawable.other);
            }


            check2= task.getAcceptStatus().toString();
            if (check2.equals("WAITING")) {
                acceptStatusView.setBackgroundResource(R.drawable.waiting);
            }
            else if (check2.equals("ACCEPT")) {
                acceptStatusView.setBackgroundResource(R.drawable.green);
            }
            else if (check2.equals("REJECT")) {
                acceptStatusView.setBackgroundResource(R.drawable.red);
            }


            check3= task.getStatus().toString();
            if (check3.equals("WAITING")) {
                statusView.setBackgroundResource(R.drawable.waiting);
            }
            else if (check3.equals("IN_PROGRESS")) {
                statusView.setBackgroundResource(R.drawable.inprogress);
            }
            else if (check3.equals("DONE")) {
                statusView.setBackgroundResource(R.drawable.done);
            }
        }
    }
}