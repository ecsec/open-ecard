/*
 * Copyright 2012 Moritz Horsch.
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
package org.openecard.client.common.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;


/**
 * A set of utility functions for Integers.
 *
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class IntegerUtils {

    /**
     * Convert a int to a byte array and cut leading null bytes.
     *
     * @param i
     * @return byte[]
     */
    public static byte[] toByteArray(int i) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeInt(i);
            dos.flush();
        } catch (Throwable ignore) {
        }
        if (bos.size() == 1) {
            return bos.toByteArray();
        } else {
            return ByteUtils.cutLeadingNullBytes(bos.toByteArray());
        }
    }
}
