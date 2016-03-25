package com.liron.ots;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.liron.ots.db.TasksProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeamRecyclerAdapter extends RecyclerView.Adapter<TeamRecyclerAdapter.ViewHolder> implements View.OnClickListener
{

    private List<TeamMember> members;
    private Activity activity;

    public TeamRecyclerAdapter(Activity activity, List<TeamMember> members)
    {
        this.activity = activity;
        this.members = members;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_member_row, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        final TeamMember member = members.get(position);

        holder.bind(member);

        holder.removeMemberImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                remove(member);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return members.size();
    }

    @Override
    public void onClick(View view)
    {
        TeamMember member = (TeamMember) view.getTag();
        Intent i = new Intent(activity, EditTeamMemberActivity.class);
        i.putExtra("member", member);
        activity.startActivityForResult(i, 177);
    }

    public void add(TeamMember member)
    {
        int position = members.indexOf(member);
        if (-1 == position)
        {
            members.add(member);
            notifyItemInserted(members.size());
        }
        else
        {
            members.remove(position);
            members.add(position, member);
            notifyItemChanged(position);
        }
    }

    public void remove(TeamMember member)
    {
        int position = members.indexOf(member);
        if (-1 == position)
        {
            return;
        }

        members.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView emailView;
        public ImageView removeMemberImg;

        public ViewHolder(View itemView)
        {
            super(itemView);
            emailView = (TextView) itemView.findViewById(R.id.txtEmail);
            removeMemberImg = (ImageView) itemView.findViewById(R.id.imgRemoveMember);
        }

        public void bind(TeamMember member)
        {
            itemView.setTag(member);
            emailView.setText(member.getEmail());
            emailView.setTag(member.getPhone());
        }
    }
}