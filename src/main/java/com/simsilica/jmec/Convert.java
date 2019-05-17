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
import java.net.URL;
import java.util.*;

import org.slf4j.*;

import com.jme3.asset.*;
import com.jme3.scene.*;


/**
 *
 *
 *  @author    Paul Speed
 */
public class Convert {

    public static final String[] HEADER = {        
        "        _   __  __   ___    ___ ",
        "     _ | | |  \\/  | | __|  / __|",
        "    | || | | |\\/| | | _|  | (__ ",
        "     \\__/  |_|  |_| |___|  \\___|",
        "",
        "    JME3 Asset Converter v" + BuildInfo.getVersion() + " build:" + BuildInfo.getBuildTime(),
        ""
    };
    
    public static final String ALL_PROBE_OPTIONS = "btrscpd";
    
    public static final String[] HELP = {
        "Usage: jmec [options] [models]",
        "",
        "Where [models] are a list of JME-compatible model files.",
        "",
        "Where [options] are:",
        " -sourceRoot <dir> : specifies the asset root for the models.",
        "       Model dependency paths will be evaluated relative to this root.",
        "",
        " -targetRoot <dir> : specifies the asset target root for writing",
        "       converted assets and their dependencies.",
        "",
        " -targetPath <path> : a path string specifying where to place",
        "       the copied/converted assets within the targetRoot.  Any",
        "       internal asset keys will be 'rehomed' to this path.",
        " -probe [probe options string] : configures the information that the probe",
        "       will output.",
        "       [probe options]:",
        "       A : all options turned on, same as: " + ALL_PROBE_OPTIONS,
        "       b : show bounding volumes",
        "       t : show translations",
        "       r : show rotation",
        "       s : show scale",
        "       c : show the list of controls",
        "       p : show material parameters",
        "       d : list asset dependencies",
        "",
        "Examples:",
        "",
        ">jmec -sourceRoot C:\\Downloads\\CoolModel -targetRoot assets -targetPath Models/CoolModel C:\\Downloads\\CoolModel\\AwesomeThing.gltf",
        "",
        " Converts C:\\Downloads\\CoolModel\\AwesomeThing.gltf to a j3o and writes it",
        " to ./assets/Models/CoolModel/AwesomeThing.gltf.j3o",
        "",
        " Any dependent textures, etc. relative to C:\\Downloads\\CoolModel will",
        " be copied until the appropriate ./assets/Models/CoolModel/* subdirectory.",
        "",
        " For example:",
        "    C:\\Downloads\\CoolModel\\textures\\awesome-sauce.jpg",
        " Gets copied to:",
        "    ./assets/Models/CoolModel/textures/awesome-sauce.jpg"
    };

    static Logger log = LoggerFactory.getLogger(Convert.class);

    private File sourceRoot;
    private File targetRoot;
    private String targetAssetPath;
    private AssetReader assets;
    private AssetWriter writer;
    private Probe probe = null;
    private String probeOptions = null;
    private List<String> modelScripts = new ArrayList<>();
 
    private List<ModelProcessor> processors = new ArrayList<>();
 
    public Convert() {
    }
    
    public AssetReader getAssetReader() {
        return assets;
    }
    
    protected AssetWriter getAssetWriter() {
        if( writer == null ) {
            writer = new AssetWriter();
            processors.add(writer);
        }
        return writer;
    }
    
    protected Probe getProbe() {
        if( probe == null ) {
            probe = new Probe();
            processors.add(0, probe);
        }
        return probe; 
    }

    public void setSourceRoot( File f ) {
        if( !f.exists() ) {
            log.error("Source root doesn't exist:" + f);
            return;
        }
        if( !f.isDirectory() ) {
            log.error("Source root is not a directory:" + f);
            return;
        }
        this.sourceRoot = f;
        this.assets = new AssetReader(f);
    }
    
    public File getSourceRoot() {
        return sourceRoot;
    }
    
    public void setTargetRoot( File f ) {
        this.targetRoot = f;
        getAssetWriter().setTarget(f);
    }
    
    public File getTargetRoot() {
        return targetRoot;
    }
 
    public void setTargetAssetPath( String path ) {
        this.targetAssetPath = path;
        getAssetWriter().setAssetPath(path);
    }
    
    public String getTargetAssetPath() {
        return targetAssetPath;
    }

    public void setProbeOptions( String options ) {
        this.probeOptions = options;
        for( char c : options.toCharArray() ) {
            switch( c ) {
                case 'A':
                    setProbeOptions(ALL_PROBE_OPTIONS);
                    break;
                case 'b':
                    getProbe().setShowBounds(true);
                    break;
                case 't':
                    getProbe().setShowTranslation(true);
                    break;
                case 'r':
                    getProbe().setShowRotation(true);
                    break;
                case 's':
                    getProbe().setShowScale(true);
                    break;
                case 'c':
                    getProbe().setShowControls(true);
                    break;
                case 'p':
                    getProbe().setShowAllMaterialParameters(true);
                    break;
                case 'd':
                    getProbe().setShowDependencies(true);
                    break;
                default:
                    log.warn("Unknown probe option:" + c);
                    break;
            }
        }        
    }
    
    public String getProbeOptions() {
        return probeOptions;
    }
    
    public void addModelScript( String script ) {
        modelScripts.add(script);
        if( writer == null ) {
            // It's fine just to add them directly
            processors.add(new ModelScript(this, script));
        } else {
            // We need to add them before the asset writer
            int index = processors.indexOf(writer);
            processors.add(index, new ModelScript(this, script)); 
        }
    }
    
    public List<String> getModelScripts() {
        return modelScripts;
    }
    
    public void convert( File f ) throws IOException {
        if( !f.exists() ) {
            log.error("File doesn't exist:" + f);
            return;
        }
        log.info("Convert:" + f);        
        Spatial s = assets.loadModel(f);
 
        ModelInfo info = new ModelInfo(sourceRoot, f.getName(), s);
        runProcessors(info);          
    }

    public void runProcessors( ModelInfo info ) {
        if( processors.isEmpty() ) {
            log.warn("No output configured, probing instead.");
            getProbe(); // just let it use defaults
        }
        log.info("Processing:" + info.getModelName());
        for( ModelProcessor proc : processors ) {
            proc.apply(info);
        }
    }

    public static void printMemInfo() {
        long maxMem = Runtime.getRuntime().maxMemory();
        double mem = maxMem / (1024.0 * 1024.0);
        String s = String.format("%.2f mb", mem);
        log.info("Max memory:" + s);
    }

    public static void print( String... lines ) {
        for( String l : lines ) {
            System.out.println(l);
        }
    }

    public static void main( String... args ) throws Exception {

        boolean test = false;

        // Forward JUL logging to slf4j
        JulLogSetup.initialize();

        print(HEADER);        
 
        printMemInfo();
 
        if( args.length == 0 ) {
            print(HELP);
            if( !test ) {
                return;
            }
        }
 
        Convert convert = new Convert();
        for( Iterator<String> it = Arrays.asList(args).iterator(); it.hasNext(); ) {
            String arg = it.next();
            if( "-sourceRoot".equals(arg) ) {
                convert.setSourceRoot(new File(it.next()));
            } else if( "-targetRoot".equals(arg) ) {
                convert.setTargetRoot(new File(it.next()));
            } else if( "-targetPath".equals(arg) ) {
                convert.setTargetAssetPath(it.next());
            } else if( "-probe".equals(arg) ) {
                convert.setProbeOptions(it.next());
            } else {
                convert.convert(new File(arg));               
            }
        }
 
        if( args.length == 0 && test ) {
            convert.setSourceRoot(new File("sampleSource"));
            convert.setTargetRoot(new File("sampleTarget"));
            convert.setTargetAssetPath("foo");
            convert.setProbeOptions("bd");
            convert.addModelScript("test-script.groovy");       
            convert.convert(new File("sampleSource/scene.gltf"));            
        }
    }
}

