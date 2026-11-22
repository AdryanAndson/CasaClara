# Room e WorkManager já trazem suas regras. Mantém os workers do app.
-keep class br.com.casaclara.notifications.** { *; }

# Widget (Glance) e receivers são referenciados pelo manifest; mantém nomes para logs legíveis.
-keep class br.com.casaclara.widget.** { *; }
# Enums lidos das preferências por nome (valueOf)
-keepclassmembers enum br.com.casaclara.** { public static **[] values(); public static ** valueOf(java.lang.String); }
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
