/*
 * $Id$
 * 
 * Copyright (c) 2022, Simsilica, LLC
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

import com.jme3.asset.*;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;

import org.slf4j.*;

/**
 *  A model-specific "asset manager" that can load assets from the
 *  source model's assets and automatically add them as dependencies.
 *  This presents just the load-related methods from the AssetManager
 *  interface as well as some convenience methods for common custom
 *  asset key cases (like texture flipping). 
 *
 *  @author    Paul Speed
 */
public class ModelAssets {
    static Logger log = LoggerFactory.getLogger(ModelAssets.class);
    
    private ModelInfo model;
    private AssetManager assets;
    
    public ModelAssets( ModelInfo model, AssetManager assets ) {
        this.model = model;
        this.assets = assets;
    }
    
    public AssetManager getAssetManager() {
        return assets;
    }
    
    protected <T> T addDependency( T asset ) {
        if( asset instanceof CloneableSmartAsset ) {
            model.addDependency((CloneableSmartAsset)asset);
        }
        return asset;
    } 
    
    public <T> T loadAsset( AssetKey<T> key ) {
        return addDependency(assets.loadAsset(key));
    }

    public Object loadAsset( String name ) {
        return addDependency(assets.loadAsset(name));
    }

    public Texture loadTexture( TextureKey key ) {
        return addDependency(assets.loadTexture(key));
    }

    public Texture loadTexture( String name ) {
        return addDependency(assets.loadTexture(name));
    }

    public Texture loadTexture( String name, boolean yFlipped ) {
        return addDependency(assets.loadTexture(new TextureKey(name, yFlipped)));
    }

    public AudioData loadAudio( AudioKey key ) {
        return addDependency(assets.loadAudio(key));
    }

    public AudioData loadAudio( String name ) {
        return addDependency(assets.loadAudio(name));
    }

    public Spatial loadModel( ModelKey key ) {
        return addDependency(assets.loadModel(key));
    }

    public Spatial loadModel( String name ) {
        return addDependency(assets.loadModel(name));
    }

    public Material loadMaterial( String name ) {
        return addDependency(assets.loadMaterial(name));
    }

    public BitmapFont loadFont( String name ) {
        return addDependency(assets.loadFont(name));
    }
    
    public FilterPostProcessor loadFilter( FilterKey key ) {
        return addDependency(assets.loadFilter(key));
    }

    public FilterPostProcessor loadFilter( String name ) {
        return addDependency(assets.loadFilter(name));
    }
}
