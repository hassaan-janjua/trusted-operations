package be.kuleuven.dnet.sergiodemo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hqss on 11/27/2017.
 */

public class EncryptedBLEData implements Serializable {
    private ArrayList<Byte> encData;
    private boolean initialized;
    private int size;

    public EncryptedBLEData()
    {
        reset();
    }

    public void reset()
    {
        encData = new  ArrayList<Byte>();
        initialized = false;
        size = 0;
    }

    private void initialize(byte []data)
    {
        int i = 0;

        size = data[0] & 0xFF;
        if (size != 179)
            size = 0;
        initialized = true;
        i = 1;

        append_internal(data, 1);
    }

    private void append_internal(byte []data, int offset)
    {
        int i = offset;
        int prev_len = encData.size();

        while(data.length > i && size > (i - offset + prev_len))
        {
            encData.add(data[i]);
            i++;
        }
    }

    public boolean isFull()
    {
        return size == encData.size();
    }

    public void append(byte []data)
    {
        if (isFull())
        {
            reset();
        }

        if (initialized)
        {
            append_internal(data, 0);
        }
        else
        {
            initialize(data);
        }
    }

    public int getSize()
    {
        return size;
    }

    public byte[] getRawBytes()
    {
        final int n = encData.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = encData.get(i);
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Byte b: encData)
        {
            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }

}
