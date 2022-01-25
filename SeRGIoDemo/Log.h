/*
 * log.h
 *
 *  Created on: 06 Mar 2018
 *      Author: hassaanjanjua
 */

#ifndef LOG_H_
#define LOG_H_

#include "Arduino.h"

#define LOG_LEVEL 1

#ifndef LOG_LEVEL
#define LOG_LEVEL 1
#endif

#define __log_prefix()                  \
    {                                   \
        Serial1.print(__FILE__);        \
        Serial1.print(":");             \
        Serial1.print(__LINE__);        \
        Serial1.print(" ");             \
    }

#define __logstart()                    \
    {                                   \
        Serial1.print("Start: ");       \
        Serial1.print(__FILE__);        \
        Serial1.print(":");             \
        Serial1.println(__FUNCTION__);  \
    }

#define __logend()                      \
    {                                   \
        Serial1.print("End: ");         \
        Serial1.print(__FILE__);        \
        Serial1.print(":");             \
        Serial1.println(__FUNCTION__);  \
    }

// Log
#define __logp(msg, val, param)         \
    {                                   \
        __log_prefix();                 \
        Serial1.println(val, param);    \
    }

#define __logm(msg, val)                \
    {                                   \
        __log_prefix();                 \
        Serial1.println(val);           \
    }

#define __log(val)                      \
    {                                   \
        __log_prefix();                 \
        Serial1.println(val);           \
    }


// Error Log Level 0
#define logpe(msg, val, param) __logp(msg, val, param)
#define logme(msg, val)        __logm(msg, val)
#define loge(val)              __log(val)
#define logstarte()            __logstart()
#define logende()              __logend()

// Debug Log Level 1
#if LOG_LEVEL >= 1
#define logpd(msg, val, param) __logp(msg, val, param)
#define logmd(msg, val)        __logm(msg, val)
#define logd(val)              __log(val)
#define logstartd()            __logstart()
#define logendd()              __logend()
#else
#define logpd(msg, val, param)
#define logmd(msg, val)
#define logd(val)
#define logstartd()
#define logendd()
#endif

// Verbose Log Level 2
#if LOG_LEVEL >= 2
#define logpv(msg, val, param) __logp(msg, val, param)
#define logmv(msg, val)        __logm(msg, val)
#define logv(val)              __log(val)
#define logstartv()            __logstart()
#define logendv()              __logend()
#else
#define logpv(msg, val, param)
#define logmv(msg, val)
#define logv(val)
#define logstartv()
#define logendv()
#endif

#if LOG_LEVEL >= 2
#define logstart()             __logstart()
#define logend()               __logend()
#else
#define logstart()
#define logend()
#endif

#if LOG_LEVEL >= 1
#define logp(msg, val, param) __logp(msg, val, param)
#define logm(msg, val)        __logm(msg, val)
#define log(val)              __log(val)
#else
#define logp(msg, val, param)
#define logm(msg, val)
#define log(val)
#endif


#endif /* LOG_H_ */
