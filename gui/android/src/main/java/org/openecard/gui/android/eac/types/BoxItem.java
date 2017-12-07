/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.gui.android.eac.types;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 *
 * @author Tobias Wich
 */
public class BoxItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public BoxItem(@Nonnull String name, boolean selected, boolean disabled, @Nullable String additionalText) {
	this.name = name;
	this.selected = selected;
	this.disabled = disabled;
	this.additionalText = additionalText;
    }

    /**
     * Enum name of the Access Right.
     *
     * The following Data Groups are defined:
     * <dl>
     * <dt>DG01</dt><dd>Document Type</dd>
     * <dt>DG02</dt><dd>Issuing State, Region and Municipality</dd>
     * <dt>DG03</dt><dd>Date of Expiry</dd>
     * <dt>DG04</dt><dd>Given Names</dd>
     * <dt>DG05</dt><dd>Family Names</dd>
     * <dt>DG06</dt><dd>Nom de Plume</dd>
     * <dt>DG07</dt><dd>Academic Title</dd>
     * <dt>DG08</dt><dd>Date of Birth</dd>
     * <dt>DG09</dt><dd>Place of Birth</dd>
     * <dt>DG10</dt><dd>Nationality</dd>
     * <dt>DG11</dt><dd>Sex</dd>
     * <dt>DG12</dt><dd>Optional Data</dd>
     * <dt>DG13</dt><dd>Birth Name</dd>
     * <dt>DG14</dt><dd>Written Signature</dd>
     * <dt>DG15</dt><dd>Date of Issuance</dd>
     * <dt>DG16</dt><dd>--</dd>
     * <dt>DG17</dt><dd>Normal Place of Residence (multiple)</dd>
     * <dt>DG18</dt><dd>Municipality ID</dd>
     * <dt>DG19</dt><dd>Residence Permit I</dd>
     * <dt>DG20</dt><dd>Residence Permit II</dd>
     * <dt>DG21</dt><dd>Phone Number</dd>
     * <dt>DG22</dt><dd>Email Address</dd>
     * <dt>INSTALL_QUALIFIED_CERTIFICATE</dt><dd>Install signature certificate</dd>
     * <dt>INSTALL_CERTIFICATE</dt><dd>Install non-qualified signature certificate</dd>
     * <dt>PIN_MANAGEMENT</dt><dd>PIN Management</dd>
     * <dt>CAN_ALLOWED</dt><dd>CAN allowed</dd>
     * <dt>PRIVILEGED_TERMINAL</dt><dd>Privileged Terminal</dd>
     * <dt>RESTRICTED_IDENTIFICATION</dt><dd>Restricted Identification</dd>
     * <dt>COMMUNITY_ID_VERIFICATION</dt><dd>Address verification</dd>
     * <dt>AGE_VERIFICATION</dt><dd>Age verification (≥ %d)</dd>
     * </dl>
     *
     * @see CHAT.DataGroup
     * @see CHAT.SpecialFunction
     * @see BSI TR-03110-4 (v2.21) Sec. 2.2.3.2
     */
    protected String name;
    /**
     * Indicates the selection state of the item.
     */
    protected boolean selected;
    /**
     * Indicates that this item may not be changed, meaning the attribute is required.
     */
    protected boolean disabled;
    /**
     * Additional data such as verification age.
     */
    @Nullable
    protected String additionalText;

    public String getName() {
	return name;
    }

    public boolean isSelected() {
	return selected;
    }

    public void setSelected(boolean selected) {
	this.selected = selected;
    }

    public boolean isDisabled() {
	return disabled;
    }

    @Nullable
    public String getAdditionalText() {
	return additionalText;
    }

}
