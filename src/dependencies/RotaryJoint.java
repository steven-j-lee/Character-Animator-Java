package dependencies;
import com.jogamp.opengl.GLAutoDrawable;
import mintools.parameters.DoubleParameter;

import javax.vecmath.Vector3d;

//steven lee 260803947
public class RotaryJoint extends GraphNode {
    DoubleParameter theta;
    Vector3d translation;
    // x = 1 ; y = 2; z = 3;
    int definedAxisRot;
    double setMaxVal;
    double setMinVal;
    

    public RotaryJoint(String name, int axisRot, double minVal, double maxVal,  Vector3d inputTranslation) {
        super(name);
        translation = inputTranslation;
        definedAxisRot = axisRot;
        setMaxVal = maxVal;
        setMinVal = minVal;
        
        dofs.add(theta = new DoubleParameter(name+" theta", 0, -2,2 ));
        
        /*
        theta.setDefaultValue((double) 0);
        theta.setMaximum(setMaxVal);
        theta.setMinimum(setMinVal); */
    
    }

    @Override
    public void display(GLAutoDrawable drawable, BasicPipeline pipeline){
        pipeline.push();
        //same value as parent
        pipeline.translate(translation.x, translation.y, translation.z);
        
        //rotation performed along a defined axis as taught in math 223
        
        if(definedAxisRot == 1) {
        	//rotation along x axis
            pipeline.rotate(theta.getValue(), 1, 0, 0);
        } else if(definedAxisRot == 2) {
        	//rotation along y axis
        	pipeline.rotate(theta.getValue(), 0, 1, 0);
        } else if(definedAxisRot == 3) {
        	//rotation along z axis
            pipeline.rotate(theta.getValue(), 0, 0, 1);
        }
        
        super.display(drawable, pipeline);
        pipeline.pop();
    }
}
