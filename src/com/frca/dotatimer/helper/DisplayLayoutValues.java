package com.frca.dotatimer.helper;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.frca.dotatimer.R;

public class DisplayLayoutValues {

    private String targetText;
    private String targetAuthor;
    private String mainText;
    private String mainAuthor;
    private boolean isDeleted;
    private TimerData.UserList users;
    private TimerData linkedData;
    private View lastParent;

    public boolean fromNewData(TimerData data)
    {
        if (data == null)
        {
            return false;
        }

        linkedData = data;
        targetText = data.getTimerString();

        if (data.isDeleted())
        {
            isDeleted = true;
            mainText = data.delete.value;
            mainAuthor = data.delete.nick;

        }
        else
        {
            isDeleted = false;
            mainText = data.getRemainingString();
            mainAuthor = null;
        }

        users = data.userList;

        return true;
    }

    public void setUpLayout(View parent, Context con)
    {
        lastParent = parent;
        TextView target = (TextView)parent.findViewById(R.id.text_target);
        target.setText(targetText);
        if (isDeleted)
            target.setPaintFlags(target.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        TextView remaining = (TextView)parent.findViewById(R.id.text_main);
        remaining.setText(mainText);

        TextView target_author = (TextView)parent.findViewById(R.id.text_target_author);
        if (targetAuthor != null)
        {
            target_author.setVisibility(View.VISIBLE);
            target_author.setText("by "+targetAuthor);
        }
        else
            target_author.setVisibility(View.GONE);

        TextView main_author = (TextView)parent.findViewById(R.id.text_main_author);
        if (mainAuthor != null)
        {
            main_author.setVisibility(View.VISIBLE);
            main_author.setText("by "+mainAuthor);
        }
        else
            main_author.setVisibility(View.GONE);

        ListView list_view = (ListView)parent.findViewById(R.id.list_users);
        list_view.setAdapter(new UserAdapter(con, users));
    }

    public void shortUpdater()
    {
        if (isDeleted)
            return;

        mainText = linkedData.getRemainingString();
    }

    public void shortUpdateLayout()
    {
        if (isDeleted || lastParent == null)
            return;

        TextView mainTextView = (TextView)lastParent.findViewById(R.id.text_main);
        mainTextView.setText(mainText);
    }

}
