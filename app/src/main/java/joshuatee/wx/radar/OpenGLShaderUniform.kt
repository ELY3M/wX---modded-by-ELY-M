package joshuatee.wx.radar

import android.opengl.GLES20

// thanks! http://androidblog.reindustries.com/a-real-open-gl-es-2-0-2d-tutorial-part-1/

internal object OpenGLShaderUniform {

    // Program variables
    var sp_SolidColorUniform: Int = 0

    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    const val vs_SolidColorUnfiform =
        "uniform    mat4        uMVPMatrix;" +
                "attribute  vec4        vPosition;" +
                "attribute  vec4        a_Color;" + // was attribute

                "varying  vec4        v_Color;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  v_Color = a_Color;" +
                "}"

    const val fs_SolidColorUnfiform =
        "precision mediump float;" +
                "uniform vec4 v_Color;" +
                "void main() {" +
                "  gl_FragColor = v_Color;" +
                "}"

    fun loadShader(type: Int, shaderCode: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // return the shader
        return shader
    }
}
