package com.sdk.karzalivness;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class AppExecutors {

    private final Executor exeDiskIO;
    private final Executor exeNetworkIO;
    private final Executor exeMainThread;
    private static AppExecutors executors;


    //*****************************************************************************//
    //******************** Instantiation and Constructors *************************//

    public static AppExecutors getInstance() {
        if (executors == null) {
            executors = new AppExecutors();
        }
        return executors;
    }

    private AppExecutors(Executor exeDiskIO, Executor exeNetworkIO, Executor exeMainThread) {
        this.exeDiskIO = exeDiskIO;
        this.exeNetworkIO = exeNetworkIO;
        this.exeMainThread = exeMainThread;
    }


    //**************************************************************************//
    //*************** Public methods to get the thread executors ***************//

    public Executor getExeDiskIO() {
        return exeDiskIO;
    }

    public Executor getExeNetworkIO() {
        return exeNetworkIO;
    }

    public Executor getExeMainThread() {
        return exeMainThread;
    }


    //**************************************************************************//
    //****************** Singleton Public Method + Extra Method ****************//

    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3),
                new MainThreadExecutor());
    }

    private static class MainThreadExecutor implements Executor {
        private Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    }

}
