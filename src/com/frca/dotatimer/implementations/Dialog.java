package com.frca.dotatimer.implementations;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.R;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.TimerData;
import com.frca.dotatimer.tasks.RequestManager;

public class Dialog {

    /*public static void showNick(final MainActivity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Nastaven� nicku");

        final EditText input = new EditText(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        final Preferences prefs = Preferences.getPreferences(activity);
        alertDialogBuilder.setMessage("Nastaven� tvoj� p�ezd�vky").setCancelable(false).setView(input)
            .setPositiveButton("Potvrdit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String new_nick = input.getText().toString().trim();
                    if (new_nick != "") {
                        // prefs.putAndCommit(TimerData.TAG_NICK, new_nick);
                        // Toast.makeText(activity, "Nick zm�n�n na: " + prefs.getNick(), Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(activity, "Neplatn� nick", Toast.LENGTH_LONG).show();
                }
            }).setNegativeButton("Zru�it", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //if (prefs.getNick() == null)
                    //    activity.finish();
                    //else
                    //    dialog.cancel();
                }
            });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        // if (prefs.getNick() == null)
        // dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
    }*/

    public static void showJoin(final MainActivity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("P�ihl�en�");

        View values = activity.getLayoutInflater().inflate(R.layout.dialog_login, null);
        final EditText inputChannel = (EditText) values.findViewById(R.id.edit_channel_name);
        final EditText inputPass = (EditText) values.findViewById(R.id.edit_channel_pass);

        alertDialogBuilder.setMessage("Login").setCancelable(false).setView(values).setPositiveButton("Potvrdit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String enteredChannel = inputChannel.getText().toString().trim();
                String enteredPass = inputPass.getText().toString().trim();
                if (!TextUtils.isEmpty(enteredChannel))
                    Toast.makeText(activity, "Neplatn� jm�no kan�lu", Toast.LENGTH_LONG).show();
                else if (!TextUtils.isEmpty(enteredPass))
                    Toast.makeText(activity, "Mus�te zadat heslo", Toast.LENGTH_LONG).show();
                else {
                    RequestManager.requestTeamAuthenticate(activity, enteredChannel, enteredPass);
                }
            }
        }).setNegativeButton("Zru�it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public static void showDelete(final MainActivity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Zru�en� timeru");

        final EditText input = new EditText(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialogBuilder.setMessage("Zadej d�vod pro zru�en� sou�asn�ho timeru").setCancelable(false).setView(input).setPositiveButton("Potvrdit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String deleteReason = input.getText().toString().trim();
                if (!TextUtils.isEmpty(deleteReason)) {
                    Toast.makeText(activity, "Mus� uv�st d�vod", Toast.LENGTH_LONG).show();
                    return;
                }

                ParameterMap params = new ParameterMap(activity);
                params.put(TimerData.TAG_DELETE, deleteReason);
                RequestManager.requestTeamUpdate(activity, params);
            }
        }).setNegativeButton("Zru�it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }
}
