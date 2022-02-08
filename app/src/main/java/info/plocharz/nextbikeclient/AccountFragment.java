package info.plocharz.nextbikeclient;

import info.plocharz.nextbikeclient.model.User;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AccountFragment extends Fragment implements Observer {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        
        return inflater.inflate(R.layout.account_fragment, container, false);
    }
    
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        ((Application) this.getActivity().getApplication()).getAuth().addObserver(this);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.updateCredits();
    }
    
    @Override
    public void onDetach(){
        ((Application) this.getActivity().getApplication()).getAuth().deleteObserver(this);
        super.onDetach();
    }


    private void updateCredits(){
        Auth auth = ((Application) this.getActivity().getApplication()).getAuth();
        if(!auth.isValid())
            return;
        User user = auth.getUser();
        
        TextView textView = (TextView) this.getView().findViewById(R.id.credits_value);
        Double credits = (double)user.getCredits() / 100;
        textView.setText(new DecimalFormat("###,###.00").format(credits));
        
        TextView phoneView = (TextView) this.getView().findViewById(R.id.phone_number);
        String phoneNumber= user.getMobile();
        phoneView.setText(phoneNumber.toString());
        
        View view = this.getView().findViewById(R.id.account_layout);
        view.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void update(Observable observable, Object data) {
        updateCredits();
    }
    
    static class AccountFactory implements FragmentFactory {
        public Fragment create(){
            return new AccountFragment();
        }
    }
}
