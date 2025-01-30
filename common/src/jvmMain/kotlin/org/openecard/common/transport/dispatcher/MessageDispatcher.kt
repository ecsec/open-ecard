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

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.RequestType
import iso.std.iso_iec._24727.tech.schema.ResponseType
import org.openecard.common.event.ApiCallEventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.*
import org.openecard.common.util.HandlerUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

private val LOG = KotlinLogging.logger {  }

/**
 * Implementation of the `Dispatcher` interface.
 * This implementation defers its actual reflection work to the [Service] class.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class MessageDispatcher : Dispatcher {

    private val environment: Environment

    /** Key is parameter classname  */
    private val serviceMap: TreeMap<String, Service>

    /** Key is service interface classname  */
    private val serviceInstMap: TreeMap<String, Method>

    private val availableServiceNames: MutableList<String>

    private val isFilter: Boolean

    /**
     * Creates a new MessageDispatcher instance and loads all definitions from the webservice interfaces in the
     * environment.
     *
     * @param environment The environment with the webservice interface getters.
     */
    constructor(environment: Environment) {
        this.environment = environment
        isFilter = false
        serviceMap = TreeMap()
        serviceInstMap = TreeMap()
        initDefinitions()
        availableServiceNames = ArrayList()
        createServiceList()
    }

    private constructor(environment: Environment, isFilter: Boolean) {
        this.environment = environment
        this.isFilter = isFilter
        serviceMap = TreeMap()
        serviceInstMap = TreeMap()
        initDefinitions()
        availableServiceNames = ArrayList()
        createServiceList()
    }


    @Throws(DispatcherException::class, InvocationTargetException::class)
    override fun deliver(req: Any): Any {
        val disp = environment.eventDispatcher
        // send API CALL STARTED event
        val handle = HandlerUtils.extractHandle(req)
        if (disp != null && req is RequestType) {
            val startEvt = ApiCallEventObject<RequestType, ResponseType>(handle, req)
			LOG.debug { "Sending API_CALL_STARTED event." }
            disp.notify(EventType.API_CALL_STARTED, startEvt)
        }

        try {
            val reqClass: Class<*> = req.javaClass
            val s = getService(reqClass)
            val serviceImpl = getServiceImpl(s)

			LOG.debug { "Delivering message of type: ${req.javaClass.getName()}" }

            val result = s.invoke(serviceImpl, req)

            // send API CALL FINISHED event
            if (disp != null && req is RequestType && result is ResponseType) {
                val finEvt = ApiCallEventObject<RequestType, ResponseType>(handle, req)
				finEvt.response = result
				LOG.debug { "Sending API_CALL_FINISHED event." }
                disp.notify(EventType.API_CALL_FINISHED, finEvt)
            }

            return result
        } catch (ex: IllegalAccessException) {
            throw DispatcherException(ex.message, ex)
        } catch (ex: IllegalArgumentException) {
            throw DispatcherException(ex.message, ex)
        }
    }

    @Throws(DispatcherExceptionUnchecked::class, InvocationTargetExceptionUnchecked::class)
    override fun safeDeliver(request: Any): Any {
        try {
            return deliver(request)
        } catch (ex: DispatcherException) {
            throw DispatcherExceptionUnchecked(ex.message, ex.cause)
        } catch (ex: InvocationTargetException) {
            throw InvocationTargetExceptionUnchecked(ex.message, ex.cause)
        }
    }

    @Throws(IllegalAccessException::class)
    private fun getService(reqClass: Class<*>): Service {
        if (!serviceMap.containsKey(reqClass.getName())) {
            val msg = "No service with a method containing parameter type " + reqClass.getName() + " present."
            throw IllegalAccessException(msg)
        }
        return serviceMap.get(reqClass.getName())!!
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class)
    private fun getServiceImpl(s: Service): Any {
        val m = serviceInstMap.get(s.serviceInterface.getName())
        if (m == null) {
            val msg = "The environment does not contain a service for class " + s.serviceInterface.getName()
            throw IllegalAccessException(msg)
        }
        val impl = m.invoke(environment)
        return impl
    }


    private fun initDefinitions() {
        // load all annotated service methods from environment
        val envClass: Class<*> = this.environment.javaClass
        val envMethods = envClass.getMethods()

        // loop over methods and build index structure
        for (nextAccessor in envMethods) {
            // is the method annotated?
            if (nextAccessor.getAnnotation(Dispatchable::class.java) != null) {
                // check access rights and stuff
                val modifier = nextAccessor.modifiers
                if (Modifier.isAbstract(modifier)) {
                    continue
                } else if (!Modifier.isPublic(modifier)) {
                    continue
                } else if (Modifier.isStatic(modifier)) {
                    continue
                }

                // try to read class from annotation, if not take return value
                val methodAnnotation = nextAccessor.getAnnotation(Dispatchable::class.java)
                val returnType = methodAnnotation.interfaceClass.java

                // check if the service is already defined
                if (this.serviceInstMap.containsKey(returnType.name)) {
                    val msg = "Omitting service type ${returnType.getName()}, because its type already associated with another service."
					LOG.warn { msg }
                    continue
                }

                // add env method mapping
                this.serviceInstMap.put(returnType.getName(), nextAccessor)

                // update type mentioned in Dispatchable annotation to the actual type returned by the function
                var returnTypeImpl = returnType
                try {
                    val result = nextAccessor.invoke(environment)
                    if (result != null) {
                        returnTypeImpl = result.javaClass
                    }
                } catch (ex: IllegalAccessException) {
					LOG.error(ex) { "Actual type could not be retrieved from method ${nextAccessor}." }
                    continue
                } catch (ex: IllegalArgumentException) {
					LOG.error(ex) { "Actual type could not be retrieved from method ${nextAccessor}." }
                    continue
                } catch (ex: InvocationTargetException) {
					LOG.error(ex) { "Actual type could not be retrieved from method ${nextAccessor}." }
                    continue
                }

                // create service and map its request parameters
                val service = Service(returnType, returnTypeImpl, isFilter)

                for (reqClass in service.getRequestClasses()) {
                    if (serviceMap.containsKey(reqClass.getName())) {
                        var msg = "Omitting method with parameter type ${reqClass.getName()} in service interface ${returnType.getName()} because its "
                        msg += "type already associated with another service."
						LOG.warn { msg }
                    } else {
                        serviceMap.put(reqClass.getName(), service)
                    }
                }
            }
        }
    }

    override val serviceList: List<String>
		get() {
			return Collections.unmodifiableList<String?>(availableServiceNames)
		}

    override val filter: Dispatcher
		get() {
			if (isFilter) {
				return this
			}
			return MessageDispatcher(this.environment, true)
		}

    private fun createServiceList() {
        val services = TreeSet<Service>()
        services.addAll(serviceMap.values)
        for (service in services) {
            availableServiceNames.addAll(service.actionList)
        }
    }
}
