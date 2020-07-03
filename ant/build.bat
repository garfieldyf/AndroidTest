echo off
rem ant compile-all -Dmodule.name=androidext -Djni.package=android/ext -Djava.package=android.ext -Dmax_pool_size=64 -Dcompile_barcode=true -Dcompile_foucs=true
ant compile-java -Dmodule.name=androidext -Djni.package=android/ext -Djava.package=android.ext -Dmax_pool_size=64 -Dcompile_barcode=true -Dcompile_foucs=true