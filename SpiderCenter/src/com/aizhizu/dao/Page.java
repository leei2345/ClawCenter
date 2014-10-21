package com.aizhizu.dao;

public class Page
{

    public Page()
    {
    }

    public long getInc()
    {
        return inc;
    }

    public long getFrom()
    {
        return from;
    }

    public void setFrom(long from)
    {
        this.from = from;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public void incFrom(long inc)
    {
        this.inc = inc;
        from += inc;
    }

    private long from;
    private long size;
    private long inc;
}