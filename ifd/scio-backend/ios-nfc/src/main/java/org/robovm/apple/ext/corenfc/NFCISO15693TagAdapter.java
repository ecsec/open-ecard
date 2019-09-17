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
/*<visibility>*/public/*</visibility>*/ class /*<name>*/NFCISO15693TagAdapter/*</name>*/ 
    extends /*<extends>*/NFCTagAdapter/*</extends>*/ 
    /*<implements>*/implements NFCISO15693Tag/*</implements>*/ {

    /*<ptr>*/
    /*</ptr>*/
    /*<bind>*/
    /*</bind>*/
    /*<constants>*//*</constants>*/
    /*<constructors>*//*</constructors>*/
    /*<properties>*/
    @NotImplemented("identifier")
    public NSData getIdentifier() { return null; }
    @NotImplemented("icManufacturerCode")
    public @MachineSizedUInt long getIcManufacturerCode() { return 0; }
    @NotImplemented("icSerialNumber")
    public NSData getIcSerialNumber() { return null; }
    @NotImplemented("isAvailable")
    public boolean isAvailable() { return false; }
    /*</properties>*/
    /*<members>*//*</members>*/
    /*<methods>*/
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("sendCustomCommandWithConfiguration:completionHandler:")
    public void sendCustomCommand(NFCISO15693CustomCommandConfiguration commandConfiguration, @Block VoidBlock2<NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 11.0 and later.
     */
    @NotImplemented("readMultipleBlocksWithConfiguration:completionHandler:")
    public void readMultipleBlocks(NFCISO15693ReadMultipleBlocksConfiguration readConfiguration, @Block VoidBlock2<NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("stayQuietWithCompletionHandler:")
    public void stayQuietWithCompletionHandler(@Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("readSingleBlockWithRequestFlags:blockNumber:completionHandler:")
    public void readSingleBlockWithRequestFlags$blockNumber$completionHandler$(RequestFlag flags, byte blockNumber, @Block VoidBlock2<NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeSingleBlockWithRequestFlags:blockNumber:dataBlock:completionHandler:")
    public void writeSingleBlockWithRequestFlags$blockNumber$dataBlock$completionHandler$(RequestFlag flags, byte blockNumber, NSData dataBlock, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("lockBlockWithRequestFlags:blockNumber:completionHandler:")
    public void lockBlockWithRequestFlags$blockNumber$completionHandler$(RequestFlag flags, byte blockNumber, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("readMultipleBlocksWithRequestFlags:blockRange:completionHandler:")
    public void readMultipleBlocksWithRequestFlags$blockRange$completionHandler$(RequestFlag flags, @ByVal NSRange blockRange, @Block VoidBlock2<NSArray<NSData>, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeMultipleBlocksWithRequestFlags:blockRange:dataBlocks:completionHandler:")
    public void writeMultipleBlocksWithRequestFlags$blockRange$dataBlocks$completionHandler$(RequestFlag flags, @ByVal NSRange blockRange, NSArray<NSData> dataBlocks, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("selectWithRequestFlags:completionHandler:")
    public void selectWithRequestFlags$completionHandler$(RequestFlag flags, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("resetToReadyWithRequestFlags:completionHandler:")
    public void resetToReadyWithRequestFlags$completionHandler$(RequestFlag flags, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeAFIWithRequestFlag:afi:completionHandler:")
    public void writeAFIWithRequestFlag$afi$completionHandler$(RequestFlag flags, byte afi, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("lockAFIWithRequestFlag:completionHandler:")
    public void lockAFIWithRequestFlag$completionHandler$(RequestFlag flags, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("writeDSFIDWithRequestFlag:dsfid:completionHandler:")
    public void writeDSFIDWithRequestFlag$dsfid$completionHandler$(RequestFlag flags, byte dsfid, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("lockDFSIDWithRequestFlag:completionHandler:")
    public void lockDFSIDWithRequestFlag$completionHandler$(RequestFlag flags, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("getSystemInfoWithRequestFlag:completionHandler:")
    public void getSystemInfoWithRequestFlag$completionHandler$(RequestFlag flags, @Block("(@MachineSizedSInt,@MachineSizedSInt,@MachineSizedSInt,@MachineSizedSInt,@MachineSizedSInt,)") VoidBlock6<Long, Long, Long, Long, Long, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("getMultipleBlockSecurityStatusWithRequestFlag:blockRange:completionHandler:")
    public void getMultipleBlockSecurityStatusWithRequestFlag$blockRange$completionHandler$(RequestFlag flags, @ByVal NSRange blockRange, @Block VoidBlock2<NSArray<NSNumber>, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("customCommandWithRequestFlag:customCommandCode:customRequestParameters:completionHandler:")
    public void customCommandWithRequestFlag$customCommandCode$customRequestParameters$completionHandler$(RequestFlag flags, @MachineSizedSInt long customCommandCode, NSData customRequestParameters, @Block VoidBlock2<NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("extendedReadSingleBlockWithRequestFlags:blockNumber:completionHandler:")
    public void extendedReadSingleBlockWithRequestFlags$blockNumber$completionHandler$(RequestFlag flags, @MachineSizedSInt long blockNumber, @Block VoidBlock2<NSData, NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("extendedWriteSingleBlockWithRequestFlags:blockNumber:dataBlock:completionHandler:")
    public void extendedWriteSingleBlockWithRequestFlags$blockNumber$dataBlock$completionHandler$(RequestFlag flags, @MachineSizedSInt long blockNumber, NSData dataBlock, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("extendedLockBlockWithRequestFlags:blockNumber:completionHandler:")
    public void extendedLockBlockWithRequestFlags$blockNumber$completionHandler$(RequestFlag flags, @MachineSizedSInt long blockNumber, @Block VoidBlock1<NSError> completionHandler) {}
    /**
     * @since Available in iOS 13.0 and later.
     */
    @NotImplemented("extendedReadMultipleBlocksWithRequestFlags:blockRange:completionHandler:")
    public void extendedReadMultipleBlocksWithRequestFlags$blockRange$completionHandler$(RequestFlag flags, @ByVal NSRange blockRange, @Block VoidBlock2<NSArray<NSData>, NSError> completionHandler) {}
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
