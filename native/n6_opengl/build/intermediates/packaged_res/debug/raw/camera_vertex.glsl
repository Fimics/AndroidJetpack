//顶点坐标，用来确定要画的形状
attribute vec4 vPosition;
//纹理坐标，采样器采样图片的坐标
attribute vec4 vCoord;
//变换矩阵
uniform mat4 vMatrix;
//传给片元着色器的， 像素点
varying vec2 aCoord;

void main(){
    //内置变量， 把顶点数据赋值给这个变量
    gl_Position = vPosition;
    aCoord = (vMatrix * vCoord).xy;
//    aCoord = vCoord.xy;
}