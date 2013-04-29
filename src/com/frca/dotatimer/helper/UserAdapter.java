package com.frca.dotatimer.helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.frca.dotatimer.R;

public class UserAdapter extends ArrayAdapter<String> {

    private final TimerData.UserList users;

    public UserAdapter(Context context, TimerData.UserList _users) {
        super(context, R.layout.view_list_user, _users.userNicks());
        users = _users;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getCustomView(position, convertView, parent);
    }

    public TimerData.UserData getDataPair(int position)
    {
        return users.get(position);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent)
    {
        View row = super.getView(position, convertView, parent);
          /*
        LocalTask lTask = GetLocalTask(position);

        TextView textAccount = (TextView) row.findViewById(R.id.text_account);
        if (S.showingAll)
        {
            textAccount.setVisibility(View.VISIBLE);
            textAccount.setText(S.GetAccount(lTask.ownerGuid).accountName);
        }
        else
            textAccount.setVisibility(View.GONE);

        ImageView button = (ImageView) row.findViewById(R.id.button);
        if (lTask.task.getStatus().equals(C.COMPLETE))
            button.setBackgroundResource(R.drawable.btn_check_on);
        else
            button.setBackgroundResource(R.drawable.btn_check_off);

        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                int idx = lv.indexOfChild((View)v.getParent().getParent());
                LocalTask lTask = tasks.get(idx);
                Intent newIntent = new Intent(activity, Connecter.class);
                newIntent.putExtra(C.CONNECTER_CALL_TYPE, Connecter.TYPE_FINISH);
                newIntent.putExtra(C.TASK_ID, lTask.localId);
                newIntent.putExtra(C.ACCOUNT, lTask.ownerGuid);
                activity.startService(newIntent);
            }
        });
*/
        return row;
    }

    @Override
    public boolean isEmpty()
    {
        return users.isEmpty();
//        return tasks.isEmpty();
    }
}

