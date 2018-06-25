package org.theta.framework.core.trace;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

/**
 * 
 * @概述
 * @功能
 * @作者 陈望旭
 * @创建时间 2018年6月14日
 * @类调用特殊情况
 */
@Target(value = { TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceTrace {

}
