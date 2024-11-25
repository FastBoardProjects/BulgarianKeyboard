#noinspection ShrinkerUnresolvedReference

-keep class androidx.appcompat.widget.** { *; }

-keepattributes SourceFile,LineNumberTable

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.google.appengine.** { *; }
-keepattributes *Annotation*
-keepattributes Signature
-ignorewarnings
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**
-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**รณ

-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }

    # Add this global rule
    -keepattributes Signature
    -keepclassmembers class com.maya.newbulgariankeyboard.** {
      *;
    }

