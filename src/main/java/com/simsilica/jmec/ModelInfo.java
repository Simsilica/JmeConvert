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

package com.simsilica.jmec;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import com.jme3.asset.*;
import com.jme3.material.*;
import com.jme3.scene.*;

/**
 *  Inspected meta-data about a loaded model asset.
 *  
 *  @author    Paul Speed
 */
public class ModelInfo {

    static Logger log = LoggerFactory.getLogger(ModelInfo.class);
 
    private File root;
    private String name;
    private Spatial model; 
    private Map<CloneableSmartAsset, Dependency> dependencies = new HashMap<>(); 
    
    public ModelInfo( File root, String name, Spatial model ) {
        this.root = root;
        this.name = name;    
        this.model = model;
        findDependencies(model);
    }
 
    public Spatial getModel() {
        return model;
    }
 
    public void setModelName( String name ) {
        this.name = name;
    }
 
    public String getModelName() {
        return name;
    }
 
    public Collection<Dependency> getDependencies() {
        return dependencies.values();
    }
 
    public Dependency getDependency( CloneableSmartAsset asset ) {
        return dependencies.get(asset);
    }
    
    private void findDependencies( Spatial s ) {
        log.debug("findDependencies(" + s + ")");
        if( s instanceof Node ) {
            Node n = (Node)s;
            for( Spatial child : n.getChildren() ) {
                findDependencies(child);
            }
        } else if( s instanceof Geometry ) {
            findDependencies(((Geometry)s).getMaterial());
        } 
    }
    
    private void findDependencies( Material m ) {
        log.debug("findDependencies(" + m + ")");
        if( m.getKey() != null ) {
            dependencies.put(m, new Dependency(root, m));   
        }
        for( MatParam mp : m.getParams() ) {
            log.debug("Checking:" + mp);
            Object val = mp.getValue();
            if( !(val instanceof CloneableSmartAsset) ) {
                continue;
            }
            CloneableSmartAsset asset = (CloneableSmartAsset)val;
            log.debug("material asset:" + asset);
            if( asset.getKey() != null ) {
                addDependency(root, asset);
            }
        }        
    }
    
    private Dependency addDependency( File root, CloneableSmartAsset asset ) {
        Dependency result = dependencies.get(asset);
        if( result == null ) {
            dependencies.put(asset, new Dependency(root, asset));
            return result; 
        }
        
        // Else just add it to the existing
        result.instances.add(asset);
        return result; 
    }
    
    public static class Dependency implements Comparable<Dependency> {
        private AssetKey key;
        private File file;
        private List<CloneableSmartAsset> instances = new ArrayList<>();
        
        public Dependency( File root, CloneableSmartAsset asset ) {
            instances.add(asset);
            this.key = asset.getKey();
            if( asset.getKey() != null ) {
                this.file = new File(root, asset.getKey().toString());
            }
        }

        public int compareTo( Dependency other ) {
            String s1 = key.toString();
            String s2 = other.getKey().toString();
            return s1.compareTo(s2);
        }

        public AssetKey getOriginalKey() {
            return key;
        }

        public void setKey( AssetKey key ) {
            for( CloneableSmartAsset asset : instances ) {
                asset.setKey(key);
            }
        }
        
        public AssetKey getKey() {
            return instances.get(0).getKey();
        }
        
        public File getSourceFile() {
            return file;
        }
 
        public List<CloneableSmartAsset> getInstances() {
            return instances;
        }
 
        @Override       
        public String toString() {
            return "Dependency[file=" + file + ", key=" + key + "]";
        } 
    }
}
