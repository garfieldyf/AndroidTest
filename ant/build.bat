echo off
rem ant compile-all -Dmodule.name=androidext -Djni.package=com/android/ext -Djava.package=com.android.ext -Dmax_pool_size=48 -Dmax_thread_count=5 -Dcompile_barcode=true -Dcompile_foucs=true -DuseAndroidX=true
ant compile-java -Dmodule.name=androidext -Djni.package=com/android/ext -Djava.package=com.android.ext -Dmax_pool_size=48 -Dmax_thread_count=5 -Dcompile_barcode=false -Dcompile_foucs=false -DuseAndroidX=true
