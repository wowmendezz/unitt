package com.unitt.servicemanager.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.unitt.servicemanager.websocket.MessageBody;
import com.unitt.servicemanager.websocket.MessageResponse;
import com.unitt.servicemanager.websocket.MessageRoutingInfo;

public class MockServiceDelegate extends ServiceDelegate
{
    public static final int SERVICE_ARG_COUNT = 101;
    public static final String SERVICE_ARG_VALUE = "TestValue";
    
    protected BlockingQueue<MessageResponse> destQueue;
    protected ConcurrentMap<String, MessageBody> bodyMap;
    
    public MockServiceDelegate(Object aService)
    {
        super(aService, 10000);
        
        destQueue = new ArrayBlockingQueue<MessageResponse>( 10 );
        bodyMap = new ConcurrentHashMap<String, MessageBody>();
    }
    
    @Override
    public Object[] getArguments( MessageRoutingInfo aInfo )
    {
        return new Object[] {SERVICE_ARG_COUNT, SERVICE_ARG_VALUE};
    }

    @Override
    public ConcurrentMap<String, MessageBody> getBodyMap( MessageRoutingInfo aInfo )
    {
        return bodyMap;
    }

    @Override
    public BlockingQueue<MessageResponse> getDestinationQueue( MessageRoutingInfo aInfo )
    {
        return destQueue;
    }

}
