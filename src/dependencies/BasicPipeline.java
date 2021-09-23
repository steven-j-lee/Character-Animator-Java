package main.dependencies;

import javax.management.RuntimeErrorException;
import javax.swing.*;
import javax.vecmath.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import mintools.swing.VerticalFlowPanel;

import java.awt.*;
import java.util.*;

public class BasicPipeline {

	public boolean debugMode = false;

	private int glslProgramID;

	private int MMatrixID;
	private int MinvTMatrixID;
	private int VMatrixID;
	private int PMatrixID;

	private int kdID;
	private int lightDirID;
	private int positionAttributeID;
	private int normalAttributeID;
	private int tmpViewVectorID;
	private int textureCoodrinateAttributeID;

	private int ksID;
	private int shininessID;
	private int enableLightingID;
	private int lightPosID;
	private int lightColorID;
	private int sigmaID;
	private int shadowMapID;
	private int lightVID;

	private Vector4d lightPosition;

	BasicPipeline firstPass;
	BasicPipeline secondPass;

	public int getKdID(){
		return this.kdID;
	}

	public void setKD(GLAutoDrawable drawable, float r, float g, float b){
		GL4 gl = drawable.getGL().getGL4();
		gl.glUniform3f(kdID, r, g, b);
	}

	public int getNormalAttributeID(){
		return this.normalAttributeID;
	}

	public int getPositionAttributeID(){
		return this.positionAttributeID;
	}

	//exception
	public Exception EmptyStack = new Exception("Stack is empty.");
	//stack for the matrix
	private Stack<Matrix4d> matrixStack = new Stack<>();

	private Matrix4d MMatrix = new Matrix4d();
	/** Inverse Transpose of Modeling matrix */
	private Matrix4d MinvTMatrix = new Matrix4d();
	/** Inverse Transpose of Modeling matrix */

	/** View matrix */
	private Matrix4d VMatrix = new Matrix4d();
	/** Projection matrix */
	private Matrix4d PMatrix = new Matrix4d();

	private Matrix4d tmpMatrix4d = new Matrix4d();

	/** Camera components **/
	private Camera camera = new Camera();
	private ArcBall arcBall = new ArcBall();

	/** Shadow Map two pass**/
	ShadowMap shadowMap = new ShadowMap(1024);
	PointLightCamera pointLightSource = new PointLightCamera();

	public BasicPipeline( GLAutoDrawable drawable ) {
		initMatricies();
		GL4 gl = drawable.getGL().getGL4();
		// Create the GLSL program
		glslProgramID = createProgram( drawable, "basicLighting" );
		// Get the IDs of the parameters (i.e., uniforms and attributes)
		gl.glUseProgram( glslProgramID );
		MMatrixID = gl.glGetUniformLocation( glslProgramID, "M" );
		MinvTMatrixID = gl.glGetUniformLocation( glslProgramID, "MinvT" );
		VMatrixID = gl.glGetUniformLocation( glslProgramID, "V" );
		PMatrixID = gl.glGetUniformLocation( glslProgramID, "P" );
		kdID = gl.glGetUniformLocation( glslProgramID, "kd" );
		lightDirID = gl.glGetUniformLocation( glslProgramID, "lightDir" );
		positionAttributeID = gl.glGetAttribLocation( glslProgramID, "position" );
		normalAttributeID = gl.glGetAttribLocation( glslProgramID, "normal" );

		//added
		tmpViewVectorID = gl.glGetAttribLocation(glslProgramID, "tmpViewVector");
	}

	/**
	 * Enables the basic pipeline, sets viewing and projection matrices, and enables
	 * the position and normal vertex attributes
	 * @param drawable
	 */
	public void enable( GLAutoDrawable drawable ) {
		GL4 gl = drawable.getGL().getGL4();
		gl.glUseProgram( glslProgramID );
		gl.glEnableVertexAttribArray( positionAttributeID );
		gl.glEnableVertexAttribArray( normalAttributeID );
		glUniformMatrix( gl, VMatrixID, VMatrix );
		glUniformMatrix( gl, PMatrixID, PMatrix );
		glUniformMatrix( gl, MMatrixID, MMatrix );
		glUniformMatrix( gl, MinvTMatrixID, MinvTMatrix );

		Vector3f lightDir = new Vector3f( 1f, 1f, 1f );
		lightDir.normalize();
		gl.glUniform3f( lightDirID, lightDir.x, lightDir.y, lightDir.z );


		Vector3d tmpViewVector = new Vector3d(VMatrix.m13, VMatrix.m23, VMatrix.m33);
		tmpViewVector.normalize();
		gl.glUniform3f(tmpViewVectorID, (float)tmpViewVector.x, (float) tmpViewVector.y, (float) tmpViewVector.z);
	}

	/** Sets the modeling matrix with the current top of the stack */
	public void setModelingMatrixUniform( GL4 gl ) {
		glUniformMatrix( gl, MMatrixID, MMatrix );
		glUniformMatrix( gl, MinvTMatrixID, MinvTMatrix);
	}

	/**
	 * Pushes the modeling matrix and its inverse transpose onto the stack so
	 * that the state can be restored later
	 */
	public void push() {
		//modeling matrix first then its inverse transpose
		Matrix4d tmpMMAtrix = new Matrix4d();
		Matrix4d tmpMinvTMatrix = new Matrix4d();
		tmpMMAtrix.set(MMatrix);
		tmpMinvTMatrix.set(MinvTMatrix);
		this.matrixStack.push(tmpMinvTMatrix);
		this.matrixStack.push(tmpMMAtrix);
	}

	/**
	 * Pops the matrix stack, setting the current modeling matrix and inverse transpose
	 * to the previous state.
	 */
	public void pop() {
		if(this.matrixStack.isEmpty()){
			throw new EmptyStackException();
		}
		if(matrixStack.peek().equals(null)){
			//do nothing
		}
		else
		{
			MMatrix = this.matrixStack.peek();
			this.matrixStack.pop();
			MinvTMatrix = this.matrixStack.peek();
			this.matrixStack.pop();
		}

	}

	/**
	 * Applies a translation to the current modeling matrix.
	 * Note: setModelingMatrixUniform must be called before drawing!
	 * @param x
	 * @param y
	 * @param z
	 */
	public void translate( double x, double y, double z ) {
		//set identity, then add the scale transforms
		tmpMatrix4d.setIdentity();
		Vector3d translateM = new Vector3d(x,y,z);
		tmpMatrix4d.setTranslation(translateM);

		MMatrix.mul(tmpMatrix4d);
		MinvTMatrix.mul(tmpMatrix4d);
	}

	/**
	 * Applies a scale to the current modeling matrix.
	 * Note: setModelingMatrixUniform must be called before drawing!
	 * @param x
	 * @param y
	 * @param z
	 */
	public void scale( double x, double y, double z ) {
		tmpMatrix4d.setIdentity();
		tmpMatrix4d.m00 = x;
		tmpMatrix4d.m11 = y;
		tmpMatrix4d.m22 = z;

		MMatrix.mul(tmpMatrix4d);
		MinvTMatrix.mul(tmpMatrix4d);
	}

	/**
	 * Applies a rotation to the current modeling matrix.
	 * The rotation is in radians, and the axis specified by its
	 * components x, y, and z should probably be unit length!
	 * @param radians
	 * @param x
	 * @param y
	 * @param z
	 */
	public void rotate( double radians, double x, double y, double z ) {
		AxisAngle4d aa = new AxisAngle4d( x, y, z, radians );
		tmpMatrix4d.set( aa );
		MMatrix.mul(tmpMatrix4d);
		MinvTMatrix.mul(tmpMatrix4d); // inverse transpose is the same rotation
	}

	private float[] columnMajorMatrixData = new float[16];

	/**
	 * Wrapper to glUniformMatrix4fv for vecmath Matrix4d
	 * @param gl
	 * @param ID
	 * @param M
	 */
	public void glUniformMatrix( GL4 gl, int ID, Matrix4d M ) {
		columnMajorMatrixData[0] = (float) M.m00;
		columnMajorMatrixData[1] = (float) M.m10;
		columnMajorMatrixData[2] = (float) M.m20;
		columnMajorMatrixData[3] = (float) M.m30;
		columnMajorMatrixData[4] = (float) M.m01;
		columnMajorMatrixData[5] = (float) M.m11;
		columnMajorMatrixData[6] = (float) M.m21;
		columnMajorMatrixData[7] = (float) M.m31;
		columnMajorMatrixData[8] = (float) M.m02;
		columnMajorMatrixData[9] = (float) M.m12;
		columnMajorMatrixData[10] = (float) M.m22;
		columnMajorMatrixData[11] = (float) M.m32;
		columnMajorMatrixData[12] = (float) M.m03;
		columnMajorMatrixData[13] = (float) M.m13;
		columnMajorMatrixData[14] = (float) M.m23;
		columnMajorMatrixData[15] = (float) M.m33;
		gl.glUniformMatrix4fv( ID, 1, false, columnMajorMatrixData, 0 );
	}

	public void initMatricies() {
		MMatrix.set( new double[] {
				1,  0,  0,  0,
				0,  1,  0,  0,
				0,  0,  1,  0,
				0,  0,  0,  1,
		} );
		MinvTMatrix.set( new double[] {
				1,  0,  0,  0,
				0,  1,  0,  0,
				0,  0,  1,  0,
				0,  0,  0,  1,
		} );
		VMatrix.set( new double[] {
				1,  0,  0,  0,
				0,  1,  0,  0,
				0,  0,  1, -2.5,
				0,  0,  0,  1,
		} );

		PMatrix.set( new double[] {
				1,  0,  0,  0,
				0,  1,  0,  0,
				0,  0, -2, -3,
				0,  0, -1,  1,
		} );
	}

	public void matrixOp(GLAutoDrawable drawable, Matrix4d matrix){
		MMatrix.mul(matrix);
		if(MMatrix.determinant() != 0.0){
			tmpMatrix4d.invert(MMatrix);
			tmpMatrix4d.transpose();
			MinvTMatrix.mul(tmpMatrix4d);
			setModelingMatrixUniform(drawable.getGL().getGL4());

		}
	}

	public void controlLighting(GLAutoDrawable drawable, boolean flag){
		GL4 gl = drawable.getGL().getGL4();
		if(flag){
			gl.glUniform1i(enableLightingID, 1);
		}
		else{
			gl.glUniform1i(enableLightingID, 0);
		}
	}

	/**
	 * Creates a GLSL program from the .vp and .fp code provided in the shader directory
	 * @param drawable
	 * @param name
	 * @return
	 */
	private int createProgram( GLAutoDrawable drawable, String name ) {
		GL4 gl = drawable.getGL().getGL4();
		ShaderCode vsCode = ShaderCode.create( gl, GL4.GL_VERTEX_SHADER, this.getClass(), "glsl", "glsl/bin", name, false );
		ShaderCode fsCode = ShaderCode.create( gl, GL4.GL_FRAGMENT_SHADER, this.getClass(), "glsl", "glsl/bin", name, false );
		ShaderProgram shaderProgram = new ShaderProgram();
		shaderProgram.add( vsCode );
		shaderProgram.add( fsCode );
		if ( !shaderProgram.link( gl, System.err ) ) {
			throw new GLException( "Couldn't link program: " + shaderProgram );
		}
		shaderProgram.init(gl);
		return shaderProgram.program();
	}

	public void startCameraViewPass(GLAutoDrawable drawable){
		//System.out.println("initialized camera");
		GL4 gl = drawable.getGL().getGL4();
		shadowMap.bindPrimaryFrameBuffer(drawable);
		camera.updateMatrix(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
		camera.updateMatrix(512.0, 512.0);
		camera.V.mul(arcBall.R);
		gl.glUseProgram(glslProgramID);
		gl.glEnableVertexAttribArray(positionAttributeID);
		if(normalAttributeID != -1){
			gl.glEnableVertexAttribArray(normalAttributeID);
		}
		glUniformMatrix(gl, PMatrixID, camera.P);
		glUniformMatrix(gl, VMatrixID, camera.V);
		glUniformMatrix(gl, MMatrixID, MMatrix);
		if(MinvTMatrixID == -1){
			return;
		}
		glUniformMatrix(gl, MinvTMatrixID, MinvTMatrix);

		pointLightSource.getPositionInWorld(lightPosition);
		camera.V.transform(lightPosition);

		gl.glUniform3f(lightPosID, lightPosition.x, lightPosition.y, lightPosition.z);
	}

	//first scene pass from eye's pov
	public void startLightViewPass(GLAutoDrawable drawable){
		GL4 gl = drawable.getGL().getGL4();
		shadowMap.bindLightPassFrameBuffer(drawable);
		pointLightSource.updateMatrix(512.0, 512.0);
		gl.glUseProgram(glslProgramID);
		gl.glEnableVertexAttribArray(positionAttributeID);
		gl.glEnableVertexAttribArray(normalAttributeID);
		gl.glEnableVertexAttribArray(textureCoodrinateAttributeID);
		glUniformMatrix(gl, PMatrixID, pointLightSource.P);
		glUniformMatrix(gl, VMatrixID, pointLightSource.V);
		glUniformMatrix(gl, MMatrixID, MMatrix);

	}

	public void drawLabel( GLAutoDrawable drawable, String text ) {
		Vector4f vec = new Vector4f(0,0,0,1);
		MMatrix.transform(vec);
		VMatrix.transform(vec);
		PMatrix.transform(vec);
		vec.scale( 1/vec.w );
		int w = drawable.getSurfaceWidth();
		int h = drawable.getSurfaceHeight();
		float screenx = (float) ((vec.x + 1) / 2 * w);
		float screeny = (float) ((1 - vec.y) / 2 * h);
		enable(drawable);
	}

	public JPanel getControls(){
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.add(camera.getControls());
		vfp.add(arcBall.getControls());
		return vfp.getPanel();
	}

	public void attachArcBall(Component c){
		arcBall.attach(c);
	}

	public Vector3f Vec3dToFloat(Vector3d){
		Vector3f convertedVector = new Vector3f();
		return convertedVector;
	}
}
