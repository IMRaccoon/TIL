package com.example.jni;

public class JNISample {
    private native void nativeMethod();

    public static void main(String[] args) {
        new JNISample().nativeMethod();
    }

    static {
        System.loadLibrary("jnisample");
    }
}

// javac -h /Users/user/Github/Raccoon/TIL/jvm-study/src/main/java/com/example/jni /Users/user/Github/Raccoon/TIL/jvm-study/src/main/java/com/example/jni/JNISample.java
// gcc -I/Users/user/.jenv/versions/21/include -I/Users/user/.jenv/versions/21/include/darwin -I/Users/user/Github/Raccoon/TIL/jvm-study/src/main/java/com/example/jni/com_example_JNISample.h -shared -m64  /Users/user/Github/Raccoon/TIL/jvm-study/src/main/java/com/example/jni/JNISample.c -o /Users/user/Github/Raccoon/TIL/jvm-study/src/main/java/com/example/jni/libjnisample.dylib
// java -Djava.library.path=/Users/user/Github/Raccoon/TIL/jvm-study/src/main/java/com/example/jni  com.example.jni.JNISample


