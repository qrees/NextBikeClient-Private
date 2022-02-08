package info.plocharz.nextbikeclient;

import info.plocharz.nextbikeclient.model.Account;
import info.plocharz.nextbikeclient.model.Operation;

import java.util.ArrayList;
import java.util.Observable;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryFragment extends BusyFragment implements Auth.AuthListener {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        
        return inflater.inflate(R.layout.history_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.updateHistory();
    }
    
    @Override
    public void onDetach(){
        super.onDetach();
    }


    private void updateHistory(){
        Auth auth = ((Application) this.getActivity().getApplication()).getAuth();
        if(!auth.isValid())
            return;
        if(this.getActivity() == null)
            return;
        auth.verify(this, true);
        setBusy(true);
    }

    public void showHistory(){
        Activity activity = this.getActivity();
        if (activity == null)
            return;
        Auth auth = ((Application) activity.getApplication()).getAuth();
        Account account = auth.getAccount();
        ArrayList<Operation> operations = account.getOperations();

        ListView historyList = (ListView) this.getView().findViewById(R.id.account_history);

        historyList.setAdapter(new HistoryAdapter(this.getActivity(), operations));
    }

    @Override
    public void onDone() {
        setBusy(false);
        showHistory();
    }

    @Override
    public void onLogout() {
        setBusy(false);
    }

    @Override
    public void onFailure() {
        setBusy(false);
        Logger.i("Login fail, and unable to verify");
        Context context = this.getContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, R.string.no_network, duration);
        toast.show();
    }

    static class HistoryFactory implements FragmentFactory {
        public Fragment create(){
            return new HistoryFragment();
        }
    }
}
