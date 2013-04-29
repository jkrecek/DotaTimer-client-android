package com.frca.dotatimer;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.frca.dotatimer.tasks.RequestTask;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends FragmentActivity {

    public static final String MODE = "mode";
    public static final int LOGIN = 1;
    public static final int REGISTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        switchToDefaultView();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof LoginSectionFragment)
            switchToDefaultView();
        else if (fragment instanceof LoginDefaultFragment) {
            // if has already set user info
            if (true) {
                super.onBackPressed();
            }
            // else do nothing, user must set his account

        }
    }

    private void switchToDefaultView() {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.container, new LoginDefaultFragment());
        fm.commit();
    }

    public static class LoginDefaultFragment extends Fragment {

        public View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mode = view.getId() == R.id.sign_in_button ? LOGIN : REGISTER;
                LoginSectionFragment fragment = new LoginSectionFragment();

                Bundle args = new Bundle();
                args.putInt(MODE, mode);

                fragment.setArguments(args);

                FragmentTransaction fm = getFragmentManager().beginTransaction();
                fm.replace(R.id.container, fragment);
                fm.commit();
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_login_default, null);

            view.findViewById(R.id.sign_in_button).setOnClickListener(clickListener);
            view.findViewById(R.id.sign_register_button).setOnClickListener(clickListener);
            return view;
        }
    }

    public static class LoginSectionFragment extends Fragment {
        // Values for email and password at the time of the login attempt.
        private String mAccount;
        private String mDisplayName;

        // UI references.
        private Spinner mAccountView;
        private EditText mDisplayNameView;
        private ImageView mLogoView;
        private View mLoginFormView;
        private View mLoginStatusView;
        private TextView mLoginStatusMessageView;
        private Button mSignButton;

        // Other
        private AccountManager accManager;
        private Account[] googleAccs;

        private int mode;

        public LoginSectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mode = getArguments().getInt(MODE);

            View view = inflater.inflate(R.layout.fragment_login, null);

            mLogoView = (ImageView) view.findViewById(R.id.logo);

            // Set up the login form.
            accManager = AccountManager.get(getActivity());
            googleAccs = accManager.getAccountsByType("com.google");

            mAccountView = (Spinner) view.findViewById(R.id.email);

            List<String> googleAccNames = new ArrayList<String>();
            for (Account googleAcc : googleAccs)
                googleAccNames.add(googleAcc.name);

            mAccountView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, googleAccNames));

            mDisplayNameView = (EditText) view.findViewById(R.id.display_name);
            if (mode == REGISTER) {
                mDisplayNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });
            } else
                mDisplayNameView.setVisibility(View.GONE);

            mLoginFormView = view.findViewById(R.id.login_form);
            mLoginStatusView = view.findViewById(R.id.login_status);
            mLoginStatusMessageView = (TextView) view.findViewById(R.id.login_status_message);

            mSignButton = (Button) view.findViewById(R.id.sign_in_button);
            int stringId = mode == LOGIN ? R.string.action_sign_in_short : R.string.action_sign_register_short;
            mSignButton.setText(stringId);

            mSignButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            return view;
        }

        public void attemptLogin() {
            if (RequestTask.isTaskInProgress())
                return;

            mDisplayNameView.setError(null);

            mAccount = mAccountView.getSelectedItem().toString();

            boolean cancel = false;
            View focusView = null;

            if (mode == REGISTER) {
                mDisplayName = mDisplayNameView.getText().toString();

                // Check for a valid display name
                if (TextUtils.isEmpty(mDisplayName)) {
                    mDisplayNameView.setError(getString(R.string.error_display_name_required));
                    focusView = mDisplayNameView;
                    cancel = true;
                } else if (mDisplayName.length() < 4) {
                    mDisplayNameView.setError(getString(R.string.error_invalid_display_name));
                    focusView = mDisplayNameView;
                    cancel = true;
                }
            }

            if (cancel) {
                focusView.requestFocus();
            } else {
                mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
                showProgress(true);

                Account selectedAccount = null;
                for (Account acc : googleAccs) {
                    if (acc.name.equals(mAccount)) {
                        selectedAccount = acc;
                        break;
                    }
                }

                /*accManager.getAuthToken(selectedAccount, "ah", null, getActivity(), new AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> result) {
                        Bundle bundle = null;
                        try {
                            bundle = result.getResult();
                        } catch (OperationCanceledException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            return;
                        } catch (AuthenticatorException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        for (String key : bundle.keySet())
                            Log.e("FU", "Key:" + key + ", val:" + bundle.get(key).toString());
                    }

                }, null);*/
                // AccountManager am = accManager.getAuthToken(account, authTokenType, notifyAuthFailure, callback, handler)
                // RequestManager.requestUserAuthenticate((LoginActivity) getActivity(), mAccount, mDisplayName);
            }
        }

        public void showProgress(final boolean show) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mLogoView.setVisibility(View.VISIBLE);
            mLogoView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLogoView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
    }
}
