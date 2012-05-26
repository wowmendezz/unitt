package com.unitt.servicemanager.websocket.server.jetty;


import com.unitt.servicemanager.util.ValidationUtil;
import com.unitt.servicemanager.websocket.MessagingWebSocket;
import com.unitt.servicemanager.websocket.MessagingWebSocketManager;
import com.unitt.servicemanager.websocket.ServerWebSocket;
import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class ServerSocketAdapter implements WebSocket, WebSocket.OnBinaryMessage, ServerWebSocket
{
    private static Logger             logger = LoggerFactory.getLogger( ServerSocketAdapter.class );

    private String                    requestUri;
    private String                    protocol;
    private MessagingWebSocketManager manager;
    private Connection                connection;
    private MessagingWebSocket        socket;
    private boolean                   isInitialized;


    // constructors
    // ---------------------------------------------------------------------------
    public ServerSocketAdapter()
    {
        // default
    }

    public ServerSocketAdapter( HttpServletRequest aRequest, String aProtocol, MessagingWebSocketManager aFactory )
    {
        manager = aFactory;
        aRequest.getRequestURI();
    }


    // lifecycle logic
    // ---------------------------------------------------------------------------
    public void initialize()
    {
        String missing = null;

        // validate we have all properties
        if ( getManager() == null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing websocket manager. " );
        }
        if ( getConnection() == null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing websocket connection. " );
        }
        if ( getSocket() == null )
        {
            missing = ValidationUtil.appendMessage( missing, "Missing websocket. " );
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
        setConnection( null );
        setManager( null );
        setProtocol( null );
        setRequestUri( null );
        setSocket( null );
        setInitialized( false );
    }

    protected void setInitialized( boolean aIsInitialized )
    {
        isInitialized = aIsInitialized;
    }


    // getters & setters
    // ---------------------------------------------------------------------------
    public String getRequestUri()
    {
        return requestUri;
    }

    public void setRequestUri( String aRequestUri )
    {
        requestUri = aRequestUri;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol( String aProtocol )
    {
        protocol = aProtocol;
    }

    public MessagingWebSocketManager getManager()
    {
        return manager;
    }

    public void setManager( MessagingWebSocketManager aManager )
    {
        manager = aManager;
    }

    public Connection getConnection()
    {
        return connection;
    }

    public void setConnection( Connection aConnection )
    {
        connection = aConnection;
    }

    public MessagingWebSocket getSocket()
    {
        return socket;
    }

    public void setSocket( MessagingWebSocket aSocket )
    {
        socket = aSocket;
    }


    // server web socket logic
    // ---------------------------------------------------------------------------
    public void sendMessage( byte[] aMessage ) throws IOException
    {
        getConnection().sendMessage( aMessage, 0, aMessage.length );
    }


    // jetty web socket logic
    // ---------------------------------------------------------------------------
    public void onMessage( byte[] aData, int aOffset, int aLength )
    {
        if ( aData.length != aLength && aOffset != 0 )
        {
            byte[] data = new byte[aLength];
            System.arraycopy( aData, aOffset, data, 0, aLength );
            getSocket().onMessage( data );
        }
        else
        {
            getSocket().onMessage( aData );
        }
    }

    public void onClose( int aCloseCode, String aMessage )
    {
        getManager().destroyWebSocket( getSocket() );
        destroy();
    }

    public void onOpen( Connection aConnection )
    {
        setConnection( aConnection );
        setSocket( getManager().createWebSocket(this) );
        initialize();
    }
}
