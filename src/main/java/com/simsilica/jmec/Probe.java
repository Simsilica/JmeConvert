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

import java.util.*;

import org.slf4j.*;

import com.jme3.asset.*;
import com.jme3.material.*;
import com.jme3.scene.*;
import com.jme3.scene.control.*;

/**
 *  A model processor that logs information about the 
 *  loaded model.
 *
 *  @author    Paul Speed
 */
public class Probe implements ModelProcessor {

    static Logger log = LoggerFactory.getLogger(Probe.class);

    private boolean showBounds;
    private boolean showTranslation;
    private boolean showRotation;
    private boolean showScale;
    private boolean showControls;
    private boolean showAllMaterialParameters;
    private boolean showDependencies;

    public Probe() {
    }

    public void setShowDependencies( boolean showDependencies ) {
        this.showDependencies = showDependencies;
    }
    
    public boolean getShowDependencies() {
        return showDependencies;
    }
   
    public void setShowBounds( boolean showBounds ) {
        this.showBounds = showBounds;
    }
    
    public boolean getShowBounds() {
        return showBounds;
    }
    
    public void setShowTranslation( boolean showTranslation ) {
        this.showTranslation = showTranslation;
    }
    
    public boolean getShowTranslation() {
        return showTranslation;
    }
    
    public void setShowRotation( boolean showRotation ) {
        this.showRotation = showRotation;
    }
    
    public boolean getShowRotation() {
        return showRotation;
    }
    
    public void setShowScale( boolean showScale ) {
        this.showScale = showScale;
    }
    
    public boolean getShowScale() {
        return showScale;
    }
    
    public void setShowControls( boolean showControls ) {
        this.showControls = showControls;
    }
    
    public boolean getShowControls() {
        return showControls;
    }
    
    public void setShowAllMaterialParameters( boolean showAllMaterialParameters ) {
        this.showAllMaterialParameters = showAllMaterialParameters;
    }
    
    public boolean getShowAllMaterialParameters() {
        return showAllMaterialParameters;
    }    
    
    @Override
    public void apply( ModelInfo info ) {
        probe("", info.getModelRoot(), info);
        if( showDependencies ) {
            listDependencies("", info);
        }
    }
 
    protected void listDependencies( String indent, ModelInfo info ) {
        if( info.getDependencies().isEmpty() ) {
            return;
        }
        log.info(indent + "Asset dependencies:");
        Set<ModelInfo.Dependency> deps = new TreeSet<>(info.getDependencies());
        for( ModelInfo.Dependency dep : deps ) {
            probe(indent + "  ", dep, info);
        }
    }
    
    protected void probe( String indent, ModelInfo.Dependency dep, ModelInfo info ) {
        StringBuilder sb = new StringBuilder();
        if( dep.getSourceFile() == null ) {
            sb.append(dep.getKey());
        } else {
            sb.append(dep.getSourceFile());
        }
        if( !Objects.equals(dep.getKey().toString(), dep.getOriginalKey().toString()) ) {
            sb.append(" -> " + dep.getKey());
        }
        if( dep.getInstances().size() > 1 ) {
            sb.append(" (x" + dep.getInstances().size() + ")"); 
        }
        log.info(indent + sb);
    } 
    
    protected void probe( String indent, Spatial s, ModelInfo info ) {
        StringBuilder sb = new StringBuilder();
        if( s.getName() == null ) {
            sb.append(s.getClass().getSimpleName() + "()");
        } else {
            sb.append(s.getClass().getSimpleName() + "(" + s.getName() + ")");
        }
        if( s.getKey() != null ) {
            sb.append(" key:" + s.getKey());
        }
        log.info(indent + sb);
        writeAttributes(indent + "   -> ", s, info);
    
        if( s instanceof Node ) {
            Node n = (Node)s;
            for( Spatial child : n.getChildren() ) {
                probe(indent + "  ", child, info);
            }
        } if( s instanceof Geometry ) {
            probe(indent + "      ", ((Geometry)s).getMaterial(), info);                  
        }        
    }

    protected void writeAttributes( String indent, Spatial s, ModelInfo info ) {
        if( showBounds ) {
            log.info(indent + "worldBounds:" + s.getWorldBound());
        }
        if( showTranslation ) {
            log.info(indent + "localTranslation:" + s.getLocalTranslation());
        } 
        if( showRotation ) {
            log.info(indent + "localRotation:" + s.getLocalRotation());
        } 
        if( showScale ) {
            log.info(indent + "localScale:" + s.getLocalScale());
        } 
        if( showControls ) {
            if( s.getNumControls() > 0 ) {
                log.info(indent + "controls:");
                for( int i = 0; i < s.getNumControls(); i++ ) {
                    Control c = s.getControl(i);
                    log.info(indent + " [" + i + "]:" + c);
                }
            }
        } 
    }

    protected void probe( String indent, Material m, ModelInfo info ) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.toString());
        if( m.getKey() != null ) {
            sb.append("  key:" + m.getKey());
        }
        log.info(indent + sb);
        if( m.getKey() != null ) {
            ModelInfo.Dependency dep = info.getDependency(m);
            if( dep != null && dep.getSourceFile() != null ) {
                log.info(indent + "  -> source:" + dep.getSourceFile());
            }
        }
        if( showAllMaterialParameters ) {
            for( MatParam mp : m.getParams() ) {
                log.info(indent + "  " + mp);
                Object val = mp.getValue();
                if( val instanceof CloneableSmartAsset ) {
                    CloneableSmartAsset asset = (CloneableSmartAsset)val;
                    ModelInfo.Dependency dep = info.getDependency(asset);
                    if( dep != null && dep.getSourceFile() != null ) {
                        log.info(indent + "    -> source:" + dep.getSourceFile());
                    } 
                }
            }
        }
    }
    
}


