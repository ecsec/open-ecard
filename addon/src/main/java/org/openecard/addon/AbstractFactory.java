/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @param <C>
 * @author Dirk Petrautzki
 */
public class AbstractFactory <C extends LifecycleTrait> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFactory.class);
    private final String implClass;
    private ClassLoader classLoader;

    public AbstractFactory(String implClass, ClassLoader classLoader) {
	this.implClass = implClass;
	this.classLoader = classLoader;
    }

    protected C loadInstance(Context ctx, Class<C> clazz) throws ActionInitializationException {
	try {
	    Class<?> classToLoad = classLoader.loadClass(implClass);
	    Class<? extends C> typedClass = classToLoad.asSubclass(clazz);
	    Constructor<? extends C> ctor = typedClass.getConstructor();
	    ctor.setAccessible(true);
	    C c = typedClass.cast(ctor.newInstance());
	    c.init(ctx);
	    return c;
	} catch (InstantiationException e) {
	    logger.error("Given class could not be instantiated.", e);
	    throw new ActionInitializationException(e);
	} catch (InvocationTargetException e) {
	    logger.error("Exception in nullary constructor of given class.", e);
	    throw new ActionInitializationException(e);
	} catch (IllegalAccessException e) {
	    logger.error("The class or its nullary constructor is not accessible.", e);
	    throw new ActionInitializationException(e);
	} catch (ClassNotFoundException e) {
	    logger.error("Given class could not be found.", e);
	    throw new ActionInitializationException(e);
	} catch (ClassCastException e) {
	    logger.error("Given class does not extend FactoryBaseType.", e);
	    throw new ActionInitializationException(e);
	} catch (NoSuchMethodException e) {
	    logger.error("Default constructor does not exist in action implementation.", e);
	    throw new ActionInitializationException(e);
	}
    }

}
