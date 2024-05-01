package edu.osu.cws.evals.backend;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BackendAction {

    /**
     * This class is the Driver for the Backend cron job. It takes care of getting an
     * instance of the BackendMgr and calling the process method to execute things.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new BackendModule());
        BackendMgr mgr = injector.getInstance(BackendMgr.class);
        mgr.process();
    }

}
