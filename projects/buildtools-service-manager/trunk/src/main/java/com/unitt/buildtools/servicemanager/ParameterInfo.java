/*
 *  Licensed to UnitT Software, Inc. under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.unitt.buildtools.servicemanager;


public class ParameterInfo
{
    protected String name;
    protected String nameWithoutPrefix;
    protected String prefix = "a";
    protected String id;
    protected boolean isPrimitive;
    protected String type;
    
    public boolean isPrimitive()
    {
        return isPrimitive;
    }

    public void setPrimitive(boolean aIsPrimitive)
    {
        isPrimitive = aIsPrimitive;
    }

    public String getNameWithoutPrefix()
    {
        return nameWithoutPrefix;
    }

    public void setNameWithoutPrefix( String aNameWithoutPrefix )
    {
        nameWithoutPrefix = aNameWithoutPrefix;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix( String aPrefix )
    {
        prefix = aPrefix;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String aName)
    {
        id = aName;
        name = aName;
        nameWithoutPrefix = aName.substring(prefix.length(), prefix.length() + 1 ).toLowerCase() + aName.substring(prefix.length() + 1);
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String aType )
    {
        type = aType;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + ": name=" + name;
    }
}