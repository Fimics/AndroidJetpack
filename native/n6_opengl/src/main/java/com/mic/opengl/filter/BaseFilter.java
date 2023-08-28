package com.mic.opengl.filter;

import android.content.Context;
import com.mic.opengl.utils.ShaderHelper;
import com.mic.opengl.utils.BufferHelper;
import com.mic.opengl.utils.TextResourceReader;
import java.nio.FloatBuffer;
import static android.opengl.GLES20.*;

public class BaseFilter {
    private int mVertexSourceId;
    private int mFragmentSourceId;

    protected FloatBuffer mVertexBuffer;//顶点坐标数据缓冲区
    protected FloatBuffer mTextureBuffer;//纹理坐标数据缓冲区

    protected int mProgramId;
    protected int vPosition;
    protected int vCoord;
    protected int vMatrix;
    protected int vTexture;
    protected int mWidth;
    protected int mHeight;

    public BaseFilter(Context context, int vertexSourceId, int fragmentSourceId) {
        this.mVertexSourceId = vertexSourceId;
        this.mFragmentSourceId = fragmentSourceId;

        float[] VERTEX = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };
        mVertexBuffer = BufferHelper.getFloatBuffer(VERTEX);
        // float[] TEXTURE = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,};
        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        mTextureBuffer = BufferHelper.getFloatBuffer(TEXTURE);
        init(context);
        changeTextureData();
    }

    /**
     * 修改纹理坐标 textureData（有需求可以重写该方法）
     */
    protected void changeTextureData(){

    }

    private void init(Context context) {
        String vertexSource = TextResourceReader.readTextFileFromResource(context, mVertexSourceId);
        String fragmentSource = TextResourceReader.readTextFileFromResource(context,
                mFragmentSourceId);

        int vertexShaderId = ShaderHelper.compileVertexShader(vertexSource);
        // int fragmentShaderId = ShaderHelper.compileVertexShader(fragmentSource);
        int fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentSource);

        mProgramId = ShaderHelper.linkProgram(vertexShaderId, fragmentShaderId);
        // TODO 得到程序了，着色器可以释放了
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        //        ShaderHelper.validateProgram(mProgramId);
        vPosition = glGetAttribLocation(mProgramId, "vPosition");
        vCoord = glGetAttribLocation(mProgramId, "vCoord");
        vMatrix = glGetUniformLocation(mProgramId, "vMatrix");
        vTexture = glGetUniformLocation(mProgramId, "vTexture");
    }

    public void onReady(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int onDrawFrame(int textureId) {
        //设置视窗大小
        glViewport(0, 0, mWidth, mHeight);
        glUseProgram(mProgramId);

        //画画
        //顶点坐标赋值
        mVertexBuffer.position(0);
        //传值
        glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, mVertexBuffer);
        //激活
        glEnableVertexAttribArray(vPosition);

        //纹理坐标赋值
        mTextureBuffer.position(0);
        //传值
        glVertexAttribPointer(vCoord, 2, GL_FLOAT, false, 0, mTextureBuffer);
        //激活
        glEnableVertexAttribArray(vCoord);

        //片元 vTexture
        //激活图层
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D ,textureId);
        glUniform1i(vTexture, 0);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindTexture(GL_TEXTURE_2D ,0);
        return textureId;
    }

    public void release(){
        glDeleteProgram(mProgramId);
    }
}
