package org.debs.challenger2;

import org.apache.cxf.endpoint.Server;
import org.debs.challenger2.rest.RestServer;

public class ShutdownHook implements Runnable{

    private final Server restServer;

    public ShutdownHook(Server restServer){
        this.restServer = restServer;
    }
    @Override
    public void run() {

        restServer.destroy();

    }
}
