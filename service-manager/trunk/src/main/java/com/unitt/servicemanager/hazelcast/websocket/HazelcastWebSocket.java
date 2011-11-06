package com.unitt.servicemanager.hazelcast.websocket;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.unitt.servicemanager.routing.MessageRouterJob;
import com.unitt.servicemanager.util.ValidationUtil;
import com.unitt.servicemanager.websocket.MessageSerializerRegistry;
import com.unitt.servicemanager.websocket.MessagingWebSocket;
import com.unitt.servicemanager.websocket.SerializedMessageBody;
import com.unitt.servicemanager.websocket.ServerWebSocket;


public class HazelcastWebSocket extends MessagingWebSocket
{
    private static Logger                   logger = LoggerFactory.getLogger( HazelcastWebSocket.class );

    private HazelcastInstance               hazelcastClient;
    private BlockingQueue<MessageRouterJob> headerQueue;
    private String                          headerQueueName;

    protected boolean                       isInitialized;


    // constructors
    // ---------------------------------------------------------------------------
    public HazelcastWebSocket()
    {
        // default
    }

    public HazelcastWebSocket( MessageSerializerRegistry aSerializers, long aQueueTimeoutInMillis, String aHeaderQueueName, ServerWebSocket aServerWebSocket, HazelcastInstance aHazelcastClient )
    {
        this( aSerializers, aQueueTimeoutInMillis, aHeaderQueueName, aServerWebSocket, aHazelcastClient, null );
    }

    public HazelcastWebSocket( MessageSerializerRegistry aSerializers, long aQueueTimeoutInMillis, String aHeaderQueueName, ServerWebSocket aServerWebSocket, HazelcastInstance aHazelcastClient, String aSocketId )
    {
        super( aSerializers, aQueueTimeoutInMillis, aServerWebSocket, aSocketId );
        setHazelcastClient( aHazelcastClient );
        setHeaderQueueName( aHeaderQueueName );
    }


    // lifecycle logic
    // ---------------------------------------------------------------------------
    public void initialize()
    {
        String missing = null;

        // validate we have all properties
        if ( getHazelcastClient() == null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing hazelcast client. " );
        }
        if ( getHazelcastClient().getQueue( getSocketId() ) != null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing socket queue. " );
        }
        if ( getHazelcastClient().getMap( getSocketId() ) != null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing socket map. " );
        }
        if ( getHeaderQueueName() == null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing header queue name." );
        }
        if ( getHeaderQueue() == null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing header queue: " + getHeaderQueueName() + ". " );
        }

        // fail out with appropriate message if missing anything
        if ( missing != null )
        {
            logger.error( missing );
            throw new IllegalStateException( missing );
        }

        setInitialized( true );
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    public void destroy()
    {
        // destroy map
        try
        {
            getHazelcastClient().getMap( getSocketId() ).destroy();
        }
        catch ( Exception e )
        {
            logger.error( "An error occurred while destroying the body map for websocket: " + getSocketId(), e );
        }

        // clear hazelcast
        setHazelcastClient( null );
        setHeaderQueueName( null );
        headerQueue = null;
        isInitialized = false;
    }


    // getters & setters
    // ---------------------------------------------------------------------------
    public HazelcastInstance getHazelcastClient()
    {
        return hazelcastClient;
    }

    public void setHazelcastClient( HazelcastInstance aClient )
    {
        hazelcastClient = aClient;
    }

    public String getHeaderQueueName()
    {
        return headerQueueName;
    }

    public void setHeaderQueueName( String aHeaderQueueName )
    {
        headerQueueName = aHeaderQueueName;
    }


    // service logic
    // ---------------------------------------------------------------------------
    public ConcurrentMap<String, SerializedMessageBody> getBodyMap()
    {
        return getHazelcastClient().getMap( "body:" + getSocketId() );
    }

    public BlockingQueue<MessageRouterJob> getHeaderQueue()
    {
        if ( headerQueue == null )
        {
            headerQueue = getHazelcastClient().getQueue( getHeaderQueueName() );
        }

        return headerQueue;
    }
}
