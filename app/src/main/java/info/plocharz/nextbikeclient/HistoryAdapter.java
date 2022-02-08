package info.plocharz.nextbikeclient;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import info.plocharz.nextbikeclient.ReturnDialogFragment.OnReturnListener;
import info.plocharz.nextbikeclient.model.Operation;
import info.plocharz.nextbikeclient.model.Rental;
import info.plocharz.nextbikeclient.model.Transaction;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistoryAdapter extends ArrayAdapter<Operation> implements OnClickListener {
    private static final int TYPE_RENTED = 0;
    private static final int TYPE_RETURNED = 1;
    private static final int TYPE_TRANSACTION = 2;

    public HistoryAdapter(Context context, ArrayList<Operation> items){
        super(context, R.layout.history_item, items);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        Operation op = getItem(position);
        if (Rental.class.isInstance(op)) {
            Rental rental = (Rental) op;
            if (!rental.isReturned())
                return 0;
            else
                return 1;
        }
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position; 
    }

    private View view_for_position(Integer position){
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());

        if (this.getItemViewType(position) == 0)
            return vi.inflate(R.layout.history_item_rented, null);
        if (this.getItemViewType(position) == 1)
            return vi.inflate(R.layout.history_item, null);
        if (this.getItemViewType(position) == 2)
            return vi.inflate(R.layout.history_item_transaction, null);
        throw new RuntimeException("Invalid View Type");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    
        View v = convertView;
        Operation p = getItem(position);
        assert(p != null);
    
        if (v == null) {
            v = this.view_for_position(position);
        }

        TextView tt = (TextView) v.findViewById(R.id.history_text);

        TextView tc = (TextView) v.findViewById(R.id.credit_amount);
        if(tc != null) {
            Double amount = p.getCreditAmount();
            if (amount != null)
                tc.setText(new DecimalFormat("###,##0.00").format(amount));
            else
                tc.setText("--");
        }

        TextView td = (TextView) v.findViewById(R.id.history_date);
        if(td != null) {
            Date netDate = (new Date(p.getTimestamp()));
            String formatted = DateFormat.getDateInstance(DateFormat.SHORT).format(netDate);
            td.setText(formatted);
        }

        View returnButton = v.findViewById(R.id.return_button);
        if(returnButton != null) {
            returnButton.setOnClickListener(this);
            returnButton.setTag(position);
        }

        if (Rental.class.isInstance(p)) {
            tt.setText(p.toLocalString(this.getContext()));
            TextView code_view = (TextView) v.findViewById(R.id.code);
            TextView code_view_text = (TextView) v.findViewById(R.id.code_text);

            if (this.isRecent(p.getTimestamp())){
                Rental rental = (Rental) p;
                code_view.setText(rental.getUnlockCode());
                code_view.setVisibility(View.VISIBLE);
                code_view_text.setVisibility(View.VISIBLE);
            } else {
                code_view.setVisibility(View.GONE);
                code_view_text.setVisibility(View.GONE);
            }
        }

        return v;
    }

    boolean isRecent(long timestamp) {
        Long now = System.currentTimeMillis();
        return timestamp > now - 24L * 60L *60L * 1000L;  // 1 hour
    }

    @Override
    public void onClick(View v) {
        final Integer position = (Integer)v.getTag();
        final Rental rental = (Rental)getItem(position);
        Logger.i("Return bike: " + rental.toLocalString(this.getContext()));
        
        ReturnDialogFragment newFragment = new ReturnDialogFragment();
        newFragment.setRental(rental);
        newFragment.show(((FragmentActivity)this.getContext()).getSupportFragmentManager(), "return");
        
        newFragment.setOnReturnListener(new OnReturnListener() {
                    public void onReturn(Rental return_) {
                        HistoryAdapter.this.remove(rental);
                        HistoryAdapter.this.insert(return_, position);
                    }
                    
                }
        );
    }

}
