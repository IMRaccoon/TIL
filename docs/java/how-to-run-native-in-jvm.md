## JVM 환경에서 C, C++ 네이티브 코드 실행하기

- java에서 native code를 실행하는 방법은 크게 2가지가 있다.
    - JNI(Java Native Interface)
    - JNA(Java Native Access)
- JNI는 java에서 C, C++ 코드를 실행할 수 있도록 하는 인터페이스이다.
- java에서는 `native` 키워드를 통해 해당 메서드가 JNI를 사용한다는 것을 명시한다.

### Example
> 참고: [jni](../jvm-study/src/main/java/com/example/jni)  
> PROJECT_ROOT=~/TIL/jvm-study

#### 1. Java 코드 작성 (및 native method 정의)
- 파일명: src/main/java/com/example/jni/JNISample.java
```java
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
```

#### 2. Header 코드 생성

- java 9 이전에는 `javac`를 통해 class 파일 생성, 이 후에는 `javah`를 통해 header 파일 생성.
    - `javac ${JAVA_FILE}`
    - `javah -classpath ${PROJECT_ROOT} com.example.jni.JNISample`
- java 9 이후에는 `javac`를 통해 class 파일 & header 파일 생성.
    - `javac -h ${HEADER_FILE_DEST} ${JAVA_FILE}`
    - `javac -h ${PROJECT_ROOT}/src/main/java/com/example/jni/ ${PROJECT_ROOT}/src/main/java/com/example/jni/JNISample.java`
    
#### 3. C 코드 작성
- 파일명: src/main/java/com/example/jni/JNISample.c
- `javah`를 통해 생성된 header 파일을 include 하면서, header 파일에 정의된 native method를 구현한다.

```c 
#include <jni.h>
#include "com_example_jni_JNISample.h"

JNIEXPORT void JNICALL Java_com_example_jni_JNISample_nativeMethod(JNIEnv *env, jobject obj) {
       printf("JNI는 이렇게 동작해요");
}
```

#### 4. C 코드 컴파일
- `gcc`를 통해 C 코드를 컴파일한다.
- Dynamic Library Design Guidelines의 다음 규약에 따라 앞의 lib와 뒤의 .dylib를 제외한 'jnisample'이 라이브러리 이름으로 인식된다.
    - `lib` + `jnisample` + `.dylib`

```shell
gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin \
  -I${PROJECT_ROOT}/src/main/java/com/example/jni/com_example_JNISample.h\
  -shared -m64 ${PROJECT_ROOT}/src/main/java/com/example/jni/JNISample.c\
  -o ${PROEJCT_ROOT}/src/main/java/com/example/jni/libjnisample.dylib
```

#### 5. 실행
- `java` 명령어를 통해 java 코드를 실행한다.

```shell
cd ${PROJECT_ROOT}/src/main/java
java -Djava.library.path=${PROJECT_ROOT}/src/main/java/com/example/jni  com.example.jni.JNISample
```
