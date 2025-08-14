package com.example.swp.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogActivity {
    String action();              // "User logged in", "Created voucher", etc.
    String entityType() default "";  // Optional: "Voucher", "Order"
    String entityName() default "";  // Optional: "Voucher #123"
    String detailLink() default "";  // Optional: "/voucher/123"
}
