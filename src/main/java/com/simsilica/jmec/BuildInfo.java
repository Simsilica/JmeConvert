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
import java.util.*;

import org.slf4j.*;

/**
 *  Loads build-time information and makes it available to a caller.
 *
 *  @author    Paul Speed
 */
public class BuildInfo {

    static Logger log = LoggerFactory.getLogger(BuildInfo.class);

    private static Properties props = new Properties();

    static {
        try {
            load("/git.properties");
        } catch( IOException e ) {
            log.error("Error reading git.properties", e);
        }
        try {
            load("/jmec.build.date");
        } catch( IOException e ) {
            log.error("Error reading jmec.build.date", e);
        }
    }

    public static String getVersion() {
        return get("git.build.version", "1.unknown");
    }

    public static String getBuildTime() {
        return get("git.build.time", get("build.date", "Unknown"));
    }

    public static String get( String name, String defaultValue ) {
        return props.getProperty(name, defaultValue);
    }

    public static void load( String classResource ) throws IOException {
        URL u = BuildInfo.class.getResource(classResource);
        if( u == null ) {
            log.warn("No build info at:" + classResource);
        }
        log.debug("Loading build info from:" + u);
        InputStream in = u.openStream();
        try {
            props.load(in);
        } finally {
            in.close();
        }
    }
}
