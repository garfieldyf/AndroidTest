集成说明
1、将 out/debug   目录下面的所有文件拷贝到工程目录 app/src/debug
   将 out/release 目录下面的所有文件拷贝到工程目录 app/src/release
   将 android-sdk-stubs.jar 文件拷贝到工程目录 app/src/release
   将 out 目录下面的 xml 资源文件拷贝到工程的 res/values 目录

2、在 build.gradle 文件中加入：
    allprojects {
        gradle.projectsEvaluated {
            tasks.withType(JavaCompile) {
                options.compilerArgs.add('-Xbootclasspath/p:app/src/release/android-sdk-stubs.jar')
            }
        }
    }

    android {
        defaultConfig {
            ndk {
                abiFilters "arm64-v8a", "armeabi-v7a"
            }
        }

        buildTypes {
            release {
                proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'src/release/xxx-proguard.txt'
            }
        }

        compileOptions {
            sourceCompatibility = '1.8'
            targetCompatibility = '1.8'
        }
    }

    dependencies {
        debugImplementation files('src/debug/xxx.jar')
        releaseImplementation files('src/release/xxx.jar')
    }

使用说明
1、DebugUtils 中的所有方法只能在开发过程中使用，不能集成到发布版本中。
2、所有 @hide 注释的方法不能直接调用。
3、所有以双下划线(__) 命名的方法和变量都不能使用。
4、所有 dump、dumpXXXX 方法只能在开发过程中使用，不能集成到发布版本中。
