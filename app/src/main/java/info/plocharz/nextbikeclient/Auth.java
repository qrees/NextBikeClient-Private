package info.plocharz.nextbikeclient;

import java.util.Observable;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import info.plocharz.nextbikeclient.model.Account;
import info.plocharz.nextbikeclient.model.Nextbike;
import info.plocharz.nextbikeclient.model.User;
import info.plocharz.nextbikeclient.requests.AccountRequest;
import android.content.SharedPreferences;

public class Auth extends Observable implements RequestListener<Nextbike> {
    
    public final static String PASSWORD_PREF = "loginkey";
    private SharedPreferences pref;
    private String password;
    private boolean valid = false;
    private boolean unableToVerify = false;
    private User user;
    private Account account;
    private AccountRequest request;
    private SpiceManager spiceManager;
    private AuthListener onVerifyDone;
    
    Auth(SharedPreferences pref, SpiceManager spiceManager){
        this.pref = pref;
        this.spiceManager = spiceManager;
        this.password = pref.getString(PASSWORD_PREF, null);
    }
    
    public String getLoginKey(){
        return password;
    }
    
    public boolean isValid(){
        return valid;
    }
    
    public User getUser(){
        return this.user;
    }
    
    public Account getAccount(){
        return this.account;
    }
    
    public void setLoginKey(Nextbike loginResponse){
        String loginKey = loginResponse.getUser().getLoginkey();
        this.password = loginKey;
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putString(PASSWORD_PREF, loginKey);
        editor.apply();
    }
    
    public void onLoginSuccess(Nextbike loginResponse) {
        String loginKey = loginResponse.getUser().getLoginkey();
        this.password = loginKey;
        this.user = loginResponse.getUser();
        this.account = loginResponse.getAccount();
        this.unableToVerify = false;
        SharedPreferences.Editor editor = this.pref.edit();
        editor.putString(PASSWORD_PREF, loginKey);
        editor.apply();
        this.valid = true;
        this.setChanged();
        this.notifyObservers();
        this.onDone();
    }

    private void cleanLogin(){
        SharedPreferences.Editor editor = this.pref.edit();
        editor.remove(PASSWORD_PREF);
        editor.apply();
        request = null;
        this.valid = false;
        this.user = null;
        this.account = null;
        this.setChanged();
        this.notifyObservers();
    }

    public void logout() {
        this.cleanLogin();
        this.onLogout();
    }

    @Override
    public void onRequestFailure(SpiceException exc) {
        request = null;
        Logger.e(this.toString() + " exception: " + exc.getMessage(), exc);
        if(NoNetworkException.class.isInstance(exc)){
            this.unableToVerify = true;
            Logger.e("No Network Exception");
        } else if (NetworkException.class.isInstance(exc)) {
            this.unableToVerify = true;
            NetworkException nexc = (NetworkException) exc;
            Logger.e("Auth NetworkException: " + nexc.getClass().toString(), exc);
        } else {
            this.unableToVerify = false;
            Logger.e("Auth Request failed: " + exc.getClass().toString(), exc);
        }
        this.onFailure();
        onVerifyDone = null;
    }

    @Override
    public void onRequestSuccess(Nextbike login) {
        request = null;
        Logger.i("Auth Request success");

        if (login.getUser() == null){
            this.cleanLogin();
            this.unableToVerify = false;
            Logger.e("Auth Response empty");
        } else {
            this.onLoginSuccess(login);
        }
        onVerifyDone = null;
    }

    private void onDone(){
        if(onVerifyDone != null)
            onVerifyDone.onDone();
    }

    private void onFailure(){
        request = null;
        Logger.i("Auth Request failed");
        if(onVerifyDone != null)
            onVerifyDone.onFailure();
    }

    private void onLogout(){
        if(onVerifyDone != null)
            onVerifyDone.onLogout();
    }

    public void verify(boolean force) {
        Logger.i("Auth verify " + this.password + " " + this.request);
        if(this.valid && !force){
            Logger.i("Auth already valid and not forced");
            this.onDone();
            onVerifyDone = null;
            return;
        }
        if (request != null) {
            Logger.i("Request is still in progress");
            return;
        }

        if(this.password != null){
            Logger.i("Auth has password and is pending and request is null");
            request = new AccountRequest(this.password);
            spiceManager.execute(request, this);
        } else {
            Logger.i("Auth has no password");
            this.onFailure();
            onVerifyDone = null;
        }
    }

    public void verify(AuthListener listener, boolean force) {
        onVerifyDone = listener;
        this.verify(force);
    }

    public void verify(AuthListener listener) {
        this.verify(listener, false);
    }

    public void verify() {
        this.verify(false);
    }

    public boolean isUnableToVerify(){
        return this.unableToVerify;
    }

    public interface AuthListener {
        void onDone();
        void onLogout();
        void onFailure();
    }
}

