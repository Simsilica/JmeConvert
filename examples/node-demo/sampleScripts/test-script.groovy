
// The following bindings are setup by default:
// convert - the JME Convert instance
// model - the loaded ModelInfo.  model.modelRoot is the loaded spatial.
println "Convert:" + convert
println "Model:" + model

// Example of finding all nodes in a scene
println "All nodes in the scene..."
model.findAll(com.jme3.scene.Node.class).each { node ->
    println "found:" + node
}

println "All nodes named 'Scene' in the scene..."
model.findAll("Scene", com.jme3.scene.Node.class).each { node ->
    println "found:" + node
}

// Move the model so that it's base is at y=0
def bound = model.modelRoot.getWorldBound()
model.modelRoot.setLocalTranslation(0, (float)(bound.yExtent - bound.center.y), 0);

// Make sure all included materials with a name get a generated .j3m
import com.jme3.asset.MaterialKey;
import com.jme3.scene.Geometry;
model.findAll(Geometry.class).each { geom ->

    if( geom.material.name != null ) {
        model.generateMaterial(geom.material, "materials/" + geom.material.name);
    } else {
        // We'll try to make up a unique name
        def name = geom.name;
        for( def s = geom.parent; s != null; s = s.parent ) {
            name = s.name + "_" + name;
        }
        model.generateMaterial(geom.material, "materials/" + name);
    }
}

float x = 0;
println "All nodes...";
model.findAll(com.jme3.scene.Spatial.class).each { node ->
    println node

    println "User data keys:" + node.userDataKeys

    String submodel = node.getUserData("submodel");
    if( submodel != null ) {
        println "Submode:" + submodel;
        model.extractSubmodel(node, submodel);
    }
}
