package com.mic.opengl.filter;

import android.content.Context;
import com.mic.opengl.R;
import com.mic.opengl.face.Face;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import static android.opengl.GLES20.*;

// 对人脸的，一双眼睛 做 局部放大 的专属效果过滤处理
public class BigEyeFilter extends BaseFrameFilter {
    private final int left_eye; // 左眼坐标的属性索引
    private final int right_eye; // 右眼坐标的属性索引
    private FloatBuffer left; // 左眼的buffer
    private FloatBuffer right; // 右眼的buffer
    private Face mFace; // 人脸追踪+人脸5关键点 最终的成果

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.bigeye_fragment);
        left_eye = glGetUniformLocation(mProgramId, "left_eye"); // 左眼坐标的属性索引
        right_eye = glGetUniformLocation(mProgramId, "right_eye"); // 右眼坐标的属性索引

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();  // 左眼buffer申请空间
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer(); // 右眼buffer申请空间
    }

    @Override
    public int onDrawFrame(int textureID) {
        if (null == mFace) { // 如果没有找到人脸，就不需要做事情
            return textureID;
        }
        // 1：设置视窗
        glViewport(0, 0, mWidth, mHeight);
        // 这里是因为要渲染到FBO缓存中，而不是直接显示到屏幕上
        glBindFramebuffer(GL_FRAMEBUFFER, mFrameBuffers[0]);

        // 2：使用着色器程序
        glUseProgram(mProgramId);

        // 渲染 传值
        // 1：顶点数据
        mVertexBuffer.position(0);
        glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, mVertexBuffer); // 传值
        glEnableVertexAttribArray(vPosition); // 传值后激活

        // 2：纹理坐标
        mTextureBuffer.position(0);
        glVertexAttribPointer(vCoord, 2, GL_FLOAT, false, 0, mTextureBuffer); // 传值
        glEnableVertexAttribArray(vCoord); // 传值后激活

        float[] landmarks =  mFace.landmarks; // 传 mFace 眼睛坐标 给着色器

        /*
          x = landmarks[2] / mFace.imgWidth 换算到纹理坐标0~1之间范围
          landmarks 他的相对位置是，是从C++里面得到的坐标，这个坐标是正对整个屏幕的
          但是我们要用OpenGL纹理的坐标才行，因为我们是OpenGL着色器语言代码，OpenGL纹理坐标是 0~1范围
          所以需要 / 屏幕的宽度480/高度800来得到 x/y 是等于 0~1范围
         */

        // 左眼： 的 x y 值，保存到 左眼buffer中
        float x = landmarks[2] / mFace.imgWidth;
        float y = landmarks[3] / mFace.imgHeight;
        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        glUniform2fv(left_eye, 1, left);

        // 右眼： 的 x y 值，保存到 右眼buffer中
        x = landmarks[4] / mFace.imgWidth;
        y = landmarks[5] / mFace.imgHeight;
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        glUniform2fv(right_eye, 1, right);

        // 片元 vTexture
        glActiveTexture(GL_TEXTURE0); // 激活图层
        glBindTexture(GL_TEXTURE_2D, textureID); // 绑定
        glUniform1i(vTexture, 0); // 传递参数

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4); // 通知opengl绘制

        // 解绑fbo
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        // return textureID;  // 同学们注意：这里是一个Bug，你要返回大眼后的纹理ID
        return mFrameBufferTextures[0];//返回fbo的纹理id
    }

    public void setFace(Face mFace) { // C++层把人脸最终5关键点成果的(mFaceTrack.getFace()) 赋值给此函数
        this.mFace = mFace;
    }
}
