//*********************************************************************
//绯荤粺鍚嶇О锛歂RXT-Utils
//Copyright(C)2000-2008 NARI Information and Communication Technology
//Branch. All rights reserved.
//鐗堟湰淇℃伅锛歂RXT-Utils.V1.0.0.SNAPSHOT
//#浣滆�咃細闄堟湜鏃� 鏉冮噸锛�100 鎵嬫満 13912929646#
//鐗堟湰                     鏃ユ湡            浣滆��    鍙樻洿璁板綍
//V1.0.0.SNAPSHOT 2018骞�7鏈�11鏃� 闄堟湜鏃�� 鏂板缓
//*********************************************************************

package org.theta.framework.core.trace;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * @姒傝堪
 * @鍔熻兘
 * @浣滆�� 闄堟湜鏃� @鍒涘缓鏃堕棿 2018骞�7鏈�11鏃� @绫昏皟鐢ㄧ壒娈婃儏鍐�
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ServiceTraceInterceptor.class)
public @interface EnableServiceTrace {

}
