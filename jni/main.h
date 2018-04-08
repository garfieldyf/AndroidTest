///////////////////////////////////////////////////////////////////////////////
// main.h
//
// Copyright(c) 2018, Garfield. All rights reserved.
// Author  : Garfield
// Version : 1.0
// Creation Date : 2012/4/16

#include <jni.h>

///////////////////////////////////////////////////////////////////////////////
// Global variables
//

#ifdef __BUILD_GIFIMAGE__
// Class FileDescriptor field IDs.
extern jfieldID _descriptorID;
#endif

#ifdef __BUILD_FILEUTILS__
// Class FileUtils method IDs.
extern jmethodID _setStatID;
extern jmethodID _addDirentID;

// Class ScanCallback method IDs.
extern jmethodID _onScanFileID;
#endif
