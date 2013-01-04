package com.frca.dotatimer.helper;

import java.util.HashMap;

public class ParameterMap extends HashMap<String, String>
{
    private static final long serialVersionUID = 1L;
    
    public ParameterMap()
    {
        super();
        put(Constants.TAG_PASS, Constants.HASH_PASS);
    }

}
