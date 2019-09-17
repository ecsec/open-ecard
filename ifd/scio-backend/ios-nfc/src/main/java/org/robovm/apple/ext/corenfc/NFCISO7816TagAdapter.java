/*
 * Copyright (C) 2013-2015 RoboVM AB
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.apple.ext.corenfc;

/*<imports>*/
import java.io.*;
import java.nio.*;
import java.util.*;
import org.robovm.objc.*;
import org.robovm.objc.annotation.*;
import org.robovm.objc.block.*;
import org.robovm.rt.*;
import org.robovm.rt.annotation.*;
import org.robovm.rt.bro.*;
import org.robovm.rt.bro.annotation.*;
import org.robovm.rt.bro.ptr.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.dispatch.*;
/*</imports>*/

/*<javadoc>*/
/*</javadoc>*/
/*<annotations>*//*</annotations>*/
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NFCISO7816TagAdapter/*</name>*/ 
    extends /*<extends>*/NFCTagAdapter/*</extends>*/ 
    /*<implements>*/implements NFCISO7816Tag/*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*/
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("initialSelectedAID")
    public String getInitialSelectedAID() { return null; }
    @NotImplemented("identifier")
    public NSData getIdentifier() { return null; }
    @NotImplemented("historicalBytes")
    public NSData getHistoricalBytes() { return null; }
    @NotImplemented("applicationData")
    public NSData getApplicationData() { return null; }
    @NotImplemented("proprietaryApplicationDataCoding")
    public boolean isProprietaryApplicationDataCoding() { return false; }
    @NotImplemented("isAvailable")
    public boolean isAvailable() { return false; }
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("sendCommandAPDU:completionHandler:")
    public void sendCommandAPDU$completionHandler$(NFCISO7816APDU apdu, @Block VoidBlock4<NSData, Byte, Byte, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("queryNDEFStatusWithCompletionHandler:")
    public void queryNDEFStatusWithCompletionHandler(@Block("(,@MachineSizedUInt,)") VoidBlock3<NFCNDEFStatus, Long, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("readNDEFWithCompletionHandler:")
    public void readNDEFWithCompletionHandler(@Block VoidBlock2<NFCNDEFMessage, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeNDEF:completionHandler:")
    public void writeNDEF$completionHandler$(NFCNDEFMessage ndefMessage, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeLockWithCompletionHandler:")
    public void writeLockWithCompletionHandler(@Block VoidBlock1<NSError> completionHandler) {}
    @NotImplemented("encodeWithCoder:")
    public void encode(NSCoder coder) {}
    /*</methods>*/
}
