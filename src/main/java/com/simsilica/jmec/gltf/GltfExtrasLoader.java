/*
 * $Id$
 * 
 * Copyright (c) 2019, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.jmec.gltf;

import java.lang.reflect.Array;
import java.util.*;

import org.slf4j.*;

import com.google.gson.*;

import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.gltf.*;

/**
 *  Attaches GLTF "extras" data to objects as appropriate.
 *
 *  @author    Paul Speed
 */
public class GltfExtrasLoader implements ExtrasLoader {
    static Logger log = LoggerFactory.getLogger(GltfExtrasLoader.class);
    
    public static final GltfExtrasLoader INSTANCE = new GltfExtrasLoader();
    
    public GltfExtrasLoader() {
    }
    
    /**
     *  Utility method to create a ModelKey that is configured to use this
     *  ExtrasLoader.
     */
    public static GltfModelKey createModelKey( String path ) {
        GltfModelKey key = new GltfModelKey(path);
        key.setExtrasLoader(INSTANCE);
        return key;
    } 
    
    @Override
    public Object handleExtras( GltfLoader loader, String parentName, 
                                JsonElement parent, JsonElement extras, Object input ) {
 
        log.debug("handleExtras(" + loader + ", " + parentName + ", " + parent + ", " + extras + ", " + input + ")");     

        // Only interested in composite objects
        if( !extras.isJsonObject() ) {
            log.warn("Skipping extras:" + extras);
            return input; 
        }
        JsonObject jo = extras.getAsJsonObject();
        apply(input, jo);
        return input;
    }
    
    protected void apply( Object input, JsonObject extras ) {
        if( input == null ) {
            return;
        }
        if( input.getClass().isArray() ) {
            applyToArray(input, extras);   
        } else if( input instanceof Spatial ) {
            applyToSpatial((Spatial)input, extras); 
        } else {
            log.warn("Unhandled input type:" + input.getClass());
        }
    }
    
    protected void applyToArray( Object array, JsonObject extras ) {
        int size = Array.getLength(array);
        for( int i = 0; i < size; i++ ) {
            Object o = Array.get(array, i);            
            log.debug("processing array[" + i + "]:" + o);
            apply(o, extras);
        }
    }
    
    protected void applyToSpatial( Spatial spatial, JsonObject extras ) {
        for( Map.Entry<String, JsonElement> el : extras.entrySet() ) {
            log.debug(el.toString());
            Object val = toAttribute(el.getValue(), false);
            if( log.isDebugEnabled() ) {
                log.debug("setUserData(" + el.getKey() + ", " + val + ")");
            }            
            spatial.setUserData(el.getKey(), val);
        }         
    }
 
    protected Object toAttribute( JsonElement el, boolean nested ) {
        if( el.isJsonObject() ) {
            return toAttribute(el.getAsJsonObject(), nested);
        } else if( el.isJsonArray() ) {
            return toAttribute(el.getAsJsonArray(), nested);
        } else if( el.isJsonPrimitive() ) {
            return toAttribute(el.getAsJsonPrimitive(), nested);
        } else if( el.isJsonNull() ) {
            return null;
        }
        log.warn("Unhandled extras element:" + el);
        return null;       
    }
    
    protected Object toAttribute( JsonObject jo, boolean nested ) {
        Map<String, Object> result = new HashMap<>();
        for( Map.Entry<String, JsonElement> el : jo.entrySet() ) {
            result.put(el.getKey(), toAttribute(el.getValue(), true)); 
        }
        return result;       
    }
    
    protected Object toAttribute( JsonArray ja, boolean nested ) {
        List<Object> result = new ArrayList<>();
        for( JsonElement el : ja ) {
            result.add(toAttribute(el, true));
        }
        return result;
    }
    
    protected Object toAttribute( JsonPrimitive jp, boolean nested ) {
        if( jp.isBoolean() ) {
            return jp.getAsBoolean();
        } else if( jp.isNumber() ) {
            // JME doesn't save Maps properly and treats them as two
            // separate Lists... and it doesn't like saving Doubles
            // in lists so we'll just return strings in the case where
            // the value would end up in a map.  If users someday really
            // need properly typed map values and JME map storage hasn't
            // been fixed then perhaps we give the users the option of
            // flattening the nested properties into dot notation, ie:
            // all directly on UserData with no Map children.
            if( nested ) {
                return jp.getAsString();
            }
            Number num = jp.getAsNumber();
            // JME doesn't like to save GSON's LazilyParsedNumber so we'll
            // convert it into a real number.  I don't think we can reliably
            // guess what type of number the user intended.  It would take
            // some expirimentation to determine if things like 0.0 are preserved
            // during export or just get exported as 0.
            // Rather than randomly flip-flop between number types depending
            // on the inclusion (or not) of a decimal point, we'll just always
            // return Double. 
            return num.doubleValue();
        } else if( jp.isString() ) {
            return jp.getAsString();
        }
        log.warn("Unhandled primitive:" + jp);
        return null;
    }
}


