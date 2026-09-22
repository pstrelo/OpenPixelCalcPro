# Regole di ottimizzazione R8 Full Mode per Calcolatrice Pixel
-repackageclasses 'com.google.pixel.calculator.internal'
-allowaccessmodification
-mergeinterfacesaggressively

# Mantieni classi serializzabili necessarie
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Preserva le classi Compose necessarie a runtime
-keep class androidx.compose.** { *; }
