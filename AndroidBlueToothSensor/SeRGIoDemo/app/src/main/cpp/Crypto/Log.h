/*
 * log.h
 *
 *  Created on: 06 Mar 2018
 *      Author: hassaanjanjua
 */

#ifndef LOG_H_
#define LOG_H_
#include <android/log.h>

#define LOG_LEVEL 1

#ifndef LOG_LEVEL
#define LOG_LEVEL 1
#endif

#define  LOG_TAG    "someTag"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#define __log_prefix()                                      \
    {                                                       \
        LOGI("%s: %d ", __FILE__, __LINE__);                \
    }

#define __logstart()                                        \
    {                                                       \
        LOGI("START %s: %s ", __FILE__, __FUNCTION__);      \
    }

#define __logend()                                          \
    {                                                       \
        LOGI("END %s: %s ", __FILE__, __FUNCTION__);        \
    }

// Log
#define __logp(msg, val, param)         \
    {                                   \
        __log_prefix();                 \
        LOGI("%s %d", msg, val);        \
    }

#define __logm(msg, val)                \
    {                                   \
        __log_prefix();                 \
        LOGI("%s %s", msg, val);        \
    }

#define __log(val)                      \
    {                                   \
        __log_prefix();                 \
        LOGI("%s", val);                \
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
