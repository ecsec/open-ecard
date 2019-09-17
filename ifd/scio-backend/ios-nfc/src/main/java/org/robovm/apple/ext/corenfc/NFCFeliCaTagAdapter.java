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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NFCFeliCaTagAdapter/*</name>*/ 
    extends /*<extends>*/NFCTagAdapter/*</extends>*/ 
    /*<implements>*/implements NFCFeliCaTag/*</implements>*/ {

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
    @NotImplemented("currentSystemCode")
    public NSData getCurrentSystemCode() { return null; }
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("currentIDm")
    public NSData getCurrentIDm() { return null; }
    @NotImplemented("isAvailable")
    public boolean isAvailable() { return false; }
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("pollingWithSystemCode:requestCode:timeSlot:completionHandler:")
    public void pollingWithSystemCode$requestCode$timeSlot$completionHandler$(NSData systemCode, PollingRequestCode requestCode, PollingTimeSlot timeSlot, @Block VoidBlock3<NSData, NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("requestServiceWithNodeCodeList:completionHandler:")
    public void requestServiceWithNodeCodeList$completionHandler$(NSArray<NSData> nodeCodeList, @Block VoidBlock2<NSArray<NSData>, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("requestResponseWithCompletionHandler:")
    public void requestResponseWithCompletionHandler(@Block("(@MachineSizedSInt,)") VoidBlock2<Long, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("readWithoutEncryptionWithServiceCodeList:blockList:completionHandler:")
    public void readWithoutEncryptionWithServiceCodeList$blockList$completionHandler$(NSArray<NSData> serviceCodeList, NSArray<NSData> blockList, @Block("(@MachineSizedSInt,@MachineSizedSInt,,)") VoidBlock4<Long, Long, NSArray<NSData>, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeWithoutEncryptionWithServiceCodeList:blockList:blockData:completionHandler:")
    public void writeWithoutEncryptionWithServiceCodeList$blockList$blockData$completionHandler$(NSArray<NSData> serviceCodeList, NSArray<NSData> blockList, NSArray<NSData> blockData, @Block("(@MachineSizedSInt,@MachineSizedSInt,)") VoidBlock3<Long, Long, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("requestSystemCodeWithCompletionHandler:")
    public void requestSystemCodeWithCompletionHandler(@Block VoidBlock2<NSArray<NSData>, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("requestServiceV2WithNodeCodeList:completionHandler:")
    public void requestServiceV2WithNodeCodeList$completionHandler$(NSArray<NSData> nodeCodeList, @Block("(@MachineSizedSInt,@MachineSizedSInt,,,,)") VoidBlock6<Long, Long, Long, NSArray<NSData>, NSArray<NSData>, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("requestSpecificationVersionWithCompletionHandler:")
    public void requestSpecificationVersionWithCompletionHandler(@Block("(@MachineSizedSInt,@MachineSizedSInt,,,)") VoidBlock5<Long, Long, NSData, NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("resetModeWithCompletionHandler:")
    public void resetModeWithCompletionHandler(@Block("(@MachineSizedSInt,@MachineSizedSInt,)") VoidBlock3<Long, Long, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("sendFeliCaCommandPacket:completionHandler:")
    public void sendFeliCaCommandPacket$completionHandler$(NSData commandPacket, @Block VoidBlock2<NSData, NSError> completionHandler) {}
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
