/*
 * $Id$
 *
 * Copyright (c) 2020, Simsilica, LLC
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

package com.simsilica.jmec.view;

import java.io.*;

import org.slf4j.*;

import com.google.common.io.Files;

import com.jme3.asset.AssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.scene.*;
import com.jme3.util.SafeArrayList;

import com.simsilica.jmec.*;

/**
 *  Watches a model plus optional set of scripts and reloads/reconverts
 *  the model when changes in the files are detected.
 *
 *  @author    Paul Speed
 */
public class JmecNode extends Node {
    static Logger log = LoggerFactory.getLogger(JmecNode.class);

    private Convert convert = new Convert();
    private ModelInfo model;
    private VersionedFile modelFile;
    private SafeArrayList<VersionedScript> scripts = new SafeArrayList<>(VersionedScript.class);
    private AssetManager assets;

    public JmecNode() {
        this((File)null);
    }

    public JmecNode( String modelFile ) {
        this(new File(modelFile));
    }

    public JmecNode( File modelFile ) {
        setRequiresUpdates(true);
        if( modelFile != null ) {
            this.modelFile = new VersionedFile(modelFile);
            setName(modelFile.getName());
        }

        // Set some defaults
        convert.setTargetRoot(new File("assets"));
        convert.setTargetAssetPath("Models/" + Files.getNameWithoutExtension(modelFile.getName()));
    }

    public void setModelFile( File modelFile ) {
        if( modelFile == null ) {
            this.modelFile = null;
            return;
        }
        this.modelFile = new VersionedFile(modelFile);
        if( getName() == null ) {
            setName(modelFile.getName());
        }
    }

    public File getModelFile() {
        return modelFile != null ? modelFile.getFile() : null;
    }

    /**
     *  Returns the JME Convert object to allow configuring
     *  additional custom settings if required.
     */
    public Convert getConvert() {
        return convert;
    }

    public void addModelScript( File f ) {
        scripts.add(new VersionedScript(f));
    }

    /**
     *  Sets an asset manager that can be used to resolve AssetLinkNodes.
     */
    public void setAssetManager( AssetManager assets ) {
        this.assets = assets;
    }

    public AssetManager getAssetManager() {
        return assets;
    }

    @Override
    public void updateLogicalState( float tpf ) {

        if( updateDependencies() ) {
            // Remove the old model
            if( model != null && model.getModelRoot() != null ) {
                model.getModelRoot().removeFromParent();
            }

            Spatial child = loadModel();
            if( child != null ) {
                attachChild(child);
            }
        }

        super.updateLogicalState(tpf);
    }

    protected Spatial loadModel() {
        if( modelFile == null ) {
            throw new RuntimeException("No model file has been set");
        }

        File f = modelFile.getFile();
        if( !f.exists() ) {
            throw new RuntimeException("Model does not exist:" + f);
        }
        // Load the model
        if( convert.getSourceRoot() == null ) {
            convert.setSourceRoot(f.getParentFile());
        }

        if( !scripts.isEmpty() ) {
            // Make sure the converter has the latest scripts
            convert.clearModelScripts();
            for( VersionedScript script : scripts.getArray() ) {
                try {
                    convert.addModelScript(script.getScript());
                } catch( RuntimeException e ) {
                    log.error("Error compiling script:" + script.getScript().getScriptName(), e.getCause());
                }
            }
        }

        try {
            model = convert.convert(f);

            // Clear the cache for any linked dependencies
            // This should cover AssetLinkNodes as well as any generated materials
            // _they_ may try to load.
            for( ModelInfo.Dependency dep : model.getDependencies() ) {
                log.info("Clear cached dependency for:" + dep.getKey());
                if( !deleteFromCache(dep) ) {
                    // This can and will happen quite normally... either the first
                    // time we load or if there are more than one reference to the
                    // same dependency.  No big deal.
                    //log.warn("Cached asset was not cleared:" + dep.getKey());
                }

                // Note: alternately, if we were not concerned about checking
                // loading, we could directly register the already loaded
                // child in the dependency object.
            }

            // Since we imported it and converted it in RAM, the AssetLinkNodes
            // would not have been resolved.
            model.getModelRoot().depthFirstTraversal(new SceneGraphVisitorAdapter() {
                    public void visit( Node node ) {
                        if( node instanceof AssetLinkNode ) {
                            if( assets == null ) {
                                log.error("No AssetManager specified, cannot load asset link:" + node);
                            } else {
                                log.info("Loading linked assets for:" + node);
                                AssetLinkNode link = (AssetLinkNode)node;
                                link.attachLinkedChildren(assets);
                            }
                        }
                    }
                });

        } catch( IOException e ) {
            log.error("Cannot load:" + f, e);
        } catch( RuntimeException e ) {
            // Figure out what kind of error
            if( e.getCause() instanceof javax.script.ScriptException ) {
                log.error("Script error", e.getCause());
            } else {
                throw e;
            }
        }

        return model == null ? null : model.getModelRoot();
    }

    @SuppressWarnings("unchecked")
    protected boolean deleteFromCache( ModelInfo.Dependency dep ) {
        return assets.deleteFromCache(dep.getKey());
    }

    protected boolean updateDependencies() {
        boolean changed = false;
        if( modelFile != null && modelFile.update() ) {
            changed = true;
        }
        for( VersionedScript f : scripts.getArray() ) {
            if( f.update() ) {
                changed = true;
            }
        }
        return changed;
    }

    protected static class VersionedFile {
        File file;
        long lastVersion;

        public VersionedFile( File file ) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public boolean update() {
            long time = file.lastModified();
            if( lastVersion == time ) {
                return false;
            }
            lastVersion = time;
            return true;
        }
    }

    protected class VersionedScript {
        File file;
        ModelScript script;
        long lastVersion;

        public VersionedScript( File file ) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public ModelScript getScript() {
            if( script == null ) {
                String text = ModelScript.loadScript(file);
                script = new ModelScript(convert, file.getName(), text);
            }
            return script;
        }

        public boolean update() {
            long time = file.lastModified();
            if( lastVersion == time ) {
                return false;
            }
            lastVersion = time;
            script = null;
            return true;
        }
    }
}
