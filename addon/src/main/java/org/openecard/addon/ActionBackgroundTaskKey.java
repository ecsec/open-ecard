/****************************************************************************
 * Copyright (C) 2021 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.addon;

import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;

/**
 *
 * @author Tobias Wich
 */
class ActionBackgroundTaskKey {

    public final String addonId;
    public final String actionId;

    public ActionBackgroundTaskKey(AddonSpecification addon, AppExtensionSpecification extension) {
	this.addonId = addon.getId();
	this.actionId = extension.getId();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ActionBackgroundTaskKey) {
	    ActionBackgroundTaskKey other = (ActionBackgroundTaskKey) obj;
	    return addonId.equals(other.addonId) && actionId.equals(other.actionId);
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	return addonId.hashCode() + actionId.hashCode();
    }

    @Override
    public String toString() {
	return String.format("%s_%s", addonId, actionId);
    }

}
