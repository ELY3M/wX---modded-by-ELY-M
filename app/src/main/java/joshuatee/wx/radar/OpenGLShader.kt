package joshuatee.wx.radar

import android.opengl.GLES20

// thanks! http://androidblog.reindustries.com/a-real-open-gl-es-2-0-2d-tutorial-part-1/

internal object OpenGLShader {

    // Program variables
    var sp_SolidColor: Int = 0

    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    const val vs_SolidColor =
            "uniform    mat4        uMVPMatrix;" +
                    "attribute  vec4        vPosition;" +
                    "attribute  vec3        a_Color;" + // was attribute

                    "varying  vec3        v_Color;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_Color = a_Color;" +
                    "}"

    const val fs_SolidColor =
            "precision mediump float;" +
                    "varying vec3 v_Color;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(v_Color,1.0);" +
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