package info.plocharz.nextbikeclient;

import org.springframework.http.converter.HttpMessageNotReadableException;

import info.plocharz.nextbikeclient.model.Bike;
import info.plocharz.nextbikeclient.model.BikeStateAction;
import info.plocharz.nextbikeclient.model.Rental;
import info.plocharz.nextbikeclient.model.RentalAction;
import info.plocharz.nextbikeclient.requests.BikeStateRequest;
import info.plocharz.nextbikeclient.requests.RentRequest;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RentFragment extends BusyFragment implements RequestListener<RentalAction>, OnClickListener {
    String bikeId;
    public static final int BIKE_ID_LENGTH = 5;
    private Toast current_toast = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.bikeId = getArguments().getString("bikeId");
            Logger.i(" === bikeId = " + bikeId);
        }
        super.onCreate(savedInstanceState);
    }

    public void setBikeId(String bikeId){
        this.bikeId = bikeId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        
        View inflated = inflater.inflate(R.layout.rent_fragment, container, false);
        Button rentButton = (Button)inflated.findViewById(R.id.rent_button);
        rentButton.setOnClickListener(this);
        return inflated;
    }
    
    private Application getApp(){
        return (Application) this.getActivity().getApplication();
    }

    private void show_toast(int string_resource){
        if(current_toast != null){
            current_toast.cancel();
        }
        current_toast = Toast.makeText(this.getContext(), string_resource, Toast.LENGTH_LONG);
        current_toast.show();
    }

    private void performRequest() {
        if(isBusy()){
            return;
        }
        setBusy(true);
        
        String loginKey = this.getApp().getAuth().getLoginKey();
        TextView textView = (TextView) this.getView().findViewById(R.id.bikeId);
        String bike_id = textView.getText().toString();
        if (bike_id.equals("")){
            show_toast(R.string.invalid_bike_id);
        }
        RentRequest request = new RentRequest(loginKey, textView.getText().toString());
        
        SpiceManager spiceManager = this.getApp().getSpiceManager();
        spiceManager.execute(request, this);
    }
    
    private class BikeStateListener implements RequestListener<BikeStateAction> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            setBusy(false);
            Logger.e("Error while checking bike state", spiceException);
            Logger.e(this.toString() + " exception: " + spiceException.getMessage(), spiceException);
        }

        @Override
        public void onRequestSuccess(BikeStateAction result) {
            setBusy(false);
            Bike bike = result.getBike();
            Logger.i("Received bike: " + bike.getNumber() + " state: " + bike.getState());
            View rentView = RentFragment.this.getView();
            if (rentView == null)
                return;
            TextView bike_state_view = (TextView) rentView.findViewById(R.id.bike_state);

            if(bike.getState().equals("ok")){
                bike_state_view.setText(getResources().getString(R.string.bike_ok));
            }
            else if(bike.getState().equals("ok_but_unknown_number")){
                bike_state_view.setText(getResources().getString(R.string.bike_ok));
            }
            else if(bike.getState().equals("recently_returned")){
                bike_state_view.setText(getResources().getString(R.string.bike_ok));
            }
            else if(bike.getState().startsWith("return")){
                bike_state_view.setText(getResources().getString(R.string.bike_already_rented));
            }
            else if(bike.getState().equals("unknown")){
                bike_state_view.setText(getResources().getString(R.string.bike_unknown));
            }
            else if(bike.getState().equals("locked")){
                bike_state_view.setText(getResources().getString(R.string.bike_locked));
            }
            else if(bike.getState().equals("limit")){
                bike_state_view.setText(getResources().getString(R.string.bike_limit));
            }
            else if(bike.getState().equals("occupied")){
                bike_state_view.setText(getResources().getString(R.string.bike_occupied));
            }
            else if(bike.getState().equals("low_credits")){
                bike_state_view.setText(getResources().getString(R.string.bike_low_credits));
            }
            else if(bike.getState().equals("not_available_over_night")){
                bike_state_view.setText(getResources().getString(R.string.bike_not_available_over_night));
            }
            else if(bike.getState().contains("but_foreign")){
                bike_state_view.setText(getResources().getString(R.string.bike_foreign));
            } else {
                bike_state_view.setText(getResources().getString(R.string.bike_unknown));
            }
        }
        
    }
    
    private void checkBikeState(String number){
        if(number.length() == BIKE_ID_LENGTH) {
            BikeStateRequest bikeStateRequest = new BikeStateRequest(number);

            SpiceManager spiceManager = this.getApp().getSpiceManager();
            spiceManager.execute(bikeStateRequest, new BikeStateListener());
            setBusy(true);
        } else {
            TextView bike_state_view = (TextView) RentFragment.this.getView().findViewById(R.id.bike_state);
            bike_state_view.setText("");
        }
    }
    
    @Override
    public void onStart() {
        final TextView textView = (TextView) this.getView().findViewById(R.id.bikeId);
        if (this.bikeId != null){
            textView.setText(this.bikeId);
            this.checkBikeState(this.bikeId);
        }  
        textView.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                String bikeNumber = textView.getText().toString();
                RentFragment.this.bikeId = bikeNumber;
                Logger.d("Text has changed to: " + bikeNumber);
                RentFragment.this.checkBikeState(bikeNumber);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
        super.onStart();
    }
    
    public static RentFragment createInstance(String bikeId){
        RentFragment fragment = new RentFragment();
        Bundle bdl = new Bundle(1);
        bdl.putString("bikeId", bikeId);
        fragment.setArguments(bdl);
        return fragment;
    }
    
    public static class RentFactory implements FragmentFactory {
        private String bikeId;

        public RentFactory() {
            this.bikeId = null;
        }
        
        public RentFactory(String bikeId) {
            this.bikeId = bikeId;
        }

        public Fragment create(){
            return RentFragment.createInstance(this.bikeId);
        }
    }

    @Override
    public void onRequestFailure(SpiceException e) {
        setBusy(false);
        Logger.e("Rent request failed", e);
        Logger.e(this.toString() + " exception: " + e.getMessage(), e);

        if(HttpMessageNotReadableException.class.isInstance(e.getCause())){
            HttpMessageNotReadableException httpErr = (HttpMessageNotReadableException)e.getCause();
        }
        show_toast(R.string.no_network);
    }

    @Override
    public void onRequestSuccess(RentalAction rental) {
        setBusy(false);
        final TextView textView = (TextView) this.getView().findViewById(R.id.bikeId);
        Rental rental_details = rental.getRental();
        if (rental_details == null) {
            Logger.w("Rental failed, no details");
            show_toast(R.string.rentail_failed);
            textView.setError(getString(R.string.rentail_failed));
        } else {
            Logger.i("Rental success: " + rental.getRental().getStartTime() + " " + rental.getRental().getEndTime());
            show_toast(R.string.bike_rented);
            textView.setText("");
            textView.setError(null);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO : make sure that "v" is "rent_button"
        TextView textView = (TextView) this.getView().findViewById(R.id.bikeId);
        String bike_id = textView.getText().toString();
        if (bike_id.length() < BIKE_ID_LENGTH){
            textView.setError(getResources().getString(R.string.invalid_bike_id));
        } else {
            textView.setError(null);
            this.performRequest();
        }
    }
}
