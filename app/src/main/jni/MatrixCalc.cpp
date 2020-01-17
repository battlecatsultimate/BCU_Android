//
// Created by user on 1/13/2020.
//

// Signature List
// Z : boolean
// B : byte
// C : char
// S : short
// I : int
// J : long
// F : float
// D : double
// L calzz : clazz
// [ clazz : clazz[]
// (Param)Return : method(Param) return Return

#include <jni.h>
#include "android/log.h"

extern "C" {
    JNIEXPORT void JNICALL Java_com_mandarin_bcu_androidutil_fakeandroid_CVGraphics_Ntranslate(JNIEnv* env, jobject thiz, jfloat x, jfloat y) {
        jclass cv = env -> GetObjectClass(thiz);

        jmethodID trans = env -> GetMethodID(cv,"Ctranslate","(FF)V");

        env -> CallVoidMethod(thiz,trans,x,y);
    }

    JNIEXPORT void JNICALL Java_com_mandarin_bcu_androidutil_fakeandroid_CVGraphics_NScale(JNIEnv *env, jobject thiz, jint hf, jint vf) {
        jclass cv = env -> GetObjectClass(thiz);

        jmethodID scal = env -> GetMethodID(cv,"CScale","(II)V");

        env -> CallVoidMethod(thiz,scal,hf,vf);
    }

    JNIEXPORT void JNICALL Java_com_mandarin_bcu_androidutil_fakeandroid_CVGraphics_NsetTransform(JNIEnv *env, jobject thiz, jobject at) {
        jclass cv = env -> GetObjectClass(thiz);

        jmethodID  form = env -> GetMethodID(cv,"CsetTransform","(Lcommon/system/fake/FakeTransform;)V");

        env -> CallVoidMethod(thiz,form,at);
    }

    JNIEXPORT void JNICALL Java_com_mandarin_bcu_androidutil_fakeandroid_CVGraphics_Nrotate(JNIEnv *env, jobject thiz, jdouble d) {
        jclass cv = env -> GetObjectClass(thiz);

        jmethodID  rot = env -> GetMethodID(cv,"Crotate","(D)V");

        env -> CallVoidMethod(thiz,rot,d);
    }

    JNIEXPORT void JNICALL Java_com_mandarin_bcu_androidutil_fakeandroid_CVGraphics_NdrawImage(JNIEnv *env, jobject thiz, jobject bimg, jdouble x, jdouble y, jdouble d, jdouble e) {
        jclass cv = env -> GetObjectClass(thiz);

        jmethodID dimg = env -> GetMethodID(cv,"CdrawImage","(Lcommon/system/fake/FakeImage;DDDD)V");

        env -> CallVoidMethod(thiz,dimg,bimg,x,y,d,e);
    }
}