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

package demo;

import java.io.*;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.*;
import com.jme3.math.*;
import com.jme3.material.Material;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;

import com.simsilica.jmec.*;
import com.simsilica.jmec.view.*;

/**
 *
 *
 *  @author    Paul Speed
 */
public class Main extends SimpleApplication {

    public static void main( String... args ) {
        Main main = new Main();
        main.start();
    }

    public void simpleInitApp() {

        // Setup so that we can toggle the fly cam on/off with the spacebard
        inputManager.addMapping("FlyCamToggle", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(new FlyCamToggle(), "FlyCamToggle");

        // Make sure that we can see our changes when we're switched to another
        // window
        setPauseOnLostFocus(false);

        // Setup the main light
        DirectionalLight sun = new DirectionalLight(new Vector3f(0.25f, -1, -1f).normalizeLocal());
        rootNode.addLight(sun);

        // Setup the default light probe
        //Spatial probeHolder = assetManager.loadModel("Probes/studio.j3o");
        //Spatial probeHolder = assetManager.loadModel("Probes/River_Road.j3o");
        Spatial probeHolder = assetManager.loadModel("Probes/Stonewall.j3o");
        //Spatial probeHolder = assetManager.loadModel("Probes/Parking_Lot.j3o");
        LightProbe probe = (LightProbe)probeHolder.getLocalLightList().get(0);
        probe.setPosition(Vector3f.ZERO);
        probeHolder.removeLight(probe);
        rootNode.addLight(probe);

        cam.setLocation(new Vector3f(0.0f, 3.5173976f, 10.0f));
        cam.setRotation(new Quaternion(0.0f, 0.9947752f, -0.102089666f, 0.0f));

        // Setup a floor
        Box box = new Box(10, 1, 10);
        Geometry geom = new Geometry("box", box);
        geom.setLocalTranslation(0, -1, 0);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setColor("Diffuse", ColorRGBA.Gray);
        mat.setColor("Ambient", ColorRGBA.Gray);
        mat.setBoolean("UseMaterialColors", true);
        //Texture texture = assetManager.loadTexture("Textures/Monkey.png");
        //mat.setTexture("DiffuseMap", texture);

        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        // Setup the JMEC node
        JmecNode jmecNode = new JmecNode(new File("samples/test-model.gltf"));
        jmecNode.addModelScript(new File("sampleScripts/test-script.groovy"));
        rootNode.attachChild(jmecNode);
    }

    private class FlyCamToggle implements ActionListener {
        @Override
        public void onAction( String name, boolean value, float tpf ) {
            if( value ) {
                flyCam.setEnabled(!flyCam.isEnabled());

                // Fly cam does not manage this correctly
                inputManager.setCursorVisible(!flyCam.isEnabled());
            }
        }
    }
}


