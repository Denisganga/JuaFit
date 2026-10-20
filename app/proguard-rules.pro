# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.pathwayfit.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.pathwayfit.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
