echo off
rem ant compile-all -Dmodule.name=androidext -Djni.package=android/ext -Djava.package=android.ext -Dcompile_barcode=true -Dcompile_foucs=false
ant compile-java -Dmodule.name=androidext -Djni.package=android/ext -Djava.package=android.ext -Dcompile_barcode=true -Dcompile_foucs=false