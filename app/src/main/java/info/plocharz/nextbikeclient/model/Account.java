package info.plocharz.nextbikeclient.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="account", strict=false)
public class Account {
    
    @ElementList(entry="rental", inline=true, required = false)
    private Collection<Rental> rentals = new ArrayList<Rental>();

    @ElementList(entry="transaction", inline=true, required = false)
    private Collection<Transaction> transactions = new ArrayList<Transaction>();
    
    private class OperationsComparator implements Comparator<Operation>{

        @Override
        public int compare(Operation lhs, Operation rhs) {
            return (int) -(lhs.getStartTime() - rhs.getStartTime());
        }
        
    }
    
    public ArrayList<Operation> getOperations() {
        ArrayList<Operation> operations = new ArrayList<Operation>();

        for(Operation operation: this.rentals){
            if(operation.isValid())
                operations.add(operation);
        }
        operations.addAll(this.transactions);
        Collections.sort(operations, new OperationsComparator());
        return operations;
    }
}
