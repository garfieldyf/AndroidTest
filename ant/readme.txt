集成说明
1、将 debug 目录下面的 so 文件拷贝到工程目录 app/src/debug/jniLibs
2、将 release 目录下面的 so 文件拷贝到工程目录 app/src/release/jniLibs
   在 build.gradle 文件中加入：
    defaultConfig {
        ndk {
            abiFilters "arm64-v8a", "armeabi-v7a"
        }
    }

3、将 debug 目录下面的 jar 文件拷贝到工程目录 app/libs/debug
   如: app/libs/debug/xxx.jar
   在 build.gradle 中加入：debugImplementation files('libs/debug/xxx.jar')

4、将 release 目录下面的 jar 文件拷贝到工程目录 app/libs/release
   如: app/libs/release/xxx.jar
   在 build.gradle 中加入：releaseImplementation files('libs/release/xxx.jar')

5、将 xml 资源文件拷贝到工程的 res/values 目录, proguard 文件拷贝到工程目录
   在 build.gradle 文件加入：proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'xxx-proguard.txt'

6、将 android-sdk-stubs.jar 文件拷贝到工程目录 app/libs/compile
   在 build.gradle 文件中加入：
        allprojects {
            gradle.projectsEvaluated {
                tasks.withType(JavaCompile) {
                    options.compilerArgs.add('-Xbootclasspath/p:libs/compile/android-sdk-stubs.jar')
                }
            }
        }

使用说明
1、DebugUtils 中的所有方法只能在开发过程中使用，不能集成到发布版本中。
2、所有 @hide 注释的方法不能直接调用。
3、所有以双下划线(__) 命名的方法和变量都不能使用。
4、所有 dump、dumpXXXX 方法只能在开发过程中使用，不能集成到发布版本中。
