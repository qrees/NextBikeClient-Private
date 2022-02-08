package info.plocharz.nextbikeclient;

import info.plocharz.nextbikeclient.model.Nextbike;
import info.plocharz.nextbikeclient.model.User;
import info.plocharz.nextbikeclient.requests.LoginRequest;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginDialogFragment extends DialogFragment implements RequestListener<Nextbike>{
   
    private SpiceManager spiceManager;
    private LoginRequest request;
    private Auth auth;

    public MainActivity getMainActivity(){
        return (MainActivity) this.getActivity();
    }
    
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = this.getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        
        final View view = inflater.inflate(R.layout.login_view, null);
        
        builder.setView(view)
           .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
               }
           })
           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
//                   LoginDialogFragment.this.getDialog().cancel();
               }
           });
        Dialog dialog = builder.create();
        
        return dialog;
    }
    

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivity mainActivity = this.getMainActivity();
        this.spiceManager = mainActivity.getMyApplication().getSpiceManager();
        this.auth = mainActivity.getMyApplication().getAuth();
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialogInstance = (AlertDialog)getDialog();
        EditText usernameView = (EditText)dialogInstance.findViewById(R.id.username);
        EditText passwordView = (EditText)dialogInstance.findViewById(R.id.password);
        usernameView.setRawInputType(Configuration.KEYBOARD_QWERTY);
        passwordView.setRawInputType(Configuration.KEYBOARD_QWERTY);

        if(dialogInstance != null)
        {
            Button positiveButton = (Button) dialogInstance.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    EditText usernameView = (EditText)dialogInstance.findViewById(R.id.username);
                    EditText passwordView = (EditText)dialogInstance.findViewById(R.id.password);
                    LoginDialogFragment.this.performRequest(usernameView.getText().toString(), passwordView.getText().toString());
                }
            });
        }
    }
    
    private void performRequest(String username, String password) {
        request = new LoginRequest(username, password);
        
        spiceManager.execute(request, this);
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        Logger.e(this.toString() + " exception: " + e.getMessage(), e);
        Logger.e("Request failed", e);
    }


    @Override
    public void onRequestSuccess(Nextbike login) {
        AlertDialog dialogInstance = (AlertDialog)getDialog();
        
        User user = login.getUser();
        if (user == null){
            Logger.i("Login failed");
            if(dialogInstance != null){
                EditText passwordView = (EditText)dialogInstance.findViewById(R.id.password);
                passwordView.setError("Invalid PIN");
            }
        }else{
            Logger.i("Login key: " + login.getUser().getLoginkey());
            this.auth.setLoginKey(login);
            getMainActivity().login_success();
            if(dialogInstance != null){
                LoginDialogFragment.this.getDialog().dismiss();
            }
        }
    }
    
}
