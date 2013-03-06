package edu.osu.cws.evals.backend;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BackendAction {

    public static void main(String[] args) throws Exception
    {
        Injector injector = Guice.createInjector(new BackendModule());
        BackendMgr mgr = injector.getInstance(BackendMgr.class);
        System.out.println(mgr.toString());
    }

}
