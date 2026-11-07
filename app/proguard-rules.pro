# Minify выключен, но на всякий случай сохраним модели JGit от вырезания,
# если однажды включите R8.
-keep class org.eclipse.jgit.** { *; }
-keep class com.googlecode.javaewah.** { *; }
-dontwarn java.awt.**
-dontwarn javax.servlet.**
-dontwarn org.apache.**
-dontwarn com.jcraft.**
