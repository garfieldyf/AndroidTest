echo off
rem ant compile-all -DMODULE_NAME=androidext -DJNI_PACKAGE=com/android/ext -DJAVA_PACKAGE=com.android.ext -DINCLUDE_PAGE=true -DINCLUDE_FOCUS=true -DINCLUDE_BARCODE=true -DUSE_ANDROIDX=true
ant compile-java -DMODULE_NAME=androidext -DJNI_PACKAGE=com/android/ext -DJAVA_PACKAGE=com.android.ext -DINCLUDE_PAGE=false -DINCLUDE_FOCUS=false -DINCLUDE_BARCODE=false -DUSE_ANDROIDX=true
