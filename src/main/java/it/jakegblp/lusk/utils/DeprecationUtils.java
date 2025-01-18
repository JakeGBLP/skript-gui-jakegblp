package it.jakegblp.lusk.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Version;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The contents of this class have been made with support and translated over from <a href="https://github.com/JakeGBLP/Lusk">Lusk</a>.
 * @author <a href="https://jakegblp.github.io/">JakeGBLP</a>
 */
public class DeprecationUtils {

    public static final boolean
            SKRIPT_2_10 = !Skript.getVersion().isSmallerThan(new Version(2,10)),
            SKRIPT_2_7 = !Skript.getVersion().isSmallerThan(new Version(2,7));

    @SuppressWarnings("all")
    public static <T> boolean test(Event event, Expression<T> expr, Predicate<T> predicate) {
        if (SKRIPT_2_10) {
            return expr.check(event, predicate);
        } else {
            try {
                Class<?> checkerClass = Class.forName("ch.njol.util.Checker");

                Object checkerInstance = Proxy.newProxyInstance(
                        checkerClass.getClassLoader(),
                        new Class<?>[]{checkerClass},
                        (proxy, method, args) -> {
                            if ("check".equals(method.getName()) && args != null && args.length == 1) {
                                return predicate.test((T) args[0]);
                            }
                            return null;
                        }
                );

                Method checkMethod = expr.getClass().getMethod("check", Event.class, checkerClass);
                return (boolean) checkMethod.invoke(expr, event, checkerInstance);
            } catch (InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
                Skript.warning("Something went wrong with a condition: "+ e.getMessage());
            }
        }
        return false;
    }

    public static <T> boolean test(Event event, Expression<T> expr, Predicate<T> predicate, @Nullable Boolean negated) {
        boolean bool = test(event, expr, predicate);
        if (negated != null) return bool ^ negated;
        return bool;
    }


    @SuppressWarnings("all")
    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Function<F, T> converter) {
        if (SKRIPT_2_7) {
            Converters.registerConverter(from, to, converter::apply);
        } else {
            Logger skriptLogger = Skript.getInstance().getLogger();
            Level logLevel = skriptLogger.getLevel();
            skriptLogger.setLevel(Level.OFF);
            try {
                Class<?> converterClass = Class.forName("ch.njol.skript.classes.Converter");

                Object converterInstance = Proxy.newProxyInstance(
                        converterClass.getClassLoader(),
                        new Class<?>[]{converterClass},
                        (proxy, method, args) -> {
                            if ("convert".equals(method.getName()) && args != null && args.length == 1) {
                                return converter.apply((F) args[0]);
                            }
                            return null;
                        }
                );

                Method registerConverterMethod = Class.forName("ch.njol.skript.registrations.Converters").getMethod("registerConverter", Class.class, Class.class, converterClass);
                registerConverterMethod.invoke(from, to, converterInstance);
                skriptLogger.setLevel(logLevel);
            } catch (InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
                Skript.warning("Something went wrong with converters: " + e.getMessage());
            }
        }
    }
}
