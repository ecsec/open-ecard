/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
 */
package org.openecard.transport.dispatcher

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.interfaces.DispatcherException
import org.openecard.common.interfaces.Publish
import org.openecard.ws.ECardApiMethod
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import javax.xml.transform.TransformerException

private val LOG = KotlinLogging.logger {  }

/**
 * Service class encapsulating one webservice for the [MessageDispatcher].
 * This class takes care of the actual interface analysis and reflection part.
 *
 * @author Tobias Wich
 */
internal class Service @JvmOverloads constructor(
    /**
     * Gets the webservice interface class this instance is initialized with.
     *
     * @return The webservice interface belonging to this instance.
     */
    val serviceInterface: Class<*>, private val impl: Class<*>, private val isFilter: Boolean = false
) : Comparable<Service?> {
    private val requestClasses: ArrayList<Class<*>> = ArrayList()
	private val requestMethods: TreeMap<String, Method> = TreeMap()
	private val objectLoggers: HashMap<Class<*>, MessageLogger> = HashMap()
	private val actions: MutableList<String> = ArrayList()

	/**
     * Creates a new Service instance and initializes it with the given webservice interface class.
     *
     * @param serviceInterface The webservice interface class.
     */
    init {
		init()
    }

    private fun init() {
        val methods = impl.getDeclaredMethods()
        for (m in methods) {
            val webAnnotation: ECardApiMethod? = getAnnotation(m, ECardApiMethod::class.java)
            if (isReqParam(m) && webAnnotation != null) {
                val reqClass = getReqParamClass(m)
                if (requestMethods.containsKey(reqClass!!.getName())) {
                    var msg = "Omitting method ${m.name} in service interface ${impl.getName()}, because its parameter type is "
                    msg += "already associated with another method."
					LOG.warn { msg }
                } else {
                    val action = webAnnotation.action
                    if (isFilter) {
                        if (getAnnotation(m, Publish::class.java) != null) {
                            requestClasses.add(reqClass)
                            requestMethods.put(reqClass.getName(), m)
                            actions.add(action)
                        }
                    } else {
                        requestClasses.add(reqClass)
                        requestMethods.put(reqClass.getName(), m)
                        actions.add(action)
                    }
                }
            }
        }
    }

    /**
     * Gets the logger for the given object.
     * This method creates a new logger if none is present yet. After the logger is created, always the same logger is
     * returned. This method is thread safe.
     *
     * @param ifaceImpl Implementation for which the logger is requested.
     * @return The requested logger.
     */
    private fun getLogger(ifaceImpl: Any): MessageLogger {
        val implClass: Class<*> = ifaceImpl.javaClass
        if (objectLoggers.containsKey(implClass)) {
            return objectLoggers.get(implClass)!!
        } else {
            synchronized(this) {
                val implLogger = MessageLogger(ifaceImpl.javaClass)
                objectLoggers.put(implClass, implLogger)
                return implLogger
            }
        }
    }

    /**
     * Invokes the webservice method related to the request object in the given webservice class instance.
     *
     * @param ifaceImpl The instance implementing the webservice interface this instance is responsible for.
     * @param req The request object to dispatch.
     * @return The result of the method invocation.
     * @throws DispatcherException In case an error happens in the reflections part of the dispatcher.
     * @throws InvocationTargetException In case the dispatched method throws en exception.
     */
    @Throws(DispatcherException::class, InvocationTargetException::class)
    fun invoke(ifaceImpl: Any, req: Any): Any {
        try {
            val l = getLogger(ifaceImpl)
            val reqClass: Class<*> = req.javaClass
            val m = getMethod(reqClass.getName())
            // invoke method
            l.logRequest(req)
            val res = m.invoke(ifaceImpl, req)
            l.logResponse(res)
            return res
        } catch (ex: IllegalAccessException) {
            throw DispatcherException(ex.message, ex)
        } catch (ex: NoSuchMethodException) {
            throw DispatcherException(ex.message, ex)
        } catch (ex: IllegalArgumentException) {
            throw DispatcherException(ex.message, ex)
        }
    }


    private fun getReqParamClass(m: Method): Class<*>? {
        // get parameters of this method
        val params = m.parameterTypes
        // methods must have exactly one parameter
        if (params.size != 1) {
            return null
        }

        // TODO: add other checks
        return params[0]
    }

    private fun isReqParam(m: Method): Boolean {
        return getReqParamClass(m) != null
    }

    fun getRequestClasses(): List<Class<*>> {
        return requestClasses.toList()
    }

    @Throws(NoSuchMethodException::class)
    private fun getMethod(paramClass: String?): Method {
        val m = requestMethods.get(paramClass)
        if (m == null) {
            var msg = "Method containing parameter with class '" + paramClass + "' does not exist in interface '"
            msg += serviceInterface.getName() + "'."
            throw NoSuchMethodException(msg)
        }
        return m
    }

    val actionList: List<String>
        /**
         * Get a list with all the action names of this service.
         *
         * @return An unmodifiable list containing all the action names of this service.
         */
        get() = actions.toList()




    override fun compareTo(o: Service?): Int {
        return this.serviceInterface.toString().compareTo(o?.serviceInterface.toString())
    }

    companion object {
        private fun <A : Annotation> getAnnotation(m: Method, aClass: Class<out A>): A? {
            // direct lookup
            var m = m
            var a: A? = m.getAnnotation(aClass)
            if (a != null) {
                return a
            } else {
                // try interfaces and superclass
                val children = ArrayList<Class<*>>()
                val declaringClass = m.declaringClass
                // find all interfaces and the super class
                children.addAll(listOf(*declaringClass.interfaces))
                if (declaringClass.getSuperclass() != null) {
                    children.add(declaringClass.getSuperclass())
                }

                // try to find annotation in any of the childs
                for (c in children) {
                    try {
                        m = c.getDeclaredMethod(m.name, *m.parameterTypes)
                    } catch (ex: NoSuchMethodException) {
                        continue
                    } catch (ex: SecurityException) {
                        continue
                    }
                    a = getAnnotation(m, aClass)
                    if (a != null) {
                        return a
                    }
                }
                // nothing found
                return null
            }
        }
    }
}

/**
 * Internal logger class for request and response objects.
 * It only logs
 */
private class MessageLogger(receiverClass: Class<*>) {
	private val l: KLogger = KotlinLogging.logger(receiverClass.name)

	private val reqLogMsg = String.format("Delivering request object to %s:", receiverClass.getName())
	private val resLogMsg = "Returning response object:"

	fun logRequest(msgObj: Any) {
		logObject(l, reqLogMsg, msgObj)
	}

	fun logResponse(msgObj: Any) {
		logObject(l, resLogMsg, msgObj)
	}

	fun logObject(l: KLogger, msg: String?, msgObj: Any) {
		try {
			if (l.isTraceEnabled()) {
				val m = createInstance()
				val msgObjStr = m.doc2str(m.marshal(msgObj))
				l.trace { "${msg}\n${msgObjStr}" }
			} else if (LOG.isTraceEnabled()) {
				// check if the message needs to be logged in the dispatcher class
				val m = createInstance()
				val msgObjStr = m.doc2str(m.marshal(msgObj))
				LOG.trace { "${msg}\n${msgObjStr}" }
			}
		} catch (ex: TransformerException) {
			LOG.error(ex) { "Failed to log message." }
		} catch (ex: WSMarshallerException) {
			LOG.error(ex) { "Failed to log message." }
		}
	}
}
