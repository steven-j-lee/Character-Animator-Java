package main.dependencies.geometry;
import com.jogamp.opengl.GLAutoDrawable;
import main.dependencies.BasicPipeline;
import main.dependencies.GraphNode;
import mintools.parameters.DoubleParameter;
import javax.vecmath.Vector3d;
//steven lee 260803947
public class SphericalJoint extends GraphNode {
    Vector3d translation;
    DoubleParameter rx;
    DoubleParameter ry;
    DoubleParameter rz;

    public SphericalJoint(String name, Vector3d parentTranslation){
        super(name);
        dofs.add(rx = new DoubleParameter(name+" rx", 0,(Math.PI * (-2)) , (Math.PI * 2)));
        dofs.add(ry = new DoubleParameter(name+" ry", 0, (Math.PI * (-2)) , (Math.PI * 2)));
        dofs.add(rz = new DoubleParameter(name+" rz", 0, (Math.PI * (-2)) , (Math.PI * 2)));
        translation = parentTranslation;
    }

    @Override
    public void display(GLAutoDrawable drawable, BasicPipeline pipeline){
        pipeline.push();
        pipeline.translate(translation.x, translation.y, translation.z);
        pipeline.rotate(Math.PI, rx.getValue(), ry.getValue(), rz.getValue());
        super.display(drawable, pipeline);
        pipeline.pop();
    }
}
