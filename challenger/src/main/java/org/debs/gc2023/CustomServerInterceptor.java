package org.debs.gc2023;

import java.net.InetSocketAddress;

import io.grpc.*;

public class CustomServerInterceptor implements ServerInterceptor {

    private String clientIpAddress;
    private int clientLocalPort;
    private String proxyIdentifier;

    public String getProxyIdentifier() {
        return proxyIdentifier;
    }

    public int getClientLocalPort() {
        return clientLocalPort;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
                    
        // Attempt to extract proxy information from the remote address
        Object remoteAddrObj = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteAddrObj instanceof InetSocketAddress) {
            InetSocketAddress remoteAddr = (InetSocketAddress) remoteAddrObj;
            int proxyPort = remoteAddr.getPort();

            // Use the proxy port as an identifier (you may need to adjust this based on your proxy setup)
            proxyIdentifier = "ProxyPort_" + proxyPort;
        } else {
            // Unable to extract proxy information from the remote address
            proxyIdentifier = "UnknownProxy";
        }


        this.clientIpAddress = call.getAttributes().get(io.grpc.Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
               

        // Continue processing the gRPC call
        return next.startCall(call, headers);
    }
}