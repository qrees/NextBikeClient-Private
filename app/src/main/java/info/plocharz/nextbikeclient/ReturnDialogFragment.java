package info.plocharz.nextbikeclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import info.plocharz.nextbikeclient.model.Place;
import info.plocharz.nextbikeclient.model.Rental;
import info.plocharz.nextbikeclient.model.ReturnAction;
import info.plocharz.nextbikeclient.orm.Station;
import info.plocharz.nextbikeclient.requests.ReturnRequest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ReturnDialogFragment
    extends DialogFragment
    implements OnItemClickListener, RequestListener<ReturnAction> {

    private Rental rental;
    public ArrayList<Station> places;
    private OnReturnListener onReturnListener;
    private boolean busy = false;

    public ReturnDialogFragment(){}

    void setBusy(boolean busy){
        this.busy = busy;
    }

    boolean isBusy(){
        return this.busy;
    }

    private Station placeForId(int station_id){
        Application app = (Application) this.getActivity().getApplication();
        List<Station> stations = app.getMarkers().getMarkers();
        for(Station station: stations) {
            if (Integer.parseInt(station.getUid()) == station_id) {
                return station;
            }
        }
        return null;
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = this.getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.return_view, null);

        builder.setView(view)
            .setPositiveButton(R.string.return_, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReturnDialogFragment.this.onReturnButtonClick();
                    }
                });
            }
        });


        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog dialogInstance = (AlertDialog)getDialog();
        ListView markersList = (ListView) dialogInstance.findViewById(R.id.station_list);
        markersList.setOnItemClickListener(this);
        this.updateStations();
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    private class StationComparator implements Comparator<Station>{

        private LatLng latlng;

        StationComparator(Application app, LatLng latLng) {
            this.latlng = latLng;
        }

        @Override
        public int compare(Station lhs, Station rhs) {
            return (int) (Application.distance(lhs.getLatLng(), this.latlng) - Application.distance(rhs.getLatLng(), this.latlng));
        }

    }

    private class SortLocationsTask extends AsyncTask<Location, Integer, Long> {

        private Application app;
        private ArrayList<Station> places;
        private Rental rental;

        SortLocationsTask(Application app, Rental rental) {
            super();
            this.app = app;
            this.rental = rental;
        }

        protected Long doInBackground(Location... locations) {
            String startPlaceUid = this.rental.getStartPlace();

            Station startPlace = placeForId(Integer.parseInt(startPlaceUid));
            LatLng latLng;
            if(startPlace != null) {
                latLng = startPlace.getLatLng();
                Logger.i("Searching station near rent location");
            }else if(null != locations[0]){
                latLng = new LatLng(locations[0].getLatitude(), locations[0].getLongitude());
                Logger.i("Searching station near rent gps location");
            } else {
                return (long) 0;
            }
            places = app.getMarkers().getPlacesNear(latLng, 10000.0);
            Collections.sort(places, new StationComparator(app, latLng));
            return (long) 0;
        }

        protected void onPostExecute(Long result) {
            final AlertDialog dialogInstance = (AlertDialog)getDialog();
            if(dialogInstance != null && this.places != null){
                ListView markersList = (ListView) dialogInstance.findViewById(R.id.station_list);
                markersList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, this.places));
            } else if (dialogInstance != null){
                ListView markersList = (ListView) dialogInstance.findViewById(R.id.station_list);
                markersList.setVisibility(View.GONE);
            }
            ReturnDialogFragment.this.places = this.places;
        }
    }

    private void updateStations() {
        Application app = (Application) this.getActivity().getApplication();
        Location location = app.getBestLocation();

        new SortLocationsTask(app, this.rental).execute(location);
    }

    private void performReturnRequest(Rental rental, Station place){
        if (isBusy()) {
            return;
        }
        setBusy(true);
        MainActivity activity = (MainActivity) this.getActivity();
        Application app = activity.getMyApplication();

        String loginKey = app.getAuth().getLoginKey();
        ReturnRequest request = new ReturnRequest(loginKey, rental.getId(), rental.getBikeId(), place.getUid());

        SpiceManager spiceManager = app.getSpiceManager();
        spiceManager.execute(request, this);
    }

    private void onReturnButtonClick() {
        final AlertDialog dialogInstance = (AlertDialog)getDialog();
        if(dialogInstance == null ){
            return;
        }
        Toast toast = Toast.makeText(this.getContext(), R.string.invalid_station_id, Toast.LENGTH_SHORT);
        Station station;
        Integer station_id;
        EditText station_id_edit_text = (EditText) dialogInstance.findViewById(R.id.stationId);
        try {
            station_id = Integer.parseInt(station_id_edit_text.getText().toString());
        } catch (NumberFormatException exc) {
            Logger.e("Failed to parse stations id", exc);
            toast.show();
            return;
        }
        station = this.placeForId(station_id);
        if (station == null){
            Logger.e("Station not found");
            toast.show();
            return;
        }
        dialogInstance.dismiss();
        performReturnRequest(this.rental, station);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Station place = this.places.get(position);
        Logger.i("Selected place: " + place.toString());
        Logger.i("Selected place: " + place.getUid());
        performReturnRequest(this.rental, place);
    }

    @Override
    public void onRequestFailure(SpiceException exception) {
        Logger.e(this.toString() + " exception: " + exception.getMessage(), exception);
        setBusy(false);
        Logger.e(String.format("Return request failed: %s", exception.getMessage()), exception);
    }

    @Override
    public void onRequestSuccess(ReturnAction response) {
        setBusy(false);

        Dialog dialog = getDialog();
        Rental return_ = response.getRental();
        Logger.i("Return success: " + return_.getCreditAmount() + " "  + return_.getEndPlace());
        this.onReturnListener.onReturn(return_);
        if(dialog != null)
            dialog.dismiss();
    }

    public void setOnReturnListener(OnReturnListener listener){
        this.onReturnListener = listener;
    }

    public interface OnReturnListener {
        void onReturn(Rental return_);
    }
}
