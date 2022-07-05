package br.com.cardoso.aop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Aspect
@Component
public class CoreWebAspect {

    Logger LOGGER = LogManager.getLogger(CoreWebAspect.class);

    @Around("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.stereotype.Service *)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        LOGGER.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            if (result instanceof Mono) {
                //Codigo que reproduz o erro
//                ((Mono<Object>) result).subscribe(monoResult -> {
//                    LOGGER.info("Exit Mono: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
//                    joinPoint.getSignature().getName(), monoResult);
//                });
                return ((Mono<?>) result).doOnSuccess(obj ->
                        LOGGER.info("Exit Mono: {}.{}() with result = {}",
                                joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), obj));
            } else {
                LOGGER.info("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), e);
            throw e;
        }
    }


}
