package dependencies.geometry;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;
import dependencies.BasicPipeline;

public class Cylinder {
    private static boolean initialized = false;
    private static int positionBufferID;
    private static int normalBufferID1;
    private static int normalBufferID2;
    private static int elementBufferID;
    public static int slices = 32;
    private static int bottom;
    private static int top;

    public static void draw(GLAutoDrawable drawable, BasicPipeline pipeline){
        GL4 gl = drawable.getGL().getGL4();
        if(!initialized){
            initialized = true;
        }

    }
}
