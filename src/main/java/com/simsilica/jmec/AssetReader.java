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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URL;

import org.slf4j.*;

import com.google.common.io.Files;

import com.jme3.asset.*;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.scene.*;

import com.simsilica.jmec.gltf.GltfExtrasLoader;

/**
 *  Wraps an AssetManager with localized configuration for reading assets
 *  from a certain directory tree.
 *
 *  @author    Paul Speed
 */
public class AssetReader {

    public static final String DESKTOP_ASSET_CONFIG = "/com/jme3/asset/Desktop.cfg";

    static Logger log = LoggerFactory.getLogger(AssetReader.class);

    private Path root;
    private final DesktopAssetManager assets;

    public AssetReader() {
        this(new File("."));
    }

    public AssetReader( File assetRoot ) {
        this(assetRoot, (URL) null);
    }

    public AssetReader( File assetRoot, URL assetConfig ) {
        if( assetConfig == null ) {
            assetConfig = getClass().getResource(DESKTOP_ASSET_CONFIG);
            log.info("Found assetConfig:" + assetConfig);
        }

        this.assets = new DesktopAssetManager(assetConfig);
        setAssetRoot(assetRoot);
    }

    public AssetReader( File assetRoot, DesktopAssetManager assets ) {
        this.assets = assets;
        setAssetRoot(assetRoot);
    }

    public void setAssetRoot( File assetRoot ) {
        if (root != null) {
            assets.unregisterLocator(root.toString(), FileLocator.class);
        }
        if (assetRoot == null) {
            root = null;
            return;
        }

        try {
            this.root = assetRoot.getCanonicalFile().toPath();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error getting canonical path for:" + assetRoot, e);
        }
        log.info("Using source asset root:" + root);

        assets.registerLocator(root.toString(), FileLocator.class);
    }

    public File getAssetRoot() {
        return root != null ? root.toFile() : null;
    }

    public DesktopAssetManager getAssetManager() {
        return assets;
    }

    public Spatial loadModel( File f ) {
        if (root == null) {
            throw new RuntimeException("Asset root is not set.");
        }

        log.debug("loadModel(" + f + ")");
        if( !f.exists() ) {
            throw new IllegalArgumentException("Model file does not exist:" + f);
        }

        try {
            // Make sure the file is completely collapsed into canonical form.
            // Note: apparently getAbsoluteFile() is not good enough.
            f = f.getCanonicalFile();
        } catch( IOException e ) {
            throw new IllegalArgumentException("File cannot be converted to canonical path:" + f, e);
        }
        // Find the relative path
        String path = root.relativize(f.getAbsoluteFile().toPath()).toString();

        log.info("Loading asset:" + path);

        // Make sure the cache is clear
        assets.clearCache();

        // AssetManager doesn't really give us a better way to resolve types
        // so we'll make some assumptions... it helps that we control the
        // asset manager ourselves here.
        String extension = Files.getFileExtension(f.getName());
        if( "gltf".equalsIgnoreCase(extension) || "glb".equalsIgnoreCase(extension) ) {
            // We do special setup for GLTF
            return assets.loadModel(GltfExtrasLoader.createModelKey(path));
        } else {
            return assets.loadModel(path);
        }
    }
}


