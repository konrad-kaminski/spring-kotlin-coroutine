package org.springframework.kotlin.experimental.coroutine

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.type.AnnotatedTypeMetadata
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
@Conditional(OnClassCondition::class)
annotation class ConditionalOnClass (
    val value: String = ""
)

internal open class OnClassCondition: Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean =
            try {
                (metadata.getAnnotationAttributes(ConditionalOnClass::class.java.name)?.get("value") as String).let {
                    Class.forName(it)
                }

                true
            } catch (e: ClassNotFoundException) {
                false
            }
}