# Room and Compose supply their own consumer rules.

# Keep useful source locations in production crash traces while still allowing
# R8 to optimize and obfuscate implementation details.
-keepattributes SourceFile,LineNumberTable
