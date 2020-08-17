echo off
rem ant compile-all -Dmodule.name=androidext -Djni.package=com/android/ext -Djava.package=com.android.ext -Dmax_pool_size=32 -Dcompile_barcode=true -Dcompile_foucs=true -Dandroidx_support=true
ant compile-java -Dmodule.name=androidext -Djni.package=com/android/ext -Djava.package=com.android.ext -Dmax_pool_size=32 -Dcompile_barcode=false -Dcompile_foucs=false -Dandroidx_support=true
