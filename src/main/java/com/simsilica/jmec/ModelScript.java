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
import java.net.URL;
import javax.script.*;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.slf4j.*;

/**
 *  A ModelProcessor that runs a JSR 223 style script against
 *  the supplied model.
 *
 *  @author    Paul Speed
 */
public class ModelScript implements ModelProcessor {

    static Logger log = LoggerFactory.getLogger(ModelScript.class);

    private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private Convert convert;
    private String scriptName;
    private String script;
    private ScriptEngine engine;
    private CompiledScript compiledScript;
    private Bindings bindings;

    public ModelScript( Convert convert, String scriptName ) {
        this(convert, scriptName, loadScript(scriptName));
    }

    public ModelScript( Convert convert, String scriptName, String script ) {
        this.convert = convert;
        this.scriptName = scriptName;
        this.script = script;
        String ext = Files.getFileExtension(scriptName);
        this.engine = scriptEngineManager.getEngineByExtension(ext);
        this.bindings = engine.createBindings();
        bindings.put("convert", convert);
        bindings.put("assets", convert.getAssetReader().getAssetManager());

        log.info("Script engine:" + engine);
        if( engine instanceof Compilable ) {
            try {
                this.compiledScript = ((Compilable)engine).compile(script);
            } catch( Exception e ) {
                throw new RuntimeException("Error compiling:" + scriptName, e);
            }
        }
    }

    public String getScriptName() {
        return scriptName;
    }

    public static final String loadScript( String scriptName ) {
        File f = new File(scriptName);
        if( f.exists() ) {
            // Load the file
            return loadScript(f);
        }
        // Else check for a class resource
        URL u = ModelScript.class.getResource(scriptName);
        if( u != null ) {
            return loadScript(u);
        }
        throw new IllegalArgumentException("Unable to load script as file or resource:" + scriptName);
    }

    public static final String loadScript( File f ) {
        try {
            return Files.toString(f, Charsets.UTF_8);
        } catch( IOException e ) {
            throw new RuntimeException("Error loading file:" + f, e);
        }
    }

    public static final String loadScript( URL u ) {
        try {
            return Resources.toString(u, Charsets.UTF_8);
        } catch( IOException e ) {
            throw new RuntimeException("Error loading resource:" + u, e);
        }
    }

    @Override
    public void apply( ModelInfo model ) {
        log.info("Running script:" + scriptName + " against:" + model.getModelName());
        bindings.put("model", model);
        bindings.put("assets", new ModelAssets(model, convert.getAssetReader().getAssetManager()));
        try {
            if( compiledScript != null ) {
                compiledScript.eval(bindings);
            } else {
                engine.eval(script, bindings);
            }
        } catch( Exception e ) {
            throw new RuntimeException("Error running script:" + scriptName + " against:" + model.getModelName(), e);
        }
    }
}
