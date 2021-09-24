package dependencies.geometry;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import dependencies.BasicPipeline;
import dependencies.GraphNode;

public class BaseShapeControllers extends GraphNode{
    Vector3d translation;
    Vector3d uniformScale;
    Vector3f colors;
    String name;

    public BaseShapeControllers(String name) {
        super(name);
        this.name = name;
    }

    public void setCentre(Vector3d t) {
        translation = t;
    }

    public void setScale(Vector3d t) {
        uniformScale = t;
    }

    public void setColors(Vector3f t) {
        colors = t;
    }

    @Override
    public void display(GLAutoDrawable drawable, BasicPipeline pipeline){
        pipeline.push();
        GL4 gl4 = drawable.getGL().getGL4();

        pipeline.scale(uniformScale.x, uniformScale.y, uniformScale.z);

        pipeline.translate(translation.x, translation.y, translation.z);

        gl4.glUniform3f(pipeline.getKdID(), colors.x, colors.y, colors.z);

        pipeline.setModelingMatrixUniform(gl4);

        if(this.name.equalsIgnoreCase("sphere"))
        {
            Sphere.draw(drawable, pipeline);
        }
        else if(this.name.equalsIgnoreCase("box")){
            Cube.draw(drawable, pipeline);
        }


        super.display(drawable, pipeline);
        pipeline.pop();
    }
}
