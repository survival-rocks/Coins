package me.justeli.coins.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Eli on January 11, 2021.
 * Coins: me.justeli.coins.settings
 */

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public @interface LoadConfig
{
    String path () default ""; // path in config
    String usual () default ""; // default value
}
