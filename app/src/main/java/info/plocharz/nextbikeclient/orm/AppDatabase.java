package info.plocharz.nextbikeclient.orm;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by Krzysztof on 2017-03-10.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    public static final String NAME = "AppDatabase";

    public static final int VERSION = 1;
}
