package com.unitt.framework.websocket;


import java.nio.ByteBuffer;
import java.util.Random;


/**
 * @author Josh Morris
 */
public class WebSocketFragment
{
    public enum MessageOpCode
    {
        ILLEGAL( -1 ), CONTINUATION( 0x0 ), TEXT( 0x1 ), BINARY( 0x2 ), CLOSE( 0x8 ), PING( 0x9 ), PONG( 0xA );

        private int opCode;

        private MessageOpCode( int aOpCode )
        {
            opCode = aOpCode;
        }

        public int getOpCode()
        {
            return opCode;
        }
    };

    public enum PayloadType
    {
        UNKNOWN, TEXT, BINARY
    };

    public enum PayloadLength
    {
        ILLEGAL, MINIMUM, SHORT, LONG
    };

    private static Random randomizer = new Random();

    private boolean       isFinal;
    private int           mask;
    private int           payloadStart;
    private int           payloadLength;
    private PayloadType   payloadType;
    private byte[]        payloadData;
    private MessageOpCode opCode     = MessageOpCode.ILLEGAL;
    private byte[]        fragment;


    // constructors
    // ---------------------------------------------------------------------------
    public WebSocketFragment( MessageOpCode aOpCode, boolean aIsFinal, boolean aUseMask, byte[] aPayload )
    {
        if ( aUseMask )
        {
            setMask( generateMask() );
        }
        setOpCode( aOpCode );
        setFinal( aIsFinal );
        setPayloadData( aPayload );
        buildFragment();
    }

    public WebSocketFragment( byte[] aFragment )
    {
        setOpCode( MessageOpCode.ILLEGAL );
        setFragment( aFragment );
        parseHeader();
        if ( getMessageLength() <= getFragment().length )
        {
            parseContent();
        }
    }


    // getters & setters
    // ---------------------------------------------------------------------------
    public boolean isFinal()
    {
        return isFinal;
    }

    public void setFinal( boolean aIsFinal )
    {
        isFinal = aIsFinal;
    }

    public int getMask()
    {
        return mask;
    }

    public void setMask( int aMask )
    {
        mask = aMask;
    }

    public int getPayloadStart()
    {
        return payloadStart;
    }

    public void setPayloadStart( int aPayloadStart )
    {
        payloadStart = aPayloadStart;
    }

    public int getPayloadLength()
    {
        return payloadLength;
    }

    public void setPayloadLength( int aPayloadLength )
    {
        payloadLength = aPayloadLength;
    }

    public PayloadType getPayloadType()
    {
        return payloadType;
    }

    public void setPayloadType( PayloadType aPayloadType )
    {
        payloadType = aPayloadType;
    }

    public void appendFragment( byte[] aData )
    {
        fragment = WebSocketUtil.appendArray( fragment, aData );
    }

    public byte[] getPayloadData()
    {
        return payloadData;
    }

    public void setPayloadData( byte[] aPayloadData )
    {
        payloadData = aPayloadData;
    }

    public MessageOpCode getOpCode()
    {
        return opCode;
    }

    public void setOpCode( MessageOpCode aOpCode )
    {
        opCode = aOpCode;
    }

    public void setOpCode( int aOpCode )
    {
        // set default value
        setOpCode( MessageOpCode.ILLEGAL );

        // see if the specified value matches an enum to use instead
        for ( MessageOpCode opCodeEnum : MessageOpCode.values() )
        {
            if ( opCodeEnum.getOpCode() == aOpCode )
            {
                setOpCode( opCodeEnum );
                return;
            }
        }
    }

    public byte[] getFragment()
    {
        return fragment;
    }

    public void setFragment( byte[] aFragment )
    {
        fragment = aFragment;
    }


    // fragment state
    // ---------------------------------------------------------------------------
    public boolean hasMask()
    {
        return getMask() != 0;
    }

    public boolean isControlFrame()
    {
        return getOpCode() == MessageOpCode.CLOSE || getOpCode() == MessageOpCode.PING || getOpCode() == MessageOpCode.PONG;
    }

    public boolean isDataFrame()
    {
        return getOpCode() == MessageOpCode.CONTINUATION || getOpCode() == MessageOpCode.TEXT || getOpCode() == MessageOpCode.BINARY;
    }

    public boolean isValid()
    {
        if ( getMessageLength() > 0 && fragment != null )
        {
            return getPayloadStart() + getPayloadLength() == fragment.length;
        }

        return false;
    }

    public boolean canBeParsed()
    {
        if ( getMessageLength() > 0 && fragment != null )
        {
            return fragment.length >= (getPayloadStart() + getPayloadLength());
        }

        return false;
    }

    public boolean isHeaderValid()
    {
        return payloadStart > 0;
    }

    public boolean isDataValid()
    {
        return getPayloadData() != null && getPayloadData().length > 0;
    }

    public int getMessageLength()
    {
        if ( getFragment() != null && getPayloadStart() > 0 )
        {
            return getPayloadStart() + getPayloadLength();
        }

        return 0;
    }


    // content logic
    // ---------------------------------------------------------------------------
    public void parseHeader()
    {
        // get header data bits
        int bufferLength = 14;
        if ( getFragment() != null && getFragment().length < bufferLength )
        {
            bufferLength = getFragment().length;
        }
        byte[] buffer = WebSocketUtil.copySubArray( getFragment(), 0, bufferLength );

        // determine opcode
        if ( bufferLength > 0 )
        {
            int index = 0;
            setFinal( ( buffer[index] & 0x80 ) != 0 );
            setOpCode( buffer[index++] & 0x0F );

            // handle data depending on opcode
            switch ( getOpCode() )
            {
                case TEXT:
                    setPayloadType( PayloadType.TEXT );
                    break;
                case BINARY:
                    setPayloadType( PayloadType.BINARY );
                    break;
            }

            // handle content, if any
            if ( bufferLength > 1 )
            {
                // do we have a mask
                boolean hasMask = ( buffer[index] & 0x80 ) != 0;

                // get payload length
                Long dataLength = new Integer( buffer[index++] & 0x7F ).longValue();
                if ( dataLength == 126 )
                {
                    // exit if we are missing bytes
                    if ( bufferLength < 4 )
                    {
                        return;
                    }

                    dataLength = new Integer( WebSocketUtil.convertBytesToShort( buffer, index ) ).longValue();
                    index += 2;
                }
                else if ( dataLength == 127 )
                {
                    // exit if we are missing bytes
                    if ( bufferLength < 10 )
                    {
                        return;
                    }

                    index += 8;
                    dataLength = WebSocketUtil.convertBytesToLong( buffer, index );
                }

                // if applicable, set mask value
                if ( hasMask )
                {
                    // exit if we are missing bytes
                    if ( bufferLength < index + 4 )
                    {
                        return;
                    }

                    // grab mask
                    setMask( WebSocketUtil.convertBytesToInt( buffer, index ) );
                    index += 4;
                }

                payloadStart = index;
                if ( dataLength > Integer.MAX_VALUE )
                {
                    throw new IllegalArgumentException( "Implementation does not support payload lengths in excess of " + Integer.MAX_VALUE + ": " + dataLength );
                }
                payloadLength = dataLength.intValue();
            }
        }
    }

    public void parseContent()
    {
        if ( getFragment() != null && getFragment().length >= getPayloadStart() + getPayloadLength() )
        {
            // set payload
            if ( hasMask() )
            {
                setPayloadData( WebSocketFragment.unmask( getMask(), getFragment(), getPayloadStart(), getPayloadLength() ) );
            }
            else
            {
                setPayloadData( WebSocketUtil.copySubArray( getFragment(), getPayloadStart(), getPayloadLength() ) );
            }

            // trim fragment, if necessary
            if ( getFragment() != null && getFragment().length > getMessageLength() )
            {
                setFragment( WebSocketUtil.copySubArray( getFragment(), 0, getMessageLength() ) );
            }
        }
    }

    protected int determineHeaderLength()
    {
        int length = 1;

        // account for including payload length
        int fullPayloadLength = (getPayloadData() != null) ? getPayloadData().length : 0;
        if ( fullPayloadLength <= 125 )
        {
            length += 1;
        }
        else if ( fullPayloadLength <= Short.MAX_VALUE )
        {
            length += 3;
        }
        else
        {
            length += 9;
        }

        // account for mask, if any
        if ( hasMask() )
        {
            length += 4;
        }

        return length;
    }

    public void buildFragment()
    {
        // init
        Integer fullPayloadLength =  (getPayloadData() != null) ? getPayloadData().length : 0;
        int headerLength = determineHeaderLength();
        ByteBuffer output = ByteBuffer.allocate( headerLength + fullPayloadLength );

        // build fin & reserved
        Integer b = 0x0;
        if ( isFinal() )
        {
            b = 0x80;
        }

        // build opmask
        b |= new Integer( getOpCode().opCode & 0xF );

        // push first byte
        output.put( b.byteValue() );

        // use mask
        b = hasMask() ? 0x80 : 0x0;

        // payload length
        if ( fullPayloadLength <= 125 )
        {
            b |= (byte) ( fullPayloadLength & 0xFF );
            output.put( b.byteValue() );
        }
        else if ( fullPayloadLength <= Short.MAX_VALUE )
        {
            b |= 126;
            output.put( b.byteValue() );
            output.putShort( fullPayloadLength.shortValue() );
        }
        else
        {
            b |= 127;
            output.put( b.byteValue() );
            output.putLong( fullPayloadLength.longValue() );
        }

        // mask
        if ( hasMask() )
        {
            output.putInt( getMask() );
        }

        // payload data
        setPayloadStart( headerLength );
        setPayloadLength( fullPayloadLength );
        if (fullPayloadLength > 0)
        {
            if ( hasMask() )
            {
                output.put( mask( getMask(), getPayloadData(), 0, payloadLength ) );
            }
            else
            {
                output.put( getPayloadData() );
            }
        }

        // set fragment
        setFragment( output.array() );

        // cleanup
        output = null;
    }


    // utility methods
    // ---------------------------------------------------------------------------
    public static int generateMask()
    {
        return randomizer.nextInt();
    }

    public static byte[] mask( int aMask, byte[] aData )
    {
        if ( aData != null )
        {
            return mask( aMask, aData, 0, aData.length );
        }

        return null;
    }

    public static byte[] mask( int aMask, byte[] aData, int aStart, int aLength )
    {
        if ( aData != null )
        {
            // init
            byte[] results = new byte[aLength];

            // get mask
            byte[] maskBytes = WebSocketUtil.convertIntToBytes( aMask );

            // loop through mask data, masking
            int end = aStart + aLength;
            byte current;
            int index = aStart;
            if ( end > aData.length )
            {
                end = aData.length;
            }
            int m = 0;
            while ( index < end )
            {
                // set current byte
                current = aData[index];

                // mask
                current ^= maskBytes[m++ % 4];

                // append result & continue
                results[index - aStart] = current;
                index++;
            }

            return results;
        }

        return null;
    }

    public static byte[] unmask( int aMask, byte[] aData )
    {
        return mask( aMask, aData );
    }

    public static byte[] unmask( int aMask, byte[] aData, int aStart, int aLength )
    {
        return mask( aMask, aData, aStart, aLength );
    }
}
